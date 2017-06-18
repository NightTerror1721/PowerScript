/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import nt.ps.PSGlobals;
import nt.ps.compiler.ScopeInfo.ScopeType;
import nt.ps.compiler.VariablePool.Variable;
import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.exception.CompilerErrors;
import nt.ps.compiler.parser.Assignation;
import nt.ps.compiler.parser.Assignation.AssignationPart;
import nt.ps.compiler.parser.Assignation.Location;
import nt.ps.compiler.parser.AssignationSymbol;
import nt.ps.compiler.parser.Block;
import nt.ps.compiler.parser.Code;
import nt.ps.compiler.parser.Code.CodeType;
import nt.ps.compiler.parser.Command;
import nt.ps.compiler.parser.Declaration;
import nt.ps.compiler.parser.FunctionLiteral;
import nt.ps.compiler.parser.Identifier;
import nt.ps.compiler.parser.Literal;
import nt.ps.compiler.parser.MutableLiteral;
import nt.ps.compiler.parser.Operator;
import nt.ps.compiler.parser.OperatorSymbol;
import nt.ps.compiler.parser.ParsedCode;
import nt.ps.lang.PSFunction;
import org.apache.bcel.generic.InstructionHandle;

/**
 *
 * @author mpasc
 */
final class CompilerBlock
{
    private final ScopeStack scopes;
    private final Stack stack;
    private final VariablePool vars;
    private final BytecodeGenerator bytecode;
    private final PSGlobals globals;
    private final CompilerBlockType type;
    private final CompilerErrors errors;
    private final ClassRepository repository;
    private Class<? extends PSFunction> compiledClass;
    
    public CompilerBlock(ScopeInfo source, PSGlobals globals, CompilerBlockType type, BytecodeGenerator bytecode,
            CompilerErrors errors, VariablePool parentVars, ClassRepository repository)
    {
        scopes = new ScopeStack();
        stack = new Stack();
        vars = parentVars != null ? parentVars.createChild(stack) : new VariablePool(stack, globals);
        this.bytecode = bytecode;
        this.globals = globals;
        this.type = type;
        this.errors = errors;
        this.repository = repository;
        
        this.bytecode.setCompiler(this);
        scopes.push(source);
    }
    
    public final void compile()
    {
        vars.createScope();
        if(type == CompilerBlockType.FUNCTION)
            bytecode.createUpPointerSlots();
        while(!scopes.isEmpty())
        {
            if(scopes.peek().hasMoreCommands())
            {
                Command command = scopes.peek().nextCommand();
                try { compileCommand(command); }
                catch(CompilerError error) { errors.addError(error, command); }
            }
            else
            {
                completeScope(scopes.peek());
                scopes.pop();
            }
        }
        
        try { bytecode.Return(); } catch(CompilerError error) { errors.addError(error, Command.parseErrorCommand(-1)); }
        if(type != CompilerBlockType.SCRIPT)
            bytecode.initiateUpPointersArray(vars.getUpPointers().size());
        
        compiledClass = bytecode.build(type, repository);
    }
    
    private void completeScope(ScopeInfo scopeInfo)
    {
        
    }
    
    private void compileCommand(Command command) throws CompilerError
    {
        if(command.isOperationsCommand())
        {
            compileOperation(command.getCode(0), false, false, true);
            return;
        }
        
        switch(command.getName())
        {
            case VAR: {
                ParsedCode pc = command.getCode(0);
                if(pc.is(CodeType.ASSIGNATION))
                    compileAssignation((Assignation) pc, false, true);
                else compileDeclaration((Declaration) pc, false);
            } break;
            case GLOBAL: {
                ParsedCode pc = command.getCode(0);
                if(pc.is(CodeType.ASSIGNATION))
                    compileAssignation((Assignation) pc, true, true);
                else compileDeclaration((Declaration) pc, true);
            } break;
        }
    }
    
    private boolean compileOperation(ParsedCode code, boolean isGlobal, boolean multiresult, boolean pop) throws CompilerError
    {
        boolean multiresponse = false;
        switch(code.getCodeType())
        {
            case IDENTIFIER: {
                if(!pop)
                {
                    Variable var = checkAndGetVar(code.toString(), isGlobal);
                    bytecode.load(var);
                }
            } break;
            case LITERAL: {
                if(!pop)
                    bytecode.loadLiteral((Literal) code);
            } break;
            case MUTABLE_LITERAL: {
                compileMutableLiteral((MutableLiteral) code, isGlobal);
                if(pop)
                {
                    stack.pop();
                    bytecode.pop();
                }
            } break;
            case BLOCK: {
                Block b = (Block) code;
                if(!b.isParenthesis())
                    throw CompilerError.unexpectedCode(code);
                compileOperation(b.getFirstCode(), isGlobal, multiresult, false);
                if(pop)
                {
                    stack.pop();
                    bytecode.pop();
                }
            } break;
            case FUNCTION: {
                compileFunction((FunctionLiteral) code, isGlobal);
                if(pop)
                {
                    stack.pop();
                    bytecode.pop();
                }
            } break;
            case SELF: {
                if(!pop)
                    bytecode.loadSelf();
            } break;
            case OPERATOR: {
                compileOperator((Operator) code, isGlobal, multiresult);
                if(pop)
                {
                    stack.pop();
                    bytecode.pop();
                }
            } break;
            case ASSIGNATION: {
                if(!pop)
                    throw new CompilerError("Cannot use assignation here: " + code);
                compileAssignation((Assignation) code, isGlobal, false);
            } break;
            default: throw CompilerError.unexpectedCode(code);
        }
        
        return multiresponse;
    }
    
    private void compileMutableLiteral(MutableLiteral literal, boolean isGlobal) throws CompilerError
    {
        if(literal.isLiteralArray())
        {
            if(literal.getItemCount() <= 0)
                bytecode.emptyArrayLiteral();
            else
            {
                bytecode.initArrayLiteral(literal);
                int count = 0;
                for(MutableLiteral.Item item : literal)
                    bytecode.insertArrayLiteralItem(count++, () -> compileOperation(item.getValue(), isGlobal, false, false));
                bytecode.endArrayLiteral();
            }
        }
        else if(literal.isLiteralTuple())
        {
            if(literal.getItemCount() <= 0)
                bytecode.emptyTupleLiteral();
            else
            {
                bytecode.initTupleLiteral(literal);
                int count = 0;
                for(MutableLiteral.Item item : literal)
                    bytecode.insertTupleLiteralItem(count++, () -> compileOperation(item.getValue(), isGlobal, false, false));
                bytecode.endTupleLiteral();
            }
        }
        else if(literal.isLiteralMap())
        {
            if(literal.getItemCount() <= 0)
                bytecode.emptyMapLiteral();
            else
            {
                bytecode.initMapLiteral(literal);
                int count = 0;
                for(MutableLiteral.Item item : literal)
                {
                    bytecode.insertMapLiteralItem(count++, () -> {
                        compileOperation(item.getKey(), isGlobal, false, false);
                        compileOperation(item.getValue(), isGlobal, false, false);
                    });
                }
                bytecode.endMapLiteral();
            }
        }
        else if(literal.isLiteralObject())
        {
            if(literal.getItemCount() <= 0)
                bytecode.emptyObjectLiteral();
            else
            {
                bytecode.initObjectLiteral(literal);
                int count = 0;
                for(MutableLiteral.Item item : literal)
                {
                    bytecode.insertObjectLiteralItem(count++, () -> {
                        bytecode.loadNativeString(item.getKey().toString());
                        //compileOperation(item.getKey(), isGlobal, false, false);
                        compileOperation(item.getValue(), isGlobal, false, false);
                    });
                }
                bytecode.endObjectLiteral();
            }
        }
        else throw new IllegalStateException();
    }
    
    private void compileFunction(FunctionLiteral function, boolean isGlobal) throws CompilerError
    {
        String varargs = function.isVarargs() ? function.getVarargsParameterName() : null;
        int defualts = function.getDefaultCount();
        int parameters = function.getParameterCount();
        ParsedCode assignation = function.hasAssignation() ? function.getAssignation() : null;
        ScopeInfo info = new ScopeInfo(function.getScope(), ScopeType.BASE);
        
        CompilerErrors childErrors = new CompilerErrors();
        BytecodeGenerator childGenerator = bytecode.createInstance(varargs != null ? parameters + 1 : parameters,
                defualts, varargs != null);
        CompilerBlock childCompiler = new CompilerBlock(info, globals, CompilerBlockType.FUNCTION,
                childGenerator, childErrors, vars, repository);
        
        for(int i=0;i<parameters;i++)
        {
            String par = function.getParameterName(i);
            childCompiler.vars.createParameter(par);
        }
        if(varargs != null)
            childCompiler.vars.createParameter(varargs);
        
        childCompiler.compile();
        
        if(childErrors.hasErrors())
        {
            errors.addErrors(childErrors);
            return;
        }
        
        Class<? extends PSFunction> functionClass = childCompiler.getCompiledClass();
        bytecode.createFunction(functionClass, childGenerator, childCompiler.vars.getUpPointers());
        
        if(assignation != null)
        {
            bytecode.createTemp("functionTemp");
            bytecode.storeTemp("functionTemp");
            assign(null, assignation, isGlobal, false, () -> bytecode.loadTemp("functionTemp"));
            bytecode.loadTemp("functionTemp");
            bytecode.removeTemp("functionTemp");
        }
    }
    
    
    private void compileOperator(Operator operator, boolean isGlobal, boolean multiresult) throws CompilerError
    {
        OperatorSymbol symbol = operator.getSymbol();
        if(symbol.isCall()) compileCallsOperator(operator, false, isGlobal, multiresult);
        else if(symbol.isInvoke()) compileCallsOperator(operator, true, isGlobal, multiresult);
        else if(symbol.isNew()) compileNewOperator(operator, isGlobal);
        else if(symbol.isBinary())
        {
            if(symbol == OperatorSymbol.AND)
                compileAndOperator(operator, isGlobal);
            else if(symbol == OperatorSymbol.OR)
                compileOrOperator(operator, isGlobal);
            else if(symbol == OperatorSymbol.ACCESS)
                compileAccessOperator(operator, isGlobal);
            else if(symbol == OperatorSymbol.PROPERTY_ACCESS)
                compilePropertyAccessOperator(operator, isGlobal);
            else
            {
                compileOperation(operator.getOperand(0), isGlobal, false, false);
                compileOperation(operator.getOperand(1), isGlobal, false, false);
                bytecode.callOperator(symbol);
            }
        }
        else if(symbol.isUnary())
        {
            compileOperation(operator.getOperand(0), isGlobal, false, false);
            bytecode.callOperator(symbol);
        }
        else if(symbol.isTernary())
            compileTernaryOperator(operator, isGlobal);
        else throw new IllegalStateException();
    }
    
    private void compileTernaryOperator(Operator operator, boolean isGlobal) throws CompilerError
    {
        compileOperation(operator.getOperand(0), isGlobal, false, false);
        InstructionHandle cmpIh = bytecode.computeIf();
        compileOperation(operator.getOperand(1), isGlobal, false, false);
        InstructionHandle ifIh = bytecode.emptyJump();
        bytecode.modifyJump(cmpIh);
        stack.pop();
        compileOperation(operator.getOperand(2), isGlobal, false, false);
        bytecode.modifyJump(ifIh);
    }
    
    private void compileAndOperator(Operator operator, boolean isGlobal) throws CompilerError
    {
        compileOperation(operator.getOperand(0), isGlobal, false, false);
        bytecode.dup();
        InstructionHandle ih = bytecode.computeIf();
        bytecode.pop();
        compileOperation(operator.getOperand(1), isGlobal, false, false);
        bytecode.modifyJump(ih);
    }
    
    private void compileOrOperator(Operator operator, boolean isGlobal) throws CompilerError
    {
        compileOperation(operator.getOperand(0), isGlobal, false, false);
        bytecode.dup();
        InstructionHandle ih = bytecode.computeInverseIf();
        bytecode.pop();
        compileOperation(operator.getOperand(1), isGlobal, false, false);
        bytecode.modifyJump(ih);
    }
    
    private void compileAccessOperator(Operator operator, boolean isGlobal) throws CompilerError
    {
        compileOperation(operator.getOperand(0), isGlobal, false, false);
        compileOperation(operator.getOperand(1), isGlobal, false, false);
        bytecode.callAccessOperator();
    }
    
    private void compilePropertyAccessOperator(Operator operator, boolean isGlobal) throws CompilerError
    {
        compileOperation(operator.getOperand(0), isGlobal, false, false);
        ParsedCode identifier = operator.getOperand(1);
        if(!identifier.is(Code.CodeType.IDENTIFIER))
            throw new CompilerError("Expected a valid identifier in '.' operator: " + identifier);
        bytecode.callPropertyAccessOperator(identifier.toString());
    }
    
    private int compileParameters(Operator operator, int offset, boolean isGlobal) throws CompilerError
    {
        int len = operator.getOperandCount() - offset;
        if(len <= 0)
            return 0;
        boolean multiresponse = false;
        for(int i=0;i<len;i++)
        {
            ParsedCode code = operator.getOperand(i + offset);
            if(!multiresponse && code.is(Code.CodeType.OPERATOR) && ((Operator)code).getSymbol().isCallable())
                multiresponse = true;
            compileOperation(operator.getOperand(i + offset), isGlobal, true, false);
        }
        if(multiresponse)
            bytecode.wrapVarargsTail(len);
        return multiresponse ? -1 : len;
    }
    
    private void compileCallsOperator(Operator operator, boolean isInvoke, boolean isGlobal, boolean multiresult) throws CompilerError
    {
        compileOperation(operator.getOperand(0), isGlobal, false, false);
        if(isInvoke)
        {
            bytecode.loadNativeString(operator.getOperand(1).toString());
            int parsCount = compileParameters(operator, 2, isGlobal);
            if(parsCount < 0)
                bytecode.doTailedCall(operator, true, multiresult);
            else bytecode.doCall(operator, true, multiresult);
        }
        else
        {
            int parsCount = compileParameters(operator, 1, isGlobal);
            if(parsCount < 0)
                bytecode.doTailedCall(operator, false, multiresult);
            else bytecode.doCall(operator, false, multiresult);
        }
    }
    
    private void compileNewOperator(Operator operator, boolean isGlobal) throws CompilerError
    {
        compileOperation(operator.getOperand(0), isGlobal, false, false);
        int parsCount = compileParameters(operator, 1, isGlobal);
        bytecode.callNewOperator(operator, parsCount < 0);
    }
    
    
    private void compileAssignation(Assignation assignation, boolean isGlobal, boolean createVars) throws CompilerError
    {
        int len = assignation.getPartCount();
        AssignationSymbol symbol = assignation.getSymbol();
        for(int i=0;i<len;i++)
        {
            AssignationPart part = assignation.getPart(i);
            if(part.getLocationCount() == 1)
                assign(symbol, part.getLocation(0).getCode(), isGlobal, createVars, () -> compileOperation(part.getAssignation(), isGlobal, false, false));
            else
            {
                compileOperation(part.getAssignation(), isGlobal, true, false);
                bytecode.storeExpand();
                int count = part.getLocationCount();
                for(int j=0;j<count;j++)
                {
                    Location loc = part.getLocation(j);
                    final int index = j;
                    assign(symbol, loc.getCode(), isGlobal, createVars, () -> bytecode.loadExpand(index));
                }
            }
        }
    }
    
    private void compileDeclaration(Declaration declaration, boolean isGlobal) throws CompilerError
    {
        int len = declaration.getIdentifierCount();
        for(int i=0;i<len;i++)
        {
            String name = declaration.getIdentifier(i).toString();
            if(vars.exists(name))
                throw new CompilerError("Variable \"" + name + "\" already exists");
            if(isGlobal)
            {
                vars.createGlobal(name);
            }
            else
            {
                Variable var = vars.createLocal(name);
                bytecode.storeUndefined(var);
            }
        }
    }
    
    
    
    
    
    
    
    
    
    private InstructionHandle assign(AssignationSymbol asymbol, ParsedCode to,
            boolean isGlobal, boolean createVars, AssignationParametersLoader parametersLoader) throws CompilerError
    {
        switch(to.getCodeType())
        {
            default: throw new IllegalArgumentException();
            case IDENTIFIER: return assignFromIdentifier(asymbol, (Identifier) to, isGlobal, createVars, parametersLoader);
            case OPERATOR: {
                Operator operator = (Operator) to;
                OperatorSymbol osymbol = operator.getSymbol();
                if(osymbol == OperatorSymbol.ACCESS)
                    return assignFromAccess(asymbol, operator, parametersLoader, isGlobal);
                else if(osymbol == OperatorSymbol.PROPERTY_ACCESS)
                    return assignFromPropertyAccess(asymbol, operator, parametersLoader, isGlobal);
                else throw new IllegalArgumentException();
            }
        }
    }
    
    private InstructionHandle assignFromIdentifier(AssignationSymbol asymbol, Identifier identifier,
            boolean isGlobal, boolean createVars, AssignationParametersLoader loader) throws CompilerError
    {
        String name = identifier.toString();
        Variable var;
        if(createVars)
        {
            if(vars.exists(name))
                throw new CompilerError("Variable \"" + name + "\" already exists");
            var = isGlobal
                    ? vars.createGlobal(name)
                    : vars.createLocal(name);
        }
        else
        {
            if(!vars.exists(name))
                throw new CompilerError("Variable \"" + name + "\" does not exists");
            var = vars.get(name, false);
        }
        if(asymbol.containsOperator())
        {
            bytecode.load(var);
            loader.load();
            bytecode.callOperator(asymbol.getAssociatedOperatorSymbol());
        }
        else loader.load();
        return bytecode.store(var);
    }
    
    private InstructionHandle assignFromAccess(AssignationSymbol asymbol, Operator operator, AssignationParametersLoader loader, boolean isGlobal) throws CompilerError
    {
        compileOperation(operator.getOperand(0), isGlobal, false, false);
        compileOperation(operator.getOperand(1), isGlobal, false, false);
        if(asymbol.containsOperator())
        {
            compileOperation(operator.getOperand(0), isGlobal, false, false);
            compileOperation(operator.getOperand(1), isGlobal, false, false);
            bytecode.callAccessOperator();
            loader.load();
            bytecode.callOperator(asymbol.getAssociatedOperatorSymbol());
        }
        else loader.load();
        return bytecode.callStoreAccess();
    }
    
    private InstructionHandle assignFromPropertyAccess(AssignationSymbol asymbol, Operator operator, AssignationParametersLoader loader, boolean isGlobal) throws CompilerError
    {
        compileOperation(operator.getOperand(0), isGlobal, false, false);
        ParsedCode code2 = operator.getOperand(1);
        if(!code2.is(Code.CodeType.IDENTIFIER))
            throw new CompilerError("Expected valid identifier in property assignation: " + operator);
        if(asymbol.containsOperator())
        {
            bytecode.dup();
            bytecode.callPropertyAccessOperator(code2.toString());
            loader.load();
            bytecode.callOperator(asymbol.getAssociatedOperatorSymbol());
        }
        else loader.load();
        return bytecode.callStorePropertyAccess(code2.toString());
    }
    
    
    private Variable checkAndGetVar(String nameVar, boolean globalModifier) throws CompilerError
    {
        if(!vars.exists(nameVar))
            throw new CompilerError("Identifier \"" + nameVar + "\" not found");
        return vars.get(nameVar, globalModifier);
    }
    
    public final Class<? extends PSFunction> getCompiledClass() { return compiledClass; }
    
    public static final PSFunction buildFunctionInstance(Class<? extends PSFunction> baseClass, PSGlobals globals)
    {
        if(globals == null)
            throw new NullPointerException();
        try
        {
            Constructor cns = baseClass.getConstructor();
            PSFunction function = (PSFunction) cns.newInstance();
            
            baseClass.getMethod(BytecodeGenerator.STR_FUNC_SET_GLOBALS, SET_GLOBALS_SIGNATURE)
                    .invoke(function, globals);
            
            return function;
        }
        catch(IllegalAccessException | IllegalArgumentException | InstantiationException |
                NoSuchMethodException | SecurityException | InvocationTargetException ex)
        {
            throw new IllegalStateException(ex);
        }
    }
    
    public final Stack getStack() { return stack; }
    public final VariablePool getVariables() { return vars; }
    
    public static enum CompilerBlockType { SCRIPT, FUNCTION }
    private static final Class<?>[] SET_GLOBALS_SIGNATURE = { PSGlobals.class };
    
    @FunctionalInterface
    private static interface AssignationParametersLoader { void load() throws CompilerError; }
}

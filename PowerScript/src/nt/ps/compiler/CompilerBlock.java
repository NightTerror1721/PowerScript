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
import nt.ps.compiler.parser.Block;
import nt.ps.compiler.parser.Code;
import nt.ps.compiler.parser.Command;
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
    private Class<? extends PSFunction> compiledClass;
    
    public CompilerBlock(ScopeInfo source, PSGlobals globals, CompilerBlockType type, BytecodeGenerator bytecode, CompilerErrors errors, VariablePool parentVars)
    {
        scopes = new ScopeStack();
        stack = new Stack();
        vars = parentVars != null ? parentVars.createChild(stack) : new VariablePool(stack);
        this.bytecode = bytecode;
        this.globals = globals;
        this.type = type;
        this.errors = errors;
        
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
        
        compiledClass = bytecode.build(type);
    }
    
    private void completeScope(ScopeInfo scopeInfo)
    {
        
    }
    
    private void compileCommand(Command command) throws CompilerError
    {
        if(command.isOperationsCommand())
        {
            compileOperation(command.getCode(0), false);
            return;
        }
        
        switch(command.getName())
        {
            case VAR: VAR(command);
        }
    }
    
    private void VAR(Command command)
    {
        
    }
    
    private void compileOperation(ParsedCode code, boolean isGlobal) throws CompilerError
    {
        switch(code.getCodeType())
        {
            case IDENTIFIER: {
                Variable var = checkAndGetVar(code.toString(), isGlobal);
                bytecode.load(var);
            } break;
            case LITERAL: {
                bytecode.loadLiteral((Literal) code);
            } break;
            case MUTABLE_LITERAL: {
                compileMutableLiteral((MutableLiteral) code, isGlobal);
            } break;
            case BLOCK: {
                Block b = (Block) code;
                if(!b.isParenthesis())
                    throw CompilerError.unexpectedCode(code);
                compileOperation(b.getFirstCode(), isGlobal);
            } break;
            case FUNCTION: {
                compileFunction((FunctionLiteral) code, isGlobal);
            } break;
            case SELF: {
                bytecode.loadSelf();
            } break;
            case OPERATOR: {
                
            } break;
            case ASSIGNATION: {
                
            } break;
            default: throw CompilerError.unexpectedCode(code);
        }
    }
    
    private void compileMutableLiteral(MutableLiteral literal, boolean isGlobal) throws CompilerError
    {
        if(literal.isLiteralArray())
        {
            bytecode.initArrayLiteral(literal);
            int count = 0, max = literal.getItemCount();
            for(MutableLiteral.Item item : literal)
            {
                compileOperation(item.getValue(), isGlobal);
                bytecode.insertArrayLiteralItem(count++, count >= max);
            }
            bytecode.endArrayLiteral();
        }
        else if(literal.isLiteralTuple())
        {
            bytecode.initTupleLiteral(literal);
            int count = 0, max = literal.getItemCount();
            for(MutableLiteral.Item item : literal)
            {
                compileOperation(item.getValue(), isGlobal);
                bytecode.insertTupleLiteralItem(count++, count >= max);
            }
            bytecode.endTupleLiteral();
        }
        else if(literal.isLiteralMap())
        {
            bytecode.initMapLiteral(literal);
            int count = 0, max = literal.getItemCount();
            for(MutableLiteral.Item item : literal)
            {
                compileOperation(item.getKey(), isGlobal);
                compileOperation(item.getValue(), isGlobal);
                bytecode.insertMapLiteralItem(count++, count >= max);
            }
            bytecode.endMapLiteral();
        }
        else if(literal.isLiteralObject())
        {
            bytecode.initObjectLiteral(literal);
            int count = 0, max = literal.getItemCount();
            for(MutableLiteral.Item item : literal)
            {
                compileOperation(item.getKey(), isGlobal);
                compileOperation(item.getValue(), isGlobal);
                bytecode.insertObjectLiteralItem(count++, count >= max);
            }
            bytecode.endObjectLiteral();
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
                childGenerator, childErrors, vars);
        
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
            assign(assignation, isGlobal, () -> {
                bytecode.loadTemp("functionTemp");
                return 1;
            });
            bytecode.loadTemp("functionTemp");
            bytecode.removeTemp("functionTemp");
        }
    }
    
    
    
    
    
    
    
    
    
    private InstructionHandle assign(ParsedCode to, boolean isGlobal, AssignationParametersLoader parametersLoader) throws CompilerError
    {
        switch(to.getCodeType())
        {
            default: throw new IllegalArgumentException();
            case IDENTIFIER: return assignFromIdentifier((Identifier) to, parametersLoader);
            case OPERATOR: {
                Operator operator = (Operator) to;
                OperatorSymbol symbol = operator.getSymbol();
                if(symbol == OperatorSymbol.ACCESS)
                    return assignFromAccess(operator, parametersLoader, isGlobal);
                else if(symbol == OperatorSymbol.PROPERTY_ACCESS)
                    return assignFromPropertyAccess(operator, parametersLoader, isGlobal);
                else throw new IllegalArgumentException();
            }
        }
    }
    
    private InstructionHandle assignFromIdentifier(Identifier identifier, AssignationParametersLoader loader) throws CompilerError
    {
        String name = identifier.toString();
        if(!vars.exists(name))
            throw new CompilerError("Variable \"" + name + "\" does not exists");
        Variable var = vars.get(name, false);
        int amount = loader.load();
        if(amount < 1)
            throw new IllegalStateException();
        bytecode.wrapArgsToArray(amount);
        return bytecode.store(var);
    }
    
    private InstructionHandle assignFromAccess(Operator operator, AssignationParametersLoader loader, boolean isGlobal) throws CompilerError
    {
        compileOperation(operator.getOperand(0), isGlobal);
        compileOperation(operator.getOperand(1), isGlobal);
        int amount = loader.load();
        if(amount < 1)
            throw new IllegalStateException();
        bytecode.wrapArgsToArray(amount);
        return bytecode.callStoreAccess();
    }
    
    private InstructionHandle assignFromPropertyAccess(Operator operator, AssignationParametersLoader loader, boolean isGlobal) throws CompilerError
    {
        compileOperation(operator.getOperand(0), isGlobal);
        ParsedCode code2 = operator.getOperand(1);
        if(!code2.is(Code.CodeType.IDENTIFIER))
            throw new CompilerError("Expected valid identifier in property assignation: " + operator);
        bytecode.loadLiteral(Literal.valueOf(code2.toString()));
        int amount = loader.load();
        if(amount < 1)
            throw new IllegalStateException();
        bytecode.wrapArgsToArray(amount);
        return bytecode.callStorePropertyAccess();
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
    private static interface AssignationParametersLoader { int load(); }
}

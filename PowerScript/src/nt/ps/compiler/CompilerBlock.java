/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
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
import nt.ps.compiler.parser.Block.Scope;
import nt.ps.compiler.parser.Code;
import nt.ps.compiler.parser.Code.CodeType;
import nt.ps.compiler.parser.Command;
import nt.ps.compiler.parser.CommandWord.CommandName;
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
    private final ScopeInfo scopeSource;
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
        scopeSource = source;
        scopes = new ScopeStack();
        stack = new Stack();
        this.bytecode = bytecode;
        this.vars = parentVars != null ? parentVars.createChild(bytecode, stack) : new VariablePool(bytecode, stack, globals);
        this.globals = globals;
        this.type = type;
        this.errors = errors;
        this.repository = repository;
        
        this.bytecode.setCompiler(this);
    }
    
    public final void compile(boolean createScope, boolean destroyScope)
    {
        if(type == CompilerBlockType.FUNCTION)
            bytecode.createUpPointerSlots();
        
        if(createScope)
            vars.createScope();
        compileScope(scopeSource);
        if(destroyScope)
        {
            try { vars.destroyScope(); }
            catch(CompilerError error) { errors.addError(error, Command.parseErrorCommand(-1)); }
        }
        
        bytecode.Return();
        if(type != CompilerBlockType.SCRIPT)
            bytecode.initiateUpPointersArray(vars.getUpPointers().size());
        
        compiledClass = bytecode.build(type, repository);
    }
    
    private void compileScope(ScopeInfo scopeInfo) { compileScope(scopeInfo, null, null); }
    private void compileScope(ScopeInfo scopeInfo, AssignationParametersLoader preCompile, AssignationParametersLoader postCompile)
    {
        if(!scopes.isEmpty())
            scopeInfo.setStartReference(bytecode.getLastHandle());
        scopes.push(scopeInfo);
        if(!scopeInfo.isBase())
            vars.createScope();
        if(preCompile != null) try
        {
            preCompile.load();
        }
        catch(CompilerError error) { errors.addError(error, Command.parseErrorCommand(0)); }
        if(!scopeInfo.hasMoreCommands())
            bytecode.nop();
        else while(scopeInfo.hasMoreCommands())
        {
            Command command = scopeInfo.nextCommand();
            try { compileCommand(command); }
            catch(CompilerError error) { errors.addError(error, command); }
        }
        try
        {
            if(postCompile != null)
                postCompile.load();
            if(!scopeInfo.isBase())
                vars.destroyScope();
        }
        catch(CompilerError error) { errors.addError(error, Command.parseErrorCommand(0)); }
        scopes.pop();
        if(!scopes.isEmpty())
            scopeInfo.setEndReference(bytecode.getLastHandle());
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
            case IF: {
                compileIf(command);
            } break;
            case ELSE: {
                throw new CompilerError("\"else\" command can only put after \"if\" command");
            }
            case WHILE: {
                compileWhile(command);
            } break;
            case FOR: {
                compileFor(command);
            } break;
            case SWITCH: {
                compileSwitch(command);
            } break;
            case CASE: {
                compileCase(command);
            } break;
            case DEFAULT: {
                compileDefault(command);
            } break;
            case BREAK: {
                compileBreak(command);
            } break;
            case CONTINUE: {
                compileContinue(command);
            } break;
            case TRY: {
                compileTry(command);
            } break;
            case CATCH: {
                throw new CompilerError("\"catch\" command can only put after \"try\" command");
            }
            case THROW: {
                compileThrow(command);
            } break;
            case RETURN: {
                compileReturn(command);
            } break;
            case YIELD: {
                compileYield(command);
            } break;
        }
    }
    
    private void compileYield(Command command) throws CompilerError
    {
        if(!bytecode.isGenerator())
            throw new CompilerError("\"yield\" command only works on generator functions");
        int pars = compileMultipleRaises(command.getCode(0));
        bytecode.computeYield(pars);
    }
    
    private void compileReturn(Command command) throws CompilerError
    {
        int pars = compileMultipleRaises(command.getCode(0));
        if(bytecode.isGenerator() && pars > 0)
            throw new CompilerError("\"return\" command cannot return any in generator functions");
        bytecode.computeReturn(pars);
    }
    
    private void compileThrow(Command command) throws CompilerError
    {
        int pars = compileMultipleRaises(command.getCode(0));
        bytecode.computeThrow(pars);
    }
    
    private int compileMultipleRaises(Block pars) throws CompilerError
    {
        int len = pars.getCodeCount();
        if(len < 1)
            return 0;
        boolean multiresponse = false;
        for(int i=0;i<len;i++)
        {
            ParsedCode code = pars.getCode(i);
            if(!multiresponse && code.is(Code.CodeType.OPERATOR) && ((Operator)code).getSymbol().isCallable())
                multiresponse = true;
            compileOperation(code, false, true, false);
        }
        if(multiresponse)
            bytecode.wrapVarargsTail(len);
        return multiresponse ? -1 : len;
    }
    
    private void compileTry(Command command) throws CompilerError
    {
        if(!bytecode.hasAnyInstruction())
            bytecode.nop();
        
        ScopeInfo info = new ScopeInfo(command.getCode(0), ScopeType.TRY);
        compileScope(info);
        info.setEndReference(bytecode.emptyJump());
        
        if(!scopes.peek().hasMoreCommands() || scopes.peek().peekNextCommand().getName() != CommandName.CATCH)
            throw new CompilerError("Expected a valid \"catch\" command after \"try\" command");
        Command catchCommand = scopes.peek().nextCommand();
        ScopeInfo catchInfo = new ScopeInfo(catchCommand.getCode(1), ScopeType.CATCH);
        
        compileScope(catchInfo, () -> {
            Variable var = vars.createLocal(catchCommand.getCode(0).toString());
            bytecode.wrapThrowable(var);
        }, () -> {
            bytecode.createTryCatchHandler(info.getStartReference().getNext(), info.getEndReference(), info.getEndReference().getNext());
            bytecode.modifyJump(info.getEndReference());
        });
    }
    
    private void compileContinue(Command command) throws CompilerError
    {
        if(!scopes.peek().isContinuable())
            throw new CompilerError("\"continue\" command is not valid in " +
                    scopes.peek().getScopeType().name().toLowerCase() + " scope");
        InstructionHandle inst = scopes.peek().getStartReference().getNext();
        bytecode.jump(inst == null ? bytecode.nop() : inst);
    }
    
    private void compileBreak(Command command) throws CompilerError
    {
        if(!scopes.peek().isBreakable())
            throw new CompilerError("\"break\" command is not valid in " +
                    scopes.peek().getScopeType().name().toLowerCase() + " scope");
        scopes.peek().addBranchReference(bytecode.emptyJump());
    }
    
    private void compileDefault(Command command) throws CompilerError
    {
        if(scopes.peek().getScopeType() != ScopeType.SWITCH)
            throw new CompilerError("\"default\" command only works in \"switch\" scope");
        InstructionHandle last = bytecode.isNopLastInstruction()
                ? bytecode.getLastHandle()
                : bytecode.nop();
        scopes.peek().getSwitchModel().addDefaultCase(last);
    }
    
    private void compileCase(Command command) throws CompilerError
    {
        if(scopes.peek().getScopeType() != ScopeType.SWITCH)
            throw new CompilerError("\"case\" command only works in \"switch\" scope");
        Literal lit = command.getCode(0);
        InstructionHandle last = bytecode.isNopLastInstruction()
                ? bytecode.getLastHandle()
                : bytecode.nop();
        scopes.peek().getSwitchModel().addCase(last, lit);
    }
    
    private void compileSwitch(Command command) throws CompilerError
    {
        compileOperation(command.<Block>getCode(0).getCode(0), false, false, false);
        InstructionHandle ihStart = bytecode.getLastHandle();
        
        ScopeInfo info = new ScopeInfo(command.getCode(1), ScopeType.SWITCH);
        info.createSwitchModel(ihStart);
        
        compileScope(info);
        SwitchModel smodel = info.getSwitchModel();
        if(!smodel.hasDefaultCase())
            smodel.addDefaultCase(bytecode.nop());
        for(InstructionHandle jump : info.getAllBranchs())
            bytecode.modifyJump(jump);
        
        bytecode.computeSwitch(smodel);
    }
    
    private void compileFor(Command command) throws CompilerError
    {
        if(!bytecode.hasAnyInstruction())
            bytecode.nop();
        
        if(command.size() == 4)
        {
            final ScopeInfo info = new ScopeInfo(command.getCode(3), ScopeType.FOR);
            final InstructionHandle[] ih = new InstructionHandle[] { null };
            
            compileScope(info, () -> {
                if(command.getCode(0) != null)
                {
                    if(!command.getCode(0).is(CodeType.COMMAND))
                        throw new IllegalStateException();
                    compileCommand((Command) command.getCode(0));
                    info.setStartReference(bytecode.getLastHandle());
                }
                if(command.getCode(1) != null)
                {
                    compileOperation(command.getCode(1), false, false, false);
                    ih[0] = bytecode.computeIf();
                }
            }, () -> {
                if(command.getCode(2) != null)
                    compileOperation(command.getCode(2), false, false, true);
                bytecode.jump(info.getStartReference().getNext());
                if(ih[0] != null)
                    bytecode.modifyJump(ih[0]);
                for(InstructionHandle jump : info.getAllBranchs())
                    bytecode.modifyJump(jump);
            });
        }
        else
        {
            ScopeInfo info = new ScopeInfo(command.getCode(2), ScopeType.FOREACH);
            final String tempVar = createTempForVar();
            compileOperation(command.getCode(1), false, true, false);
            bytecode.createIteratorInstance();
            bytecode.storeTemp(tempVar);
            stack.pop();
            InstructionHandle[] ih = new InstructionHandle[1];
            
            compileScope(info, () -> {
                Variable[] foreachVars = initiateForeachVars(command.getCode(0));
                
                bytecode.loadTemp(tempVar);
                ih[0] = bytecode.invokeIteratorHasNext();
                bytecode.loadTemp(tempVar);
                bytecode.invokeIteratorNext(foreachVars);
            }, () -> {
                bytecode.jump(info.getStartReference().getNext());
                bytecode.modifyJump(ih[0]);
                for(InstructionHandle jump : info.getAllBranchs())
                    bytecode.modifyJump(jump);
                bytecode.removeTemp(tempVar);
            });
        }
    }
    
    private Variable[] initiateForeachVars(ParsedCode vcode) throws CompilerError
    {
        switch(vcode.getCodeType())
        {
            case IDENTIFIER:
                return new Variable[] { vars.createLocal(vcode.toString()) };
            case DECLARATION: {
                Declaration dec = (Declaration) vcode;
                int len = dec.getIdentifierCount();
                Variable[] foreachVars = new Variable[len];
                for(int i=0;i<len;i++)
                    foreachVars[i] = vars.createLocal(dec.getIdentifier(i).toString());
                return foreachVars;
            }
            default: throw new IllegalStateException();
        }
    }
    
    private String createTempForVar()
    {
        for(int i=0;;i++)
        {
            String name = "foreach_temp" + i;
            if(!bytecode.existsTemp(name))
            {
                bytecode.createTemp(name);
                return name;
            }
        }
    }
    
    private void compileWhile(Command command) throws CompilerError
    {
        if(!bytecode.hasAnyInstruction())
            bytecode.nop();
        InstructionHandle startTag = bytecode.getLastHandle();
        
        compileOperation(command.<Block>getCode(0).getFirstCode(), false, false, false);
        InstructionHandle condTag = bytecode.computeIf();
        
        ScopeInfo info = new ScopeInfo(command.getCode(1), ScopeType.WHILE);
        compileScope(info);
        bytecode.jump(startTag.getNext());
        
        for(InstructionHandle jump : info.getAllBranchs())
            bytecode.modifyJump(jump);
        bytecode.modifyJump(condTag);
    }
    
    
    
    private void compileIf(Command command) throws CompilerError
    {
        InstructionHandle condTag = compileConditionalIf(command.getCode(0));
        compileScope(new ScopeInfo(command.getCode(1), ScopeType.IF));
        
        if(!scopes.peek().hasMoreCommands() || scopes.peek().peekNextCommand().getName() != CommandName.ELSE)
        {
            bytecode.modifyJump(condTag);
            return;
        }
        
        LinkedList<InstructionHandle> jumps = new LinkedList<>();
        do
        {
            jumps.add(bytecode.emptyJump());
            bytecode.modifyJump(condTag);
            
            Command cmd = scopes.peek().nextCommand();
            Code cscope = cmd.getCode(0);
            if(cscope.is(CodeType.BLOCK))
            {
                Scope scope = (Scope) cscope;
                compileScope(new ScopeInfo(scope, ScopeType.ELSE));
                condTag = null;
                break;
            }
            else
            {
                Command cmdIf = (Command) cscope;
                condTag = compileConditionalIf(cmdIf.getCode(0));
                compileScope(new ScopeInfo(cmdIf.getCode(1), ScopeType.ELSEIF));
            }
        }
        while(scopes.peek().hasMoreCommands() && scopes.peek().peekNextCommand().getName() == CommandName.ELSE);
        
        if(condTag != null)
            bytecode.modifyJump(condTag);
        jumps.forEach(bytecode::modifyJump);
    }
    
    private InstructionHandle compileConditionalIf(Block cond) throws CompilerError
    {
        compileOperation(cond.getFirstCode(), false, false, false);
        return bytecode.computeIf();
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
                compileOperator((Operator) code, isGlobal, multiresult || pop);
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
        boolean generator = function.isGenerator();
        ParsedCode assignation = function.hasAssignation() ? function.getAssignation() : null;
        ScopeInfo info = new ScopeInfo(function.getScope(), ScopeType.BASE);
        
        CompilerErrors childErrors = new CompilerErrors();
        BytecodeGenerator childGenerator = bytecode.createInstance(varargs != null ? parameters + 1 : parameters,
                defualts, varargs != null, generator);
        CompilerBlock childCompiler = new CompilerBlock(info, globals, CompilerBlockType.FUNCTION,
                childGenerator, childErrors, vars, repository);
        
        childCompiler.vars.createScope();
        for(int i=0;i<parameters;i++)
        {
            String par = function.getParameterName(i);
            childCompiler.vars.createParameter(par);
        }
        if(varargs != null)
            childCompiler.vars.createParameter(varargs);
        
        if(assignation != null)
            assign(AssignationSymbol.ASSIGNATION, assignation, isGlobal, true, true, () -> {
                childCompiler.compile(false, true);
        
                if(childErrors.hasErrors())
                {
                    errors.addErrors(childErrors);
                    return;
                }
                
                int len = function.getDefaultCount();
                for(int i=0;i<len;i++)
                    bytecode.loadLiteral(function.getDefault(i));

                Class<? extends PSFunction> functionClass = childCompiler.getCompiledClass();
                bytecode.createFunction(functionClass, childGenerator, childCompiler.vars.getUpPointers());
            });
        else
        {
            childCompiler.compile(false, true);
        
            if(childErrors.hasErrors())
            {
                errors.addErrors(childErrors);
                return;
            }
            
            int len = function.getDefaultCount();
                for(int i=0;i<len;i++)
                    bytecode.loadLiteral(function.getDefault(i));

            Class<? extends PSFunction> functionClass = childCompiler.getCompiledClass();
            bytecode.createFunction(functionClass, childGenerator, childCompiler.vars.getUpPointers());
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
            if((symbol == OperatorSymbol.INCREMENT || symbol == OperatorSymbol.DECREMENT) && isAssignable(operator.getOperand(0)))
            {
                if(operator.isRightOrder())
                {
                    assign(AssignationSymbol.ASSIGNATION, operator.getOperand(0), isGlobal, false, false, () -> {
                        compileOperation(operator.getOperand(0), isGlobal, false, false);
                        bytecode.dup();
                        bytecode.callOperator(symbol);
                    });
                    stack.push();
                }
                else
                {
                    assign(AssignationSymbol.ASSIGNATION, operator.getOperand(0), isGlobal, false, true, () -> {
                        compileOperation(operator.getOperand(0), isGlobal, false, false);
                        bytecode.callOperator(symbol);
                    });
                }
            }
            else
            {
                compileOperation(operator.getOperand(0), isGlobal, false, false);
                bytecode.callOperator(symbol);
            }
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
                assign(symbol, part.getLocation(0).getCode(), isGlobal, createVars, false, () -> compileOperation(part.getAssignation(), isGlobal, false, false));
            else
            {
                compileOperation(part.getAssignation(), isGlobal, true, false);
                bytecode.storeExpand();
                int count = part.getLocationCount();
                for(int j=0;j<count;j++)
                {
                    Location loc = part.getLocation(j);
                    final int index = j;
                    assign(symbol, loc.getCode(), isGlobal, createVars, false, () -> bytecode.loadExpand(index));
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
            if(vars.exists(name, true))
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
            boolean isGlobal, boolean createVars, boolean dup, AssignationParametersLoader parametersLoader) throws CompilerError
    {
        if(dup)
            stack.push();
        switch(to.getCodeType())
        {
            default: throw new IllegalArgumentException();
            case IDENTIFIER: return assignFromIdentifier(asymbol, (Identifier) to, isGlobal, createVars, dup, parametersLoader);
            case OPERATOR: {
                Operator operator = (Operator) to;
                OperatorSymbol osymbol = operator.getSymbol();
                if(osymbol == OperatorSymbol.ACCESS)
                    return assignFromAccess(asymbol, operator, parametersLoader, isGlobal, dup);
                else if(osymbol == OperatorSymbol.PROPERTY_ACCESS)
                    return assignFromPropertyAccess(asymbol, operator, parametersLoader, isGlobal, dup);
                else throw new IllegalArgumentException();
            }
        }
    }
    
    private boolean isAssignable(ParsedCode code)
    {
        switch(code.getCodeType())
        {
            default: return false;
            case IDENTIFIER: return true;
            case OPERATOR: {
                Operator operator = (Operator) code;
                OperatorSymbol osymbol = operator.getSymbol();
                return osymbol == OperatorSymbol.ACCESS || osymbol == OperatorSymbol.PROPERTY_ACCESS;
            }
        }
    }
    
    private InstructionHandle assignFromIdentifier(AssignationSymbol asymbol, Identifier identifier,
            boolean isGlobal, boolean createVars, boolean dup, AssignationParametersLoader loader) throws CompilerError
    {
        String name = identifier.toString();
        Variable var;
        if(createVars)
        {
            if(vars.exists(name, true))
                throw new CompilerError("Variable \"" + name + "\" already exists");
            var = isGlobal
                    ? vars.createGlobal(name)
                    : bytecode.createLocal(vars.createLocal(name));
        }
        else
        {
            if(!vars.exists(name, false))
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
        if(dup)
            bytecode.dup();
        return bytecode.store(var);
    }
    
    private InstructionHandle assignFromAccess(AssignationSymbol asymbol, Operator operator,
            AssignationParametersLoader loader, boolean isGlobal, boolean dup) throws CompilerError
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
        return bytecode.callStoreAccess(!dup);
    }
    
    private InstructionHandle assignFromPropertyAccess(AssignationSymbol asymbol, Operator operator,
            AssignationParametersLoader loader, boolean isGlobal, boolean dup) throws CompilerError
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
        return bytecode.callStorePropertyAccess(code2.toString(), !dup);
    }
    
    
    private Variable checkAndGetVar(String nameVar, boolean globalModifier) throws CompilerError
    {
        if(!vars.exists(nameVar, false))
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
            Constructor cns = baseClass.getConstructor(SET_GLOBALS_SIGNATURE);
            return (PSFunction) cns.newInstance(globals);
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

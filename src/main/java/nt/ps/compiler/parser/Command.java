/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.Arrays;
import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.parser.Block.Scope;
import nt.ps.compiler.parser.CommandWord.CommandName;

/**
 *
 * @author Asus
 */
public final class Command extends ParsedCode
{
    private final int line;
    private final CommandWord command;
    private final ParsedCode[] code;
    
    private Command(int line, CommandWord command, ParsedCode... code)
    {
        if(line < 0)
            throw new IllegalStateException("line == " + line);
        this.line = line;
        this.command = command;
        this.code = code;
    }
    
    public final int getSourceLine() { return line; }
    
    public final boolean isOperationsCommand() { return command == null; }
    
    public final int size() { return code.length; }
    
    public final <C extends ParsedCode> C getCode(int index) { return (C) code[index]; }
    
    public final CommandName getName() { return command != null ? command.getName() : null; }
    
    @Override
    public final CodeType getCodeType() { return CodeType.COMMAND; }
    
    @Override
    public String toString()
    {
        return command == null
                ? Arrays.toString(code)
                : command.getName().name().toLowerCase() + " " + Arrays.toString(code);
    }
    
    public static final Command parseErrorCommand(int line) { return new Command(line, null); }
    
    public static final Command decode(int line, Tuple tuple) throws CompilerError
    {
        if(tuple == null || tuple.isEmpty())
            return null;
        Code first = tuple.get(0);
        if(first.getCodeType() != CodeType.COMMAND_WORD)
            return new Command(line,null,tuple.pack());
        tuple = tuple.subTuple(1);
        CommandWord cmdWord = (CommandWord) first;
        switch(cmdWord.getName())
        {
            default: throw new IllegalStateException();
            case VAR: return VAR(line, tuple);
            case GLOBAL: return GLOBAL(line, tuple);
            case IF: return IF(line, tuple);
            case ELSE: return ELSE(line, tuple);
            case WHILE: return WHILE(line, tuple);
            case FOR: return FOR(line, tuple);
            case SWITCH: return SWITCH(line, tuple);
            case CASE: return CASE(line, tuple);
            case DEFAULT: return DEFAULT(line, tuple);
            case BREAK: return BREAK(line, tuple);
            case CONTINUE: return CONTINUE(line, tuple);
            case TRY: return TRY(line, tuple);
            case CATCH: return CATCH(line, tuple);
            case THROW: return THROW(line, tuple);
            case RETURN: return RETURN(line, tuple);
            case YIELD: return YIELD(line, tuple);
            case DELEGATOR_YIELD: return DELEGATOR_YIELD(line, tuple);
            case STATIC: return STATIC(line, tuple);
            case CONST: return CONST(line, tuple);
        }
    }
    
    private static Command VAR(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.isEmpty())
            throw CompilerError.expectedAny(CommandWord.VAR);
        boolean constant = false;
        if(tuple.get(0) == CommandWord.CONST)
        {
            tuple = tuple.subTuple(1);
            constant = true;
        }
        ParsedCode code = tuple.pack(true);
        switch(code.getCodeType())
        {
            case ASSIGNATION:
                if(!((Assignation) code).hasIdentifiersOnly())
                    throw new CompilerError("In \"var\" can put only identifier in left part");
            case DECLARATION:
                break;
            default: throw new CompilerError("Expected a valid assignation or declaration in \"var\" command");
        }
        
        return new Command(line, CommandWord.VAR, code, constant ? Literal.TRUE : Literal.FALSE);
    }
    
    private static Command GLOBAL(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.isEmpty())
            throw CompilerError.expectedAny(CommandWord.GLOBAL);
        boolean constant = false;
        if(tuple.get(0) == CommandWord.CONST)
        {
            tuple = tuple.subTuple(1);
            constant = true;
        }
        ParsedCode code = tuple.pack(true);
        switch(code.getCodeType())
        {
            case ASSIGNATION:
                if(!((Assignation) code).hasIdentifiersOnly())
                    throw new CompilerError("In \"global\" can put only identifier in left part");
            case DECLARATION:
                break;
            default: throw new CompilerError("Expected a valid assignation or declaration in \"global\" command");
        }
        
        return new Command(line, CommandWord.GLOBAL, code, constant ? Literal.TRUE : Literal.FALSE);
    }
    
    private static Command IF(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.isEmpty())
            throw CompilerError.expectedAny(CommandWord.IF);
        ParsedCode cond = tuple.get(0);
        Code scope = tuple.get(1);
        if(!cond.is(CodeType.BLOCK) || !((Block)cond).isArgumentsList())
            throw new CompilerError("Malformed \"if\" command");
        if(scope.is(CodeType.BLOCK))
        {
            if(((Block)scope).isScope())
            {
                if(tuple.length() != 2)
                    throw new CompilerError("Malformed \"if\" command");
                return new Command(line, CommandWord.IF, cond, (Scope) scope);
            }
        }
        tuple = tuple.subTuple(1);
        Command cmd = decode(line, tuple);
        return new Command(line, CommandWord.IF, cond, Block.scope(cmd));
    }
    
    private static Command ELSE(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.isEmpty())
            throw CompilerError.expectedAny(CommandWord.ELSE);
        Code scope = tuple.get(0);
        if(scope.is(CodeType.BLOCK))
        {
            if(((Block)scope).isScope())
            {
                if(tuple.length() != 1)
                    throw new CompilerError("Malformed \"else\" command");
                return new Command(line, CommandWord.ELSE, (Scope) scope);
            }
        }
        
        Command cmd = decode(line, tuple);
        if(cmd.getName() == CommandName.IF)
            return new Command(line, CommandWord.ELSE, cmd);
        else return new Command(line, CommandWord.ELSE, Block.scope(cmd));
    }
    
    private static Command WHILE(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.isEmpty())
            throw CompilerError.expectedAny(CommandWord.WHILE);
        Code cond = tuple.get(0);
        Code scope = tuple.get(1);
        if(!cond.is(CodeType.BLOCK) || !((Block)cond).isArgumentsList())
            throw new CompilerError("Malformed \"while\" command");
        if(scope.is(CodeType.BLOCK))
        {
            if(((Block)scope).isScope())
            {
                if(tuple.length() != 2)
                    throw new CompilerError("Malformed \"while\" command");
                return new Command(line, CommandWord.WHILE, (ParsedCode) cond, (Scope) scope);
            }
        }
        tuple = tuple.subTuple(1);
        Command cmd = decode(line, tuple);
        return new Command(line, CommandWord.WHILE, (ParsedCode) cond, Block.scope(cmd));
    }
    
    private static Command FOR(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.isEmpty())
            throw CompilerError.expectedAny(CommandWord.FOR);
        Code cpars = tuple.get(0);
        if(!cpars.is(CodeType.BLOCK) || !((Block)cpars).isArgumentsList())
            throw new CompilerError("Malformed \"for\" command");
        Block pars = (Block) cpars;
        Code cscope = tuple.get(1);
        Scope scope;
        if(cscope.is(CodeType.BLOCK) && ((Block)cscope).isScope())
        {
            if(tuple.length() != 2)
                throw new CompilerError("Malformed \"while\" command");
            scope = (Scope) cscope;
        }
        else
        {
            tuple = tuple.subTuple(1);
            Command cmd = decode(line, tuple);
            scope = Block.scope(cmd);
        }
        switch(pars.getCodeCount())
        {
            case 2: {
                ParsedCode cvars = pars.getCode(0);
                if(!cvars.is(CodeType.IDENTIFIER, CodeType.DECLARATION))
                    throw new CompilerError("Invalid vars declaration in foreach. Expected a valid identifier o several identifiers: " + cvars);
                return new Command(line, CommandWord.FOR, cvars, pars.getCode(1), scope);
            }
            case 3:
                return new Command(line, CommandWord.FOR, pars.getCode(0), pars.getCode(1), pars.getCode(2), scope);
            default:
                throw new CompilerError("Malformed \"for\" command: Expected for(code;code;code) or for(code : code)");
        }
    }
    
    private static Command SWITCH(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.length() != 2)
            throw new CompilerError("Malformed \"switch\" command");
        Code cpars = tuple.get(0);
        if(!cpars.is(CodeType.BLOCK) || !((Block)cpars).isArgumentsList())
            throw new CompilerError("Malformed \"switch\" command");
        Block pars = (Block) cpars;
        if(pars.getCodeCount() < 1)
            throw new CompilerError("Expected a valid statement in \"switch\" command");
        Code cscope = tuple.get(1);
        if(!cscope.is(CodeType.BLOCK) || !((Block)cscope).isScope())
            throw new CompilerError("Expected a valid scope in switch command");
        
        return new Command(line, CommandWord.SWITCH, pars, (Scope) cscope);
    }
    
    private static Command CASE(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.length() != 2)
            throw new CompilerError("Invalid case command. Correct form is: \"case <literal>:\"");
        if(tuple.get(1) != Separator.TWO_POINTS)
            throw new CompilerError("Invalid case command. Correct form is: \"case <literal>:\"");
        
        Code literal = tuple.get(0);
        if(!literal.is(CodeType.LITERAL))
            throw new CompilerError("Expected a valid literal in case command. Correct form is: \"case <literal>:\"");
        
        return new Command(line, CommandWord.CASE, (Literal) literal);
    }
    
    private static Command DEFAULT(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.length() != 1)
            throw new CompilerError("Invalid default command. Correct form is: \"default:\"");
        if(tuple.get(0) != Separator.TWO_POINTS)
            throw new CompilerError("Invalid default command. Correct form is: \"default:\"");
        
        return new Command(line, CommandWord.DEFAULT);
    }
    
    private static Command BREAK(int line, Tuple tuple) throws CompilerError
    {
        if(!tuple.isEmpty())
            throw new CompilerError("Invalid break command. Correct form is: \"break;\"");
        return new Command(line, CommandWord.BREAK);
    }
    
    private static Command CONTINUE(int line, Tuple tuple) throws CompilerError
    {
        if(!tuple.isEmpty())
            throw new CompilerError("Invalid continue command. Correct form is: \"continue;\"");
        return new Command(line, CommandWord.CONTINUE);
    }
    
    private static Command TRY(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.length() != 1)
            throw new CompilerError("Invalid try command. Correct form is: \"try { <...> };\"");
        Code cscope = tuple.get(0);
        if(!cscope.is(CodeType.BLOCK) || !((Block)cscope).isScope())
            throw new CompilerError("Expected a valid scope in try command");
        
        return new Command(line, CommandWord.TRY, (Scope) cscope);
    }
    
    private static Command CATCH(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.length() != 2)
            throw new CompilerError("Invalid catch command. Correct form is: \"catch(<exception_name>) { <...> };\"");
        Code cid = tuple.get(0);
        if(!cid.is(CodeType.BLOCK))
            throw new CompilerError("Expected a valid identifier for exception name in catch command");
        Block bid = (Block) cid;
        if(!bid.getCode(0).is(CodeType.IDENTIFIER))
            throw new CompilerError("Expected a valid identifier for exception name in catch command");
        Code cscope = tuple.get(1);
        if(!cscope.is(CodeType.BLOCK) || !((Block)cscope).isScope())
            throw new CompilerError("Expected a valid scope in catch command");
        
        return new Command(line, CommandWord.CATCH, bid.getCode(0), (Scope) cscope);
    }
    
    private static Command THROW(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.isEmpty())
            throw CompilerError.expectedAny(CommandWord.THROW);
        if(tuple.get(0).is(CodeType.BLOCK))
        {
            Block pars = tuple.get(0);
            if(pars.isArgumentsList())
            {
                if(tuple.length() != 1)
                    throw new CompilerError("Invalid throw command. Correct form is: \"throw <...>,...; or throw (<...>,...);\"");
                return new Command(line, CommandWord.THROW, pars);
            }
        }
        Block pars = Block.arguments(tuple, Separator.COMMA);
        return new Command(line, CommandWord.THROW, pars);
    }
    
    private static Command RETURN(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.isEmpty())
            return new Command(line, CommandWord.RETURN);
        if(tuple.get(0).is(CodeType.BLOCK))
        {
            Block pars = tuple.get(0);
            if(pars.isArgumentsList())
            {
                if(tuple.length() != 1)
                    throw new CompilerError("Invalid return command. Correct form is: \"return <...>,...; or return (<...>,...);\"");
                return new Command(line, CommandWord.RETURN, pars);
            }
        }
        Block pars = Block.arguments(tuple, Separator.COMMA);
        return new Command(line, CommandWord.RETURN, pars);
    }
    
    private static Command YIELD(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.isEmpty())
            throw CompilerError.expectedAny(CommandWord.YIELD);
        if(tuple.get(0).is(CodeType.BLOCK))
        {
            Block pars = tuple.get(0);
            if(pars.isArgumentsList())
            {
                if(tuple.length() != 1)
                    throw new CompilerError("Invalid yield command. Correct form is: \"yield <...>,...; or yield (<...>,...);\"");
                return new Command(line, CommandWord.YIELD, pars);
            }
        }
        Block pars = Block.arguments(tuple, Separator.COMMA);
        return new Command(line, CommandWord.YIELD, pars);
    }
    
    private static Command DELEGATOR_YIELD(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.isEmpty())
            throw CompilerError.expectedAny(CommandWord.DELEGATOR_YIELD);
        ParsedCode code = tuple.pack();
        return new Command(line, CommandWord.DELEGATOR_YIELD, code);
    }
    
    private static Command STATIC(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.isEmpty())
            throw CompilerError.expectedAny(CommandWord.STATIC);
        boolean constant = false;
        if(tuple.get(0) == CommandWord.CONST)
        {
            tuple = tuple.subTuple(1);
            constant = true;
        }
        ParsedCode code = tuple.pack(true);
        switch(code.getCodeType())
        {
            case ASSIGNATION:
                if(!((Assignation) code).hasIdentifiersAndLiteralsOnly())
                    throw new CompilerError("In \"static\" can put only identifier in left part and literals in right part");
            case DECLARATION:
                break;
            default: throw new CompilerError("Expected a valid assignation or declaration in \"static\" command");
        }
        
        return new Command(line, CommandWord.STATIC, code, constant ? Literal.TRUE : Literal.FALSE);
    }
    
    private static Command CONST(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.isEmpty())
            throw CompilerError.expectedAny(CommandWord.CONST);
        if(tuple.get(0).is(CodeType.COMMAND_WORD))
        {
            CommandName cname = tuple.<CommandWord>get(0).getName();
            switch(cname)
            {
                default: throw new CompilerError("Invalid command in const declaration: " + tuple);
                case VAR:
                case GLOBAL:
                case STATIC:
                    tuple = tuple.insert(1, CommandWord.CONST);
                    return decode(line, tuple);
            }
        }
        tuple = tuple.insertAtStart(CommandWord.GLOBAL, CommandWord.CONST);
        return decode(line, tuple);
    }
}

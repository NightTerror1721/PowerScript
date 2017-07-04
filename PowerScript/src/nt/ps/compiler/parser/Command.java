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
    
    public final CommandName getName() { return command.getName(); }
    
    @Override
    public final CodeType getCodeType() { return CodeType.COMMAND; }
    
    @Override
    public String toString() { return command.getName().name().toLowerCase() + " " + Arrays.toString(code); }
    
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
        }
    }
    
    private static Command VAR(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.isEmpty())
            throw CompilerError.expectedAny(CommandWord.VAR);
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
        
        return new Command(line, CommandWord.VAR, code);
    }
    
    private static Command GLOBAL(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.isEmpty())
            throw CompilerError.expectedAny(CommandWord.GLOBAL);
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
        
        return new Command(line, CommandWord.GLOBAL, code);
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
}

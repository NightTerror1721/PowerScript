/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import nt.ps.compiler.exception.CompilerError;
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
    
    public final ParsedCode getCode(int index) { return code[index]; }
    
    public final CommandName getName() { return command.getName(); }
    
    @Override
    public final CodeType getCodeType() { return CodeType.COMMAND; }
    
    @Override
    public String toString() { return command.getName().name().toLowerCase() + " " + code; }
    
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
        if(tuple.length() != 2)
            throw new CompilerError("Malformed \"if\" command");
        ParsedCode cond = tuple.get(0);
        ParsedCode scope = tuple.get(1);
        if(!cond.is(CodeType.BLOCK) || !((Block)cond).isArgumentsList())
            throw new CompilerError("Malformed \"if\" command");
        if(scope.is(CodeType.BLOCK))
        {
            if(!((Block)scope).isScope())
                throw new CompilerError("Expected a valid code object or scope in \"if\" command");
            return new Command(line, CommandWord.IF, scope);
        }
        if(!scope.isValidCodeObject())
            throw new CompilerError("Expected a valid code object or scope in \"if\" command");
        return new Command(line, CommandWord.IF, scope);
    }
}

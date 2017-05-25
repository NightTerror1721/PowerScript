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
        if(line < 1)
            throw new IllegalStateException();
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
            case IF: return IF(line, tuple);
        }
    }
    
    private static Command VAR(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.isEmpty())
            throw CompilerError.expectedAny(CommandWord.VAR);
        ParsedCode code = tuple.pack();
        if(!code.is(CodeType.ASSIGNATION))
            throw new CompilerError("Expected a valid assignation in \"var\" command");
        Assignation a = (Assignation) code;
        if(!a.hasIdentifiersOnly())
            throw new CompilerError("In \"var\" can put only identifier in left part");
        
        return new Command(line, CommandWord.VAR, a);
    }
    
    private static Command IF(int line, Tuple tuple) throws CompilerError
    {
        if(tuple.isEmpty())
            throw CompilerError.expectedAny(CommandWord.IF);
        ParsedCode code = tuple.pack();
        if(!code.isValidCodeObject())
            throw new CompilerError("Expected a valid code object in \"if\" command");
        return new Command(line, CommandWord.IF, code);
    }
}

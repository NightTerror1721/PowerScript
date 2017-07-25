/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.HashMap;

/**
 *
 * @author Asus
 */
public final class CommandWord extends Code
{
    private final CommandName name;
    
    private CommandWord(CommandName name)
    {
        if(name == null)
            throw new NullPointerException();
        this.name = name;
    }
    
    public final CommandName getName() { return name; }
    
    @Override
    public final String toString() { return name.name; }
    
    @Override
    public final CodeType getCodeType() { return CodeType.COMMAND_WORD; }
    
    @Override
    public final boolean isValidCodeObject() { return true; }
    
    public static final CommandWord
            VAR = new CommandWord(CommandName.VAR),
            IF = new CommandWord(CommandName.IF),
            ELSE = new CommandWord(CommandName.ELSE),
            WHILE = new CommandWord(CommandName.WHILE),
            FOR = new CommandWord(CommandName.FOR),
            SWITCH = new CommandWord(CommandName.SWITCH),
            CASE = new CommandWord(CommandName.CASE),
            DEFAULT = new CommandWord(CommandName.DEFAULT),
            RETURN = new CommandWord(CommandName.RETURN),
            YIELD = new CommandWord(CommandName.YIELD),
            CONTINUE = new CommandWord(CommandName.CONTINUE),
            BREAK = new CommandWord(CommandName.BREAK),
            GLOBAL = new CommandWord(CommandName.GLOBAL),
            TRY = new CommandWord(CommandName.TRY),
            CATCH = new CommandWord(CommandName.CATCH),
            THROW = new CommandWord(CommandName.THROW),
            CONST = new CommandWord(CommandName.CONST);
    
    private static final HashMap<String, CommandWord> HASH = collect(CommandWord.class, k -> k.name.name);
    
    public static final boolean isCommand(String str) { return HASH.containsKey(str); }
    static final CommandWord getCommandWord(String str) { return HASH.get(str); }
    
    public enum CommandName
    {
        VAR, IF, ELSE, WHILE, FOR, SWITCH, CASE, DEFAULT, RETURN, YIELD, CONTINUE,
        BREAK, GLOBAL, TRY, CATCH, THROW, CONST;
        
        private final String name = name().toLowerCase();
    }
}

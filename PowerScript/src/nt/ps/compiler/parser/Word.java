/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.regex.Pattern;

/**
 *
 * @author Asus
 */
public class Word extends CodeObject
{
    private final String word;
    
    private Word(String word)
    {
        if(word == null)
            throw new NullPointerException();
        if(word.isEmpty())
            throw new IllegalArgumentException();
        this.word = word;
    }
    
    @Override
    public final String toString() { return word; }
    
    @Override
    public final boolean equals(Object o)
    {
        return o instanceof Word &&
                word.equals(((Word)o).word);
    }
    
    @Override
    public CodeType getCodeType() { return CodeType.WORD; }
    
    private static final Pattern ID_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    public static final boolean isValidIdentifier(String identifier)
    {
        return ID_PATTERN.matcher(identifier).matches();
    }
    
    public static final Code create(String word)
    {
        if(CommandWord.isCommand(word))
            return CommandWord.getCommandWord(word);
        switch(word)
        {
            default: return new Word(word);
            case "self": return new SelfWord();
        }
    }
    
    public static final class SelfWord extends Word
    {
        private SelfWord() { super("self"); }
        @Override
        public final CodeType getCodeType() { return CodeType.SELF; }
    }
}

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
    
    public Word(String word)
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
    public final boolean isWord() { return true; }
    
    private static final Pattern ID_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    public static final boolean isValidIdentifier(String identifier)
    {
        return ID_PATTERN.matcher(identifier).matches();
    }
}

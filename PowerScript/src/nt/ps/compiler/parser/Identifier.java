/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.regex.Pattern;
import nt.ps.compiler.exception.CompilerError;

/**
 *
 * @author Asus
 */
public class Identifier extends CodeObject
{
    private final String word;
    
    private Identifier(String word)
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
        return o instanceof Identifier &&
                word.equals(((Identifier)o).word);
    }
    
    @Override
    public CodeType getCodeType() { return CodeType.IDENTIFIER; }
    
    private static final Pattern ID_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    public static final boolean isValidIdentifier(String identifier)
    {
        return ID_PATTERN.matcher(identifier).matches();
    }
    public static final void checkValidIdentifier(String identifier) throws CompilerError
    {
        if(!isValidIdentifier(identifier))
            throw CompilerError.invalidIdentifier(identifier);
    }
    
    public static final Code create(String word, boolean canBeUnaryOperator)
    {
        if(CommandWord.isCommand(word))
            return CommandWord.getCommandWord(word);
        if(OperatorSymbol.isOperator(word))
            return OperatorSymbol.getOperator(word, canBeUnaryOperator);
        switch(word)
        {
            default: return new Identifier(word);
            case "self": return new SelfWord();
            case "super": return new SuperWord();
        }
    }
    
    public static final class SelfWord extends Identifier
    {
        private SelfWord() { super("self"); }
        @Override
        public final CodeType getCodeType() { return CodeType.SELF; }
    }
    
    public static final class SuperWord extends Identifier
    {
        private SuperWord() { super("super"); }
        @Override
        public final CodeType getCodeType() { return CodeType.SUPER; }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 *
 * @author Asus
 */
public final class Declaration extends ParsedCode
{
    private final Identifier[] identifiers;
    
    private Declaration(Identifier[] identifiers)
    {
        this.identifiers = identifiers;
    }
    
    public static final Declaration decode(Tuple tuple)
    {
        Tuple[] parts = tuple.splitByToken(Separator.COMMA);
        for(Tuple part : parts)
            if(part.length() != 1 || !part.get(0).is(CodeType.IDENTIFIER))
                return null;
        return new Declaration(Arrays.stream(parts).map(part -> (Identifier) part.get(0)).toArray(size -> new Identifier[size]));
    }
    
    @Override
    public final String toString()
    {
        StringJoiner joiner = new StringJoiner(", ");
        for(Identifier id : identifiers)
            joiner.add(id.toString());
        return joiner.toString();
    }

    @Override
    public final CodeType getCodeType() { return CodeType.DECLARATION; }
    
    public final int getIdentifierCount() { return identifiers.length; }
    public final Identifier getIdentifier(int index) { return identifiers[index]; }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

/**
 *
 * @author Asus
 */
public final class VarargsIdentifier extends ParsedCode
{
    private final Identifier identifier;
    
    public VarargsIdentifier(Identifier identifier)
    {
        if(identifier == null)
            throw new NullPointerException();
        this.identifier = identifier;
    }
    
    public final Identifier getIdentifier() { return identifier; }
    
    @Override
    public final String toString() { return identifier + "..."; }

    @Override
    public final CodeType getCodeType() { return CodeType.VARARGS_IDENTIFIER; }
    
}

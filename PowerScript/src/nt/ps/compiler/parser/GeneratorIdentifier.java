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
public final class GeneratorIdentifier extends ParsedCode
{
    public static final GeneratorIdentifier GENERATOR = new GeneratorIdentifier();
    
    private GeneratorIdentifier() {}
    
    @Override
    public final String toString() { return "*"; }

    @Override
    public final CodeType getCodeType() { return CodeType.GENERATOR_IDENTIFIER; }
    
}

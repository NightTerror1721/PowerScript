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
public final class OperatorSymbol extends CodePart
{
    private final String symbol;
    private final 
    
    private OperatorSymbol(String symbol) { this.symbol = symbol; }
    
    @Override
    public final String toString() { return symbol; }
    
    /*public static final OperatorSymbol
            ;*/
    
}

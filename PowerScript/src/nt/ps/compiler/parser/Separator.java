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
public class Separator extends CodePart
{
    private final String symbol;
    
    private Separator(String symbol) { this.symbol = symbol; }
    
    @Override
    public String toString() { return symbol; }
    
    @Override
    public final boolean isSeparator() { return true; }
    
    private static final HashMap<String, Separator> HASH = collect(Separator.class,s -> s.symbol);
    
    public static final Separator
            COMMA = new Separator(","),
            COLON = new Separator(";"),
            TWO_POINTS = new Separator(":");
    
    public static final boolean isSeparator(String str) { return HASH.containsKey(str); }
    
    public static final Separator getSeparator(String str) { return HASH.get(str); }
    
}

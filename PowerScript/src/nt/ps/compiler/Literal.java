/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import nt.ps.lang.PSValue;

/**
 *
 * @author Asus
 */
public final class Literal extends CodePart
{
    private final PSValue value;
    
    private Literal() { value = PSValue.NULL; }
    private Literal(boolean bool) { value = PSValue.valueOf(bool); }
    private Literal(int number) { value = PSValue.valueOf(number); }
    private Literal(long number) { value = PSValue.valueOf(number); }
    private Literal(float number) { value = PSValue.valueOf(number); }
    private Literal(double number) { value = PSValue.valueOf(number); }
    private Literal(String string) { value = PSValue.valueOf(string); }
    
    @Override
    public final boolean isLiteral() { return true; }
    
    @Override
    public final String toString() { return value.toJavaString(); }
    
    private static final Literal
            NULL = new Literal(),
            TRUE = new Literal(true),
            FALSE = new Literal(false),
            MINUSONE = new Literal(-1),
            ZERO = new Literal(0),
            ONE = new Literal(1),
            EMPTY_STRING = new Literal("");
    
    
    public static final Literal decode(String code)
    {
        
    }
}

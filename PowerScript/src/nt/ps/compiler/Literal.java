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
    
    @Override
    public final boolean equals(Object o)
    {
        return o instanceof Literal &&
                value.equals(((Literal)o).value).toJavaBoolean();
    }
    
    
    
    public static final Literal
            NULL = new Literal(),
            TRUE = new Literal(true),
            FALSE = new Literal(false),
            MINUSONE = new Literal(-1),
            ZERO = new Literal(0),
            ONE = new Literal(1),
            INFINITY = new Literal(Double.POSITIVE_INFINITY),
            NEGATIVE_INFINITY = new Literal(Double.NEGATIVE_INFINITY),
            NAN = new Literal(Double.NaN),
            EMPTY_STRING = new Literal("");
    
    
    public static final Literal decode(String code)
    {
        switch(code)
        {
            case "null": return NULL;
            case "true": return TRUE;
            case "false": return FALSE;
            case "-1": return MINUSONE;
            case "0": return ZERO;
            case "1": return ONE;
            case "Infinity": return INFINITY;
            case "-Infinity": return NEGATIVE_INFINITY;
            case "NaN": return NAN;
        }
        
        if((code.startsWith("\"") && code.endsWith("\"")) || (code.startsWith("\'") && code.endsWith("\'")))
            return code.length() == 2 ? EMPTY_STRING : new Literal(code.substring(1,code.length()-1));
        
        try { return new Literal(Integer.decode(code)); }
        catch(NumberFormatException ex) {}
        
        try { return new Literal(Long.decode(code)); }
        catch(NumberFormatException ex) {}
        
        try { return new Literal(Double.parseDouble(code)); }
        catch(NumberFormatException ex) {}
        
        try { return new Literal(Float.parseFloat(code)); }
        catch(NumberFormatException ex) {}
        
        return null;
    }
    
    public final Literal valueOf(int value)
    {
        switch(value)
        {
            case -1: return MINUSONE;
            case 0: return ZERO;
            case 1: return ONE;
            default: return new Literal(value);
        }
    }
    public final Literal valueOf(long value) { return new Literal(value); }
    public final Literal valueOf(float value) { return new Literal(value); }
    public final Literal valueOf(double value) { return new Literal(value); }
    public final Literal valueOf(boolean value) { return value ? TRUE : FALSE; }
    public final Literal valueOf(char value) { return new Literal(Character.toString(value)); }
    public final Literal valueOf(String value) { return value.isEmpty() ? EMPTY_STRING : new Literal(value); }
}

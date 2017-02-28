/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.datatype;

import java.util.Objects;

/**
 *
 * @author Asus
 */
public final class PSString extends PSValue
{
    public final String string;
    
    public PSString(String str) { string = str; }
    
    @Override
    public final PSDataType getPSType() { return PSDataType.STRING; }
    
    @Override
    public final PSString toPSString() { return this; }
    
    @Override
    public final int toJavaInt() { return Integer.decode(string); }

    @Override
    public final long toJavaLong() { return Long.decode(string); }

    @Override
    public final float toJavaFloat() { return Float.parseFloat(string); }

    @Override
    public final double toJavaDouble() { return Double.parseDouble(string); }

    @Override
    public final boolean toJavaBoolean() { return !string.isEmpty(); }

    @Override
    public final String toJavaString() { return string; }
    
    @Override
    public final boolean equals(Object o)
    {
        if(o instanceof PSString)
            return ((PSString)o).string.equals(string);
        return false;
    }

    @Override
    public final int hashCode()
    {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.string);
        return hash;
    }
    
    
    /* Operations */
    
    @Override public final PSValue equals(PSValue value) { return string.equals(value.toJavaString()) ? TRUE : FALSE; }
    @Override public final PSValue notEquals(PSValue value) { return string.equals(value.toJavaString()) ? FALSE : TRUE; }
    @Override public final PSValue greaterThan(PSValue value) { return string.compareTo(value.toJavaString()) > 0 ? TRUE : FALSE; }
    @Override public final PSValue smallerThan(PSValue value) { return string.compareTo(value.toJavaString()) < 0 ? TRUE : FALSE; }
    @Override public final PSValue greaterOrEqualsThan(PSValue value) { return string.compareTo(value.toJavaString()) >= 0 ? TRUE : FALSE; }
    @Override public final PSValue smallerOrEqualsThan(PSValue value) { return string.compareTo(value.toJavaString()) <= 0 ? TRUE : FALSE; }
    @Override public final PSValue negate() { return string.isEmpty() ? TRUE : FALSE; }
    
    @Override public final PSValue contains(PSValue value) { return string.contains(value.toJavaString()) ? TRUE : FALSE; }
}

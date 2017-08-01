/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang;

/**
 *
 * @author Asus
 */
public final class PSBoolean extends PSValue
{
    public final boolean bool;
    
    PSBoolean(boolean value) { bool = value; }
    
    @Override
    public final PSDataType getPSType() { return PSDataType.BOOLEAN; }
    
    @Override
    public final PSBoolean toPSBoolean() { return this; }
    
    @Override
    public final int toJavaInt() { return bool ? 1 : 0; }
    
    @Override
    public final long toJavaLong() { return bool ? 1 : 0; }
    
    @Override
    public final float toJavaFloat() { return bool ? 1 : 0; }
    
    @Override
    public final double toJavaDouble() { return bool ? 1 : 0; }
    
    @Override
    public final boolean toJavaBoolean() { return bool; }
    
    @Override
    public final String toJavaString() { return bool ? "true" : "false"; }
    
    @Override
    public final boolean equals(Object o)
    {
        return o instanceof PSBoolean && bool == ((PSBoolean)o).bool;
    }

    @Override
    public final int hashCode()
    {
        int hash = 5;
        hash = 41 * hash + (this.bool ? 1 : 0);
        return hash;
    }
    
    
    
    /* Operations */
    @Override
    public final PSValue equals(PSValue value) { return bool ? TRUE : FALSE; }
    @Override
    public final PSValue notEquals(PSValue value) { return bool ? TRUE : FALSE; }
    @Override
    public final PSValue negate() { return bool ? FALSE : TRUE; }
    
    @Override
    public PSValue createNewInstance() { return bool ? TRUE : FALSE; }
    @Override
    public PSValue createNewInstance(PSValue arg0)
    {
        return arg0.toJavaBoolean() ? TRUE : FALSE;
    }
    
    
    /* Properties */
    
    @Override
    public final PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
            case "toString": return TO_STRING;
        }
    }
    
    
    private static final PSValue TO_STRING = PSFunction.<PSBoolean>method((self) -> new PSString(self.bool ? "true" : "false"));
}

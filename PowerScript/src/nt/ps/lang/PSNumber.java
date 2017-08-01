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
public abstract class PSNumber extends PSValue
{
    private PSNumber() {}
    
    @Override
    public final PSDataType getPSType() { return PSDataType.NUMBER; }
    
    @Override
    public final PSNumber toPSNumber() { return this; }
    
    public boolean isInteger() { return false; }
    public boolean isLong() { return false; }
    public boolean isFloat() { return false; }
    public boolean isDouble() { return false; }
    
    public boolean isDecimal() { return false; }
    
    @Override
    public final boolean equals(Object o)
    {
        return o instanceof PSNumber && toJavaDouble() == ((PSNumber)o).toJavaDouble();
    }
    
    @Override
    public final int hashCode()
    {
        int hash = 7;
        hash = 29 * hash + Float.floatToRawIntBits(toJavaFloat());
        return hash;
    }
    
    @Override
    public PSValue createNewInstance() { return new PSNumber.PSDouble(toJavaDouble()); }
    @Override
    public PSValue createNewInstance(PSValue arg0)
    {
        return new PSNumber.PSDouble(arg0.toJavaDouble());
    }
    
    
    /* Properties */
    @Override
    public final PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
            case "isFinite": return IS_FINITE;
            case "isInteger": return IS_INTEGER;
            case "isNaN": return IS_NAN;
            case "integer": return new PSLong(toJavaLong());
            case "float": return new PSDouble(toJavaDouble());
            case "toString": return TO_STRING;
        }
    }
    
    private static final PSValue IS_FINITE = PSFunction.<PSNumber>method(self -> {
        if(self.isFloat())
            return Float.isFinite(((PSFloat)self).number) ? TRUE : FALSE;
        if(self.isDouble())
            return Double.isFinite(((PSDouble)self).number) ? TRUE : FALSE;
        return FALSE;
    });
    private static final PSValue IS_INTEGER = PSFunction.<PSNumber>method(self -> self.isDecimal() ? FALSE : TRUE);
    private static final PSValue IS_NAN = PSFunction.<PSNumber>method(self -> {
        if(self.isFloat())
            return Float.isNaN(((PSFloat)self).number) ? TRUE : FALSE;
        if(self.isDouble())
            return Double.isNaN(((PSDouble)self).number) ? TRUE : FALSE;
        return FALSE;
    });
    private static final PSValue TO_STRING = PSFunction.<PSNumber>method(self -> new PSString(self.toJavaString()));
    
    
    
    
    public static final class PSInteger extends PSNumber
    {
        public final int number;
        
        public PSInteger(int value) { number = value; }
        
        @Override public final boolean isInteger() { return true; }
        
        @Override public final int toJavaInt() { return number; }
        @Override public final long toJavaLong() { return number; }
        @Override public final float toJavaFloat() { return number; }
        @Override public final double toJavaDouble() { return number; }
        @Override public final boolean toJavaBoolean() { return number != 0; }
        @Override public final String toJavaString() { return Integer.toString(number); }
        
        @Override public final PSValue plus(PSValue value) { return new PSDouble(number + value.toJavaDouble()); }
        @Override public final PSValue minus(PSValue value) { return new PSDouble(number - value.toJavaDouble()); }
        @Override public final PSValue multiply(PSValue value) { return new PSDouble(number * value.toJavaDouble()); }
        @Override public final PSValue divide(PSValue value) { return new PSDouble(number / value.toJavaDouble()); }
        @Override public final PSValue module(PSValue value) { return new PSLong(number % value.toJavaLong()); }
        @Override public final PSValue negative() { return new PSInteger(-number); }
        @Override public final PSValue increase() { return new PSInteger(number + 1); }
        @Override public final PSValue decrease() { return new PSInteger(number - 1); }

        @Override public final PSValue shiftLeft(PSValue value) { return new PSLong(number << value.toJavaLong()); }
        @Override public final PSValue shiftRight(PSValue value) { return new PSLong(number >> value.toJavaLong()); }
        @Override public final PSValue logicAnd(PSValue value) { return new PSLong(number & value.toJavaLong()); }
        @Override public final PSValue logicOr(PSValue value) { return new PSLong(number | value.toJavaLong()); }
        @Override public final PSValue logicNot() { return new PSLong(~number); }
        @Override public final PSValue logicXor(PSValue value) { return new PSLong(number ^ value.toJavaLong()); }

        @Override public final PSValue equals(PSValue value) { return number == value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue notEquals(PSValue value) { return number != value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue greaterThan(PSValue value) { return number > value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue smallerThan(PSValue value) { return number < value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue greaterOrEqualsThan(PSValue value) { return number >= value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue smallerOrEqualsThan(PSValue value) { return number <= value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue negate() { return number == 0 ? TRUE : FALSE; }
    }
    
    public static final class PSLong extends PSNumber
    {
        public final long number;
        
        public PSLong(long value) { number = value; }
        
        @Override public final boolean isLong() { return true; }
        
        @Override public final int toJavaInt() { return (int) number; }
        @Override public final long toJavaLong() { return number; }
        @Override public final float toJavaFloat() { return number; }
        @Override public final double toJavaDouble() { return number; }
        @Override public final boolean toJavaBoolean() { return number != 0; }
        @Override public final String toJavaString() { return Long.toString(number); }
        
        @Override public final PSValue plus(PSValue value) { return new PSDouble(number + value.toJavaDouble()); }
        @Override public final PSValue minus(PSValue value) { return new PSDouble(number - value.toJavaDouble()); }
        @Override public final PSValue multiply(PSValue value) { return new PSDouble(number * value.toJavaDouble()); }
        @Override public final PSValue divide(PSValue value) { return new PSDouble(number / value.toJavaDouble()); }
        @Override public final PSValue module(PSValue value) { return new PSLong(number % value.toJavaLong()); }
        @Override public final PSValue negative() { return new PSLong(-number); }
        @Override public final PSValue increase() { return new PSLong(number + 1); }
        @Override public final PSValue decrease() { return new PSLong(number - 1); }

        @Override public final PSValue shiftLeft(PSValue value) { return new PSLong(number << value.toJavaLong()); }
        @Override public final PSValue shiftRight(PSValue value) { return new PSLong(number >> value.toJavaLong()); }
        @Override public final PSValue logicAnd(PSValue value) { return new PSLong(number & value.toJavaLong()); }
        @Override public final PSValue logicOr(PSValue value) { return new PSLong(number | value.toJavaLong()); }
        @Override public final PSValue logicNot() { return new PSLong(~number); }
        @Override public final PSValue logicXor(PSValue value) { return new PSLong(number ^ value.toJavaLong()); }

        @Override public final PSValue equals(PSValue value) { return number == value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue notEquals(PSValue value) { return number != value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue greaterThan(PSValue value) { return number > value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue smallerThan(PSValue value) { return number < value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue greaterOrEqualsThan(PSValue value) { return number >= value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue smallerOrEqualsThan(PSValue value) { return number <= value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue negate() { return number == 0 ? TRUE : FALSE; }
    }
    
    public static final class PSFloat extends PSNumber
    {
        public final float number;
        
        public PSFloat(float value) { number = value; }
        
        @Override public final boolean isFloat() { return true; }
        @Override public final boolean isDecimal() { return true; }
        
        @Override public final int toJavaInt() { return (int) number; }
        @Override public final long toJavaLong() { return (long) number; }
        @Override public final float toJavaFloat() { return number; }
        @Override public final double toJavaDouble() { return number; }
        @Override public final boolean toJavaBoolean() { return number != 0; }
        @Override public final String toJavaString() { return Float.toString(number); }
        
        @Override public final PSValue plus(PSValue value) { return new PSDouble(number + value.toJavaDouble()); }
        @Override public final PSValue minus(PSValue value) { return new PSDouble(number - value.toJavaDouble()); }
        @Override public final PSValue multiply(PSValue value) { return new PSDouble(number * value.toJavaDouble()); }
        @Override public final PSValue divide(PSValue value) { return new PSDouble(number / value.toJavaDouble()); }
        @Override public final PSValue module(PSValue value) { return new PSLong((long) number % value.toJavaLong()); }
        @Override public final PSValue negative() { return new PSFloat(-number); }
        @Override public final PSValue increase() { return new PSFloat(number + 1); }
        @Override public final PSValue decrease() { return new PSFloat(number - 1); }

        @Override public final PSValue shiftLeft(PSValue value) { return new PSLong((long) number << value.toJavaLong()); }
        @Override public final PSValue shiftRight(PSValue value) { return new PSLong((long) number >> value.toJavaLong()); }
        @Override public final PSValue logicAnd(PSValue value) { return new PSLong((long) number & value.toJavaLong()); }
        @Override public final PSValue logicOr(PSValue value) { return new PSLong((long) number | value.toJavaLong()); }
        @Override public final PSValue logicNot() { return new PSLong(~((long) number)); }
        @Override public final PSValue logicXor(PSValue value) { return new PSLong((long) number ^ value.toJavaLong()); }

        @Override public final PSValue equals(PSValue value) { return number == value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue notEquals(PSValue value) { return number != value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue greaterThan(PSValue value) { return number > value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue smallerThan(PSValue value) { return number < value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue greaterOrEqualsThan(PSValue value) { return number >= value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue smallerOrEqualsThan(PSValue value) { return number <= value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue negate() { return number == 0 ? TRUE : FALSE; }
    }
    
    public static final class PSDouble extends PSNumber
    {
        public final double number;
        
        public PSDouble(double value) { number = value; }
        
        @Override public final boolean isDouble() { return true; }
        @Override public final boolean isDecimal() { return true; }
        
        @Override public final int toJavaInt() { return (int) number; }
        @Override public final long toJavaLong() { return (long) number; }
        @Override public final float toJavaFloat() { return (float) number; }
        @Override public final double toJavaDouble() { return number; }
        @Override public final boolean toJavaBoolean() { return number != 0; }
        @Override public final String toJavaString() { return Double.toString(number); }
        
        @Override public final PSValue plus(PSValue value) { return new PSDouble(number + value.toJavaDouble()); }
        @Override public final PSValue minus(PSValue value) { return new PSDouble(number - value.toJavaDouble()); }
        @Override public final PSValue multiply(PSValue value) { return new PSDouble(number * value.toJavaDouble()); }
        @Override public final PSValue divide(PSValue value) { return new PSDouble(number / value.toJavaDouble()); }
        @Override public final PSValue module(PSValue value) { return new PSLong((long) number % value.toJavaLong()); }
        @Override public final PSValue negative() { return new PSDouble(-number); }
        @Override public final PSValue increase() { return new PSDouble(number + 1); }
        @Override public final PSValue decrease() { return new PSDouble(number - 1); }

        @Override public final PSValue shiftLeft(PSValue value) { return new PSLong((long) number << value.toJavaLong()); }
        @Override public final PSValue shiftRight(PSValue value) { return new PSLong((long) number >> value.toJavaLong()); }
        @Override public final PSValue logicAnd(PSValue value) { return new PSLong((long) number & value.toJavaLong()); }
        @Override public final PSValue logicOr(PSValue value) { return new PSLong((long) number | value.toJavaLong()); }
        @Override public final PSValue logicNot() { return new PSLong(~((long) number)); }
        @Override public final PSValue logicXor(PSValue value) { return new PSLong((long) number ^ value.toJavaLong()); }

        @Override public final PSValue equals(PSValue value) { return number == value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue notEquals(PSValue value) { return number != value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue greaterThan(PSValue value) { return number > value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue smallerThan(PSValue value) { return number < value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue greaterOrEqualsThan(PSValue value) { return number >= value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue smallerOrEqualsThan(PSValue value) { return number <= value.toJavaDouble() ? TRUE : FALSE; }
        @Override public final PSValue negate() { return number == 0 ? TRUE : FALSE; }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.ps.lang;

import java.util.Objects;
import java.util.StringJoiner;

/**
 *
 * @author Asus
 */
public final class PSString extends PSValue
{
    public final String string;
    
    public PSString(String str) { string = str; }
    
    @Override
    public final PSDataType getPSDataType() { return PSDataType.STRING; }
    
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
            return ((PSString) o).string.equals(string);
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
    
    
    @Override
    public PSValue createNewInstance() { return new PSString(string); }
    @Override
    public PSValue createNewInstance(PSValue arg0)
    {
        return new PSString(arg0.toJavaString());
    }
    
    
    /* Properties */
    @Override
    public final PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
            case "length": return new PSNumber.PSInteger(string.length());
            case "charAt": return CHAR_AT;
            case "concat": return CONCAT;
            case "endsWith": return ENDS_WITH;
            case "contains": return CONTAINS;
            case "indexOf": return INDEX_OF;
            case "lastIndexOf": return LAST_INDEX_OF;
            case "repeat": return REPEAT;
            case "substring": return SUBSTRING;
            case "startsWith": return STARTS_WITH;
            case "toString": return TO_STRING;
            case "toLowerCase": return TO_LOWER_CASE;
            case "toUpperCase": return TO_UPPER_CASE;
            case "trim": return TRIM;
        }
    }
    
    
    private static final PSValue CHAR_AT = PSFunction.<PSString>method((self, arg0) ->  {
        return new PSString(Character.toString(self.string.charAt(arg0.toJavaInt())));
    });
    private static final PSValue CONCAT = PSFunction.<PSString>method((self, args) -> {
        switch(args.numberOfArguments())
        {
            case 0:
                return self;
            case 1:
                return new PSString(self.string + args.arg0().toJavaString());
            default: {
                StringJoiner joiner = new StringJoiner(args.arg0().toJavaString());
                int len = args.numberOfArguments();
                for(int i=1;i<len;i++)
                    joiner.add(args.arg(i).toString());
                return new PSString(joiner.toString());
            }
        }
    });
    private static final PSValue ENDS_WITH = PSFunction.<PSString>method((self, arg0) -> {
        return self.string.endsWith(arg0.toJavaString()) ? TRUE : FALSE;
    });
    private static final PSValue CONTAINS = PSFunction.<PSString>method((self, arg0) -> {
        return self.string.contains(arg0.toJavaString()) ? TRUE : FALSE;
    });
    private static final PSValue INDEX_OF = PSFunction.<PSString>method((self, arg0) -> {
        return new PSNumber.PSInteger(self.string.indexOf(arg0.toJavaString()));
    });
    private static final PSValue LAST_INDEX_OF = PSFunction.<PSString>method((self, arg0) -> {
        return new PSNumber.PSInteger(self.string.lastIndexOf(arg0.toJavaString()));
    });
    private static final PSValue REPEAT = PSFunction.<PSString>method((self, arg0) -> {
        int times = arg0.toJavaInt();
        char[] base = self.string.toCharArray();
        char[] res = new char[base.length * times];
        for(int i=0;i<times;i++)
            System.arraycopy(base,0,res,i * base.length,base.length);
        return new PSString(new String(res));
    });
    private static final PSValue SUBSTRING = PSFunction.<PSString>method((self, arg0, arg1) -> {
        if(arg1 == UNDEFINED)
            return new PSString(self.string.substring(arg0.toJavaInt()));
        return new PSString(self.string.substring(arg0.toJavaInt(),arg1.toJavaInt()));
    });
    private static final PSValue STARTS_WITH = PSFunction.<PSString>method((self, arg0) -> {
        return self.string.startsWith(arg0.toJavaString()) ? TRUE : FALSE;
    });
    private static final PSValue TO_LOWER_CASE = PSFunction.<PSString>method((self) -> {
        return new PSString(self.string.toLowerCase());
    });
    private static final PSValue TO_STRING = PSFunction.<PSString>method((self) -> {
        return self;
    });
    private static final PSValue TO_UPPER_CASE = PSFunction.<PSString>method((self) -> {
        return new PSString(self.string.toUpperCase());
    });
    private static final PSValue TRIM = PSFunction.<PSString>method((self) -> {
        return new PSString(self.string.trim());
    });
    
}

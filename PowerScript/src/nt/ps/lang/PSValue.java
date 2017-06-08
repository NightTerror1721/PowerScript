/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import nt.ps.exception.PSCastException;
import nt.ps.exception.PSUnsupportedOperationException;

/**
 *
 * @author Asus
 */
public abstract class PSValue extends PSVarargs
{
    PSValue() {}
    
    public abstract PSDataType getPSType();
    
    public final boolean isUndefined() { return getPSType() == PSDataType.UNDEFINED; }
    public final boolean isNull() { return getPSType() == PSDataType.NULL; }
    public final boolean isNumber() { return getPSType() == PSDataType.NUMBER; }
    public final boolean isBoolean() { return getPSType() == PSDataType.BOOLEAN; }
    public final boolean isString() { return getPSType() == PSDataType.STRING; }
    public final boolean isArray() { return getPSType() == PSDataType.ARRAY; }
    public final boolean isTuple() { return getPSType() == PSDataType.TUPLE; }
    public final boolean isMap() { return getPSType() == PSDataType.MAP; }
    public final boolean isIterator() { return getPSType() == PSDataType.ITERATOR; }
    public final boolean isFunction() { return getPSType() == PSDataType.FUNCTION; }
    public final boolean isObject() { return getPSType() == PSDataType.OBJECT; }
    public final boolean isUserdata() { return getPSType() == PSDataType.USERDATA; }
    
    
    
    /* Java Casting */
    public int toJavaInt() { throw new PSUnsupportedOperationException(this,"toJavaInt"); }
    public long toJavaLong() { throw new PSUnsupportedOperationException(this,"toJavaLong"); }
    public float toJavaFloat() { throw new PSUnsupportedOperationException(this,"toJavaFloat"); }
    public double toJavaDouble() { throw new PSUnsupportedOperationException(this,"toJavaDouble"); }
    public boolean toJavaBoolean() { return true; }
    public String toJavaString() { return getPSType().getTypeName() + "::" + super.toString(); }
    public List<PSValue> toJavaList() { throw new PSUnsupportedOperationException(this,"toJavaList"); }
    public Map<PSValue, PSValue> toJavaMap() { throw new PSUnsupportedOperationException(this,"toJavaMap"); }
    
    /* Direct Casting */
    public PSNumber toPSNumber() { throw new PSCastException(this,PSDataType.NUMBER); }
    public PSBoolean toPSBoolean() { throw new PSCastException(this,PSDataType.BOOLEAN); }
    public PSString toPSString() { throw new PSCastException(this,PSDataType.STRING); }
    public PSArray toPSArray() { throw new PSCastException(this,PSDataType.ARRAY); }
    public PSTuple toPSTuple() { throw new PSCastException(this,PSDataType.TUPLE); }
    public PSMap toPSMap() { throw new PSCastException(this,PSDataType.MAP); }
    public PSIterator toPSIterator() { throw new PSCastException(this,PSDataType.ITERATOR); }
    public PSFunction toPSFunction() { throw new PSCastException(this,PSDataType.FUNCTION); }
    public PSObject toPSObject() { throw new PSCastException(this,PSDataType.OBJECT); }
    public <U extends PSUserdata> U toPSUserdata() { throw new PSCastException(this,PSDataType.USERDATA); }
    
    
    
    /* Math Operations */
    public PSValue plus(PSValue value) { throw new PSUnsupportedOperationException(this,"+"); }
    public PSValue minus(PSValue value) { throw new PSUnsupportedOperationException(this,"-"); }
    public PSValue multiply(PSValue value) { throw new PSUnsupportedOperationException(this,"*"); }
    public PSValue divide(PSValue value) { throw new PSUnsupportedOperationException(this,"/"); }
    public PSValue module(PSValue value) { throw new PSUnsupportedOperationException(this,"%"); }
    public PSValue negative() { throw new PSUnsupportedOperationException(this,"(unary) -"); }
    public PSValue increase() { throw new PSUnsupportedOperationException(this,"++"); }
    public PSValue decrease() { throw new PSUnsupportedOperationException(this,"--"); }
    
    /* Bit Operations */
    public PSValue shiftLeft(PSValue value) { throw new PSUnsupportedOperationException(this,"<<"); }
    public PSValue shiftRight(PSValue value) { throw new PSUnsupportedOperationException(this,">>"); }
    public PSValue logicAnd(PSValue value) { throw new PSUnsupportedOperationException(this,"&"); }
    public PSValue logicOr(PSValue value) { throw new PSUnsupportedOperationException(this,"|"); }
    public PSValue logicNot() { throw new PSUnsupportedOperationException(this,"~"); }
    public PSValue logicXor(PSValue value) { throw new PSUnsupportedOperationException(this,"^"); }
    
    /* Comparate Operations */
    public PSValue equals(PSValue value) { return this == value ? TRUE : FALSE; }
    public PSValue notEquals(PSValue value) { return this != value ? TRUE : FALSE; }
    public PSValue greaterThan(PSValue value) { throw new PSUnsupportedOperationException(this,">"); }
    public PSValue smallerThan(PSValue value) { throw new PSUnsupportedOperationException(this,"<"); }
    public PSValue greaterOrEqualsThan(PSValue value) { throw new PSUnsupportedOperationException(this,">="); }
    public PSValue smallerOrEqualsThan(PSValue value) { throw new PSUnsupportedOperationException(this,"<="); }
    public PSValue negate() { throw new PSUnsupportedOperationException(this,"!"); }
    
    /* Data Structure Operations */
    public PSValue contains(PSValue value) { throw new PSUnsupportedOperationException(this,"in"); }
    
    /* String Operations */
    public PSValue concat(PSValue value) { return new PSString(toJavaString().concat(value.toJavaString())); }
    
    /* Object Operations */
    public PSValue set(PSValue key, PSValue value) { throw new PSUnsupportedOperationException(this,"[]="); }
    public PSValue get(PSValue key) { throw new PSUnsupportedOperationException(this,"[]"); }
    
    /* Object Operations */
    public PSValue setProperty(String name, PSValue value) { throw new PSUnsupportedOperationException(this,"setProperty"); }
    public PSValue getProperty(String name) { throw new PSUnsupportedOperationException(this,"getProperty"); }
    
    /* Function Operations */
    protected PSVarargs innerCall(PSValue self) { throw new PSUnsupportedOperationException(this,"()"); }
    protected PSVarargs innerCall(PSValue self, PSValue arg0) { throw new PSUnsupportedOperationException(this,"()"); }
    protected PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1) { throw new PSUnsupportedOperationException(this,"()"); }
    protected PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2) { throw new PSUnsupportedOperationException(this,"()"); }
    protected PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { throw new PSUnsupportedOperationException(this,"()"); }
    protected PSVarargs innerCall(PSValue self, PSVarargs args) { throw new PSUnsupportedOperationException(this,"()"); }
    
    public final PSVarargs call() { return innerCall(NULL); }
    public final PSVarargs call(PSValue arg0) { return innerCall(NULL,arg0); }
    public final PSVarargs call(PSValue arg0, PSValue arg1) { return innerCall(NULL,arg0,arg1); }
    public final PSVarargs call(PSValue arg0, PSValue arg1, PSValue arg2) { return innerCall(NULL,arg0,arg1,arg2); }
    public final PSVarargs call(PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return innerCall(NULL,arg0,arg1,arg2,arg3); }
    public final PSVarargs call(PSVarargs args) { return innerCall(NULL,args); }
    
    public final PSVarargs invoke(String attribute) { return getProperty(attribute).innerCall(this); }
    public final PSVarargs invoke(String attribute, PSValue arg0) { return getProperty(attribute).innerCall(this,arg0); }
    public final PSVarargs invoke(String attribute, PSValue arg0, PSValue arg1) { return getProperty(attribute).innerCall(this,arg0,arg1); }
    public final PSVarargs invoke(String attribute, PSValue arg0, PSValue arg1, PSValue arg2) { return getProperty(attribute).innerCall(this,arg0,arg1,arg2); }
    public final PSVarargs invoke(String attribute, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return getProperty(attribute).innerCall(this,arg0,arg1,arg2,arg3); }
    public final PSVarargs invoke(String attribute, PSVarargs args) { return getProperty(attribute).innerCall(this,args); }
    
    
    /* Iterator Operations */
    public PSIterator createIterator() { throw new PSUnsupportedOperationException(this,"iterator"); }
    public boolean hasNext() { throw new PSUnsupportedOperationException(this,"iteratorHasNext"); }
    public PSVarargs next() { throw new PSUnsupportedOperationException(this,"iteratorNext"); }
    
    
    
    @Override public abstract boolean equals(Object o);
    @Override public abstract int hashCode();
    @Override public final String toString() { return toJavaString(); }
    
    final int superHashCode() { return super.hashCode(); }
    
    public PSValue setPointerValue(PSValue value) { throw new PSUnsupportedOperationException(this,"setPointerValue"); }
    public PSValue getPointerValue() { throw new PSUnsupportedOperationException(this,"getPointerValue"); }
    
    
    
    /* New Operator */
    public PSValue createNewInstance() { throw new PSUnsupportedOperationException(this,"new"); }
    public PSValue createNewInstance(PSValue arg0) { return createNewInstance(); }
    public PSValue createNewInstance(PSValue arg0, PSValue arg1) { return createNewInstance(arg0); }
    public PSValue createNewInstance(PSValue arg0, PSValue arg1, PSValue arg2) { return createNewInstance(arg0,arg1); }
    public PSValue createNewInstance(PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return createNewInstance(arg0,arg1,arg2); }
    public PSValue createNewInstance(PSVarargs args) { return createNewInstance(args.self(),args.arg(1),args.arg(2),args.arg(3)); }
    
    
    
    @Override public final int numberOfArguments() { return 1; }
    @Override public final PSValue self() { return this; }
    @Override public final PSValue arg(int index) { return index == 0 ? this : UNDEFINED; }
    
    private static final class PSUndefined extends PSValue
    {
        @Override
        public final PSDataType getPSType() { return PSDataType.UNDEFINED; }
        
        @Override public final boolean equals(Object o) { return this == o; }
        @Override public final int hashCode() { return 0; }
        @Override public final boolean toJavaBoolean() { return false; }
    }
    private static final class PSNull extends PSValue
    {
        @Override
        public final PSDataType getPSType() { return PSDataType.NULL; }
        
        @Override public final boolean equals(Object o) { return this == o; }
        @Override public final int hashCode() { return 0; }
        @Override public final boolean toJavaBoolean() { return false; }
    }
    
    
    public static final PSValue UNDEFINED = new PSUndefined();
    public static final PSValue NULL = new PSNull();
    public static final PSValue TRUE = new PSBoolean(true);
    public static final PSValue FALSE = new PSBoolean(false);
    public static final PSValue MINUSONE = new PSNumber.PSInteger(-1);
    public static final PSValue ZERO = new PSNumber.PSInteger(0);
    public static final PSValue ONE = new PSNumber.PSInteger(1);
    public static final PSValue EMPTY_TUPLE = new PSTuple(new PSValue[0]);
    
    
    
    /* Cast and Wrap */
    public static final PSValue valueOf(PSValue value) { return value; }
    public static final PSValue valueOf(byte value) { return new PSNumber.PSInteger(value); }
    public static final PSValue valueOf(short value) { return new PSNumber.PSInteger(value); }
    public static final PSValue valueOf(int value) { return new PSNumber.PSInteger(value); }
    public static final PSValue valueOf(long value) { return new PSNumber.PSLong(value); }
    public static final PSValue valueOf(float value) { return new PSNumber.PSFloat(value); }
    public static final PSValue valueOf(double value) { return new PSNumber.PSDouble(value); }
    public static final PSValue valueOf(boolean value) { return value ? TRUE : FALSE; }
    public static final PSValue valueOf(char value) { return new PSString(Character.toString(value)); }
    public static final PSValue valueOf(Byte value) { return new PSNumber.PSInteger(value); }
    public static final PSValue valueOf(Short value) { return new PSNumber.PSInteger(value); }
    public static final PSValue valueOf(Integer value) { return new PSNumber.PSInteger(value); }
    public static final PSValue valueOf(Long value) { return new PSNumber.PSLong(value); }
    public static final PSValue valueOf(Float value) { return new PSNumber.PSFloat(value); }
    public static final PSValue valueOf(Double value) { return new PSNumber.PSDouble(value); }
    public static final PSValue valueOf(Boolean value) { return value ? TRUE : FALSE; }
    public static final PSValue valueOf(Character value) { return new PSString(value.toString()); }
    public static final PSValue valueOf(String value) { return new PSString(value); }
    public static final PSValue valueOf(List<PSValue> value) { return new PSArray(value); }
    public static final PSValue valueOf(Map<PSValue, PSValue> value) { return new PSMap(value); }
    public static final PSValue valueOf(PSValue... values) { return new PSArray(values); }
    
    public static final <E> PSValue valueOf(List<E> value, Function<? super E, PSValue> caster)
    {
        return new PSArray(value.stream().map(caster).toArray(size -> new PSArray[size]));
    }
    
    public static final <K, V> PSValue valueOf(Map<K, V> value, Function<? super K, PSValue> keyCaster, Function<? super V, PSValue> valueCaster)
    {
        return new PSMap(value.entrySet().stream().collect(Collectors.toMap(
                e -> keyCaster.apply(e.getKey()),
                e -> valueCaster.apply(e.getValue())
        )));
    }
    
    public static final PSValue valueOf(boolean mutable, PSValue... values) { return mutable ? new PSArray(values) : new PSTuple(values); }
    
    public static final PSValue valueOf(boolean mutable, byte... values)
    {
        PSValue[] array = new PSValue[values.length];
        for(int i=0;i<array.length;i++)
            array[i] = valueOf(values[i]);
        return valueOf(mutable,array);
    }
    public static final PSValue valueOf(byte... values) { return valueOf(true,values); }
    
    public static final PSValue valueOf(boolean mutable, short... values)
    {
        PSValue[] array = new PSValue[values.length];
        for(int i=0;i<array.length;i++)
            array[i] = valueOf(values[i]);
        return valueOf(mutable,array);
    }
    public static final PSValue valueOf(short... values) { return valueOf(true,values); }
    
    public static final PSValue valueOf(boolean mutable, int... values)
    {
        return valueOf(mutable,Arrays.stream(values).mapToObj(PSValue::valueOf).<PSValue>toArray(size -> new PSValue[size]));
    }
    public static final PSValue valueOf(int... values) { return valueOf(true,values); }
    
    public static final PSValue valueOf(boolean mutable, long... values)
    {
        return valueOf(mutable,Arrays.stream(values).mapToObj(PSValue::valueOf).<PSValue>toArray(size -> new PSValue[size]));
    }
    public static final PSValue valueOf(long... values) { return valueOf(true,values); }
    
    public static final PSValue valueOf(boolean mutable, float... values)
    {
        PSValue[] array = new PSValue[values.length];
        for(int i=0;i<array.length;i++)
            array[i] = valueOf(values[i]);
        return valueOf(mutable,array);
    }
    public static final PSValue valueOf(float... values) { return valueOf(true,values); }
    
    public static final PSValue valueOf(boolean mutable, double... values)
    {
        return valueOf(mutable,Arrays.stream(values).mapToObj(PSValue::valueOf).<PSValue>toArray(size -> new PSValue[size]));
    }
    public static final PSValue valueOf(double... values) { return valueOf(true,values); }
    
    public static final PSValue valueOf(boolean mutable, boolean... values)
    {
        PSValue[] array = new PSValue[values.length];
        for(int i=0;i<array.length;i++)
            array[i] = valueOf(values[i]);
        return valueOf(mutable,array);
    }
    public static final PSValue valueOf(boolean... values) { return valueOf(true,values); }
    
    public static final PSValue valueOf(boolean mutable, char... values)
    {
        PSValue[] array = new PSValue[values.length];
        for(int i=0;i<array.length;i++)
            array[i] = valueOf(values[i]);
        return valueOf(mutable,array);
    }
    public static final PSValue valueOf(char... values) { return valueOf(true,values); }
    
    public static final <E> PSValue valueOf(boolean mutable, Function<? super E, PSValue> caster, E... values)
    {
        return valueOf(mutable,Arrays.stream(values).<PSValue>map(caster).<PSValue>toArray(size -> new PSValue[size]));
    }
    public static final <E> PSValue valueOf(Function<? super E, PSValue> caster, E... values) { return valueOf(true,caster,values); }
    
    public static final PSValue valueOf(boolean mutable, Byte... values) { return valueOf(mutable,PSValue::valueOf,values); }
    public static final PSValue valueOf(Byte... values) { return valueOf(true,PSValue::valueOf,values); }
    
    public static final PSValue valueOf(boolean mutable, Short... values) { return valueOf(mutable,PSValue::valueOf,values); }
    public static final PSValue valueOf(Short... values) { return valueOf(true,PSValue::valueOf,values); }
    
    public static final PSValue valueOf(boolean mutable, Integer... values) { return valueOf(mutable,PSValue::valueOf,values); }
    public static final PSValue valueOf(Integer... values) { return valueOf(true,PSValue::valueOf,values); }
    
    public static final PSValue valueOf(boolean mutable, Long... values) { return valueOf(mutable,PSValue::valueOf,values); }
    public static final PSValue valueOf(Long... values) { return valueOf(true,PSValue::valueOf,values); }
    
    public static final PSValue valueOf(boolean mutable, Float... values) { return valueOf(mutable,PSValue::valueOf,values); }
    public static final PSValue valueOf(Float... values) { return valueOf(true,PSValue::valueOf,values); }
    
    public static final PSValue valueOf(boolean mutable, Double... values) { return valueOf(mutable,PSValue::valueOf,values); }
    public static final PSValue valueOf(Double... values) { return valueOf(true,PSValue::valueOf,values); }
    
    public static final PSValue valueOf(boolean mutable, Boolean... values) { return valueOf(mutable,PSValue::valueOf,values); }
    public static final PSValue valueOf(Boolean... values) { return valueOf(true,PSValue::valueOf,values); }
    
    public static final PSValue valueOf(boolean mutable, Character... values) { return valueOf(mutable,PSValue::valueOf,values); }
    public static final PSValue valueOf(Character... values) { return valueOf(true,PSValue::valueOf,values); }
    
    public static final PSValue valueOf(boolean mutable, String... values) { return valueOf(mutable,PSValue::valueOf,values); }
    public static final PSValue valueOf(String... values) { return valueOf(true,PSValue::valueOf,values); }
    
    public static final PSValue valueOf(Iterator<? extends PSValue> iterator)
    {
        final PSValue[] itArgs = new PSValue[2];
        final LiteralArrayVarargs res = new LiteralArrayVarargs(itArgs);
        
        return new PSIterator()
        {
            private int count = 0;
            
            @Override
            public final boolean hasNext() { return iterator.hasNext(); }

            @Override
            public final PSVarargs next()
            {
                itArgs[0] = iterator.next();
                itArgs[1] = new PSNumber.PSInteger(count++);
                return res;
            }
        };
    }
    
    public static final <E> PSValue valueOf(Iterator<E> iterator, Function<? super E, PSValue> caster)
    {
        final PSValue[] itArgs = new PSValue[2];
        final LiteralArrayVarargs res = new LiteralArrayVarargs(itArgs);
        
        return new PSIterator()
        {
            private int count = 0;
            
            @Override
            public final boolean hasNext() { return iterator.hasNext(); }

            @Override
            public final PSVarargs next()
            {
                itArgs[0] = caster.apply(iterator.next());
                itArgs[1] = new PSNumber.PSInteger(count++);
                return res;
            }
        };
    }
}

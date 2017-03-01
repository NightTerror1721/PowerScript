/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.datatype;

import java.util.List;
import java.util.Map;
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
    public final boolean isRegExp() { return getPSType() == PSDataType.REGEXP; }
    public final boolean isArray() { return getPSType() == PSDataType.ARRAY; }
    public final boolean isTuple() { return getPSType() == PSDataType.TUPLE; }
    public final boolean isMap() { return getPSType() == PSDataType.MAP; }
    public final boolean isIterator() { return getPSType() == PSDataType.ITERATOR; }
    public final boolean isFunction() { return getPSType() == PSDataType.FUNCTION; }
    public final boolean isPrototype() { return getPSType() == PSDataType.PROTOTYPE; }
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
    //public PSRegExp toPSRegExp() { throw new PSCastException(this,PSDataType.REGEXP); }
    public PSArray toPSArray() { throw new PSCastException(this,PSDataType.ARRAY); }
    //public PSTuple toPSTuple() { throw new PSCastException(this,PSDataType.TUPLE); }
    //public PSMap toPSMap() { throw new PSCastException(this,PSDataType.MAP); }
    public PSIterator toPSIterator() { throw new PSCastException(this,PSDataType.ITERATOR); }
    public PSFunction toPSFunction() { throw new PSCastException(this,PSDataType.FUNCTION); }
    //public PSPrototype toPSPrototype() { throw new PSCastException(this,PSDataType.PROTOTYPE); }
    //public PSObject toPSObject() { throw new PSCastException(this,PSDataType.OBJECT); }
    //public <U extends PSUserdata> U toPSUserdata() { throw new PSCastException(this,PSDataType.USERDATA); }
    
    
    
    /* Math Operations */
    public PSValue plus(PSValue value) { throw new PSUnsupportedOperationException(this,"+"); }
    public PSValue minus(PSValue value) { throw new PSUnsupportedOperationException(this,"-"); }
    public PSValue multiply(PSValue value) { throw new PSUnsupportedOperationException(this,"*"); }
    public PSValue divide(PSValue value) { throw new PSUnsupportedOperationException(this,"/"); }
    public PSValue module(PSValue value) { throw new PSUnsupportedOperationException(this,"%"); }
    public PSValue negative() { throw new PSUnsupportedOperationException(this,"!"); }
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
    public PSValue negate() { throw new PSUnsupportedOperationException(this,"- (unary)"); }
    
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
    
    
    
    
    @Override public final int numberOfArguments() { return 1; }
    @Override public final PSValue self() { return this; }
    @Override public final PSValue arg(int index) { return index == 0 ? this : UNDEFINED; }
    
    private static final class PSUndefined extends PSValue
    {
        @Override
        public final PSDataType getPSType() { return PSDataType.UNDEFINED; }
        
        @Override public final boolean equals(Object o) { return this == o; }
        @Override public final int hashCode() { return 0; }
    }
    private static final class PSNull extends PSValue
    {
        @Override
        public final PSDataType getPSType() { return PSDataType.NULL; }
        
        @Override public final boolean equals(Object o) { return this == o; }
        @Override public final int hashCode() { return 0; }
    }
    
    
    public static final PSValue UNDEFINED = new PSUndefined();
    public static final PSValue NULL = new PSNull();
    public static final PSValue TRUE = new PSBoolean(true);
    public static final PSValue FALSE = new PSBoolean(false);
}

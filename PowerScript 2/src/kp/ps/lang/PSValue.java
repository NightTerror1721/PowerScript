/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.ps.lang;

import java.util.List;
import java.util.Map;
import kp.ps.exception.PSCastException;
import kp.ps.exception.PSUnsupportedOperationException;

/**
 *
 * @author Asus
 */
public abstract class PSValue extends PSVarargs implements PSIteratorResult
{
    PSValue() {}
    
    public abstract PSDataType getPSDataType();
    
    public final boolean isUndefined() { return getPSDataType() == PSDataType.UNDEFINED; }
    public final boolean isNull() { return getPSDataType() == PSDataType.NULL; }
    public final boolean isNumber() { return getPSDataType() == PSDataType.NUMBER; }
    public final boolean isBoolean() { return getPSDataType() == PSDataType.BOOLEAN; }
    public final boolean isString() { return getPSDataType() == PSDataType.STRING; }
    public final boolean isArray() { return getPSDataType() == PSDataType.ARRAY; }
    public final boolean isTuple() { return getPSDataType() == PSDataType.TUPLE; }
    public final boolean isMap() { return getPSDataType() == PSDataType.MAP; }
    public final boolean isIterator() { return getPSDataType() == PSDataType.ITERATOR; }
    public final boolean isFunction() { return getPSDataType() == PSDataType.FUNCTION; }
    public final boolean isObject() { return getPSDataType() == PSDataType.OBJECT; }
    public final boolean isUserdata() { return getPSDataType() == PSDataType.USERDATA; }
    
    
    /* Java Casting */
    public int toJavaInt() { throw new PSUnsupportedOperationException(this, "toJavaInt"); }
    public long toJavaLong() { throw new PSUnsupportedOperationException(this, "toJavaLong"); }
    public float toJavaFloat() { throw new PSUnsupportedOperationException(this, "toJavaFloat"); }
    public double toJavaDouble() { throw new PSUnsupportedOperationException(this, "toJavaDouble"); }
    public boolean toJavaBoolean() { return true; }
    public String toJavaString() { return getPSDataType().getTypeName() + "::" + Integer.toHexString(super.hashCode()); }
    public List<PSValue> toJavaList() { throw new PSUnsupportedOperationException(this, "toJavaList"); }
    public Map<PSValue, PSValue> toJavaMap() { throw new PSUnsupportedOperationException(this, "toJavaMap"); }
    
    
    /* Direct Casting */
    public PSNumber toPSNumber() { throw new PSCastException(this,PSDataType.NUMBER); }
    public PSBoolean toPSBoolean() { throw new PSCastException(this,PSDataType.BOOLEAN); }
    public PSString toPSString() { throw new PSCastException(this,PSDataType.STRING); }
    public PSArray toPSArray() { throw new PSCastException(this,PSDataType.ARRAY); }
    public PSTuple toPSTuple() { throw new PSCastException(this,PSDataType.TUPLE); }
    //public PSMap toPSMap() { throw new PSCastException(this,PSDataType.MAP); }
    //public PSIterator toPSIterator() { throw new PSCastException(this,PSDataType.ITERATOR); }
    public PSFunction toPSFunction() { throw new PSCastException(this,PSDataType.FUNCTION); }
    //public PSObject toPSObject() { throw new PSCastException(this,PSDataType.OBJECT); }
    //public <U extends PSUserdata> U toPSUserdata() { throw new PSCastException(this,PSDataType.USERDATA); }
    
    
    
    /* Math Operations */
    public PSValue plus(PSValue value) { throw new PSUnsupportedOperationException(this, "+"); }
    public PSValue minus(PSValue value) { throw new PSUnsupportedOperationException(this, "-"); }
    public PSValue multiply(PSValue value) { throw new PSUnsupportedOperationException(this, "*"); }
    public PSValue divide(PSValue value) { throw new PSUnsupportedOperationException(this, "/"); }
    public PSValue remainder(PSValue value) { throw new PSUnsupportedOperationException(this, "%"); }
    public PSValue negative() { throw new PSUnsupportedOperationException(this, "(unary) -"); }
    public PSValue increase() { throw new PSUnsupportedOperationException(this, "++"); }
    public PSValue decrease() { throw new PSUnsupportedOperationException(this, "--"); }
    
    /* Bit Operations */
    public PSValue shiftLeft(PSValue value) { throw new PSUnsupportedOperationException(this, "<<"); }
    public PSValue shiftRight(PSValue value) { throw new PSUnsupportedOperationException(this, ">>"); }
    public PSValue logicAnd(PSValue value) { throw new PSUnsupportedOperationException(this, "&"); }
    public PSValue logicOr(PSValue value) { throw new PSUnsupportedOperationException(this, "|"); }
    public PSValue logicNot() { throw new PSUnsupportedOperationException(this, "~"); }
    public PSValue logicXor(PSValue value) { throw new PSUnsupportedOperationException(this, "^"); }
    
    /* Comparate Operations */
    public PSValue equals(PSValue value) { return this == value ? TRUE : FALSE; }
    public PSValue notEquals(PSValue value) { return this != value ? TRUE : FALSE; }
    public PSValue greaterThan(PSValue value) { throw new PSUnsupportedOperationException(this, ">"); }
    public PSValue smallerThan(PSValue value) { throw new PSUnsupportedOperationException(this, "<"); }
    public PSValue greaterOrEqualsThan(PSValue value) { throw new PSUnsupportedOperationException(this, ">="); }
    public PSValue smallerOrEqualsThan(PSValue value) { throw new PSUnsupportedOperationException(this, "<="); }
    public PSValue negate() { return FALSE; }
    
    /* Data Structure Operations */
    public PSValue contains(PSValue value) { throw new PSUnsupportedOperationException(this, "in"); }
    
    /* String Operations */
    public PSValue concat(PSValue value) { return new PSString(toJavaString().concat(value.toJavaString())); }
    
    /* Object Operations */
    public PSValue set(PSValue key, PSValue value) { throw new PSUnsupportedOperationException(this, "[]="); }
    public PSValue get(PSValue key) { throw new PSUnsupportedOperationException(this,"[]"); }
    
    /* Object Operations */
    public PSValue setProperty(String name, PSValue value) { throw new PSUnsupportedOperationException(this, "setProperty"); }
    public PSValue getProperty(String name) { throw new PSUnsupportedOperationException(this, "getProperty"); }
    
    /* Function Operations */
    protected PSValue rawCall(PSValue self) { throw new PSUnsupportedOperationException(this, "()"); }
    protected PSValue rawCall(PSValue self, PSValue arg0) { throw new PSUnsupportedOperationException(this, "()"); }
    protected PSValue rawCall(PSValue self, PSValue arg0, PSValue arg1) { throw new PSUnsupportedOperationException(this, "()"); }
    protected PSValue rawCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2) { throw new PSUnsupportedOperationException(this, "()"); }
    protected PSValue rawCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { throw new PSUnsupportedOperationException(this, "()"); }
    protected PSValue rawCall(PSValue self, PSVarargs args) { throw new PSUnsupportedOperationException(this, "()"); }
    
    public final PSValue call() { return rawCall(NULL); }
    public final PSValue call(PSValue arg0) { return rawCall(NULL, arg0); }
    public final PSValue call(PSValue arg0, PSValue arg1) { return rawCall(NULL, arg0, arg1); }
    public final PSValue call(PSValue arg0, PSValue arg1, PSValue arg2) { return rawCall(NULL, arg0, arg1, arg2); }
    public final PSValue call(PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return rawCall(NULL, arg0, arg1, arg2, arg3); }
    public final PSValue call(PSVarargs args) { return rawCall(NULL, args); }
    
    public final PSValue invoke(String property) { return getProperty(property).rawCall(this); }
    public final PSValue invoke(String property, PSValue arg0) { return getProperty(property).rawCall(this, arg0); }
    public final PSValue invoke(String property, PSValue arg0, PSValue arg1) { return getProperty(property).rawCall(this, arg0, arg1); }
    public final PSValue invoke(String property, PSValue arg0, PSValue arg1, PSValue arg2) { return getProperty(property).rawCall(this, arg0, arg1, arg2); }
    public final PSValue invoke(String property, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return getProperty(property).rawCall(this, arg0, arg1, arg2, arg3); }
    public final PSValue invoke(String property, PSVarargs args) { return getProperty(property).rawCall(this,args); }
    
    
    /* Iterator Operations */
    public PSIterator createIterator() { throw new PSUnsupportedOperationException(this,"iterator"); }
    public boolean hasNext() { throw new PSUnsupportedOperationException(this,"iteratorHasNext"); }
    public PSVarargs next() { throw new PSUnsupportedOperationException(this,"iteratorNext"); }
    
    /* Iterator Results */
    public int resultElementsCount() { return 1; }
    public PSValue resultElement(int index) { return index == 0 ? this : UNDEFINED; }
    
    
    
    @Override public abstract boolean equals(Object o);
    @Override public abstract int hashCode();
    @Override public final String toString() { return toJavaString(); }
    
    final int superHashCode() { return super.hashCode(); }
    
    public void setPointerValue(PSValue value) { throw new PSUnsupportedOperationException(this,"setPointerValue"); }
    public PSValue getPointerValue() { throw new PSUnsupportedOperationException(this,"getPointerValue"); }
    
    
    
    /* New Operator */
    public PSValue createNewInstance() { throw new PSUnsupportedOperationException(this,"new"); }
    public PSValue createNewInstance(PSValue arg0) { return createNewInstance(); }
    public PSValue createNewInstance(PSValue arg0, PSValue arg1) { return createNewInstance(arg0); }
    public PSValue createNewInstance(PSValue arg0, PSValue arg1, PSValue arg2) { return createNewInstance(arg0,arg1); }
    public PSValue createNewInstance(PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return createNewInstance(arg0,arg1,arg2); }
    public PSValue createNewInstance(PSVarargs args) { return createNewInstance(args.self(),args.arg(1),args.arg(2),args.arg(3)); }
    
    
    
    /* Varargs */
    @Override public final int numberOfArguments() { return 1; }
    @Override public final PSValue arg0() { return this; }
    @Override public final PSValue arg(int index) { return index == 0 ? this : UNDEFINED; }
    
    
    
    private static final class PSUndefined extends PSValue
    {
        @Override
        public final PSDataType getPSDataType() { return PSDataType.UNDEFINED; }
        
        @Override public final boolean equals(Object o) { return false; }
        @Override public final int hashCode() { return 0; }
        @Override public final boolean toJavaBoolean() { return false; }
        @Override public final String toJavaString() { return "undefined"; }
        @Override public PSValue negate() { return TRUE; }
    }
    private static final class PSNull extends PSValue
    {
        @Override
        public final PSDataType getPSDataType() { return PSDataType.NULL; }
        
        @Override public final boolean equals(Object o) { return this == o; }
        @Override public final int hashCode() { return 0; }
        @Override public final boolean toJavaBoolean() { return false; }
        @Override public final String toJavaString() { return "null"; }
        @Override public PSValue negate() { return TRUE; }
    }
    
    
    public static final PSValue UNDEFINED = new PSUndefined();
    public static final PSValue NULL = new PSNull();
    public static final PSValue TRUE = new PSBoolean(true);
    public static final PSValue FALSE = new PSBoolean(false);
    public static final PSValue MINUSONE = new PSNumber.PSInteger(-1);
    public static final PSValue ZERO = new PSNumber.PSInteger(0);
    public static final PSValue ONE = new PSNumber.PSInteger(1);
    public static final PSValue EMPTY_TUPLE = new PSTuple(new PSValue[0]);
}
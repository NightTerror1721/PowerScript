/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Asus
 */
public final class PSPointer extends PSValue
{
    private PSValue ref;
    
    public PSPointer(PSValue value)
    {
        if(value == null)
            throw new NullPointerException();
        this.ref = value;
    }
    
    @Override public final PSDataType getPSType() { return ref.getPSType(); }

    
    /* Java Casting */
    @Override public final int toJavaInt() { return ref.toJavaInt(); }
    @Override public final long toJavaLong() { return ref.toJavaLong(); }
    @Override public final float toJavaFloat() { return ref.toJavaFloat(); }
    @Override public final double toJavaDouble() { return ref.toJavaDouble(); }
    @Override public final boolean toJavaBoolean() { return ref.toJavaBoolean(); }
    @Override public final String toJavaString() { return ref.toJavaString(); }
    @Override public final List<PSValue> toJavaList() { return ref.toJavaList(); }
    @Override public final Map<PSValue, PSValue> toJavaMap() { return ref.toJavaMap(); }
    
    /* Direct Casting */
    @Override public final PSNumber toPSNumber() { return ref.toPSNumber(); }
    @Override public final PSBoolean toPSBoolean() { return ref.toPSBoolean(); }
    @Override public final PSString toPSString() { return ref.toPSString(); }
    @Override public final PSArray toPSArray() { return ref.toPSArray(); }
    @Override public final PSTuple toPSTuple() { return ref.toPSTuple(); }
    @Override public final PSMap toPSMap() { return ref.toPSMap(); }
    @Override public final PSIterator toPSIterator() { return ref.toPSIterator(); }
    @Override public final PSFunction toPSFunction() { return ref.toPSFunction(); }
    @Override public final PSObject toPSObject() { return ref.toPSObject(); }
    @Override public final <U extends PSUserdata> U toPSUserdata() { return ref.toPSUserdata(); }
    
    
    
    /* Math Operations */
    @Override public final PSValue plus(PSValue value) { return ref.plus(value); }
    @Override public final PSValue minus(PSValue value) { return ref.minus(value); }
    @Override public final PSValue multiply(PSValue value) { return ref.multiply(value); }
    @Override public final PSValue divide(PSValue value) { return ref.divide(value); }
    @Override public final PSValue module(PSValue value) { return ref.module(value); }
    @Override public final PSValue negative() { return ref.negative(); }
    @Override public final PSValue increase() { return ref.increase(); }
    @Override public final PSValue decrease() { return ref.decrease(); }
    
    /* Bit Operations */
    @Override public final PSValue shiftLeft(PSValue value) { return ref.shiftLeft(value); }
    @Override public final PSValue shiftRight(PSValue value) { return ref.shiftRight(value); }
    @Override public final PSValue logicAnd(PSValue value) { return ref.logicAnd(value); }
    @Override public final PSValue logicOr(PSValue value) { return ref.logicOr(value); }
    @Override public final PSValue logicNot() { return ref.logicNot(); }
    @Override public final PSValue logicXor(PSValue value) { return ref.logicXor(value); }
    
    /* Comparate Operations */
    @Override public final PSValue equals(PSValue value) { return ref.equals(value); }
    @Override public final PSValue notEquals(PSValue value) { return ref.notEquals(value); }
    @Override public final PSValue greaterThan(PSValue value) { return ref.greaterThan(value); }
    @Override public final PSValue smallerThan(PSValue value) { return ref.smallerThan(value); }
    @Override public final PSValue greaterOrEqualsThan(PSValue value) { return ref.greaterOrEqualsThan(value); }
    @Override public final PSValue smallerOrEqualsThan(PSValue value) { return ref.smallerOrEqualsThan(value); }
    @Override public final PSValue negate() { return ref.negate(); }
    
    /* Data Structure Operations */
    @Override public final PSValue contains(PSValue value) { return ref.contains(value); }
    
    /* String Operations */
    @Override public final PSValue concat(PSValue value) { return ref.concat(value); }
    
    /* Object Operations */
    @Override public final PSValue set(PSValue key, PSValue value) { return ref.set(key,value); }
    @Override public final PSValue get(PSValue key) { return ref.get(key); }
    
    /* Object Operations */
    @Override public final PSValue setProperty(String name, PSValue value) { return ref.setProperty(name,value); }
    @Override public final PSValue getProperty(String name) { return ref.getProperty(name); }
    
    /* Function Operations */
    @Override protected final PSVarargs innerCall(PSValue self) { return ref.innerCall(self); }
    @Override protected final PSVarargs innerCall(PSValue self, PSValue arg0) { return ref.innerCall(self,arg0); }
    @Override protected final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1) { return ref.innerCall(self,arg0,arg1); }
    @Override protected final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2) { return ref.innerCall(self,arg0,arg1,arg2); }
    @Override protected final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return ref.innerCall(self,arg0,arg1,arg2,arg3); }
    @Override protected final PSVarargs innerCall(PSValue self, PSVarargs args) { return ref.innerCall(self,args); }
    
    
    /* Iterator Operations */
    @Override public final PSIterator createIterator() { return ref.createIterator(); }
    @Override public final boolean hasNext() { return ref.hasNext(); }
    @Override public final PSVarargs next() { return ref.next(); }
    
    
    
    @Override public final boolean equals(Object o) { return ref.equals(o); }
    @Override public final int hashCode() { return ref.hashCode(); }
    
    
    
    @Override
    public final void setPointerValue(PSValue value) { ref = value == null ? UNDEFINED : value; }
    
    @Override
    public final PSValue getPointerValue() { return ref; }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mpasc
 */
public final class PSObject extends PSValue
{
    private final PSPrototype prototype;
    private final Map<String, PSValue> properties;
    
    PSObject(Map<String, PSValue> properties, PSPrototype prototype)
    {
        if(properties == null)
            throw new NullPointerException();
        this.properties = properties;
        this.prototype = prototype;
    }
    PSObject(PSPrototype prototype) { this(new HashMap<>(),prototype); }
    public PSObject(Map<String, PSValue> properties) { this(properties,null); }
    public PSObject() { this(new HashMap<>(),null); }
    
    public final PSPrototype getPrototype() { return prototype; }
    public final boolean hasPrototype() { return prototype != null; }
    
    private PSValue property(String name)
    {
        PSValue prop = properties.get(name);
        return prop == null || prop == UNDEFINED ? null : prop;
    }
    
    @Override
    public final PSDataType getPSType() { return PSDataType.OBJECT; }
    
    @Override
    public final PSObject toPSObject() { return this; }
    
    @Override
    public final int toJavaInt()
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.TO_NUMBER)) == null
                ? super.toJavaInt()
                : prop.innerCall(this).self().toJavaInt();
    }
    @Override
    public final long toJavaLong()
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.TO_NUMBER)) == null
                ? super.toJavaLong()
                : prop.innerCall(this).self().toJavaLong();
    }
    @Override
    public final float toJavaFloat()
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.TO_NUMBER)) == null
                ? super.toJavaFloat()
                : prop.innerCall(this).self().toJavaFloat();
    }
    @Override
    public final double toJavaDouble()
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.TO_NUMBER)) == null
                ? super.toJavaDouble()
                : prop.innerCall(this).self().toJavaDouble();
    }
    @Override
    public final boolean toJavaBoolean()
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.TO_BOOLEAN)) == null
                ? super.toJavaBoolean()
                : prop.innerCall(this).self().toJavaBoolean();
    }
    @Override
    public final String toJavaString()
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.TO_STRING)) == null
                ? super.toJavaString()
                : prop.innerCall(this).self().toJavaString();
    }
    @Override
    public final List<PSValue> toJavaList()
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.TO_ARRAY)) == null
                ? super.toJavaList()
                : prop.innerCall(this).self().toJavaList();
    }
    @Override
    public final Map<PSValue, PSValue> toJavaMap()
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.TO_MAP)) == null
                ? super.toJavaMap()
                : prop.innerCall(this).self().toJavaMap();
    }

    @Override
    public final boolean equals(Object o)
    {
        return o instanceof PSObject && equals((PSObject)o).toJavaBoolean();
    }
    
    @Override
    public final int hashCode()
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.HASH_CODE)) == null
                ? superHashCode()
                : prop.innerCall(this).self().toJavaInt();
    }
    
    
    /* Operations */
    @Override
    public final PSValue plus(PSValue value)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_PLUS)) == null
                ? super.plus(value)
                : prop.innerCall(this,value).self();
    }
    @Override
    public final PSValue minus(PSValue value)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_MINUS)) == null
                ? super.minus(value)
                : prop.innerCall(this,value).self();
    }
    @Override
    public final PSValue multiply(PSValue value)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_MULTIPLY)) == null
                ? super.multiply(value)
                : prop.innerCall(this,value).self();
    }
    @Override
    public final PSValue divide(PSValue value)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_DIVIDE)) == null
                ? super.divide(value)
                : prop.innerCall(this,value).self();
    }
    @Override
    public final PSValue module(PSValue value)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_MODULE)) == null
                ? super.module(value)
                : prop.innerCall(this,value).self();
    }
    @Override
    public final PSValue negative()
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_NEGATIVE)) == null
                ? super.negative()
                : prop.innerCall(this).self();
    }
    @Override
    public final PSValue increase()
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_INCREASE)) == null
                ? super.increase()
                : prop.innerCall(this).self();
    }
    @Override
    public final PSValue decrease()
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_DECREASE)) == null
                ? super.decrease()
                : prop.innerCall(this).self();
    }
    
    @Override
    public final PSValue shiftLeft(PSValue value)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_SHIFT_LEFT)) == null
                ? super.shiftLeft(value)
                : prop.innerCall(this,value).self();
    }
    @Override
    public final PSValue shiftRight(PSValue value)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_SHIFT_RIGHT)) == null
                ? super.shiftRight(value)
                : prop.innerCall(this,value).self();
    }
    @Override
    public final PSValue logicAnd(PSValue value)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_LOGIC_AND)) == null
                ? super.logicAnd(value)
                : prop.innerCall(this,value).self();
    }
    @Override
    public final PSValue logicOr(PSValue value)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_LOGIC_OR)) == null
                ? super.logicOr(value)
                : prop.innerCall(this,value).self();
    }
    @Override
    public final PSValue logicNot()
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_LOGIC_NOT)) == null
                ? super.logicNot()
                : prop.innerCall(this).self();
    }
    @Override
    public final PSValue logicXor(PSValue value)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_LOGIC_XOR)) == null
                ? super.logicXor(value)
                : prop.innerCall(this,value).self();
    }
}

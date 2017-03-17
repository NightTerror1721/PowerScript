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
    private final PSValue prototype;
    private final HashMap<String, PSValue> properties;
    
    PSObject(HashMap<String, PSValue> properties, PSValue prototype)
    {
        if(properties == null)
            throw new NullPointerException();
        this.properties = properties;
        this.prototype = prototype;
    }
    PSObject(PSValue prototype) { this(new HashMap<>(),prototype); }
    public PSObject(HashMap<String, PSValue> properties) { this(properties,null); }
    public PSObject() { this(new HashMap<>(),null); }
    
    public final PSValue getPrototype() { return prototype; }
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
    
    @Override
    public final PSValue equals(PSValue value)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_EQUALS)) == null
                ? super.equals(value)
                : prop.innerCall(this,value).self();
    }
    @Override
    public final PSValue notEquals(PSValue value)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_NOTEQUALS)) == null
                ? super.notEquals(value)
                : prop.innerCall(this,value).self();
    }
    @Override
    public final PSValue greaterThan(PSValue value)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_GREATER)) == null
                ? super.greaterThan(value)
                : prop.innerCall(this,value).self();
    }
    @Override
    public final PSValue smallerThan(PSValue value)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_SMALLER)) == null
                ? super.smallerThan(value)
                : prop.innerCall(this,value).self();
    }
    @Override
    public final PSValue greaterOrEqualsThan(PSValue value)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_GREATER_EQUALS)) == null
                ? super.greaterOrEqualsThan(value)
                : prop.innerCall(this,value).self();
    }
    @Override
    public final PSValue smallerOrEqualsThan(PSValue value)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_SMALLER_EQUALS)) == null
                ? super.smallerOrEqualsThan(value)
                : prop.innerCall(this,value).self();
    }
    @Override
    public final PSValue negate()
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_NEGATE)) == null
                ? super.negate()
                : prop.innerCall(this).self();
    }
    
    @Override
    public final PSValue contains(PSValue value)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.CONTAINS)) == null
                ? super.contains(value)
                : prop.innerCall(this,value).self();
    }
    
    @Override
    public final PSValue set(PSValue key, PSValue value)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_SET)) == null
                ? super.set(key,value)
                : prop.innerCall(this,key,value).self();
    }
    @Override
    public final PSValue get(PSValue key)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_GET)) == null
                ? super.get(key)
                : prop.innerCall(this,key).self();
    }
    
    /* Object Operations */
    @Override
    public final PSValue setProperty(String name, PSValue value)
    {
        if(value == UNDEFINED)
            return properties.remove(name);
        properties.put(name,value);
        return value;
    }
    @Override
    public final PSValue getProperty(String name)
    {
        PSValue value;
        return (value = properties.get(name)) == null ? UNDEFINED : value;
    }
    
    @Override
    protected final PSVarargs innerCall(PSValue self)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_CALL)) == null
                ? super.innerCall(self)
                : prop.innerCall(self).self();
    }
    @Override
    protected final PSVarargs innerCall(PSValue self, PSValue arg0)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_CALL)) == null
                ? super.innerCall(self, arg0)
                : prop.innerCall(self, arg0).self();
    }
    @Override
    protected final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_CALL)) == null
                ? super.innerCall(self, arg0, arg1)
                : prop.innerCall(self, arg0, arg1).self();
    }
    @Override
    protected final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_CALL)) == null
                ? super.innerCall(self, arg0, arg1, arg2)
                : prop.innerCall(self, arg0, arg1, arg2).self();
    }
    @Override
    protected final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_CALL)) == null
                ? super.innerCall(self, arg0, arg1, arg2, arg3)
                : prop.innerCall(self, arg0, arg1, arg2, arg3).self();
    }
    @Override
    protected final PSVarargs innerCall(PSValue self, PSVarargs args)
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_CALL)) == null
                ? super.innerCall(self, args)
                : prop.innerCall(self, args).self();
    }
    
    @Override
    public final PSIterator createIterator()
    {
        PSValue prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_CALL)) == null
                ? super.createIterator()
                : prop.innerCall(this).self().toPSIterator();
    }
    
    
    @Override
    public final PSValue createNewInstance()
    {
        PSObject instance = new PSObject((HashMap<String, PSValue>) properties.clone(), this);
        PSValue init = instance.property(ObjectSpecialOpsNames.OPERATOR_NEW);
        if(init != null)
            init.innerCall(instance);
        return instance;
    }
    @Override
    public final PSValue createNewInstance(PSValue arg0)
    {
        PSObject instance = new PSObject((HashMap<String, PSValue>) properties.clone(), this);
        PSValue init = instance.property(ObjectSpecialOpsNames.OPERATOR_NEW);
        if(init != null)
            init.innerCall(instance,arg0);
        return instance;
    }
    @Override
    public final PSValue createNewInstance(PSValue arg0, PSValue arg1)
    {
        PSObject instance = new PSObject((HashMap<String, PSValue>) properties.clone(), this);
        PSValue init = instance.property(ObjectSpecialOpsNames.OPERATOR_NEW);
        if(init != null)
            init.innerCall(instance,arg0,arg1);
        return instance;
    }
    @Override
    public final PSValue createNewInstance(PSValue arg0, PSValue arg1, PSValue arg2)
    {
        PSObject instance = new PSObject((HashMap<String, PSValue>) properties.clone(), this);
        PSValue init = instance.property(ObjectSpecialOpsNames.OPERATOR_NEW);
        if(init != null)
            init.innerCall(instance,arg0,arg1,arg2);
        return instance;
    }
    @Override
    public final PSValue createNewInstance(PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3)
    {
        PSObject instance = new PSObject((HashMap<String, PSValue>) properties.clone(), this);
        PSValue init = instance.property(ObjectSpecialOpsNames.OPERATOR_NEW);
        if(init != null)
            init.innerCall(instance,arg0,arg1,arg2,arg3);
        return instance;
    }
    @Override
    public final PSValue createNewInstance(PSVarargs args)
    {
        PSObject instance = new PSObject((HashMap<String, PSValue>) properties.clone(), this);
        PSValue init = instance.property(ObjectSpecialOpsNames.OPERATOR_NEW);
        if(init != null)
            init.innerCall(instance,args);
        return instance;
    }
}

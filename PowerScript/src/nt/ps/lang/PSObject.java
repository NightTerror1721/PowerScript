/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import nt.ps.exception.PSRuntimeException;

/**
 *
 * @author mpasc
 */
public final class PSObject extends PSValue
{
    private final PSObject parent;
    private final Map<String, Property> properties;
    private boolean frozen;
    
    PSObject(Map<String, Property> properties, PSObject parent)
    {
        if(properties == null)
            throw new NullPointerException();
        this.properties = properties;
        this.parent = parent;
    }
    PSObject(PSObject parent) { this(new HashMap<>(),parent); }
    public PSObject(Map<String, Property> properties) { this(properties,null); }
    public PSObject() { this(new HashMap<>(),null); }
    
    public final PSObject copy()
    {
        return new PSObject(new HashMap<>(properties), parent);
    }
    
    public final PSValue getParent() { return parent; }
    public final boolean hasParent() { return parent != null; }
    
    public final boolean hasProperty(String name) { return properties.containsKey(name); }
    public final int getPropertyCount() { return properties.size(); }
    
    public final Iterable<PropertyEntry> properties() { return PropertyIterator::new; }
    
    public final void setFrozen(boolean flag) { frozen = flag; }
    public final boolean isFrozen() { return frozen; }
    
    public final void setPropertyFrozen(String name, boolean frozen)
    {
        Property p = property(name);
        if(p == null)
            throw new PSRuntimeException("Property \"" + name + "\" not found");
        p.frozen = frozen;
    }
    public final boolean isPropertyFrozen(String name)
    {
        Property p = property(name);
        if(p == null)
            throw new PSRuntimeException("Property \"" + name + "\" not found");
        return p.frozen;
    }
    
    private Property property(String name)
    {
        Property prop = properties.get(name);
        return prop == null
                ? parent != null ? parent.property(name) : null
                : prop;
    }
    
    @Override
    public final PSDataType getPSType() { return PSDataType.OBJECT; }
    
    @Override
    public final PSObject toPSObject() { return this; }
    
    @Override
    public final int toJavaInt()
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.TO_NUMBER)) == null
                ? super.toJavaInt()
                : prop.value.innerCall(this).self().toJavaInt();
    }
    @Override
    public final long toJavaLong()
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.TO_NUMBER)) == null
                ? super.toJavaLong()
                : prop.value.innerCall(this).self().toJavaLong();
    }
    @Override
    public final float toJavaFloat()
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.TO_NUMBER)) == null
                ? super.toJavaFloat()
                : prop.value.innerCall(this).self().toJavaFloat();
    }
    @Override
    public final double toJavaDouble()
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.TO_NUMBER)) == null
                ? super.toJavaDouble()
                : prop.value.innerCall(this).self().toJavaDouble();
    }
    @Override
    public final boolean toJavaBoolean()
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.TO_BOOLEAN)) == null
                ? super.toJavaBoolean()
                : prop.value.innerCall(this).self().toJavaBoolean();
    }
    @Override
    public final String toJavaString()
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.TO_STRING)) == null
                ? super.toJavaString()
                : prop.value.innerCall(this).self().toJavaString();
    }
    @Override
    public final List<PSValue> toJavaList()
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.TO_ARRAY)) == null
                ? super.toJavaList()
                : prop.value.innerCall(this).self().toJavaList();
    }
    @Override
    public final Map<PSValue, PSValue> toJavaMap()
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.TO_MAP)) == null
                ? super.toJavaMap()
                : prop.value.innerCall(this).self().toJavaMap();
    }

    @Override
    public final boolean equals(Object o)
    {
        return o instanceof PSObject && equals((PSObject)o).toJavaBoolean();
    }
    
    @Override
    public final int hashCode()
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.HASH_CODE)) == null
                ? superHashCode()
                : prop.value.innerCall(this).self().toJavaInt();
    }
    
    
    /* Operations */
    @Override
    public final PSValue plus(PSValue value)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_PLUS)) == null
                ? super.plus(value)
                : prop.value.innerCall(this,value).self();
    }
    @Override
    public final PSValue minus(PSValue value)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_MINUS)) == null
                ? super.minus(value)
                : prop.value.innerCall(this,value).self();
    }
    @Override
    public final PSValue multiply(PSValue value)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_MULTIPLY)) == null
                ? super.multiply(value)
                : prop.value.innerCall(this,value).self();
    }
    @Override
    public final PSValue divide(PSValue value)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_DIVIDE)) == null
                ? super.divide(value)
                : prop.value.innerCall(this,value).self();
    }
    @Override
    public final PSValue module(PSValue value)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_MODULE)) == null
                ? super.module(value)
                : prop.value.innerCall(this,value).self();
    }
    @Override
    public final PSValue negative()
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_NEGATIVE)) == null
                ? super.negative()
                : prop.value.innerCall(this).self();
    }
    @Override
    public final PSValue increase()
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_INCREASE)) == null
                ? super.increase()
                : prop.value.innerCall(this).self();
    }
    @Override
    public final PSValue decrease()
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_DECREASE)) == null
                ? super.decrease()
                : prop.value.innerCall(this).self();
    }
    
    @Override
    public final PSValue shiftLeft(PSValue value)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_SHIFT_LEFT)) == null
                ? super.shiftLeft(value)
                : prop.value.innerCall(this,value).self();
    }
    @Override
    public final PSValue shiftRight(PSValue value)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_SHIFT_RIGHT)) == null
                ? super.shiftRight(value)
                : prop.value.innerCall(this,value).self();
    }
    @Override
    public final PSValue logicAnd(PSValue value)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_LOGIC_AND)) == null
                ? super.logicAnd(value)
                : prop.value.innerCall(this,value).self();
    }
    @Override
    public final PSValue logicOr(PSValue value)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_LOGIC_OR)) == null
                ? super.logicOr(value)
                : prop.value.innerCall(this,value).self();
    }
    @Override
    public final PSValue logicNot()
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_LOGIC_NOT)) == null
                ? super.logicNot()
                : prop.value.innerCall(this).self();
    }
    @Override
    public final PSValue logicXor(PSValue value)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_LOGIC_XOR)) == null
                ? super.logicXor(value)
                : prop.value.innerCall(this,value).self();
    }
    
    @Override
    public final PSValue equals(PSValue value)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_EQUALS)) == null
                ? super.equals(value)
                : prop.value.innerCall(this,value).self();
    }
    @Override
    public final PSValue notEquals(PSValue value)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_NOTEQUALS)) == null
                ? super.notEquals(value)
                : prop.value.innerCall(this,value).self();
    }
    @Override
    public final PSValue greaterThan(PSValue value)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_GREATER)) == null
                ? super.greaterThan(value)
                : prop.value.innerCall(this,value).self();
    }
    @Override
    public final PSValue smallerThan(PSValue value)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_SMALLER)) == null
                ? super.smallerThan(value)
                : prop.value.innerCall(this,value).self();
    }
    @Override
    public final PSValue greaterOrEqualsThan(PSValue value)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_GREATER_EQUALS)) == null
                ? super.greaterOrEqualsThan(value)
                : prop.value.innerCall(this,value).self();
    }
    @Override
    public final PSValue smallerOrEqualsThan(PSValue value)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_SMALLER_EQUALS)) == null
                ? super.smallerOrEqualsThan(value)
                : prop.value.innerCall(this,value).self();
    }
    @Override
    public final PSValue negate()
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_NEGATE)) == null
                ? super.negate()
                : prop.value.innerCall(this).self();
    }
    
    @Override
    public final PSValue contains(PSValue value)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.CONTAINS)) == null
                ? super.contains(value)
                : prop.value.innerCall(this,value).self();
    }
    
    @Override
    public final PSValue set(PSValue key, PSValue value)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_SET)) == null
                ? super.set(key,value)
                : prop.value.innerCall(this,key,value).self();
    }
    @Override
    public final PSValue get(PSValue key)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_GET)) == null
                ? super.get(key)
                : prop.value.innerCall(this,key).self();
    }
    
    /* Object Operations */
    @Override
    public final PSValue setProperty(String name, PSValue value)
    {
        if(frozen)
            throw new PSRuntimeException("Cannot change properties in frozen object");
        Property p = properties.get(name);
        if(p == null)
        {
            if(value != UNDEFINED)
                properties.put(name, new Property(value, false));
            return value;
        }
        if(p.frozen)
            throw new PSRuntimeException("Property \"" + name + "\" is frozen");
        if(value == UNDEFINED)
            return properties.remove(name).value;
        p.value = value;
        return value;
    }
    @Override
    public final PSValue getProperty(String name)
    {
        Property prop;
        return (prop = property(name)) == null ? UNDEFINED : prop.value;
    }
    
    @Override
    protected final PSVarargs innerCall(PSValue self)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_CALL)) == null
                ? super.innerCall(self)
                : prop.value.innerCall(self).self();
    }
    @Override
    protected final PSVarargs innerCall(PSValue self, PSValue arg0)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_CALL)) == null
                ? super.innerCall(self, arg0)
                : prop.value.innerCall(self, arg0).self();
    }
    @Override
    protected final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_CALL)) == null
                ? super.innerCall(self, arg0, arg1)
                : prop.value.innerCall(self, arg0, arg1).self();
    }
    @Override
    protected final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_CALL)) == null
                ? super.innerCall(self, arg0, arg1, arg2)
                : prop.value.innerCall(self, arg0, arg1, arg2).self();
    }
    @Override
    protected final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_CALL)) == null
                ? super.innerCall(self, arg0, arg1, arg2, arg3)
                : prop.value.innerCall(self, arg0, arg1, arg2, arg3).self();
    }
    @Override
    protected final PSVarargs innerCall(PSValue self, PSVarargs args)
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_CALL)) == null
                ? super.innerCall(self, args)
                : prop.value.innerCall(self, args).self();
    }
    
    @Override
    public final PSIterator createIterator()
    {
        Property prop;
        return (prop = property(ObjectSpecialOpsNames.OPERATOR_CALL)) == null
                ? super.createIterator()
                : prop.value.innerCall(this).self().toPSIterator();
    }
    
    
    @Override
    public final PSValue createNewInstance()
    {
        PSObject instance = new PSObject(new HashMap<>(), this);
        Property init = instance.property(ObjectSpecialOpsNames.OPERATOR_NEW);
        if(init != null)
            init.value.innerCall(instance);
        return instance;
    }
    @Override
    public final PSValue createNewInstance(PSValue arg0)
    {
        PSObject instance = new PSObject(new HashMap<>(), this);
        Property init = instance.property(ObjectSpecialOpsNames.OPERATOR_NEW);
        if(init != null)
            init.value.innerCall(instance,arg0);
        return instance;
    }
    @Override
    public final PSValue createNewInstance(PSValue arg0, PSValue arg1)
    {
        PSObject instance = new PSObject(new HashMap<>(), this);
        Property init = instance.property(ObjectSpecialOpsNames.OPERATOR_NEW);
        if(init != null)
            init.value.innerCall(instance,arg0,arg1);
        return instance;
    }
    @Override
    public final PSValue createNewInstance(PSValue arg0, PSValue arg1, PSValue arg2)
    {
        PSObject instance = new PSObject(new HashMap<>(), this);
        Property init = instance.property(ObjectSpecialOpsNames.OPERATOR_NEW);
        if(init != null)
            init.value.innerCall(instance,arg0,arg1,arg2);
        return instance;
    }
    @Override
    public final PSValue createNewInstance(PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3)
    {
        PSObject instance = new PSObject(new HashMap<>(), this);
        Property init = instance.property(ObjectSpecialOpsNames.OPERATOR_NEW);
        if(init != null)
            init.value.innerCall(instance,arg0,arg1,arg2,arg3);
        return instance;
    }
    @Override
    public final PSValue createNewInstance(PSVarargs args)
    {
        PSObject instance = new PSObject(new HashMap<>(), this);
        Property init = instance.property(ObjectSpecialOpsNames.OPERATOR_NEW);
        if(init != null)
            init.value.innerCall(instance,args);
        return instance;
    }
    
    public static final class Property
    {
        private PSValue value;
        private boolean frozen;
        
        public Property(PSValue value, boolean freeze)
        {
            this.value = value;
            this.frozen = freeze;
        }
        
        public final PSValue getValue() { return value; }
        public final boolean isFrozen() { return frozen; }
        
        public final void setValue(PSValue value) { this.value = value == null ? UNDEFINED : value; }
        public final void setFrozen(boolean freeze) { this.frozen = freeze; }
    }
    
    public final class PropertyEntry
    {
        private Map.Entry<String, Property> entry;
        
        public final String getName() { return entry.getKey(); }
        public final PSValue getValue() { return entry.getValue().value; }
        public final boolean isFrozen() { return entry.getValue().frozen; }
    }
    
    private final class PropertyIterator implements Iterator<PropertyEntry>
    {
        private final Iterator<Map.Entry<String, Property>> it = properties.entrySet().iterator();
        private final PropertyEntry current = new PropertyEntry();

        @Override
        public final boolean hasNext() { return it.hasNext(); }

        @Override
        public final PropertyEntry next()
        {
            current.entry = it.next();
            return current;
        }
    }
}

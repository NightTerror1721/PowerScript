/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import nt.ps.lang.ObjectSpecialOpsNames;
import nt.ps.lang.PSDataType;
import nt.ps.lang.PSFunction;
import nt.ps.lang.PSIterator;
import nt.ps.lang.PSObject;
import nt.ps.lang.PSObject.Property;
import nt.ps.lang.PSObject.PropertyEntry;
import nt.ps.lang.PSString;
import nt.ps.lang.PSValue;
import nt.ps.lang.PSVarargs;

/**
 *
 * @author Asus
 */
public final class PSObjectReference extends ImmutableCoreLibrary
{
    @Override
    public final PSValue createNewInstance() { return new PSObject(); }
    
    @Override
    public PSValue createNewInstance(PSValue arg0)
    {
        switch(arg0.getPSType())
        {
            default: return createNewInstance();
            case MAP: {
                Map<PSValue, PSValue> map = arg0.toJavaMap();
                return new PSObject(new HashMap<>(map.entrySet().stream().collect(Collectors.toMap(
                        e -> e.getKey().toJavaString(),
                        e -> new Property(e.getValue(), false)
                ))));
            }
            case OBJECT: {
                return arg0.toPSObject().copy();
            }
        }
    }
    
    @Override
    public PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
            case "toString": return TO_STRING;
            case "deepToString": return DEEP_TO_STRING;
            case "setProperty": return SET_PROPERTY;
            case "getProperty": return GET_PROPERTY;
            case "properties": return PROPERTIES;
            case "freeze": return FREEZE;
            case "freezeProperty": return FREEZE_PROPERTY;
            case "isFrozen": return IS_FROZEN;
            case "isPropertyFrozen": return IS_PROPERTY_FROZEN;
            case "extends": return EXTENDS;
            case "prototype": return PROTOTYPE;
            case "super": return SUPER;
            case "superConstructor": return SUPER_CONSTRUCTOR;
        }
    }
    
    
    private static final PSValue
            TO_STRING = PSFunction.function((arg0) -> new PSString(toString(arg0, false))),
            DEEP_TO_STRING = PSFunction.function((arg0) -> new PSString(toString(arg0, true))),
            SET_PROPERTY = PSFunction.voidFunction((arg0, arg1, arg2) -> {
                arg0.setProperty(arg1.toJavaString(), arg2);
            }),
            GET_PROPERTY = PSFunction.function((arg0, arg1) -> arg0.getProperty(arg1.toJavaString())),
            PROPERTIES = PSFunction.function((arg0) -> {
                return new PSIterator()
                {
                    private final Iterator<PropertyEntry> it = arg0.toPSObject().properties().iterator();
                    private final PSValue[] array = new PSValue[2];
                    private final PSVarargs ret = varargsOf(array);
                    
                    @Override
                    public final boolean hasNext() { return it.hasNext(); }

                    @Override
                    public final PSVarargs next()
                    {
                        PropertyEntry p = it.next();
                        array[0] = valueOf(p.getName());
                        array[1] = p.getValue();
                        return ret;
                    }
                };
            }),
            FREEZE = PSFunction.function((arg0) -> { 
                arg0.toPSObject().setFrozen(true);
                return arg0;
            }),
            FREEZE_PROPERTY = PSFunction.voidFunction((arg0, arg1) -> arg0.toPSObject().setPropertyFrozen(arg1.toJavaString(), true)),
            IS_FROZEN = PSFunction.function((arg0) -> arg0.toPSObject().isFrozen() ? TRUE : FALSE),
            IS_PROPERTY_FROZEN = PSFunction.function((arg0, arg1) -> arg0.toPSObject().isPropertyFrozen(arg1.toJavaString()) ? TRUE : FALSE),
            EXTENDS = PSFunction.function((arg0, arg1) -> PSObject.createExtended(arg0.toPSObject(), arg1.toPSObject())),
            PROTOTYPE = PSFunction.function((arg0) -> {
                PSValue parent = arg0.toPSObject().getParent();
                return parent == null ? NULL : parent;
            }),
            SUPER = PSFunction.function((arg0) -> {
                PSValue parent = getSuper(arg0.toPSObject());
                return parent == null ? NULL : parent;
            }),
            SUPER_CONSTRUCTOR = PSFunction.voidVarFunction((args) -> {
                PSObject self = args.self().toPSObject();
                PSValue parent = getSuper(self);
                if(parent == null)
                    return;
                PSValue constructor = parent.getProperty(ObjectSpecialOpsNames.OPERATOR_NEW);
                if(constructor != null && constructor != UNDEFINED)
                    constructor.invoke("call", args);
            });
    
    private static String toString(PSValue value, boolean deep)
    {
        return value.getPSType() == PSDataType.OBJECT
                ? toString((PSObject) value, deep)
                : value.toString();
    }
    
    public static final String toString(PSObject obj, boolean deep)
    {
        if(obj.getPropertyCount() <= 0 && (!deep || !obj.hasParent()))
            return "{}";
        
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        
        if(deep && obj.hasParent())
            sb.append("<super>: ").append(toString(obj.getParent(), true)).append('\n');
        
        for(PropertyEntry p : obj.properties())
        {
            sb.append('\t').append(p.getName()).append(": ")
                    .append(toString(p.getValue(), deep).replace("\n", "\n\t")).append('\n');
        }
        sb.append('}');
        
        return sb.toString();
    }
    
    private static PSValue getSuper(PSObject object)
    {
        PSValue proto = object.getParent();
        if(proto == null || !proto.isObject())
            return null;
        return proto.toPSObject().getParent();
    }
    
    
}

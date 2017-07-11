/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import nt.ps.lang.PSDataType;
import nt.ps.lang.PSFunction;
import nt.ps.lang.PSObject;
import nt.ps.lang.PSObject.Property;
import nt.ps.lang.PSString;
import nt.ps.lang.PSValue;

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
                        e -> e.getValue()
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
        }
    }
    
    
    private static final PSValue
            TO_STRING = PSFunction.function((arg0) -> new PSString(toString(arg0, false))),
            DEEP_TO_STRING = PSFunction.function((arg0) -> new PSString(toString(arg0, true))),
            SET_PROPERTY = PSFunction.voidFunction((arg0, arg1, arg2) -> {
                arg0.toPSObject().setProperty(arg1.toJavaString(), arg2);
            }),
            GET_PROPERTY = PSFunction.function((arg0, arg1) -> arg0.toPSObject().getProperty(arg1.toJavaString()));
    
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
        
        for(Property p : obj.properties())
        {
            sb.append('\t').append(p.getName()).append(": ")
                    .append(toString(p.getValue(), deep).replace("\n", "\n\t")).append('\n');
        }
        sb.append('}');
        
        return sb.toString();
    }
    
    
    
}

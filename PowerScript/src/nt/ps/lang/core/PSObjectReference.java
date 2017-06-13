/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import nt.ps.lang.PSObject;
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
        }
    }
    
    
    
    
}

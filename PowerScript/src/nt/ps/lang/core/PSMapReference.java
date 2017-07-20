/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import java.util.HashMap;
import nt.ps.lang.PSMap;
import nt.ps.lang.PSNumber;
import nt.ps.lang.PSObject.PropertyEntry;
import nt.ps.lang.PSString;
import nt.ps.lang.PSValue;
import nt.ps.lang.PSVarargs;
import nt.ps.lang.core.ImmutableCoreLibrary.PrimitiveReference;

/**
 *
 * @author Asus
 */
public final class PSMapReference extends PrimitiveReference
{
    @Override
    public final PSValue createNewInstance() { return new PSMap(); }
    
    @Override
    public PSValue createNewInstance(PSValue arg0)
    {
        switch(arg0.getPSType())
        {
            default: return createNewInstance();
            case MAP: return new PSMap(arg0.toJavaMap());
            case OBJECT: {
                HashMap<PSValue, PSValue> map = new HashMap<>();
                for(PropertyEntry p : arg0.toPSObject().properties())
                    map.put(new PSString(p.getName()), p.getValue());
                return new PSMap(map);
            }
            case TUPLE: case ARRAY: {
                HashMap<PSValue, PSValue> map = new HashMap<>();
                int count = 0;
                for(PSValue value : arg0.toJavaList())
                    map.put(new PSNumber.PSInteger(count++), value);
                return new PSMap(map);
            }
        }
    }
    
    @Override
    protected final PSVarargs innerCall(PSValue self) { return new PSMap(); }

    @Override
    protected final PSVarargs innerCall(PSValue self, PSValue arg0) { return new PSMap(arg0.toJavaMap()); }
    
    @Override
    public PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
        }
    }
}

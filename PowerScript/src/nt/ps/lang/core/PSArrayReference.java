/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import nt.ps.lang.PSArray;
import nt.ps.lang.PSValue;
import nt.ps.lang.PSVarargs;
import nt.ps.lang.core.ImmutableCoreLibrary.PrimitiveReference;

/**
 *
 * @author Asus
 */
public class PSArrayReference extends PrimitiveReference
{
    @Override
    public final PSValue createNewInstance() { return new PSArray(); }
    
    @Override
    public PSValue createNewInstance(PSValue arg0)
    {
        switch(arg0.getPSType())
        {
            default: return createNewInstance();
            case TUPLE: return arg0.toPSTuple().asPSArray();
            case ARRAY: return arg0.toPSArray().copy();
            case NUMBER: return new PSArray(arg0.toJavaInt());
        }
    }
    
    @Override
    protected final PSVarargs innerCall(PSValue self) { return new PSArray(); }

    @Override
    protected final PSVarargs innerCall(PSValue self, PSValue arg0)
    {
        return new PSArray(arg0.toJavaList());
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import nt.ps.lang.PSValue;
import nt.ps.lang.PSVarargs;
import nt.ps.lang.core.ImmutableCoreLibrary.PrimitiveReference;

/**
 *
 * @author Asus
 */
public final class PSBooleanReference extends PrimitiveReference
{
    @Override
    public final PSValue createNewInstance() { return TRUE; }
    
    @Override
    public PSValue createNewInstance(PSValue arg0) { return arg0.toJavaBoolean() ? TRUE : FALSE; }
    
    @Override
    protected final PSVarargs innerCall(PSValue self) { return TRUE; }

    @Override
    protected final PSVarargs innerCall(PSValue self, PSValue arg0) { return arg0.toJavaBoolean() ? TRUE : FALSE; }
    
    @Override
    public PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
        }
    }
}

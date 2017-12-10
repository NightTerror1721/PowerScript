/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import nt.ps.exception.PSRuntimeException;
import nt.ps.lang.PSIterator;
import nt.ps.lang.PSValue;
import static nt.ps.lang.PSValue.UNDEFINED;
import nt.ps.lang.PSVarargs;
import nt.ps.lang.core.ImmutableCoreLibrary.PrimitiveReference;

/**
 *
 * @author Asus
 */
public final class PSIteratorReference extends PrimitiveReference
{
    @Override
    public final PSValue createNewInstance() { throw new PSRuntimeException("Expected a function pair"); }
    
    @Override
    public PSValue createNewInstance(PSValue arg0) { throw new PSRuntimeException("Expected a function pair"); }
    
    @Override
    public PSValue createNewInstance(PSValue arg0, PSValue arg1)
    {
        return new PSIterator()
        {
            @Override
            public final boolean hasNext() { return arg1.call(NULL).self().toJavaBoolean(); }

            @Override
            public final PSVarargs next() { return arg0.call(); }
        };
    }
    
    @Override
    protected final PSVarargs innerCall(PSValue self) { throw new PSRuntimeException("Expected a valid value"); }

    @Override
    protected final PSVarargs innerCall(PSValue self, PSValue arg0) { return arg0.createIterator(); }
    
    @Override
    public PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
        }
    }
}

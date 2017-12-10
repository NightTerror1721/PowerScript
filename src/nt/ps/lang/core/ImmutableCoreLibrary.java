/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import nt.ps.lang.PSUserdata;
import nt.ps.lang.PSValue;
import nt.ps.lang.PSVarargs;

/**
 *
 * @author Asus
 */
abstract class ImmutableCoreLibrary extends PSUserdata
{
    @Override
    public abstract PSValue getProperty(String name);
    
    @Override
    public final PSValue setProperty(String name, PSValue value)
    {
        return super.setProperty(name, value);
    }
    
    static abstract class PrimitiveReference extends ImmutableCoreLibrary
    {
        @Override
        protected abstract PSVarargs innerCall(PSValue self);
        
        @Override
        protected abstract PSVarargs innerCall(PSValue self, PSValue arg0);
        
        @Override
        protected final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1)
        {
            return innerCall(self, arg0);
        }
        
        @Override
        protected final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2)
        {
            return innerCall(self, arg0);
        }
        
        @Override
        protected final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3)
        {
            return innerCall(self, arg0);
        }
        
         @Override
        protected final PSVarargs innerCall(PSValue self, PSVarargs args)
        {
            return innerCall(self, args.self());
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import nt.ps.lang.PSValue;

/**
 *
 * @author Asus
 */
public abstract class PSGlobals
{
    public final PSValue getGlobalValue(String name)
    {
        PSValue value;
        return (value = innerGetGlobalValue(name)) == null ? PSValue.UNDEFINED : value;
    }
    protected abstract PSValue innerGetGlobalValue(String name);
    
    public final void setGlobalValue(String name, PSValue value)
    {
        if(value == PSValue.UNDEFINED || value == null)
            removeGlobalValue(name);
        else innerSetGlobalValue(name,value);
    }
    protected abstract void innerSetGlobalValue(String name, PSValue value);
    
    public abstract void removeGlobalValue(String name);
    
    public Collection<String> keys() { throw new UnsupportedOperationException(); }
    public Collection<PSValue> values() { throw new UnsupportedOperationException(); }
    
    
    public static final PSGlobals instance() { return new DefaultGlobals(); }
    public static final PSGlobals valueOf(Map<String, PSValue> map) { return new DefaultGlobals(map); }
    public static final PSGlobals wrap(PSValue value) { return new WrappedGlobals(value); }
    
    
    private static final class DefaultGlobals extends PSGlobals
    {
        private final Map<String, PSValue> globals;
        
        private DefaultGlobals(Map<String, PSValue> map)
        {
            if(map == null)
                throw new NullPointerException();
            this.globals = map;
        }
        private DefaultGlobals() { this(new HashMap<>()); }
        
        @Override
        public final PSValue innerGetGlobalValue(String name) { return globals.get(name); }

        @Override
        public final void innerSetGlobalValue(String name, PSValue value) { globals.put(name,value); }

        @Override
        public final void removeGlobalValue(String name) { globals.remove(name); }
        
        @Override
        public final Collection<String> keys() { return globals.keySet(); }
        
        @Override
        public final Collection<PSValue> values() { return globals.values(); }
    }
    
    private static final class WrappedGlobals extends PSGlobals
    {
        private final PSValue globals;
        
        private WrappedGlobals(PSValue value)
        {
            if(value == null)
                throw new NullPointerException();
            this.globals = value;
        }
        
        @Override
        public final PSValue innerGetGlobalValue(String name) { return globals.getProperty(name); }

        @Override
        public final void innerSetGlobalValue(String name, PSValue value) { globals.setProperty(name,value); }

        @Override
        public final void removeGlobalValue(String name) { globals.setProperty(name,PSValue.UNDEFINED); }
    }
}

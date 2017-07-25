/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import nt.ps.lang.PSValue;

/**
 *
 * @author Asus
 */
public abstract class PSGlobals
{
    private final PSGlobals parent;
    
    protected PSGlobals(PSGlobals parent) { this.parent = parent; }
    
    public final PSValue getGlobalValue(String name)
    {
        PSValue value;
        return (value = innerGetGlobalValue(name)) == null
                ? parent == null ? PSValue.UNDEFINED : parent.getGlobalValue(name)
                : value;
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
    
    
    public final PSValue getNativeValue(String name) { return getNativeValue0(name); }
    PSValue getNativeValue0(String name)
    {
        return parent == null ? PSValue.UNDEFINED : parent.getNativeValue0(name);
    }
    
    public final Set<String> getNativeNames() { return getNativeNames0(); }
    Set<String> getNativeNames0()
    {
        return parent == null ? Collections.emptySet() : parent.getNativeNames0();
    }
    
    public final boolean hasNativeValue(String name) { return hasNativeValue0(name); }
    boolean hasNativeValue0(String name)
    {
        return parent == null ? false : parent.hasNativeValue0(name);
    }
    
    
    
    public static final PSGlobals instance(PSGlobals parent) { return new DefaultGlobals(parent); }
    public static final PSGlobals valueOf(PSGlobals parent, Map<String, PSValue> map) { return new DefaultGlobals(parent, map); }
    public static final PSGlobals wrap(PSGlobals parent, PSValue value) { return new WrappedGlobals(parent, value); }
    
    
    private static final class DefaultGlobals extends PSGlobals
    {
        private final Map<String, PSValue> globals;
        
        private DefaultGlobals(PSGlobals parent, Map<String, PSValue> map)
        {
            super(Objects.requireNonNull(parent));
            if(map == null)
                throw new NullPointerException();
            this.globals = map;
        }
        private DefaultGlobals(PSGlobals parent) { this(parent, new HashMap<>()); }
        
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
        
        private WrappedGlobals(PSGlobals parent, PSValue value)
        {
            super(Objects.requireNonNull(parent));
            if(value == null)
                throw new NullPointerException();
            this.globals = value;
        }
        
        @Override
        public final PSValue innerGetGlobalValue(String name)
        {
            PSValue value;
            return (value = globals.getProperty(name)) == PSValue.UNDEFINED ? null : value;
        }

        @Override
        public final void innerSetGlobalValue(String name, PSValue value) { globals.setProperty(name,value); }

        @Override
        public final void removeGlobalValue(String name) { globals.setProperty(name,PSValue.UNDEFINED); }
    }
}

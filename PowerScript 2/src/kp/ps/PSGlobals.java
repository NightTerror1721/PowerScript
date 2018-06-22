/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.ps;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import kp.ps.lang.PSValue;

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
    
    public final boolean hasGlobalValue(String name)
    {
        PSValue value = innerGetGlobalValue(name);
        return value != null && value != PSValue.UNDEFINED;
    }
    
    public Collection<String> keys() { throw new UnsupportedOperationException(); }
    public Collection<PSValue> values() { throw new UnsupportedOperationException(); }
    
    
    public final PSClassLoader getClassLoader() { return getClassLoader0(); }
    PSClassLoader getClassLoader0() { return parent == null ? new PSClassLoader(getClass().getClassLoader()) : parent.getClassLoader(); }
    
    
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
    
    File adaptPath(String strPath) { return parent == null ? new File(strPath) : parent.adaptPath(strPath); }
    public final PSValue importScript(String strPath)
    {
        File file = adaptPath(strPath);
        PSObject wrapper = new PSObject();
        PSGlobals globals = wrap(this, wrapper);
        PSFunction callable = getCompiledCallable(file, globals, getClassLoader());
        callable.call();
        wrapper.setFrozen(true);
        return wrapper;
    }
    
    public final void includeScript(String strPath)
    {
        File file = adaptPath(strPath);
        PSFunction callable = getCompiledCallable(file, this, getClassLoader());
        callable.call();
    }
    
    private static PSFunction getCompiledCallable(File file, PSGlobals globals, PSClassLoader classLoader)
    {
        String name = file.getName().replace('.','_');
        Class<? extends PSFunction> clazz = classLoader.findClassInCache(name);
        if(clazz != null)
            return CompilerUnit.createCompiledClassInstance(clazz, globals);
        
        try(FileInputStream fis = new FileInputStream(file))
        {
            return CompilerUnit.compile(fis, globals, classLoader, file.getName().replace('.','_'), false);
        }
        catch(Throwable th)
        {
            throw new PSRuntimeException(th);
        } 
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

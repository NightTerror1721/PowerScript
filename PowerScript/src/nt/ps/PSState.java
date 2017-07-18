/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import nt.ps.lang.PSValue;
import nt.ps.lang.core.PSArrayReference;
import nt.ps.lang.core.PSBooleanReference;
import nt.ps.lang.core.PSIteratorReference;
import nt.ps.lang.core.PSMapReference;
import nt.ps.lang.core.PSNumberReference;
import nt.ps.lang.core.PSObjectReference;
import nt.ps.lang.core.PSStringReference;
import nt.ps.lang.core.PSTupleReference;

/**
 *
 * @author Asus
 */
public final class PSState extends PSGlobals
{
    private final HashMap<String, PSValue> globals = new HashMap<>();
    private final HashMap<String, PSValue> natives = new HashMap<>();
    
    private final PSClassLoader classLoader;
    
    public PSState()
    {
        super(null);
        classLoader = new PSClassLoader(getClass().getClassLoader());
    }
    
    public final PSClassLoader getClassLoader() { return classLoader; }
    
    public static final PSState createDefaultInstance()
    {
        PSState state = new PSState();
        state.insertDefaultNatives();
        return state;
    }
    
    public final void insertDefaultNatives()
    {
        natives.put("Object", new PSObjectReference());
        natives.put("Array", new PSArrayReference());
        natives.put("Tuple", new PSTupleReference());
        natives.put("Boolean", new PSBooleanReference());
        natives.put("Number", new PSNumberReference());
        natives.put("String", new PSStringReference());
        natives.put("Map", new PSMapReference());
        natives.put("Iterator", new PSIteratorReference());
    }
    
    @Override
    protected final PSValue innerGetGlobalValue(String name) { return globals.get(name); }

    @Override
    protected final void innerSetGlobalValue(String name, PSValue value) { globals.put(name,value); }

    @Override
    public final void removeGlobalValue(String name) { globals.remove(name); }

    @Override
    public final Collection<String> keys() { return globals.keySet(); }

    @Override
    public final Collection<PSValue> values() { return globals.values(); }
    
    @Override
    final PSValue getNativeValue0(String name)
    {
        PSValue v;
        return (v = natives.get(name)) != null ? v : PSValue.UNDEFINED;
    }
    
    public final void setNativeValue(String name, PSValue value)
    {
        if(natives.containsKey(name))
            throw new IllegalArgumentException(name + " value has already exists");
        natives.put(name, value);
    }
    
    @Override
    final Set<String> getNativeNames0()
    {
        return natives.keySet();
    }
    
    @Override
    final boolean hasNativeValue0(String name)
    {
        return natives.containsKey(name);
    }
}

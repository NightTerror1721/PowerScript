/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps;

import java.util.Collection;
import java.util.HashMap;
import nt.ps.lang.PSValue;

/**
 *
 * @author Asus
 */
public final class PSState extends PSGlobals
{
    private final HashMap<String, PSValue> globals = new HashMap<>();
    
    
    
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
    
}

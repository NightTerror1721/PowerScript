/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.util.HashMap;
import nt.ps.lang.PSValue;
import nt.ps.lang.PSVarargs;

/**
 *
 * @author Asus
 */
public final class LangUtils
{
    private LangUtils() {}
    
    public static final class ProtoMap extends HashMap<PSValue, PSValue>
    {
        public ProtoMap() { super(); }
        public ProtoMap(int initialCapacity) { super(initialCapacity); }
        public ProtoMap(int initialCapacity, float loadFactor) { super(initialCapacity, loadFactor); }
    }
    
    public static final class ProtoObject extends HashMap<String, PSValue>
    {
        public ProtoObject() { super(); }
        public ProtoObject(int initialCapacity) { super(initialCapacity); }
        public ProtoObject(int initialCapacity, float loadFactor) { super(initialCapacity, loadFactor); }
    }
    
    public static final PSVarargs wrap2Args(PSValue arg0, PSValue arg1)
    {
        return PSVarargs.varargsOf(arg0, arg1);
    }
}

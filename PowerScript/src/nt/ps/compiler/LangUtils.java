/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.util.HashMap;
import nt.ps.lang.PSObject;
import nt.ps.lang.PSString;
import nt.ps.lang.PSValue;

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
    
    public static final PSValue operatorTypeof(PSValue value)
    {
        return new PSString(value.getPSType().getTypeName());
    }
    
    public static final PSValue operatorInstanceof(PSValue object, PSValue parent)
    {
        PSObject obj = object.toPSObject();
        return obj.getParent()== parent ? PSValue.TRUE : PSValue.FALSE;
    }
    
    public static final PSValue operatorEqualsReference(PSValue value0, PSValue value1)
    {
        return value0.getPSType() == value1.getPSType()
                ? value0.equals(value1)
                : PSValue.FALSE;
    }
    
    public static final PSValue operatorNotEqualsReference(PSValue value0, PSValue value1)
    {
        return value0.getPSType() == value1.getPSType()
                ? value0.notEquals(value1)
                : PSValue.TRUE;
    }
}

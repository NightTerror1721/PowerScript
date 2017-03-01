/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.datatype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 * @author Asus
 */
public final class PSArray extends PSValue implements Iterable<PSValue>
{
    public final List<PSValue> array;
    
    public PSArray(int length)
    {
        this.array = new SimpleArrayList(new PSValue[length],length);
    }
    public PSArray(List<PSValue> list) { array = list; }
    public PSArray(Collection<? extends PSValue> c) { array = new ArrayList<>(c); }
    public PSArray(PSValue... array) { this.array = Arrays.asList(array); }
    public PSArray(Supplier<List<PSValue>> s) { array = s.get(); }
    public PSArray() { array = new ArrayList<>(); }
    
    @Override
    public final PSDataType getPSType() { return PSDataType.ARRAY; }
    
    @Override
    public final PSArray toPSArray() { return this; }
    
    @Override
    public final boolean toJavaBoolean() { return !array.isEmpty(); }

    @Override
    public final String toJavaString() { return array.toString(); }

    @Override
    public final List<PSValue> toJavaList() { return array; }
    
    @Override
    public final Map<PSValue, PSValue> toJavaMap()
    {
        HashMap<PSValue,PSValue> map = new HashMap<>(array.size());
        int count = 0;
        for(PSValue value : array)
            map.put(new PSNumber.PSInteger(count++),value);
        return map;
    }

    @Override
    public boolean equals(Object o)
    {
        return o instanceof PSArray && array.equals(((PSArray)o).array);
    }

    @Override
    public final int hashCode()
    {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.array);
        return hash;
    }
    
    /* Operators */
    @Override
    public final PSValue plus(PSValue value)
    {
        ArrayList list = new ArrayList<>(array);
        list.addAll(value.toJavaList());
        return new PSArray(list);
    }
    
    @Override
    public final PSValue shiftLeft(PSValue value)
    {
        array.add(value);
        return this;
    }
    @Override
    public final PSValue shiftRight(PSValue value)
    {
        array.remove(value);
        return this;
    }
    
    @Override
    public final PSValue equals(PSValue value) { return array.equals(value.toJavaList()) ? TRUE : FALSE; }
    
    @Override
    public final PSValue notEquals(PSValue value) { return array.equals(value.toJavaList()) ? FALSE : TRUE; }
    
    @Override
    public final PSValue contains(PSValue value) { return array.contains(value) ? TRUE : FALSE; }
    
    @Override
    public final PSIterator createIterator() { return new LPLArrayIterator(); }
    
    
    
    public final PSVarargs expand()
    {
        return array.isEmpty() ? EMPTY : PSVarargs.varargsOf(
                array.toArray(new PSValue[array.size()]),EMPTY);
    }
    
    public static final PSVarargs expand(List<PSValue> list)
    {
        return list.isEmpty() ? EMPTY : PSVarargs.varargsOf(
                list.toArray(new PSValue[list.size()]),EMPTY);
    }
    
    @Override
    public final Iterator<PSValue> iterator() { return array.iterator(); }
    
    private final class LPLArrayIterator extends PSIterator
    {
        private final Iterator<PSValue> it = array.iterator();
        private int count = 0;
        
        @Override
        public final boolean hasNext()
        {
            return it.hasNext();
        }

        @Override
        public final PSVarargs next()
        {
            return varargsOf(it.next(),new PSNumber.PSInteger(count++));
        }
    }
}

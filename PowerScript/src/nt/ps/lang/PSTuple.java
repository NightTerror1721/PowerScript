/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 *
 * @author Asus
 */
public final class PSTuple extends PSValue implements Iterable<PSValue>
{
    public final PSValue[] tuple;
    
    public PSTuple(PSValue... array)
    {
        tuple = new PSValue[array.length];
        System.arraycopy(array,0,tuple,0,array.length);
    }
    public PSTuple(List<PSValue> list) { tuple = list.toArray(new PSValue[list.size()]); }
    public PSTuple(Collection<? extends PSValue> c) { tuple = c.toArray(new PSValue[c.size()]); }
    public PSTuple(Supplier<PSValue[]> s) { this(s.get()); }
    public PSTuple() { tuple = new PSValue[0]; }
    
    @Override
    public final PSDataType getPSType() { return PSDataType.TUPLE; }
    
    @Override
    public final PSTuple toPSTuple() { return this; }
    
    @Override
    public final boolean toJavaBoolean() { return tuple.length != 0; }

    @Override
    public final String toJavaString()
    {
        if(tuple.length == 0)
            return "()";
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for(int i=0;i<tuple.length;i++)
            sb.append(tuple[i]).append(", ");
        sb.delete(sb.length()-2,sb.length()).append(')');
        return sb.toString();
    }

    @Override
    public final List<PSValue> toJavaList() { return Arrays.asList(tuple); }
    
    @Override
    public final Map<PSValue, PSValue> toJavaMap()
    {
        HashMap<PSValue,PSValue> map = new HashMap<>(tuple.length);
        for(int i=0;i<tuple.length;i++)
            map.put(new PSNumber.PSInteger(i),tuple[i]);
        return map;
    }

    @Override
    public final boolean equals(Object o)
    {
        return o instanceof PSTuple &&
                Arrays.equals(tuple,((PSTuple)o).tuple);
    }

    @Override
    public final int hashCode()
    {
        int hash = 7;
        hash = 23 * hash + Arrays.deepHashCode(this.tuple);
        return hash;
    }
    
    
    /* Operations */
    @Override
    public final PSValue plus(PSValue value)
    {
        if(value.getPSType() == PSDataType.TUPLE)
        {
            PSValue[] tuple2 = value.toPSTuple().tuple;
            PSValue[] newTuple = new PSValue[tuple.length + tuple2.length];
            System.arraycopy(tuple,0,newTuple,0,tuple.length);
            System.arraycopy(tuple2,0,newTuple,tuple.length,tuple2.length);
            return new PSTuple(newTuple);
        }
        List<PSValue> list = value.toJavaList();
        PSValue[] newTuple = new PSValue[tuple.length + list.size()];
        System.arraycopy(tuple,0,newTuple,0,tuple.length);
        int i = tuple.length;
        for(PSValue listValue : list)
            newTuple[i++] = listValue;
        return new PSTuple(newTuple);
    }
    
    @Override
    public final PSValue equals(PSValue value)
    {
        if(value.getPSType() == PSDataType.TUPLE)
            return Arrays.equals(tuple,value.toPSTuple().tuple) ? TRUE : FALSE;
        return Arrays.asList(tuple).equals(value.toJavaList()) ? TRUE : FALSE;
    }
    
    @Override
    public final PSValue notEquals(PSValue value) { return equals(value).negate(); }
    
    @Override
    public final PSValue contains(PSValue value)
    {
        for(int i=0;i<tuple.length;i++)
            if(tuple[i].equals(value).toJavaBoolean())
                return TRUE;
        return FALSE;
    }
    
    @Override
    public final PSIterator createIterator() { return new PSTupleIterator(); }
    
    
    
    public final PSVarargs expand()
    {
        return tuple.length == 0 ? EMPTY : varargsOf(tuple);
    }
    
    public static final PSVarargs expand(PSValue[] array)
    {
        return array.length == 0 ? EMPTY : varargsOf(array);
    }
    
    @Override
    public final Iterator<PSValue> iterator()
    {
        return new Iterator<PSValue>()
        {
            private int it = 0;
            
            @Override
            public final boolean hasNext() { return it < tuple.length; }

            @Override
            public final PSValue next() { return tuple[it++]; }
        };
    }
    
    private final class PSTupleIterator extends PSIterator
    {
        private int it = 0;
        
        @Override
        public final boolean hasNext() { return it < tuple.length; }

        @Override
        public final PSVarargs next() { return tuple[it++]; }
    }
}

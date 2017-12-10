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
import java.util.StringJoiner;
import java.util.function.Supplier;
import nt.ps.exception.PSRuntimeException;
import static nt.ps.lang.PSValue.FALSE;
import static nt.ps.lang.PSValue.TRUE;
import static nt.ps.lang.PSValue.UNDEFINED;
import static nt.ps.lang.PSVarargs.EMPTY;
import static nt.ps.lang.PSVarargs.varargsOf;

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
    
    public final PSValue asPSArray() { return new PSArray(tuple); }
    public final PSValue copy() { return new PSTuple(Arrays.copyOf(tuple, tuple.length)); }
    
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
    
    @Override
    public final PSValue get(PSValue key) { return tuple[key.toJavaInt()]; }
    
    @Override
    public final PSValue set(PSValue key, PSValue value)
    {
        throw new PSRuntimeException("Cannot modify any value in a tuple");
    }
    
    
    @Override
    public PSValue createNewInstance()
    {
        PSValue[] tuple2 = new PSValue[tuple.length];
        System.arraycopy(tuple,0,tuple2,0,tuple2.length);
        return new PSTuple(tuple2);
    }
    
    @Override
    public PSValue createNewInstance(PSValue arg0)
    {
        int count = 0;
        List<PSValue> list = arg0.toJavaList();
        PSValue[] array2 = new PSValue[list.size()];
        for(PSValue value : list)
            array2[count++] = value;
        return new PSTuple(array2);
    }
    
    
    
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
        public final PSVarargs next() { return varargsOf(tuple[it++],new PSNumber.PSInteger(it)); }
    }
    
    
    /* Properties */
    @Override
    public final PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
            case "length": return new PSNumber.PSInteger(tuple.length);
            case "concat": return CONCAT;
            case "getSorted": return SORT;
            case "subTuple": return SUB_TUPLE;
            case "expand": return EXPAND;
            case "toString": return TO_STRING;
        }
    }
    
    private static final PSValue CONCAT = PSFunction.<PSTuple>method((self, arg0, arg1, arg2) -> {
        List<PSValue> list = self.toJavaList();
        String del = arg0 == UNDEFINED ? "" : arg0.toJavaString();
        int from = arg1 == UNDEFINED ? 0 : arg1.toJavaInt();
        int to = arg2 == UNDEFINED ? list.size() : arg2.toJavaInt();
        if(to <= from)
            throw new PSRuntimeException("'to' value cannot small than 'from'");
        from = from < 0 ? 0 : from;
        to = to > list.size() ? list.size() : to;
        StringJoiner sj = new StringJoiner(del);
        int count = 0;
        for(PSValue val : list)
        {
            if(count++ < from)
                continue;
            if(count > to)
                break;
            sj.add(val.toJavaString());
        }
        return new PSString(sj.toString());
    });
    
    private static final PSValue SORT = PSFunction.<PSTuple>method((self, arg0) -> {
        PSValue[] tuple2 = new PSValue[self.tuple.length];
        System.arraycopy(self.tuple,0,tuple2,0,self.tuple.length);
        if(arg0 == UNDEFINED)
        {
            Arrays.sort(tuple2,(val1, val2) -> {
                return val1.equals(val2).toJavaBoolean()
                        ? 0
                        : val1.smallerThan(val2).toJavaBoolean()
                            ? -1
                            : 1;
            });
        }
        else
        {
            Arrays.sort(tuple2,(val1, val2) -> {
                return arg0.call(val1,val2).self().toJavaInt();
            });
        }
        return new PSTuple(tuple2);
    });
    
    private static final PSValue SUB_TUPLE = PSFunction.<PSTuple>method((self, arg0, arg1) -> {
        int start = arg0.toJavaInt();
        PSValue[] tuple2;
        if(arg1 == UNDEFINED)
        {
            tuple2 = new PSValue[self.tuple.length - start];
            System.arraycopy(self.tuple,start,tuple2,0,tuple2.length);
        }
        else
        {
            int end = arg1.toJavaInt();
            tuple2 = new PSValue[end - start];
            System.arraycopy(self.tuple,start,tuple2,0,tuple2.length);
        }
        return new PSTuple(tuple2);
    });
    
    private static final PSValue EXPAND = PSFunction.<PSTuple>method((self) -> {
        return self.tuple.length == 0
                ? PSValue.EMPTY
                : varargsOf(self.tuple);
    });
    
    private static final PSValue TO_STRING = PSFunction.<PSTuple>method((self) -> {
        return new PSString(self.toJavaString());
    });
}

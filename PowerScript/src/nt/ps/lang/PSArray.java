/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Supplier;
import nt.ps.exception.PSRuntimeException;
import nt.ps.lang.Utils.NativeObjectLibOneArg;

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
    public final PSIterator createIterator() { return new PSArrayIterator(); }
    
    @Override
    public final PSValue get(PSValue key) { return array.get(key.toJavaInt()); }
    
    @Override
    public final PSValue set(PSValue key, PSValue value)
    {
        array.set(key.toJavaInt(),value);
        return value;
    }
    
    @Override
    public PSValue createNewInstance()
    {
        int count = 0;
        PSValue[] array2 = new PSValue[array.size()];
        for(PSValue value : array)
            array2[count++] = value;
        return new PSArray(array2);
    }
    @Override
    public PSValue createNewInstance(PSValue arg0)
    {
        if(arg0.isNumber())
            return new PSArray(arg0.toJavaInt());
        int count = 0;
        List<PSValue> list = arg0.toJavaList();
        PSValue[] array2 = new PSValue[list.size()];
        for(PSValue value : list)
            array2[count++] = value;
        return new PSArray(array2);
    }
    
    
    
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
    
    private final class PSArrayIterator extends PSIterator
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
    
    /* Properties */
    @Override
    public final PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
            case "concat": return CONCAT;
            case "add": return ADD;
            case "addAll": return ADD_ALL;
            case "remove": return REMOVE;
            case "sort": return SORT;
            case "subArray": return SUB_ARRAY;
            case "expand": return EXPAND;
            case "toString": return TO_STRING;
        }
    }
    
    
    
    public static final PSValue OBJECT_LIB = new NativeObjectLibOneArg(name -> {
        switch(name)
        {
            default: return null;
        }
    }) {
        @Override
        protected final PSVarargs innerCall(PSValue self) { return new PSArray(); }
        
        @Override
        protected final PSVarargs innerCall(PSValue self, PSValue arg0)
        {
            if(arg0.isNumber())
                return new PSArray(arg0.toJavaInt());
            return new PSArray(arg0.toJavaList());
        }
    };
    
    private static final PSValue CONCAT = PSFunction.<PSArray>method((self, arg0, arg1, arg2) -> {
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
    
    private static final PSValue ADD = PSFunction.<PSArray>method((self, arg0, arg1) -> {
        if(arg1 == UNDEFINED)
            return self.array.add(arg0) ? TRUE : FALSE;
        self.array.add(arg0.toJavaInt(),arg1);
        return TRUE;
    });
    
    private static final PSValue ADD_ALL = PSFunction.<PSArray>varMethod((self, args) -> {
        switch(args.numberOfArguments())
        {
            case 2:
                return self.array.addAll(args.arg(1).toJavaList()) ? TRUE : FALSE;
            case 3:
                return self.array.addAll(args.arg(1).toJavaInt(),args.arg(2).toJavaList()) ? TRUE : FALSE;
            default:
                if(args.arg(1) == UNDEFINED)
                    return self.array.addAll(PSValue.varargsAsList(args,2)) ? TRUE : FALSE;
                else return self.array.addAll(args.arg(1).toJavaInt(),varargsAsList(args,2)) ? TRUE : FALSE;
        }
    });
    
    private static final PSValue REMOVE = PSFunction.<PSArray>method((self, arg0) -> {
        if(arg0 == UNDEFINED)
            return self.array.remove(self.array.size()-1);
        else if(arg0.isNumber())
            return self.array.remove(arg0.toJavaInt());
        return self.array.remove(arg0) ? arg0 : UNDEFINED;
    });
    
    private static final PSValue SORT = PSFunction.<PSArray>voidMethod((self, arg0) -> {
        if(arg0 == UNDEFINED)
        {
            Collections.sort(self.array,(val1, val2) -> {
                return val1.equals(val2).toJavaBoolean()
                        ? 0
                        : val1.smallerThan(val2).toJavaBoolean()
                            ? -1
                            : 1;
            });
        }
        else
        {
            Collections.sort(self.array,(val1, val2) -> {
                return arg0.call(val1,val2).self().toJavaInt();
            });
        }
    });
    
    private static final PSValue SUB_ARRAY = PSFunction.<PSArray>method((self, arg0, arg1) -> {
        if(arg1 == UNDEFINED)
            return new PSArray(self.array.subList(arg0.toJavaInt(),self.array.size()));
        return new PSArray(self.array.subList(arg0.toJavaInt(),arg1.toJavaInt()));
    });
    
    private static final PSValue EXPAND = PSFunction.<PSArray>method((self) -> {
        return self.array.isEmpty()
                ? PSValue.EMPTY
                : varargsOf(self.array.toArray(new PSValue[self.array.size()]),EMPTY);
    });
    
    private static final PSValue TO_STRING = PSFunction.<PSArray>method((self) -> {
        return new PSString(self.toJavaString());
    });
}

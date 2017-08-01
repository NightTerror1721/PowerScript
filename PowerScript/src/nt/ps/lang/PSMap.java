/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import nt.ps.exception.PSRuntimeException;

/**
 *
 * @author mpasc
 */
public final class PSMap extends PSValue implements Iterable<Map.Entry<PSValue, PSValue>>
{
    private final Map<PSValue, PSValue> map;
    
    public PSMap(Map<PSValue, PSValue> map) { this.map = map; }
    public PSMap() { map = new HashMap<>(); }
    
    @Override
    public final PSDataType getPSType() { return PSDataType.MAP; }
    
    @Override
    public final PSMap toPSMap() { return this; }

    @Override
    public final boolean equals(Object o)
    {
        return o instanceof PSMap && map.equals(((PSMap)o).map);
    }

    @Override
    public final int hashCode()
    {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.map);
        return hash;
    }
    
    
    /* Operations */
    @Override
    public final boolean toJavaBoolean() { return !map.isEmpty(); }
    
    @Override
    public final String toJavaString() { return map.toString(); }
    
    @Override
    public final List<PSValue> toJavaList() { return new ArrayList<>(map.values()); }
    
    @Override
    public final Map<PSValue, PSValue> toJavaMap() { return map; }
    
    
    @Override
    public final PSValue plus(PSValue value)
    {
        HashMap<PSValue, PSValue> nmap = new HashMap<>(map);
        nmap.putAll(value.toJavaMap());
        return new PSMap(nmap);
    }
    
    
    @Override
    public final PSValue equals(PSValue value) { return map.equals(value.toJavaMap()) ? TRUE : FALSE; }
    
    @Override
    public final PSValue notEquals(PSValue value) { return map.equals(value.toJavaMap()) ? FALSE : TRUE; }
    
    @Override
    public final PSValue negate() { return map.isEmpty() ? TRUE : FALSE; }
    
    
    @Override
    public final PSValue contains(PSValue value) { return map.containsKey(value) ? TRUE : FALSE; }
    
    
    @Override
    public final PSValue set(PSValue key, PSValue value)
    {
        if(key == UNDEFINED)
            throw new PSRuntimeException("Key value cannot be a undefined value");
        return map.put(key,value);
    }
    
    @Override
    public final PSValue get(PSValue key)
    {
        if(key == UNDEFINED)
            throw new PSRuntimeException("Key value cannot be a undefined value");
        PSValue value;
        return (value = map.get(key)) == null ? UNDEFINED : value;
    }
    
    @Override
    public final PSIterator createIterator()
    {
        return new MapIterator();
    }
    
    
    @Override
    public PSValue createNewInstance()
    {
        return new PSMap(map.entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey(),
                e -> e.getValue()
        )));
    }
    @Override
    public PSValue createNewInstance(PSValue arg0)
    {
        return new PSMap(arg0.toJavaMap().entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey(),
                e -> e.getValue()
        )));
    }
    
    
    @Override
    public final Iterator<Map.Entry<PSValue, PSValue>> iterator()
    {
        return map.entrySet().iterator();
    }
    
    private final class MapIterator extends PSIterator
    {
        private final Iterator<Map.Entry<PSValue, PSValue>> it = map.entrySet().iterator();
        private final PSValue[] values = new PSValue[] { UNDEFINED, UNDEFINED, UNDEFINED };
        private final PSVarargs pars = varargsOf(values);
        private int count = 0;
        
        @Override
        public final boolean hasNext()
        {
            return it.hasNext();
        }

        @Override
        public final PSVarargs next()
        {
            Map.Entry<PSValue, PSValue> e = it.next();
            values[0] = e.getKey();
            values[1] = e.getValue();
            values[2] = new PSNumber.PSInteger(count++);
            return pars;
        }
    }
    
    
    /* Properties */
    @Override
    public final PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
            case "length": return new PSNumber.PSInteger(map.size());
            case "put": return PUT;
            case "putAll": return PUT_ALL;
            case "get": return GET;
            case "remove": return REMOVE;
            case "clear": return CLEAR;
            case "isEmpty": return IS_EMPTY;
            case "containsKey": return CONTAINS_KEY;
            case "containsValue": return CONTAINS_VALUE;
            case "keys": return KEYS;
            case "values": return VALUES;
            case "entries": return ENTRIES;
            case "toString": return TO_STRING;
        }
    }
    
    private static final PSValue PUT = PSFunction.<PSMap>method((self, arg0, arg1) -> {
        if(arg0 == UNDEFINED)
            throw new PSRuntimeException("Key value cannot be a undefined value");
        PSValue value = self.map.put(arg0,arg1);
        return value == null ? UNDEFINED : value;
    });
    
    private static final PSValue PUT_ALL = PSFunction.<PSMap>voidMethod((self, arg0) -> {
        self.map.putAll(arg0.toJavaMap());
    });
    
    private static final PSValue GET = PSFunction.<PSMap>method((self, arg0) -> {
        if(arg0 == UNDEFINED)
            throw new PSRuntimeException("Key value cannot be a undefined value");
        return self.map.get(arg0);
    });
    
    private static final PSValue REMOVE = PSFunction.<PSMap>method((self, arg0) -> {
        if(arg0 == UNDEFINED)
            throw new PSRuntimeException("Key value cannot be a undefined value");
        return self.map.remove(arg0);
    });
    
    private static final PSValue CLEAR = PSFunction.<PSMap>voidMethod((self) -> {
        self.map.clear();
    });
    
    private static final PSValue IS_EMPTY = PSFunction.<PSMap>method((self) -> {
        return self.map.isEmpty() ? TRUE : FALSE;
    });
    
    private static final PSValue CONTAINS_KEY = PSFunction.<PSMap>method((self, arg0) -> {
        return self.map.containsKey(arg0)? TRUE : FALSE;
    });
    
    private static final PSValue CONTAINS_VALUE = PSFunction.<PSMap>method((self, arg0) -> {
        return self.map.containsValue(arg0)? TRUE : FALSE;
    });
    
    private static final PSValue KEYS = PSFunction.<PSMap>method((self) -> {
        return new PSTuple(self.map.keySet());
    });
    
    private static final PSValue VALUES = PSFunction.<PSMap>method((self) -> {
        return new PSTuple(self.map.values());
    });
    
    private static final PSValue ENTRIES = PSFunction.<PSMap>method((self) -> {
        return new PSTuple(self.map.entrySet().stream()
                .map(e -> new PSTuple(e.getKey(),e.getValue()))
                .toArray(size -> new PSValue[size]));
    });
    
    private static final PSValue TO_STRING = PSFunction.<PSMap>method((self) -> {
        return new PSString(self.toJavaString());
    });
}

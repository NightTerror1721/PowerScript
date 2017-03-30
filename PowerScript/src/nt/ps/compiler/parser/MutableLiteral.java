/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.Arrays;
import java.util.Iterator;

/**
 *
 * @author Asus
 */
public final class MutableLiteral extends CodeObject implements Iterable<MutableLiteral.Item>
{
    private final Item[] items;
    private final int type;
    
    private MutableLiteral(Tuple tuple, int type)
    {
        Tuple[] tuples = tuple.splitByToken(Separator.COMMA);
        switch (type)
        {
            case TYPE_ARRAY:
            case TYPE_TUPLE:
                items = Arrays.stream(tuples).map(t -> new Item(null,t)).toArray(size -> new Item[size]);
                break;
            case TYPE_MAP:
            case TYPE_OBJECT:
                items = Arrays.stream(tuples).map(t -> {
                    Tuple[] parts = t.splitByToken(Separator.TWO_POINTS);
                    if(parts.length != 2)
                        throw new IllegalArgumentException();
                    return new Item(parts[0], parts[1]);
                }).toArray(size -> new Item[size]);
                break;
            default:
                throw new IllegalStateException();
        }
        this.type = type;
    }
    
    public final int getItemCount() { return items.length; }
    public final Item getItem(int index) { return items[index]; }
    
    public final boolean isLiteralArray() { return type == TYPE_ARRAY; }
    public final boolean isLiteralTuple() { return type == TYPE_TUPLE; }
    public final boolean isLiteralMap() { return type == TYPE_MAP; }
    public final boolean isLiteralObject() { return type == TYPE_OBJECT; }
    
    
    @Override
    public String toString() { return Arrays.toString(items); }
    
    @Override
    public final CodeType getCodeType() { return CodeType.MUTABLE_LITERAL; }

    @Override
    public final Iterator<Item> iterator()
    {
        return new Iterator<Item>()
        {
            private int it = 0;
            
            @Override
            public boolean hasNext() { return it < items.length; }

            @Override
            public Item next() { return items[it++]; }
            
        };
    }
    
    
    public static final MutableLiteral array(Tuple tuple) { return new MutableLiteral(tuple,TYPE_ARRAY); }
    public static final MutableLiteral tuple(Tuple tuple) { return new MutableLiteral(tuple,TYPE_TUPLE); }
    public static final MutableLiteral map(Tuple tuple) { return new MutableLiteral(tuple,TYPE_MAP); }
    public static final MutableLiteral object(Tuple tuple) { return new MutableLiteral(tuple,TYPE_OBJECT); }
    
    
    public final class Item
    {
        private final ParsedCode key;
        private final ParsedCode value;
        
        private Item(Tuple key, Tuple value)
        {
            if(value == null)
                throw new NullPointerException();
            this.key = key.pack();
            this.value = value.pack();
        }
        
        public final ParsedCode getKey() { return key; }
        public final ParsedCode getValue() { return value; }
        
        @Override
        public final String toString()
        {
            if(key != null)
                return "(" + key.toString() + ", " + value.toString() + ")";
            return value.toString();
        }
    }
    
    private static final int TYPE_ARRAY = 0;
    private static final int TYPE_TUPLE = 1;
    private static final int TYPE_MAP = 2;
    private static final int TYPE_OBJECT = 3;
}

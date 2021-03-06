/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.Arrays;
import java.util.Iterator;
import nt.ps.compiler.exception.CompilerError;

/**
 *
 * @author Asus
 */
public final class MutableLiteral extends CodeObject implements Iterable<MutableLiteral.Item>
{
    private final Item[] items;
    private final int type;
    private boolean isConst;
    
    private MutableLiteral(Tuple tuple, int type) throws CompilerError
    {
        Tuple[] tuples = tuple.splitByToken(Separator.COMMA);
        switch(this.type = type)
        {
            case TYPE_ARRAY:
            case TYPE_TUPLE:
                items = Tuple.mapArray(tuples,t -> new Item(null,t), new Item[tuples.length]);
                break;
            case TYPE_MAP:
                if(tuple.length() == 1 && tuple.get(0) == Separator.TWO_POINTS)
                {
                    items = new Item[0];
                    break;
                }
            case TYPE_OBJECT:
                items = Tuple.mapArray(tuples, t -> {
                    Tuple[] parts = t.splitByToken(Separator.TWO_POINTS);
                    if(parts.length != 2)
                        throw new CompilerError("Malformed " + (type == TYPE_MAP ? "map" : "object") + " literal.");
                    return new Item(parts[0], parts[1]);
                }, new Item[tuples.length]);
                break;
            default:
                throw new IllegalStateException();
        }
    }
    
    public final int getItemCount() { return items.length; }
    public final Item getItem(int index) { return items[index]; }
    
    public final boolean isLiteralArray() { return type == TYPE_ARRAY; }
    public final boolean isLiteralTuple() { return type == TYPE_TUPLE; }
    public final boolean isLiteralMap() { return type == TYPE_MAP; }
    public final boolean isLiteralObject() { return type == TYPE_OBJECT; }
    
    public final boolean isConst() { return type == TYPE_OBJECT && isConst; }
    
    
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
    
    
    public static final MutableLiteral array(Tuple tuple)   throws CompilerError { return new MutableLiteral(tuple,TYPE_ARRAY); }
    public static final MutableLiteral tuple(Tuple tuple)   throws CompilerError { return new MutableLiteral(tuple,TYPE_TUPLE); }
    public static final MutableLiteral map(Tuple tuple)     throws CompilerError { return new MutableLiteral(tuple,TYPE_MAP); }
    public static final MutableLiteral object(Tuple tuple, boolean isConst)  throws CompilerError
    {
        MutableLiteral ml = new MutableLiteral(tuple,TYPE_OBJECT);
        ml.isConst = isConst;
        return ml;
    }
    
    
    public final class Item
    {
        private final ParsedCode key;
        private final ParsedCode value;
        private final boolean constKey;
        
        private Item(Tuple key, Tuple value) throws CompilerError
        {
            if(value == null)
                throw new NullPointerException();
            if(key == null)
            {
                this.key = null;
                this.constKey = false;
            }
            else
            {
                if(key.get(0) == CommandWord.CONST)
                {
                    if(type != TYPE_OBJECT)
                        throw new CompilerError("Invalid const command");
                    this.key = key.subTuple(1).pack();
                    this.constKey = true;
                }
                else
                {
                    this.key = key.pack();
                    this.constKey = false;
                }
            }
            this.value = value.pack();
        }
        
        public final ParsedCode getKey() { return key; }
        public final ParsedCode getValue() { return value; }
        public final boolean isConstKey() { return constKey; }
        
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

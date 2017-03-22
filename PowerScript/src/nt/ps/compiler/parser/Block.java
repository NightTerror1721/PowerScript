/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Asus
 */
public abstract class Block extends CodePart
{
    public abstract int getTupleCount();
    public abstract Tuple getTuple(int index);
    
    public boolean isScope() { return false; }
    public boolean isParenthesis() { return false; }
    public boolean isSquare() { return false; }
    
    public Tuple getFirstTuple() { return getTuple(0); }
    
    @Override
    public final boolean isBlock() { return true; }
    
    @Override
    public final boolean isValidCodeObject() { return isParenthesis(); }
    
    
    public static final Block fromParenthesis(Tuple tuple)
    {
        if(tuple.has(Separator.COMMA))
        {
            Tuple[] tuples = tuple.splitByToken(Separator.COMMA);
            return new DefaultImmutableBlock(tuples,BlockType.TUPLE);
        }
        return new SingleBlock(tuple,BlockType.PARENTHESIS);
    }
    
    public static final Block scope(List<Tuple> tuples)
    {
        if(tuples == null)
            throw new NullPointerException();
        return new DefaultImmutableBlock(tuples.toArray(new Tuple[tuples.size()]),BlockType.SCOPE);
    }
    
    public static final Block object(Tuple tuple)
    {
        Tuple[] tuples = tuple.splitByToken(Separator.COMMA);
        return new DefaultImmutableBlock(tu)
    }
    
    
    private static final class DefaultImmutableBlock extends Block
    {
        private final Tuple[] tuples;
        private final BlockType type;
        
        private DefaultImmutableBlock(Tuple[] tuples, BlockType type)
        {
            this.tuples = tuples;
            this.type = type;
        }
        
        @Override
        public final int getTupleCount() { return tuples.length; }

        @Override
        public Tuple getTuple(int index) { return tuples[index]; }

        @Override
        public final BlockType getBlockType() { return type; }

        @Override
        public String toString() { return Arrays.toString(tuples); }
    }
    
    private static final class SingleBlock extends Block
    {
        private final Tuple tuple;
        private final BlockType type;
        
        private SingleBlock(Tuple tuple, BlockType type)
        {
            this.tuple = tuple;
            this.type = type;
        }
        
        @Override
        public final int getTupleCount() { return 1; }

        @Override
        public Tuple getTuple(int index) { return tuple; }
        
        @Override
        public Tuple getFirstTuple() { return tuple; }

        @Override
        public final BlockType getBlockType() { return type; }

        @Override
        public String toString() { return tuple.toString(); }
    }
}

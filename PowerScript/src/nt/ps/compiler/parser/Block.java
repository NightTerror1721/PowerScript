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
public abstract class Block extends ParsedCode
{
    public abstract int getTupleCount();
    public abstract Tuple getTuple(int index);
    
    public boolean isScope() { return false; }
    public boolean isParenthesis() { return false; }
    public boolean isSquare() { return false; }
    public boolean isArgumentsList() { return false; }
    
    public Tuple getFirstTuple() { return getTuple(0); }
    
    @Override
    public final CodeType getCodeType() { return CodeType.BLOCK; }
    
    @Override
    public final boolean isValidCodeObject() { return isParenthesis(); }
    
    
    public static final Block parenthesis(Tuple tuple) { return new SingleBlock(tuple,false); }
    
    public static final Block square(Tuple tuple) { return new SingleBlock(tuple,true); }
    
    public final static Block arguments(Tuple tuple)
    {
        Tuple[] tuples = tuple.splitByToken(Separator.COMMA);
        return new MultipleBlock(tuples,false);
    }
    
    public static final Block scope(List<Tuple> tuples)
    {
        Tuple[] atuples = tuples.toArray(new Tuple[tuples.size()]);
        return new MultipleBlock(atuples,true);
    }
    
    
    private static final class MultipleBlock extends Block
    {
        private final Tuple[] tuples;
        private final boolean scope;
        
        private MultipleBlock(Tuple[] tuples, boolean scope)
        {
            this.tuples = tuples;
            this.scope = scope;
        }
        
        @Override
        public final int getTupleCount() { return tuples.length; }

        @Override
        public final Tuple getTuple(int index) { return tuples[index]; }

        @Override
        public final boolean isScope() { return scope; }
        
        @Override
        public final boolean isArgumentsList() { return !scope; }

        @Override
        public final String toString() { return Arrays.toString(tuples); }
    }
    
    private static final class SingleBlock extends Block
    {
        private final Tuple tuple;
        private final boolean square;
        
        private SingleBlock(Tuple tuple, boolean square)
        {
            this.tuple = tuple;
            this.square = square;
        }
        
        @Override
        public final int getTupleCount() { return 1; }

        @Override
        public final Tuple getTuple(int index) { return tuple; }
        
        @Override
        public final Tuple getFirstTuple() { return tuple; }
        
        @Override
        public final boolean isParenthesis() { return !square; }
        
        @Override
        public final boolean isSquare() { return square; }

        @Override
        public final String toString() { return tuple.toString(); }
    }
}

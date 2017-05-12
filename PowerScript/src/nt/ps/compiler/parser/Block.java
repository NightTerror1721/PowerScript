/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import nt.ps.compiler.exception.CompilerError;

/**
 *
 * @author Asus
 * @param <C>
 */
public abstract class Block<C extends ParsedCode>
        extends ParsedCode
        implements Iterable<C>
{
    public abstract int getCodeCount();
    public abstract C getCode(int index);
    public abstract ParsedCode[] toArray();
    public abstract ParsedCode[] putInArray(ParsedCode[] array, int offset);
    public abstract void forEach(Consumer<C> consumer) throws CompilerError;
    
    public boolean isScope() { return false; }
    public boolean isParenthesis() { return false; }
    public boolean isSquare() { return false; }
    public boolean isArgumentsList() { return false; }
    
    public C getFirstCode() { return getCode(0); }
    
    @Override
    public final CodeType getCodeType() { return CodeType.BLOCK; }
    
    @Override
    public final boolean isValidCodeObject() { return isParenthesis(); }
    
    
    public static final Block<ParsedCode> parenthesis(Tuple tuple) throws CompilerError { return parenthesis(tuple.pack()); }
    public static final Block<ParsedCode> parenthesis(ParsedCode code) { return new SingleBlock(code,false); }
    
    public static final Block<ParsedCode> square(Tuple tuple) throws CompilerError { return square(tuple.pack()); }
    public static final Block<ParsedCode> square(ParsedCode code) { return new SingleBlock(code,true); }
    
    public final static Block<ParsedCode> arguments(Tuple tuple) throws CompilerError { return arguments(tuple,Separator.COMMA); }
    public final static Block<ParsedCode> arguments(Tuple tuple, Separator separator) throws CompilerError
    {
        Tuple[] tuples = tuple.splitByToken(separator);
        ParsedCode[] codes = new ParsedCode[tuples.length];
        for(int i=0;i<codes.length;i++)
            codes[i] = tuples[i].pack();
        return arguments(codes);
    }
    public final static Block<ParsedCode> arguments(ParsedCode... codes) { return new MultipleBlock(codes); }
    
    public static final Scope scope(List<Command> cmds) { return new Scope(cmds.toArray(new Command[cmds.size()])); }
    public static final Scope scope(Command... cmds) { return new Scope(cmds); }
    
    
    private static class MultipleBlock<C extends ParsedCode> extends Block<C>
    {
        private final C[] codes;
        
        private MultipleBlock(C[] codes)
        {
            this.codes = codes;
        }
        
        @Override
        public final int getCodeCount() { return codes.length; }

        @Override
        public final C getCode(int index) { return codes[index]; }
           
        @Override
        public final ParsedCode[] toArray()
        {
            ParsedCode[] array = new ParsedCode[codes.length];
            System.arraycopy(codes,0,array,0,array.length);
            return array;
        }
        
        @Override
        public final ParsedCode[] putInArray(ParsedCode[] array, int offset)
        {
            System.arraycopy(array,offset,codes,0,codes.length);
            return array;
        }
        
        @Override
        public final void forEach(Consumer<C> consumer) throws CompilerError
        {
            for(int i=0;i<codes.length;i++)
                consumer.apply(codes[i], i);
        }
        
        @Override
        public final Iterator<C> iterator()
        {
            return new Iterator<C>()
            {
                private int it = 0;
                
                @Override
                public final boolean hasNext() { return it < codes.length; }

                @Override
                public final C next() { return codes[it++]; }
            };
        }

        @Override
        public boolean isScope() { return false; }
        
        @Override
        public boolean isArgumentsList() { return true; }

        @Override
        public final String toString() { return Arrays.toString(codes); }
    }
    
    public static final class Scope extends MultipleBlock<Command>
    {
        private Scope(Command[] codes)
        {
            super(codes);
        }
        
        @Override
        public final boolean isScope() { return true; }
        
        @Override
        public final boolean isArgumentsList() { return false; }
    }
    
    private static final class SingleBlock extends Block<ParsedCode>
    {
        private final ParsedCode code;
        private final boolean square;
        
        private SingleBlock(ParsedCode code, boolean square)
        {
            this.code = code;
            this.square = square;
        }
        
        @Override
        public final int getCodeCount() { return 1; }

        @Override
        public final ParsedCode getCode(int index) { return code; }
        
        @Override
        public final ParsedCode[] toArray() { return new ParsedCode[]{ code }; }
        
        @Override
        public final ParsedCode[] putInArray(ParsedCode[] array, int offset)
        {
            array[offset] = code;
            return array;
        }
        
        @Override
        public final void forEach(Consumer<ParsedCode> consumer) throws CompilerError
        {
            consumer.apply(code, 1);
        }
        
        @Override
        public final Iterator<ParsedCode> iterator()
        {
            return new Iterator<ParsedCode>()
            {
                private int it = 0;
                
                @Override
                public final boolean hasNext() { return it < 1; }

                @Override
                public final ParsedCode next()
                {
                    it++;
                    return code;
                }
            };
        }
        
        @Override
        public final ParsedCode getFirstCode() { return code; }
        
        @Override
        public final boolean isParenthesis() { return !square; }
        
        @Override
        public final boolean isSquare() { return square; }

        @Override
        public final String toString() { return code.toString(); }
    }
    
    @FunctionalInterface
    public interface Consumer<T> { void apply(T obj, int index) throws CompilerError ; }
}

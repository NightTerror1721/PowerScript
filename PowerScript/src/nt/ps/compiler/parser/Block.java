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
 * @param <C>
 */
public abstract class Block<C extends ParsedCode> extends ParsedCode
{
    public abstract int getCodeCount();
    public abstract C getCode(int index);
    
    public boolean isScope() { return false; }
    public boolean isParenthesis() { return false; }
    public boolean isSquare() { return false; }
    public boolean isArgumentsList() { return false; }
    
    public C getFirstCode() { return getCode(0); }
    
    @Override
    public final CodeType getCodeType() { return CodeType.BLOCK; }
    
    @Override
    public final boolean isValidCodeObject() { return isParenthesis(); }
    
    
    public static final Block<ParsedCode> parenthesis(Tuple tuple) { return parenthesis(tuple.pack()); }
    public static final Block<ParsedCode> parenthesis(ParsedCode code) { return new SingleBlock(code,false); }
    
    public static final Block<ParsedCode> square(Tuple tuple) { return square(tuple.pack()); }
    public static final Block<ParsedCode> square(ParsedCode code) { return new SingleBlock(code,true); }
    
    public final static Block<ParsedCode> arguments(Tuple tuple) { return arguments(tuple,Separator.COMMA); }
    public final static Block<ParsedCode> arguments(Tuple tuple, Separator separator)
    {
        Tuple[] tuples = tuple.splitByToken(separator);
        return arguments(Arrays.stream(tuples).map(t -> t.pack()).toArray(size -> new ParsedCode[size]));
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
        public final ParsedCode getFirstCode() { return code; }
        
        @Override
        public final boolean isParenthesis() { return !square; }
        
        @Override
        public final boolean isSquare() { return square; }

        @Override
        public final String toString() { return code.toString(); }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 *
 * @author Asus
 */
public abstract class OperatorSymbol extends Code
{
    private final String symbol;
    private final Type type;
    private final int priority;
    
    private OperatorSymbol(String symbol, Type type, int priority)
    {
        this.symbol = symbol;
        this.type = type;
        this.priority = priority;
    }
    
    @Override
    public final CodeType getCodeType() { return CodeType.OPERATOR_SYMBOL; }
    
    @Override
    public final String toString() { return symbol; }
    
    public final String getSymbol() { return symbol; }
    
    public int getPriority() { return priority; }
    
    public boolean isUnary() { return false; }
    public boolean isBinary() { return false; }
    public boolean isTernary() { return false; }
    public boolean isCall() { return false; }
    public boolean isNew() { return false; }
    
    public boolean canBeBothUnaryOrder() { return false; }
    
    public final int comparePriority(OperatorSymbol os)
    {
        int cmp = Integer.compare(type.ordinal(), os.type.ordinal());
        if(cmp != 0)
            return cmp;
        return Integer.compare(priority,os.priority);
    }
    
    
    public static final OperatorSymbol
            PROPERTY_ACCESS = new BinaryOperatorSymbol(".",14),
            ACCESS = new BinaryOperatorSymbol("[]",14),
            NEW = new NewOperatorSymbol("new",14),
            
            CALL = new CallOperatorSymbol("()",13),
            
            NEGATE = new UnaryOperatorSymbol("!",12),
            LOGIC_NOT = new UnaryOperatorSymbol("~",12),
            NEGATIVE = new UnaryOperatorSymbol("-",12),
            INCREMENT = new UnaryOperatorSymbol("++",12,true),
            DECREMENT = new UnaryOperatorSymbol("--",12,true),
            TYPEOF = new UnaryOperatorSymbol("typeof",12),
            
            MULTIPLY = new BinaryOperatorSymbol("*",11),
            DIVIDE = new BinaryOperatorSymbol("/",11),
            MODULE = new BinaryOperatorSymbol("%",11),
            
            PLUS = new BinaryOperatorSymbol("+",10),
            MINUS = new BinaryOperatorSymbol("-",10),
            
            SHIFT_LEFT = new BinaryOperatorSymbol("<<",9),
            SHIFT_RIGHT = new BinaryOperatorSymbol(">>",9),
            
            LESS_THAN = new BinaryOperatorSymbol("<",8),
            LESS_THAN_EQUALS = new BinaryOperatorSymbol("<=",8),
            GREATER_THAN = new BinaryOperatorSymbol(">",8),
            GREATER_THAN_EQUALS = new BinaryOperatorSymbol(">=",8),
            CONTAINS = new BinaryOperatorSymbol("in",8),
            INSTANCEOF = new BinaryOperatorSymbol("instanceof",8),
            
            EQUALS = new BinaryOperatorSymbol("==",7),
            NOT_EQUALS = new BinaryOperatorSymbol("!=",7),
            EQUALS_REFERENCE = new BinaryOperatorSymbol("===",7),
            NOT_EQUALS_REFERENCE = new BinaryOperatorSymbol("!==",7),
            
            LOGIC_AND = new BinaryOperatorSymbol("&",6),
            
            LOGIC_XOR = new BinaryOperatorSymbol("^",5),
            
            LOGIC_OR = new BinaryOperatorSymbol("|",4),
            
            STRING_CONCAT = new BinaryOperatorSymbol("..",3),
            
            AND = new BinaryOperatorSymbol("&&",2),
            
            OR = new BinaryOperatorSymbol("||",1),
            
            TERNARY_CONDITION = new TernaryOperatorSymbol("?",0);
    
    
    private static final HashMap<String, OperatorSymbol[]> HASH;
    static {
        HASH = new HashMap<>();
        for(Field field : OperatorSymbol.class.getDeclaredFields())
        {
            if(field.getType() != OperatorSymbol.class || !Modifier.isStatic(field.getModifiers()))
                continue;
            try
            {
                OperatorSymbol cp = (OperatorSymbol) field.get(null);
                OperatorSymbol[] array = HASH.get(cp.symbol);
                if(array == null)
                {
                    array = new OperatorSymbol[2];
                    HASH.put(cp.symbol,array);
                }
                array[cp.isUnary()? 1 : 0] = cp;
            }
            catch(IllegalAccessException | IllegalArgumentException ex)
            {
                ex.printStackTrace(System.err);
            }
        }
    }
    
    public static final boolean isOperator(String str) { return HASH.containsKey(str); }
    public static final boolean isOperator(char c) { return isOperator(String.valueOf(c)); }
    
    public static final OperatorSymbol getOperator(String str, boolean isUnary)
    {
        OperatorSymbol[] array = HASH.get(str);
        if(array == null)
            return null;
        return array[isUnary ? 1 : 0];
    }
    public static final OperatorSymbol getOperator(char c, boolean isUnary) { return getOperator(String.valueOf(c),isUnary); }
    
    
    private enum Order { LEFT, RIGHT, BOTH }
    private enum Type { TERNARY, BINARY, UNARY, CALL, NEW; }
    
    private static final class UnaryOperatorSymbol extends OperatorSymbol
    {
        private final boolean both;
        
        public UnaryOperatorSymbol(String symbol, int priority, boolean canBeBothOrder)
        {
            super(symbol, Type.UNARY, priority);
            this.both = canBeBothOrder;
        }
        public UnaryOperatorSymbol(String symbol, int priority) { this(symbol,priority,false); }
        
        @Override
        public final boolean isUnary() { return true; }
        
        @Override
        public final boolean canBeBothUnaryOrder() { return both; }
    }
    
    private static final class BinaryOperatorSymbol extends OperatorSymbol
    {
        public BinaryOperatorSymbol(String symbol, int priority)
        {
            super(symbol, Type.BINARY, priority);
        }
        
        @Override
        public final boolean isBinary() { return true; }
    }
    
    private static final class TernaryOperatorSymbol extends OperatorSymbol
    {
        public TernaryOperatorSymbol(String symbol, int priority)
        {
            super(symbol, Type.TERNARY, priority);
        }
        
        @Override
        public final boolean isTernary() { return true; }
    }
    
    private static class CallOperatorSymbol extends OperatorSymbol
    {
        public CallOperatorSymbol(String symbol, int priority)
        {
            super(symbol, Type.CALL, priority);
        }
        
        @Override
        public final boolean isCall() { return true; }
    }
    
    private static class NewOperatorSymbol extends OperatorSymbol
    {
        public NewOperatorSymbol(String symbol, int priority)
        {
            super(symbol, Type.NEW, priority);
        }
        
        @Override
        public final boolean isNew() { return true; }
    }
}

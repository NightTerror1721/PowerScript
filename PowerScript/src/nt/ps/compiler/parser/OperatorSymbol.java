/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.HashMap;

/**
 *
 * @author Asus
 */
public abstract class OperatorSymbol extends CodePart
{
    private final String symbol;
    
    private OperatorSymbol(String symbol)
    {
        this.symbol = symbol;
    }
    
    @Override
    public final String toString() { return symbol; }
    
    public final String getSymbol() { return symbol; }
    
    public int getUnaryPriority() { throw new UnsupportedOperationException(); }
    public int getBinaryPriority() { throw new UnsupportedOperationException(); }
    public int getTernaryPriority() { throw new UnsupportedOperationException(); }
    public int getArgumentPriority() { throw new UnsupportedOperationException(); }
    
    public boolean canBeUnary() { return false; }
    public boolean canBeBinary() { return false; }
    public boolean canBeTernary() { return false; }
    public boolean canBeArgument() { return false; }
    
    public boolean hasUnaryLeftOrder() { throw new UnsupportedOperationException(); }
    public boolean hasUnaryRightOrder() { throw new UnsupportedOperationException(); }
    public boolean hasBinaryLeftOrder() { throw new UnsupportedOperationException(); }
    public boolean hasBinaryRightOrder() { throw new UnsupportedOperationException(); }
    public boolean hasTernaryLeftOrder() { throw new UnsupportedOperationException(); }
    public boolean hasTernaryRightOrder() { throw new UnsupportedOperationException(); }
    public final boolean hasArgumentLeftOrder() { return true; }
    public final boolean hasArgumentRightOrder() { return false; }
    
    
    public static final OperatorSymbol
            PROPERTY_ACCESS = new BinaryOperatorSymbol(".",Order.LEFT,14),
            ACCESS = new ArgumentsOperatorSymbol("[]",14),
            NEW = new ArgumentsOperatorSymbol("new",14),
            
            CALL = new ArgumentsOperatorSymbol("()",13),
            
            NEGATE = new UnaryOperatorSymbol("!",Order.LEFT,12),
            LOGIC_NOT = new UnaryOperatorSymbol("~",Order.LEFT,12),
            NEGATIVE = new UnaryOperatorSymbol("-",Order.LEFT,12),
            INCREMENT = new UnaryOperatorSymbol("++",Order.BOTH,12),
            DECREMENT = new UnaryOperatorSymbol("--",Order.BOTH,12),
            TYPEOF = new UnaryOperatorSymbol("typeof",Order.LEFT,12),
            
            MULTIPLY = new BinaryOperatorSymbol("*",Order.LEFT,11),
            DIVIDE = new BinaryOperatorSymbol("/",Order.LEFT,11),
            MODULE = new BinaryOperatorSymbol("%",Order.LEFT,11),
            
            PLUS = new BinaryOperatorSymbol("+",Order.LEFT,10),
            MINUS = new UnaryBinaryOperatorSymbol("-",Order.LEFT,10, Order.LEFT,12),
            
            SHIFT_LEFT = new BinaryOperatorSymbol("<<",Order.LEFT,9),
            SHIFT_RIGHT = new BinaryOperatorSymbol(">>",Order.LEFT,9),
            
            LESS_THAN = new BinaryOperatorSymbol("<",Order.LEFT,8),
            LESS_THAN_EQUALS = new BinaryOperatorSymbol("<=",Order.LEFT,8),
            GREATER_THAN = new BinaryOperatorSymbol(">",Order.LEFT,8),
            GREATER_THAN_EQUALS = new BinaryOperatorSymbol(">=",Order.LEFT,8),
            CONTAINS = new BinaryOperatorSymbol("in",Order.LEFT,8),
            INSTANCEOF = new BinaryOperatorSymbol("instanceof",Order.LEFT,8),
            
            EQUALS = new BinaryOperatorSymbol("==",Order.LEFT,7),
            NOT_EQUALS = new BinaryOperatorSymbol("!=",Order.LEFT,7),
            EQUALS_REFERENCE = new BinaryOperatorSymbol("===",Order.LEFT,7),
            NOT_EQUALS_REFERENCE = new BinaryOperatorSymbol("!==",Order.LEFT,7),
            
            LOGIC_AND = new BinaryOperatorSymbol("&",Order.LEFT,6),
            
            LOGIC_XOR = new BinaryOperatorSymbol("^",Order.LEFT,5),
            
            LOGIC_OR = new BinaryOperatorSymbol("|",Order.LEFT,4),
            
            STRING_CONCAT = new BinaryOperatorSymbol("..",Order.LEFT,3),
            
            AND = new BinaryOperatorSymbol("&&",Order.LEFT,2),
            
            OR = new BinaryOperatorSymbol("||",Order.LEFT,1),
            
            TERNARY_CONDITION = new TernaryOperatorSymbol("?",Order.RIGHT,0);
    
    
    private static final HashMap<String, OperatorSymbol> HASH = collect(OperatorSymbol.class,os -> os.symbol);
    
    public static final boolean isOperator(String str) { return HASH.containsKey(str); }
    public static final boolean isOperator(char c) { return isOperator(String.valueOf(c)); }
    
    public static final OperatorSymbol getOperator(String str) { return HASH.get(str); }
    public static final OperatorSymbol getOperator(char c) { return getOperator(String.valueOf(c)); }
    
    
    private enum Order { LEFT, RIGHT, BOTH }
    
    private static final class UnaryOperatorSymbol extends OperatorSymbol
    {
        private final int priority;
        private final Order order;
        
        public UnaryOperatorSymbol(String symbol, Order order, int priority)
        {
            super(symbol);
            this.priority = priority;
            this.order = order;
        }
        
        @Override
        public final int getUnaryPriority() { return priority; }
        
        @Override
        public final boolean canBeUnary() { return true; }
        
        @Override
        public boolean hasUnaryLeftOrder() { return order == Order.LEFT || order == Order.BOTH; }
        @Override
        public boolean hasUnaryRightOrder() { return order == Order.RIGHT || order == Order.BOTH; }
    }
    
    private static final class BinaryOperatorSymbol extends OperatorSymbol
    {
        private final int priority;
        private final Order order;
        
        public BinaryOperatorSymbol(String symbol, Order order, int priority)
        {
            super(symbol);
            this.priority = priority;
            this.order = order;
        }
        
        @Override
        public final int getBinaryPriority() { return priority; }
        
        @Override
        public final boolean canBeBinary() { return true; }
        
        @Override
        public boolean hasBinaryLeftOrder() { return order == Order.LEFT || order == Order.BOTH; }
        @Override
        public boolean hasBinaryRightOrder() { return order == Order.RIGHT || order == Order.BOTH; }
    }
    
    private static final class UnaryBinaryOperatorSymbol extends OperatorSymbol
    {
        private final int unaryPriority, binaryPriority;
        private final Order unaryOrder, binaryOrder;
        
        public UnaryBinaryOperatorSymbol(String symbol, Order unaryOrder, int unaryPriority, Order binaryOrder, int binaryPriority)
        {
            super(symbol);
            this.unaryPriority = unaryPriority;
            this.unaryOrder = unaryOrder;
            this.binaryPriority = binaryPriority;
            this.binaryOrder = binaryOrder;
        }
        
        @Override
        public final int getUnaryPriority() { return unaryPriority; }
        @Override
        public final int getBinaryPriority() { return binaryPriority; }
        
        @Override
        public final boolean canBeUnary() { return true; }
        @Override
        public final boolean canBeBinary() { return true; }
        
        @Override
        public boolean hasUnaryLeftOrder() { return unaryOrder == Order.LEFT || unaryOrder == Order.BOTH; }
        @Override
        public boolean hasUnaryRightOrder() { return unaryOrder == Order.RIGHT || unaryOrder == Order.BOTH; }
        @Override
        public boolean hasBinaryLeftOrder() { return binaryOrder == Order.LEFT || binaryOrder == Order.BOTH; }
        @Override
        public boolean hasBinaryRightOrder() { return binaryOrder == Order.RIGHT || binaryOrder == Order.BOTH; }
    }
    
    private static final class TernaryOperatorSymbol extends OperatorSymbol
    {
        private final int priority;
        private final Order order;
        
        public TernaryOperatorSymbol(String symbol, Order order, int priority)
        {
            super(symbol);
            this.priority = priority;
            this.order = order;
        }
        
        @Override
        public final int getTernaryPriority() { return priority; }
        
        @Override
        public final boolean canBeTernary() { return true; }
        
        @Override
        public boolean hasTernaryLeftOrder() { return order == Order.LEFT || order == Order.BOTH; }
        @Override
        public boolean hasTernaryRightOrder() { return order == Order.RIGHT || order == Order.BOTH; }
    }
    
    private static class ArgumentsOperatorSymbol extends OperatorSymbol
    {
        private final int priority;
        
        public ArgumentsOperatorSymbol(String symbol, int priority)
        {
            super(symbol);
            this.priority = priority;
        }
        
        @Override
        public final int getArgumentPriority() { return priority; }
        
        @Override
        public final boolean canBeArgument() { return true; }
    }
}

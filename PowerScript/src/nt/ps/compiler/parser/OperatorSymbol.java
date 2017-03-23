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
public final class OperatorSymbol extends CodePart
{
    private final String symbol;
    private final int priority;
    private final int type;
    
    private OperatorSymbol(String symbol, int type, int priority)
    {
        this.symbol = symbol;
        this.priority = priority;
        this.type = type;
    }
    
    @Override
    public final String toString() { return symbol; }
    
    public final String getSymbol() { return symbol; }
    public final int getPriority() { return priority; }
    
    public final boolean isUnary() { return type == UNARY_LEFT || type == UNARY_RIGHT; }
    public final boolean isBinary() { return type == BINARY_LEFT_TO_RIGHT || type == BINARY_RIGHT_TO_LEFT; }
    public final boolean isTernaryConditional() { return type == TERNARY_CONDITIONAL; }
    public final boolean isUnaryLeft() { return type == UNARY_LEFT; }
    public final boolean isUnaryRight() { return type == UNARY_RIGHT; }
    public final boolean isBinaryLeftToRight() { return type == BINARY_LEFT_TO_RIGHT; }
    public final boolean isBinaryRightToLeft() { return type == BINARY_RIGHT_TO_LEFT; }
    
    
    private static final int UNARY_LEFT = 0x1;
    private static final int UNARY_RIGHT = 0x2;
    private static final int BINARY_LEFT_TO_RIGHT = 0x4;
    private static final int BINARY_RIGHT_TO_LEFT = 0x8;
    private static final int TERNARY_CONDITIONAL = 0x10;
    
    
    public static final OperatorSymbol
            NEGATE = new OperatorSymbol("*",BINARY_LEFT_TO_RIGHT,12),
            LOGIC_NOT = new OperatorSymbol("~",BINARY_LEFT_TO_RIGHT,12),
            
            MULTIPLY = new OperatorSymbol("*",BINARY_LEFT_TO_RIGHT,11),
            DIVIDE = new OperatorSymbol("/",BINARY_LEFT_TO_RIGHT,11),
            MODULE = new OperatorSymbol("%",BINARY_LEFT_TO_RIGHT,11),
            
            PLUS = new OperatorSymbol("+",BINARY_LEFT_TO_RIGHT,10),
            MINUS = new OperatorSymbol("-",BINARY_LEFT_TO_RIGHT,10),
            
            SHIFT_LEFT = new OperatorSymbol("<<",BINARY_LEFT_TO_RIGHT,9),
            SHIFT_RIGHT = new OperatorSymbol(">>",BINARY_LEFT_TO_RIGHT,9),
            
            LESS_THAN = new OperatorSymbol("<",BINARY_LEFT_TO_RIGHT,8),
            LESS_THAN_EQUALS = new OperatorSymbol("<=",BINARY_LEFT_TO_RIGHT,8),
            GREATER_THAN = new OperatorSymbol(">",BINARY_LEFT_TO_RIGHT,8),
            GREATER_THAN_EQUALS = new OperatorSymbol(">=",BINARY_LEFT_TO_RIGHT,8),
            CONTAINS = new OperatorSymbol("in",BINARY_LEFT_TO_RIGHT,8),
            INSTANCEOF = new OperatorSymbol("instanceof",BINARY_LEFT_TO_RIGHT,8),
            
            EQUALS = new OperatorSymbol("==",BINARY_LEFT_TO_RIGHT,7),
            NOT_EQUALS = new OperatorSymbol("!=",BINARY_LEFT_TO_RIGHT,7),
            EQUALS_REFERENCE = new OperatorSymbol("===",BINARY_LEFT_TO_RIGHT,7),
            NOT_EQUALS_REFERENCE = new OperatorSymbol("!==",BINARY_LEFT_TO_RIGHT,7),
            
            LOGIC_AND = new OperatorSymbol("&",BINARY_LEFT_TO_RIGHT,6),
            
            LOGIC_XOR = new OperatorSymbol("^",BINARY_LEFT_TO_RIGHT,5),
            
            LOGIC_OR = new OperatorSymbol("|",BINARY_LEFT_TO_RIGHT,4),
            
            STRING_CONCAT = new OperatorSymbol("..",BINARY_LEFT_TO_RIGHT,3),
            
            AND = new OperatorSymbol("&&",BINARY_LEFT_TO_RIGHT,2),
            
            OR = new OperatorSymbol("||",BINARY_LEFT_TO_RIGHT,1),
            
            TERNARY_CONDITION = new OperatorSymbol("?",TERNARY_CONDITIONAL,0);
    
    
    private static final HashMap<String, OperatorSymbol> HASH = collect(OperatorSymbol.class,os -> os.symbol);
    
    public static final boolean isOperator(String str) { return HASH.containsKey(str); }
    public static final boolean isOperator(char c) { return isOperator(String.valueOf(c)); }
    
    public static final OperatorSymbol getOperator(String str) { return HASH.get(str); }
    public static final OperatorSymbol getOperator(char c) { return getOperator(String.valueOf(c)); }
    
}

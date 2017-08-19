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
    private final String functionName;
    
    private OperatorSymbol(String symbol, Type type, int priority, String functionName)
    {
        this.symbol = symbol;
        this.type = type;
        this.priority = priority;
        this.functionName = functionName;
    }
    
    @Override
    public final CodeType getCodeType() { return CodeType.OPERATOR_SYMBOL; }
    
    @Override
    public final String toString() { return symbol; }
    
    public final String getSymbol() { return symbol; }
    
    public int getPriority() { return priority; }
    
    public final boolean hasAssociatedFunction() { return functionName != null; }
    public final String getAssociatedFunction() { return functionName; }
    
    public boolean isUnary() { return false; }
    public boolean isBinary() { return false; }
    public boolean isTernary() { return false; }
    public boolean isCall() { return false; }
    public boolean isInvoke() { return false; }
    public boolean isNew() { return false; }
    public boolean isFunction() { return false; }
    public boolean isExtends() { return false; }
    
    public final boolean isCallable() { return isCall() || isInvoke(); }
    
    public boolean canBeBothUnaryOrder() { return false; }
    
    public final int comparePriority(OperatorSymbol os)
    {
        int cmp = type.comparePriority(os.type);
        if(cmp != 0)
            return cmp;
        return Integer.compare(priority,os.priority);
    }
    
    
    public static final OperatorSymbol
            PROPERTY_ACCESS = new BinaryOperatorSymbol(".",14,null),
            ACCESS = new BinaryOperatorSymbol("[]",14,null),
            NEW = new NewOperatorSymbol("new",14,null),
            INVOKE = new InvokeOperatorSymbol(".()",14,null),
            CALL = new CallOperatorSymbol("()",14,null),
            
            FUNCTION = new FunctionOperatorSymbol("function",13,null),
            EXTENDS = new ExtendsOperatorSymbol("extends",13,null),
            
            NEGATE = new UnaryOperatorSymbol("!",12,"negate"),
            LOGIC_NOT = new UnaryOperatorSymbol("~",12,"logicNot"),
            NEGATIVE = new UnaryOperatorSymbol("-",12,"negative"),
            INCREMENT = new UnaryOperatorSymbol("++",12,true,"increase"),
            DECREMENT = new UnaryOperatorSymbol("--",12,true,"decrease"),
            TYPEOF = new UnaryOperatorSymbol("typeof",12,null),
            IMPORT = new UnaryOperatorSymbol("import",12,null),
            
            MULTIPLY = new BinaryOperatorSymbol("*",11,"multiply"),
            DIVIDE = new BinaryOperatorSymbol("/",11,"divide"),
            MODULE = new BinaryOperatorSymbol("%",11,"module"),
            
            PLUS = new BinaryOperatorSymbol("+",10,"plus"),
            MINUS = new BinaryOperatorSymbol("-",10,"minus"),
            
            SHIFT_LEFT = new BinaryOperatorSymbol("<<",9,"shiftLeft"),
            SHIFT_RIGHT = new BinaryOperatorSymbol(">>",9,"shiftRight"),
            
            LESS_THAN = new BinaryOperatorSymbol("<",8,"smallerThan"),
            LESS_THAN_EQUALS = new BinaryOperatorSymbol("<=",8,"smallerOrEqualsThan"),
            GREATER_THAN = new BinaryOperatorSymbol(">",8,"greaterThan"),
            GREATER_THAN_EQUALS = new BinaryOperatorSymbol(">=",8,"greaterOrEqualsThan"),
            CONTAINS = new BinaryOperatorSymbol("in",8,"contains"),
            INSTANCEOF = new BinaryOperatorSymbol("instanceof",8,null),
            
            EQUALS = new BinaryOperatorSymbol("==",7,"equals"),
            NOT_EQUALS = new BinaryOperatorSymbol("!=",7,"notEquals"),
            EQUALS_REFERENCE = new BinaryOperatorSymbol("===",7,null),
            NOT_EQUALS_REFERENCE = new BinaryOperatorSymbol("!==",7,null),
            
            LOGIC_AND = new BinaryOperatorSymbol("&",6,"logicAnd"),
            
            LOGIC_XOR = new BinaryOperatorSymbol("^",5,"logicXor"),
            
            LOGIC_OR = new BinaryOperatorSymbol("|",4,"logicOr"),
            
            STRING_CONCAT = new BinaryOperatorSymbol("..",3,"concat"),
            
            AND = new BinaryOperatorSymbol("&&",2,null),
            
            OR = new BinaryOperatorSymbol("||",1,null),
            
            TERNARY_CONDITION = new TernaryOperatorSymbol("?",0,null);
    
    
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
                array[cp.isUnary() || cp.isNew() || cp.isFunction() || cp.isExtends() ? 1 : 0] = cp;
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
    private enum Type
    {
        TERNARY(0),
        BINARY(1),
        UNARY(2),
        CALL(1),
        INVOKE(1),
        NEW(1),
        FUNCTION(3),
        EXTENDS(3);
        
        private int priority;
        private Type(int priority) { this.priority = priority; }
        
        private int comparePriority(Type other)
        {
            return Integer.compare(priority, other.priority);
        }
    }
    
    private static final class UnaryOperatorSymbol extends OperatorSymbol
    {
        private final boolean both;
        
        public UnaryOperatorSymbol(String symbol, int priority, boolean canBeBothOrder, String functionName)
        {
            super(symbol, Type.UNARY, priority, functionName);
            this.both = canBeBothOrder;
        }
        public UnaryOperatorSymbol(String symbol, int priority, String functionName) { this(symbol, priority, false, functionName); }
        
        @Override
        public final boolean isUnary() { return true; }
        
        @Override
        public final boolean canBeBothUnaryOrder() { return both; }
    }
    
    private static final class BinaryOperatorSymbol extends OperatorSymbol
    {
        public BinaryOperatorSymbol(String symbol, int priority, String functionName)
        {
            super(symbol, Type.BINARY, priority, functionName);
        }
        
        @Override
        public final boolean isBinary() { return true; }
    }
    
    private static final class TernaryOperatorSymbol extends OperatorSymbol
    {
        public TernaryOperatorSymbol(String symbol, int priority, String functionName)
        {
            super(symbol, Type.TERNARY, priority, functionName);
        }
        
        @Override
        public final boolean isTernary() { return true; }
    }
    
    private static class InvokeOperatorSymbol extends OperatorSymbol
    {
        public InvokeOperatorSymbol(String symbol, int priority, String functionName)
        {
            super(symbol, Type.INVOKE, priority, functionName);
        }
        
        @Override
        public final boolean isInvoke() { return true; }
    }
    
    private static class CallOperatorSymbol extends OperatorSymbol
    {
        public CallOperatorSymbol(String symbol, int priority, String functionName)
        {
            super(symbol, Type.CALL, priority, functionName);
        }
        
        @Override
        public final boolean isCall() { return true; }
    }
    
    private static class NewOperatorSymbol extends OperatorSymbol
    {
        public NewOperatorSymbol(String symbol, int priority, String functionName)
        {
            super(symbol, Type.NEW, priority, functionName);
        }
        
        @Override
        public final boolean isNew() { return true; }
    }
    
    private static class FunctionOperatorSymbol extends OperatorSymbol
    {
        public FunctionOperatorSymbol(String symbol, int priority, String functionName)
        {
            super(symbol, Type.FUNCTION, priority, functionName);
        }
        
        @Override
        public final boolean isFunction() { return true; }
    }
    
    private static class ExtendsOperatorSymbol extends OperatorSymbol
    {
        public ExtendsOperatorSymbol(String symbol, int priority, String functionName)
        {
            super(symbol, Type.EXTENDS, priority, functionName);
        }
        
        @Override
        public final boolean isExtends() { return true; }
    }
}

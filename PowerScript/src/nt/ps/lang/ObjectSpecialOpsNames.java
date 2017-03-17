/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang;

/**
 *
 * @author mpasc
 */
public final class ObjectSpecialOpsNames
{
    private ObjectSpecialOpsNames() {}
    
    public static final String TO_NUMBER = "toNumber";
    public static final String TO_BOOLEAN = "toBoolean";
    public static final String TO_STRING = "toString";
    public static final String TO_ARRAY = "toArray";
    public static final String TO_MAP = "toMap";
    
    public static final String OPERATOR_PLUS = "boperator::+";
    public static final String OPERATOR_MINUS = "boperator::-";
    public static final String OPERATOR_MULTIPLY = "boperator::*";
    public static final String OPERATOR_DIVIDE = "boperator::/";
    public static final String OPERATOR_MODULE = "boperator::%";
    public static final String OPERATOR_NEGATIVE = "uoperator::-";
    public static final String OPERATOR_INCREASE = "uoperator::++";
    public static final String OPERATOR_DECREASE = "uoperator::--";
    
    public static final String OPERATOR_SHIFT_LEFT = "boperator::<<";
    public static final String OPERATOR_SHIFT_RIGHT = "boperator::>>";
    public static final String OPERATOR_LOGIC_AND = "boperator::&";
    public static final String OPERATOR_LOGIC_OR = "boperator::|";
    public static final String OPERATOR_LOGIC_NOT = "uoperator::~";
    public static final String OPERATOR_LOGIC_XOR = "boperator::^";
    
    public static final String OPERATOR_EQUALS = "boperator::==";
    public static final String OPERATOR_NOTEQUALS = "boperator::!=";
    public static final String OPERATOR_GREATER = "boperator::>";
    public static final String OPERATOR_SMALLER = "boperator::<";
    public static final String OPERATOR_GREATER_EQUALS = "boperator::>=";
    public static final String OPERATOR_SMALLER_EQUALS = "boperator::<=";
    public static final String OPERATOR_NEGATE = "uoperator::!";
    
    public static final String CONTAINS = "contains";
    
    public static final String OPERATOR_SET = "toperator::[]=";
    public static final String OPERATOR_GET = "boperator::[]";
    
    public static final String OPERATOR_CALL = "operator::()";
    
    public static final String ITERATOR = "iterator";
    
    public static final String OPERATOR_NEW = "operator::new";
    
    public static final String HASH_CODE = "hashCode";
}

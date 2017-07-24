/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.builder;

/**
 *
 * @author Asus
 */
public interface Literal extends Statement
{
    public static final Literal NULL = () -> "null";
    public static final Literal TRUE = () -> "true";
    public static final Literal FALSE = () -> "false";
    public static final Literal MINUSONE = () -> "-1";
    public static final Literal ZERO = () -> "0";
    public static final Literal ONE = () -> "1";
    public static final Literal INFINITE = () -> "Infinite";
    public static final Literal NAN = () -> "NaN";
    public static final Literal EMPTY_STRING = () -> "\"\"";
    public static final Literal EMPTY_LIST = () -> "[]";
    public static final Literal EMPTY_TUPLE = () -> "Tuple()";
    public static final Literal EMPTY_MAP = () -> "[:]";
    public static final Literal EMPTY_OBJECT = () -> "{}";
    
    @Override
    default String toCodeWrapped() { return toCode(); }
}

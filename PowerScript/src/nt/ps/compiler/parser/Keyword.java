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
public final class Keyword extends CodePart
{
    private final String name;
    
    private Keyword(String name)
    {
        if(name == null)
            throw new NullPointerException();
        if(name.isEmpty())
            throw new IllegalArgumentException();
        this.name = name;
    }
    
    @Override
    public final String toString() { return name; }
    
    @Override
    public final boolean isKeyword() { return true; }
    
    public static final Keyword
            VAR = new Keyword("var"),
            FUNCTION = new Keyword("function"),
            IF = new Keyword("if"),
            ELSE = new Keyword("else"),
            WHILE = new Keyword("while"),
            FOR = new Keyword("for"),
            SWITCH = new Keyword("switch"),
            RETURN = new Keyword("return"),
            CONTINUE = new Keyword("continue"),
            BREAK = new Keyword("break"),
            GLOBAL = new Keyword("global"),
            OPERATOR = new Keyword("operator"),
            SELF = new Keyword("self"),
            TRY = new Keyword("try"),
            CATCH = new Keyword("catch"),
            THROW = new Keyword("throw"),
            CONST = new Keyword("const");
    
    private static final HashMap<String, Keyword> HASH = collect(Keyword.class, k -> k.name);
    
    public static final boolean isKeyword(String str) { return HASH.containsKey(str); }
}

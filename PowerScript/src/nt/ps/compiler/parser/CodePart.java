/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @author Asus
 */
public abstract class CodePart
{
    @Override
    public abstract String toString();

    @Override
    public final int hashCode()
    {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(toString());
        return hash;
    }
    
    public boolean isWord() { return false; }
    public boolean isKeyword() { return false; }
    public boolean isLiteral() { return false; }
    public boolean isMutableLiteral() { return false; }
    public boolean isSeparator() { return false; }
    public boolean isBlock() { return false; }
    
    public boolean isValidCodeObject() { return false; }
    
    
    static final <CP extends CodePart> HashMap<String, CP> collect(Class<CP> clazz, Function<CP, String> collector)
    {
        HashMap<String, CP> map = new HashMap<>();
        for(Field field : clazz.getDeclaredFields())
        {
            if(field.getType() != clazz || !Modifier.isStatic(field.getModifiers()))
                continue;
            try
            {
                CP cp = (CP) field.get(null);
                map.put(collector.apply(cp),cp);
            }
            catch(IllegalAccessException | IllegalArgumentException ex)
            {
                ex.printStackTrace(System.err);
            }
        }
        
        return map;
    }
}

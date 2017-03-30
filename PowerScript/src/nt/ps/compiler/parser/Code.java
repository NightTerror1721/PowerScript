/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.function.Function;

/**
 *
 * @author Asus
 */
public abstract class Code
{
    public abstract CodeType getCodeType();
    
    public boolean isParsedCode() { return false; }
    
    public enum CodeType
    {
        WORD, COMMAND_WORD, LITERAL, MUTABLE_LITERAL, SEPARATOR, BLOCK, FUNCTION,
        SELF, OPERATOR_SYMBOL, OPERATOR, COMPILER_WORD, COMMAND;
    }
    
    static final <CP extends Code> HashMap<String, CP> collect(Class<CP> clazz, Function<CP, String> collector)
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang;

/**
 *
 * @author Asus
 */
public enum PSDataType
{
    UNDEFINED,
    
    NULL,
    NUMBER,
    BOOLEAN,
    STRING,
    
    ARRAY,
    TUPLE,
    MAP,
    
    ITERATOR,
    FUNCTION,
    OBJECT,
    
    USERDATA;
    
    private final String name = name().toLowerCase();
    
    public final String getTypeName() { return name; }
    
    public static final PSDataType decode(String name)
    {
        switch(name)
        {
            case "undefined": return UNDEFINED;
            case "null": return NULL;
            case "number": return NUMBER;
            case "boolean": return BOOLEAN;
            case "string": return STRING;
            case "array": return ARRAY;
            case "tuple": return TUPLE;
            case "map": return MAP;
            case "iterator": return ITERATOR;
            case "function": return FUNCTION;
            case "object": return OBJECT;
            case "userdata": return USERDATA;
            default: return null;
        }
    }
}

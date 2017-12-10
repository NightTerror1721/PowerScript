/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.ObjIntConsumer;
import nt.ps.compiler.parser.Literal;

/**
 *
 * @author mpasc
 */
public final class LiteralPool
{
    private final HashMap<Literal, Integer> literals = new HashMap<>();
    private int idCount = 0;
    
    public final Literal registerLiteral(Literal literal)
    {
        if(literal == null)
            throw new NullPointerException();
        if(!literals.containsKey(literal))
            literals.put(literal, idCount++);
        return literal;
    }
    
    public final void forEachLiteral(ObjIntConsumer<Literal> consumer)
    {
        literals.entrySet().forEach((e) -> consumer.accept(e.getKey(), e.getValue()));
    }
}

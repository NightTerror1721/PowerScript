/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.util.HashMap;
import java.util.LinkedList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;

/**
 *
 * @author Asus
 */
final class LocalVariableManager
{
    private final MethodGen mainMethod;
    private final HashMap<Integer, Integer> references = new HashMap<>();
    private final LinkedList<Integer> unused = new LinkedList<>();
    private final boolean generator;
    private int count = 0;
    
    public LocalVariableManager(MethodGen mainMethod, boolean generator)
    {
        this.mainMethod = mainMethod;
        this.generator = generator;
    }
    
    public final int create(Integer reference)
    {
        if(references.containsKey(reference))
            throw new IllegalStateException();
        if(generator)
        {
            if(unused.isEmpty())
                count++;
            else unused.removeLast();
            references.put(reference, null);
            return -1;
        }
        Integer id;
        if(unused.isEmpty())
        {
            String name = BytecodeGenerator.STR_VAR_PREFIX + references.size();
            LocalVariableGen local = mainMethod.addLocalVariable(name, BytecodeGenerator.TYPE_VALUE, null, null);
            id = local.getIndex();
            count++;
        }
        else id = unused.removeLast();
        references.put(reference, id);
        return id;
    }
    
    public final void registerParameter(Integer reference, int id)
    {
        if(generator)
            throw new IllegalStateException();
        if(references.containsKey(reference))
            throw new IllegalStateException();
        references.put(reference, id);
        count++;
    }
    
    public final void registerParameter(Integer reference)
    {
        if(!generator)
            throw new IllegalStateException();
        if(references.containsKey(reference))
            throw new IllegalStateException();
        references.put(reference, null);
        count++;
    }
    
    public final int getId(Integer reference)
    {
        if(generator)
            throw new IllegalStateException();
        Integer id = references.get(reference);
        if(id == null)
            throw new IllegalStateException();
        return id;
    }
    
    public final boolean contains(Integer reference)
    {
        return references.containsKey(reference);
    }
    
    public final void remove(Integer reference)
    {
        if(!references.containsKey(reference))
            throw new IllegalStateException();
        Integer id = references.remove(reference);
        unused.add(id);
    }
    
    public final int getMaxLocalCount() { return count; }
}

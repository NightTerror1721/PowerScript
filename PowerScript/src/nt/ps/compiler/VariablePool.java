/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import nt.ps.compiler.exception.CompilerError;

/**
 *
 * @author Asus
 */
public final class VariablePool
{
    private final VariablePool parent;
    private final Stack stack;
    private final LinkedList<VariableScope> vars;
    private final LinkedList<Variable> upPointers;
    private int upLocalRefs;
    
    private VariablePool(VariablePool parent, Stack stack)
    {
        this.parent = parent;
        if(stack == null)
            throw new NullPointerException();
        this.stack = stack;
        vars = new LinkedList<>();
        upPointers = new LinkedList<>();
        upLocalRefs = 0;
    }
    public VariablePool(Stack stack) { this(null, stack); }
    
    public final VariablePool createChild() { return new VariablePool(this, stack); }
    
    public final Stack getStack() { return stack; }
    
    
    public final void createScope()
    {
        vars.add(new VariableScope());
    }
    
    public final void destroyScope() throws CompilerError
    {
        if(vars.isEmpty())
            throw new IllegalStateException();
        VariableScope scope = vars.removeLast();
        stack.deallocateVariables(scope.getLocalCount());
    }
    
    public final Variable get(String name, boolean globalModifier) throws CompilerError
    {
        Variable var = get0(name, globalModifier);
        if(var == null)
            throw new IllegalStateException();
        return var;
    }
    
    private Variable get0(String name, boolean globalModifier) throws CompilerError
    {
        ListIterator<VariableScope> it = vars.listIterator(vars.size());
        while(it.hasPrevious())
        {
            VariableScope scope = it.previous();
            if(scope.exists(name))
                return scope.get(name);
        }
        
        if(parent == null)
            return null;
        Variable var = parent.get0(name, globalModifier);
        if(var == null)
            return globalModifier ? createGlobal(name) : null;
        if(var.isGlobal())
            return var;
        
        return vars.getFirst().createUpPointer(var);
    }
    
    public final boolean exists(String name)
    {
        ListIterator<VariableScope> it = vars.listIterator(vars.size());
        while(it.hasPrevious())
        {
            VariableScope scope = it.previous();
            if(scope.exists(name))
                return true;
        }
        return parent != null && parent.exists(name);
    }
    
    public final Variable createLocal(String name) throws CompilerError
    {
        VariableScope scope = vars.getLast();
        if(scope.exists(name))
            throw CompilerError.varAlreadyExists(name);
        return scope.createLocal(name);
    }
    
    public final Variable createGlobal(String name) throws CompilerError
    {
        VariableScope scope = vars.getLast();
        if(scope.exists(name))
            throw CompilerError.varAlreadyExists(name);
        return scope.createGlobal(name);
    }
    
    public final Variable createParameter(String name) throws CompilerError
    {
        VariableScope scope = vars.getLast();
        if(scope.exists(name))
            throw CompilerError.varAlreadyExists(name);
        return scope.createLocal(name);
    }
    
    public final List<Variable> getUpPointers() { return Collections.unmodifiableList(upPointers); }
    
    
    private final class VariableScope
    {
        private final HashMap<String, Variable> vars;
        private int count;
        
        private VariableScope()
        {
            vars = new HashMap<>();
            count = VariablePool.this.vars.isEmpty() ? 0 : VariablePool.this.vars.getLast().count;
        }
        
        private Variable create(VariableType type, String name, int ref) throws CompilerError
        {
            if(type == null)
                throw new NullPointerException();
            if(name == null)
                throw new NullPointerException();
            if(name.isEmpty())
                throw new IllegalArgumentException();
            
            if(vars.containsKey(name))
                throw new CompilerError("Variable " + name + " has already exists");
            Variable var = new Variable(type, name, ref);
            vars.put(var.getName(), var);
            
            return var;
        }
        
        public final Variable createLocal(String name) throws CompilerError
        {
            count = stack.allocateVariable();
            return create(VariableType.LOCAL, name, count);
        }
        
        public final Variable createGlobal(String name) throws CompilerError
        {
            return create(VariableType.GLOBAL, name, -1);
        }
        
        public final Variable createUpPointer(Variable varRef) throws CompilerError
        {
            Variable var = create(VariableType.UP_POINTER, varRef.getName(), upLocalRefs++);
            var.upPointerRef = varRef;
            upPointers.add(var);
            return var;
        }
        
        public final int getLocalCount() { return count; }
        public final boolean exists(String name) { return vars.containsKey(name); }
        public final Variable get(String name) throws CompilerError
        {
            Variable var = vars.get(name);
            if(var == null)
                throw new CompilerError("Variable " + name + " does not exists");
            return var;
        }
    }
    
    public final class Variable
    {
        private VariableType type;
        private final String name;
        private final int ref;
        private Variable upPointerRef;

        private Variable(VariableType type, String name, int ref)
        {
            if(type == null)
                throw new NullPointerException();
            if(name == null)
                throw new NullPointerException();
            this.type = type;
            this.name = name;
            this.ref = ref;
            upPointerRef = null;
        }

        public final String getName() { return name; }
        public final int getReference() { return ref; }
        public final Variable getUpPointerReference() { return upPointerRef; }

        public final boolean isLocal() { return type == VariableType.LOCAL; }
        public final boolean isGlobal() { return type == VariableType.GLOBAL; }
        public final boolean isLocalPointer() { return type == VariableType.LOCAL_POINTER; }
        public final boolean isUpPointer() { return type == VariableType.UP_POINTER; }
        
        public final Variable switchToLocalPointer()
        {
            if(type != VariableType.LOCAL)
                throw new IllegalStateException();
            type = VariableType.LOCAL_POINTER;
            return this;
        }
        
    }
    
    private static enum VariableType { LOCAL, GLOBAL, LOCAL_POINTER, UP_POINTER };
}

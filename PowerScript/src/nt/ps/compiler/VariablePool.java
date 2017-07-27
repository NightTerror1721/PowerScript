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
import nt.ps.PSGlobals;
import nt.ps.compiler.exception.CompilerError;

/**
 *
 * @author Asus
 */
public final class VariablePool
{
    private final VariablePool parent;
    private final BytecodeGenerator bytecode;
    private final Stack stack;
    private final PSGlobals globals;
    private final LinkedList<VariableScope> vars;
    private final LinkedList<Variable> upPointers;
    private final HashMap<String, Variable> natives;
    private int upLocalRefs;
    private int staticCount;
    
    private VariablePool(VariablePool parent, BytecodeGenerator bytecode, Stack stack, PSGlobals globals)
    {
        this.parent = parent;
        if(bytecode == null)
            throw new NullPointerException();
        this.bytecode = bytecode;
        if(stack == null)
            throw new NullPointerException();
        this.stack = stack;
        this.globals = globals;
        vars = new LinkedList<>();
        upPointers = new LinkedList<>();
        natives = new HashMap<>();
        upLocalRefs = 0;
        staticCount = 0;
    }
    public VariablePool(BytecodeGenerator bytecode, Stack stack, PSGlobals globals) { this(null, bytecode, stack, globals); }
    
    public final VariablePool createChild(BytecodeGenerator bytecode, Stack stack) { return new VariablePool(this, bytecode, stack, globals); }
    
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
        int count = vars.isEmpty()
                ? scope.getLocalCount() + 1
                : scope.getLocalCount() - vars.getLast().getLocalCount();
        if(count > 0)
            stack.deallocateVariables(count);
        scope.destroyLocals();
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
        if(globals.hasNativeValue(name))
        {
            Variable var = natives.get(name);
            if(var == null)
            {
                var = new Variable(VariableType.NATIVE, name, -1, true, true);
                natives.put(name, var);
            }
            return var;
        }
        
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
            return globalModifier ? createGlobal(name, false) : null;
        if(var.isGlobal() || var.isStatic())
            return var;
        
        return vars.getFirst().createUpPointer(var);
    }
    
    public final boolean exists(String name, boolean inCurrentScope, boolean globalModifier) throws CompilerError
    {
        if(globals.hasNativeValue(name))
            return true;
        
        if(inCurrentScope)
            return vars.getLast().exists(name);
        
        ListIterator<VariableScope> it = vars.listIterator(vars.size());
        while(it.hasPrevious())
        {
            VariableScope scope = it.previous();
            if(scope.exists(name))
                return true;
        }
        if(parent != null && parent.exists(name, inCurrentScope, globalModifier))
            return true;
        if(!globalModifier)
            return false;
        createGlobal(name, false);
        return true;
    }
    
    public final Variable createLocal(String name, boolean isConstant) throws CompilerError
    {
        VariableScope scope = vars.getLast();
        if(scope.exists(name))
            throw CompilerError.varAlreadyExists(name);
        return scope.createLocal(name, isConstant);
    }
    
    public final Variable createGlobal(String name, boolean isConstant) throws CompilerError
    {
        VariableScope scope = vars.getLast();
        if(scope.exists(name))
            throw CompilerError.varAlreadyExists(name);
        return scope.createGlobal(name, isConstant);
    }
    
    public final Variable createParameter(String name) throws CompilerError
    {
        VariableScope scope = vars.getLast();
        if(scope.exists(name))
            throw CompilerError.varAlreadyExists(name);
        return scope.createLocal(name, false);
    }
    
    public final Variable createStatic(String name, boolean isConstant) throws CompilerError
    {
        VariableScope scope = vars.getLast();
        if(scope.exists(name))
            throw CompilerError.varAlreadyExists(name);
        return scope.createStatic(name, isConstant);
    }
    
    public final List<Variable> getUpPointers() { return Collections.unmodifiableList(upPointers); }
    
    
    private final class VariableScope
    {
        private final HashMap<String, Variable> vars;
        private int count;
        
        private VariableScope()
        {
            vars = new HashMap<>();
            count = VariablePool.this.vars.isEmpty() ? -1 : VariablePool.this.vars.getLast().count;
        }
        
        private Variable create(VariableType type, String name, int ref, boolean constant) throws CompilerError
        {
            if(type == null)
                throw new NullPointerException();
            if(name == null)
                throw new NullPointerException();
            if(name.isEmpty())
                throw new IllegalArgumentException();
            
            if(vars.containsKey(name))
                throw new CompilerError("Variable " + name + " has already exists");
            Variable var = new Variable(type, name, ref, constant, type != VariableType.LOCAL && type != VariableType.STATIC);
            vars.put(var.getName(), var);
            
            return var;
        }
        
        public final Variable createLocal(String name, boolean isConstant) throws CompilerError
        {
            count = stack.allocateVariable();
            return create(VariableType.LOCAL, name, count, isConstant);
        }
        
        public final Variable createGlobal(String name, boolean isConstant) throws CompilerError
        {
            return create(VariableType.GLOBAL, name, -1, isConstant);
        }
        
        public final Variable createUpPointer(Variable varRef) throws CompilerError
        {
            Variable var = create(VariableType.UP_POINTER, varRef.getName(), upLocalRefs++, varRef.constant);
            var.upPointerRef = varRef;
            upPointers.add(var);
            return var;
        }
        
        public final Variable createStatic(String name, boolean isConstant) throws CompilerError
        {
            Variable var = create(VariableType.STATIC, name, staticCount++, isConstant);
            var.staticClassName = bytecode.getClassName();
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
        
        public final void destroyLocals()
        {
            vars.values().stream()
                    .filter(Variable::isLocal)
                    .forEach(bytecode::removeLocal);
        }
    }
    
    public final class Variable
    {
        private VariableType type;
        private final String name;
        private final int ref;
        private final boolean constant;
        private boolean initiated;
        private Variable upPointerRef;
        private String staticClassName;

        private Variable(VariableType type, String name, int ref, boolean constant, boolean initiated)
        {
            if(type == null)
                throw new NullPointerException();
            if(name == null)
                throw new NullPointerException();
            this.type = type;
            this.name = name;
            this.ref = ref;
            this.constant = constant;
            this.initiated = initiated;
            upPointerRef = null;
        }

        public final String getName() { return name; }
        public final int getReference() { return ref; }
        public final Variable getUpPointerReference() { return upPointerRef; }
        public final String getStaticClassName() { return staticClassName; }
        public final boolean isConstant() { return constant; }
        public final boolean isInitiated() { return initiated; }
        
        public final void initiate()
        {
            if(initiated)
                throw new IllegalStateException();
            initiated = true;
        }
        
        public final VariableType getVariableType() { return type; }

        public final boolean isLocal() { return type == VariableType.LOCAL; }
        public final boolean isGlobal() { return type == VariableType.GLOBAL; }
        public final boolean isLocalPointer() { return type == VariableType.LOCAL_POINTER; }
        public final boolean isUpPointer() { return type == VariableType.UP_POINTER; }
        public final boolean isStatic() { return type == VariableType.STATIC; }
        public final boolean isNative() { return type == VariableType.NATIVE; }
        
        public final Variable switchToLocalPointer()
        {
            if(type != VariableType.LOCAL)
                throw new IllegalStateException();
            type = VariableType.LOCAL_POINTER;
            return this;
        }
        
    }
    
    public static enum VariableType { LOCAL, GLOBAL, LOCAL_POINTER, UP_POINTER, STATIC, NATIVE };
}

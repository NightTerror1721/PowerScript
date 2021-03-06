/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.util.LinkedList;
import nt.ps.compiler.parser.Block.Scope;
import nt.ps.compiler.parser.Block.Scope.ScopeIterator;
import nt.ps.compiler.parser.Command;
import org.apache.bcel.generic.InstructionHandle;

/**
 *
 * @author mpasc
 */
public final class ScopeInfo
{
    private final Scope scope;
    private final ScopeType type;
    private final ScopeIterator iterator;
    private InstructionHandle startRef;
    private InstructionHandle endRef;
    private LinkedList<InstructionHandle> branchs;
    private SwitchModel smodel;
    
    public ScopeInfo(Scope scope, ScopeType type)
    {
        if(scope == null)
            throw new NullPointerException();
        if(type == null)
            throw new NullPointerException();
        this.type = type;
        this.scope = scope;
        iterator = scope.iterator();
        branchs = new LinkedList<>();
    }
    
    public final Scope getScope() { return scope; }
    
    public final ScopeType getScopeType() { return type; }
    
    public final boolean isBreakable() { return type.isBreakable(); }
    public final boolean isContinuable() { return type.isContinuable(); }
    public final boolean isBase() { return type == ScopeType.BASE; }
    
    public final void setStartReference(InstructionHandle ref)
    {
        if(ref == null)
            throw new NullPointerException();
        this.startRef = ref;
    }
    public final void setEndReference(InstructionHandle ref)
    {
        if(ref == null)
            throw new NullPointerException();
        this.endRef = ref;
    }
    
    public final boolean hasStartReference() { return startRef != null; }
    public final boolean hasEndReference() { return endRef != null; }
    
    public final InstructionHandle getStartReference()
    {
        if(startRef == null)
            throw new IllegalStateException();
        return startRef;
    }
    public final InstructionHandle getEndReference()
    {
        if(endRef == null)
            throw new IllegalStateException();
        return endRef;
    }
    
    public final void addBranchReference(InstructionHandle ref)
    {
        if(ref == null)
            throw new NullPointerException();
        branchs.add(ref);
    }
    
    public final int getBranchCount() { return branchs.size(); }
    
    public final InstructionHandle getBranch(int index) { return branchs.get(index); }
    
    public final InstructionHandle[] getAllBranchs() { return branchs.toArray(new InstructionHandle[branchs.size()]); }
    
    public final void createSwitchModel(InstructionHandle start)
    {
        if(type != ScopeType.SWITCH)
            throw new IllegalStateException("SwitchModel only works on switch scope");
        if(smodel != null)
            throw new IllegalStateException();
        smodel = new SwitchModel(start);
    }
    public final SwitchModel getSwitchModel()
    {
        if(type != ScopeType.SWITCH)
            throw new IllegalStateException("SwitchModel only works on switch scope");
        if(smodel == null)
            throw new IllegalStateException();
        return smodel;
    }
    
    
    public final boolean hasMoreCommands() { return iterator.hasNext(); }
    public final Command nextCommand() { return iterator.next(); }
    public final Command peekNextCommand() { return iterator.peekNext(); }
    public final Command currentCommand() { return iterator.peek(); }
    
    
    public static enum ScopeType
    {
        BASE,
        IF,
        ELSEIF,
        ELSE,
        WHILE(true, true),
        FOR(true, true),
        FOREACH(true, true),
        SWITCH(true, false),
        TRY,
        CATCH;
        
        private final boolean breakable;
        private final boolean continuable;
        
        private ScopeType(boolean b, boolean c)
        {
            breakable = b;
            continuable = c;
        }
        private ScopeType() { this(false, false); }
        
        public final boolean isBreakable() { return breakable; }
        public final boolean isContinuable() { return continuable; }
    }
}

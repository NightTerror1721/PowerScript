/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.util.LinkedList;
import nt.ps.compiler.parser.Block.Scope;
import org.apache.bcel.generic.InstructionHandle;

/**
 *
 * @author mpasc
 */
public final class ScopeInfo
{
    private final Scope scope;
    private final ScopeType type;
    private InstructionHandle startRef;
    private InstructionHandle endRef;
    private LinkedList<InstructionHandle> branchs;
    
    public ScopeInfo(Scope scope, ScopeType type)
    {
        if(scope == null)
            throw new NullPointerException();
        if(type == null)
            throw new NullPointerException();
        this.type = type;
        this.scope = scope;
        branchs = new LinkedList<>();
    }
    
    public final Scope getScope() { return scope; }
    
    public final ScopeType getScopeType() { return type; }
    
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
    
    
    public static enum ScopeType
    {
        IF, ELSE, WHILE, FOR, TRY, CATCH
    }
}

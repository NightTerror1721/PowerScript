/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.util.LinkedList;

/**
 *
 * @author mpasc
 */
public final class ScopeStack
{
    private final LinkedList<ScopeInfo> scopes = new LinkedList<>();
    private ScopeInfo current;
    
    public final void push(ScopeInfo scope)
    {
        if(scope == null)
            throw new NullPointerException();
        if(current != null)
            scopes.addFirst(current);
        current = scope;
    }
    
    public final ScopeInfo peek() { return current; }
    
    public final ScopeInfo pop()
    {
        if(current == null)
            throw new IllegalStateException();
        ScopeInfo scope = current;
        current = scopes.isEmpty() ? null : scopes.removeFirst();
        return scope;
    }
    
    public final int size() { return current == null ? 0 : scopes.size() + 1; }
    
    public final boolean isEmpty() { return current == null; }
}

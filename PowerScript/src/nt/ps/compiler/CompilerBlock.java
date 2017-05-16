/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.exception.CompilerErrors;

/**
 *
 * @author mpasc
 */
final class CompilerBlock
{
    private final CodeReader source;
    private final ScopeStack scopes;
    private final Stack stack;
    private final VariablePool vars;
    private final BytecodeGenerator bytecode;
    private final CompilerErrors errors;
    
    public CompilerBlock(CodeReader source, BytecodeGenerator bytecode, CompilerErrors errors, VariablePool parentVars) throws CompilerError
    {
        this.source = source;
        scopes = new ScopeStack();
        stack = new Stack();
        vars = parentVars != null ? parentVars.createChild(stack) : new VariablePool(stack);
        this.bytecode = bytecode;
        this.errors = errors;
    }
    
    private void compile() throws CompilerError
    {
        vars.createScope();
    }
}

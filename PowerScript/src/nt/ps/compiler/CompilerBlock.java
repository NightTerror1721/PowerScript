/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import nt.ps.PSGlobals;
import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.exception.CompilerErrors;
import nt.ps.compiler.parser.Command;
import nt.ps.compiler.parser.ParsedCode;
import nt.ps.lang.PSFunction;

/**
 *
 * @author mpasc
 */
final class CompilerBlock
{
    private final ScopeStack scopes;
    private final Stack stack;
    private final VariablePool vars;
    private final BytecodeGenerator bytecode;
    private final PSGlobals globals;
    private final CompilerBlockType type;
    private final CompilerErrors errors;
    private Class<? extends PSFunction> compiledClass;
    
    public CompilerBlock(ScopeInfo source, PSGlobals globals, CompilerBlockType type, BytecodeGenerator bytecode, CompilerErrors errors, VariablePool parentVars)
    {
        scopes = new ScopeStack();
        stack = new Stack();
        vars = parentVars != null ? parentVars.createChild(stack) : new VariablePool(stack);
        this.bytecode = bytecode;
        this.globals = globals;
        this.type = type;
        this.errors = errors;
        
        this.bytecode.setCompiler(this);
        scopes.push(source);
    }
    
    public final void compile()
    {
        vars.createScope();
        if(type == CompilerBlockType.FUNCTION)
            bytecode.createUpPointerSlots();
        while(!scopes.isEmpty())
        {
            if(scopes.peek().hasMoreCommands())
            {
                Command command = scopes.peek().nextCommand();
                try { compileCommand(command); }
                catch(CompilerError error) { errors.addError(error, command); }
            }
            else
            {
                completeScope(scopes.peek());
                scopes.pop();
            }
        }
        
        try { bytecode.Return(); } catch(CompilerError error) { errors.addError(error, Command.parseErrorCommand(-1)); }
        if(type != CompilerBlockType.SCRIPT)
            bytecode.initiateUpPointersArray(vars.getUpPointers().size());
        
        compiledClass = bytecode.build(type);
    }
    
    private void completeScope(ScopeInfo scopeInfo)
    {
        
    }
    
    private void compileCommand(Command command) throws CompilerError
    {
        if(command.isOperationsCommand())
        {
            compileOperation(command.getCode(0));
            return;
        }
        
        switch(command.getName())
        {
            case VAR: VAR(command);
        }
    }
    
    private void VAR(Command command)
    {
        
    }
    
    private void compileOperation(ParsedCode code)
    {
        
    }
    
    
    
    
    
    
    
    
    
    public final Class<? extends PSFunction> getCompiledClass() { return compiledClass; }
    
    public static final PSFunction buildFunctionInstance(Class<? extends PSFunction> baseClass, PSGlobals globals)
    {
        if(globals == null)
            throw new NullPointerException();
        try
        {
            Constructor cns = baseClass.getConstructor();
            PSFunction function = (PSFunction) cns.newInstance();
            
            baseClass.getMethod(BytecodeGenerator.STR_FUNC_SET_GLOBALS, SET_GLOBALS_SIGNATURE)
                    .invoke(function, globals);
            
            return function;
        }
        catch(IllegalAccessException | IllegalArgumentException | InstantiationException |
                NoSuchMethodException | SecurityException | InvocationTargetException ex)
        {
            throw new IllegalStateException(ex);
        }
    }
    
    public final Stack getStack() { return stack; }
    
    public static enum CompilerBlockType { SCRIPT, FUNCTION }
    private static final Class<?>[] SET_GLOBALS_SIGNATURE = { PSGlobals.class };
}

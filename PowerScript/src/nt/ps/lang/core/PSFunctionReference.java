/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import java.io.IOException;
import java.util.UUID;
import nt.ps.PSState;
import nt.ps.compiler.CompilerUnit;
import nt.ps.compiler.exception.PSCompilerException;
import nt.ps.exception.PSRuntimeException;
import nt.ps.lang.PSFunction;
import nt.ps.lang.PSValue;
import nt.ps.lang.PSVarargs;

/**
 *
 * @author Asus
 */
public final class PSFunctionReference extends ImmutableCoreLibrary
{
    private final PSState state;
    
    public PSFunctionReference(PSState state)
    {
        if(state == null)
            throw new NullPointerException();
        this.state = state;
    }
    
    @Override
    public final PSValue createNewInstance() { throw new PSRuntimeException("Invalid function definition"); }
    
    @Override
    public PSValue createNewInstance(PSValue arg0) { return createNewInstance((PSVarargs) arg0); }
    
    @Override
    public PSValue createNewInstance(PSValue arg0, PSValue arg1) { return createNewInstance(varargsOf(arg0, arg1)); }
    
    @Override
    public PSValue createNewInstance(PSValue arg0, PSValue arg1, PSValue arg2) { return createNewInstance(varargsOf(arg0, arg1, arg2)); }
    
    @Override
    public PSValue createNewInstance(PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return createNewInstance(varargsOf(arg0, arg1, arg2, arg3)); }
    
    @Override
    public PSValue createNewInstance(PSVarargs args)
    {
        if(args.numberOfArguments() < 1)
            throw new PSRuntimeException("Expected code for function");
        String[] sargs = new String[args.numberOfArguments() - 1];
        for(int i=0;i<sargs.length;i++)
            sargs[i] = args.arg(i).toJavaString();
        String name = UUID.randomUUID().toString();
        String code = args.arg(args.numberOfArguments() - 1).toJavaString();
        try
        {
            PSFunction func = CompilerUnit.compileFunction(state, state.getClassLoader(), name, code, sargs);
            return func;
        }
        catch(IOException | PSCompilerException ex)
        {
            throw new PSRuntimeException(ex);
        }
    }
    
    @Override
    public PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
        }
    }
}

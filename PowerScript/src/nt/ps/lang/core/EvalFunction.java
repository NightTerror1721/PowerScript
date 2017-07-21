/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import nt.ps.PSGlobals;
import nt.ps.PSScript;
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
public final class EvalFunction extends PSFunction.PSThreeArgsFunction
{
    private final PSState state;
    
    public EvalFunction(PSState state)
    {
        if(state == null)
            throw new NullPointerException();
        this.state = state;
    }
    
    @Override
    public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2)
    {
        String code = arg0.toJavaString();
        PSGlobals globals = arg1 == UNDEFINED || arg1 == NULL ?
                PSGlobals.instance(state) : PSGlobals.wrap(state, FALSE);
        PSValue subSelf = arg2 == UNDEFINED ? NULL : arg2;
        String name = randomName();
        try(ByteArrayInputStream bais = new ByteArrayInputStream(code.getBytes()))
        {
            PSScript script = CompilerUnit.compile(bais, globals, state.getClassLoader(), name, null, true);
            PSVarargs result = script.innerCall(subSelf);
            return PSVarargs.varargsAsPSArray(result, 0);
        }
        catch(IOException | PSCompilerException ex)
        {
            throw new PSRuntimeException(ex.getMessage(), ex);
        }
    }
    
    private static String randomName()
    {
        long nano = System.nanoTime();
        return "lambda_" + Long.toHexString(nano) + Long.toHexString(System.currentTimeMillis());
    }
}

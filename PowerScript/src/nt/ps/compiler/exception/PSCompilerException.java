/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.exception;

import java.util.Iterator;
import nt.ps.compiler.exception.CompilerErrors.Error;
import nt.ps.exception.PSException;

/**
 *
 * @author Asus
 */
public final class PSCompilerException extends PSException implements Iterable<Error>
{
    private final Error[] errors;
    
    public PSCompilerException(CompilerErrors errors)
    {
        super(message(errors));
        this.errors = errors.errors.toArray(new Error[errors.errors.size()]);
    }
    
    public final int getErrorCount() { return errors.length; }
    public final Error getError(int index) { return errors[index]; }
    
    @Override
    public final Iterator<Error> iterator()
    {
        return new Iterator<Error>()
        {
            private int it = 0;
            
            @Override
            public final boolean hasNext() { return it < errors.length; }

            @Override
            public final Error next() { return errors[it++]; }
        };
    }
    
    private static String message(CompilerErrors errors)
    {
        if(errors.errors.isEmpty())
            throw new IllegalArgumentException();
        StringBuilder sb = new StringBuilder(errors.errors.size() + " errors found:\n");
        for(Error error : errors.errors)
        {
            sb.append("In line ")
                    .append(error.getSourceLine())
                    .append(": ")
                    .append(error.message)
                    .append("\n");
        }
        return sb.toString();
    }
}

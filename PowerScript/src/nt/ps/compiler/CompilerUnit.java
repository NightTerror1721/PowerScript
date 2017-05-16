/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import nt.ps.PSScript;
import nt.ps.compiler.exception.CompilerErrors;
import nt.ps.compiler.exception.PSCompilerException;

/**
 *
 * @author Asus
 */
public final class CompilerUnit
{
    private final CodeReader source;
    
    private CompilerUnit(CodeReader source)
    {
        if(source == null)
            throw new NullPointerException();
        this.source = source;
    }
    
    private PSScript compile() throws PSCompilerException
    {
        CompilerErrors errors = new CompilerErrors();
        
        if(errors.hasErrors())
            throw new PSCompilerException(errors);
    }
}

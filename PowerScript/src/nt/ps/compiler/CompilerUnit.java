/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.io.EOFException;
import java.util.LinkedList;
import nt.ps.PSScript;
import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.exception.CompilerErrors;
import nt.ps.compiler.exception.PSCompilerException;
import nt.ps.compiler.parser.Block.Scope;
import nt.ps.compiler.parser.Code;
import nt.ps.compiler.parser.Command;

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
    
    private Scope parseAllInstructions()
    {
        
    }
    
    private Scope parseScope()
    {
        
    }
    
    private Command parseInstruction(int limit, boolean validEnd) throws CompilerError
    {
        InstructionBuilder sb = new InstructionBuilder();
        LinkedList<Code> codes = new LinkedList<>();
        boolean canend = false;
        
        base_loop:
        try
        {
            for(;;)
            {
                char c = source.next();
                if(limit >= 0 && source.getCurrentIndex() >= limit)
                {
                    sb.decode(codes);
                    break;
                }
                
                if(c == '/')
                {
                    if(!source.canPeek(1))
                        throw CompilerError.invalidEndChar('/');
                    switch(source.peek(1))
                    {
                        case '/': {
                            skipUntil('\n', true);
                        } break;
                        case '*': {
                            for(;;)
                            {
                                skipUntil('*', true);
                                c = source.next();
                                
                            }
                        } break;
                    }
                    
                }
            }
        }
        catch(EOFException ex)
        {
            if(!validEnd)
                throw new CompilerError("Unexpected End of File");
        }
    }
    
    private void skipUntil(char end, boolean isEndOfFileValid) throws EOFException
    {
        try
        {
            for(;;)
            {
                char c = source.next();
                if(c == end)
                    return;
            }
        }
        catch(EOFException ex)
        {
            if(!isEndOfFileValid)
                throw ex;
        }
    }
}

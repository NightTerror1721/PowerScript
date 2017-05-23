/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.io.EOFException;
import nt.ps.PSScript;
import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.exception.CompilerErrors;
import nt.ps.compiler.exception.PSCompilerException;
import nt.ps.compiler.parser.AssignationSymbol;
import nt.ps.compiler.parser.Block;
import nt.ps.compiler.parser.Block.Scope;
import nt.ps.compiler.parser.CommandWord;
import nt.ps.compiler.parser.Literal;
import nt.ps.compiler.parser.MutableLiteral;
import nt.ps.compiler.parser.OperatorSymbol;
import nt.ps.compiler.parser.Separator;
import nt.ps.compiler.parser.Tuple;

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
    
    private Scope parseScope(CodeReader source)
    {
        
    }
    
    private Tuple parseInstruction(CodeReader source, boolean validEnd, ColonMode colonMode) throws CompilerError
    {
        final InstructionBuilder sb = new InstructionBuilder();
        final int currentLine = source.getCurrentLine();
        boolean canend = true;
        
        try
        {
            base_loop:
            for(;;)
            {
                char c = source.next();
                
                switch(c)
                {
                    case '\t':
                    case '\r':
                    case '\n':
                    case ' ': {
                        sb.decode();
                    } continue;
                    
                    case ';': {
                        switch(colonMode)
                        {
                            case ENDS: break base_loop;
                            case IGNORE: continue;
                            case ERROR: throw new CompilerError("Unexpected End of Instruction ';'");
                            default: throw new IllegalStateException();
                        }
                    }
                    
                    case '(': {
                        CodeReader scopeSource = extractScope(source, '(', ')');
                        if(!sb.isEmpty() && sb.getLastCode() == CommandWord.FOR)
                        {
                            Tuple tuple = parseInstruction(scopeSource, true, ColonMode.IGNORE);
                            sb.decode();
                            sb.addCode(Block.arguments(tuple, Separator.COLON));
                        }
                        else
                        {
                            Tuple tuple = parseInstruction(scopeSource, true, ColonMode.ERROR);
                            sb.decode();

                            if(sb.isEmpty() || !sb.getLastCode().isValidCodeObject()) //Parenthesis or Tuple
                            {
                                if(tuple.has(Separator.COMMA))
                                    sb.addCode(MutableLiteral.tuple(tuple));
                                else sb.addCode(Block.parenthesis(tuple));
                            }
                            else //Arguments list
                                sb.addCode(Block.arguments(tuple, Separator.COMMA));
                        }
                    } continue;
                    
                    case ')': throw CompilerError.invalidChar(')');
                    
                    case '[': {
                        CodeReader scopeSource = extractScope(source, '[', ']');
                        Tuple tuple = parseInstruction(scopeSource, true, ColonMode.ERROR);
                        sb.decode();
                        
                        if(sb.isEmpty() || !sb.getLastCode().isValidCodeObject()) //Array or Map
                        {
                            if(tuple.has(Separator.TWO_POINTS))
                                sb.addCode(MutableLiteral.map(tuple));
                            else sb.addCode(MutableLiteral.array(tuple));
                        }
                        else //Access Operator
                            sb.addCode(OperatorSymbol.ACCESS)
                              .addCode(tuple.pack());
                    } continue;
                    
                    case ']': throw CompilerError.invalidChar(']');
                    
                    case '{': {
                        CodeReader scopeSource = extractScope(source, '{', '}');
                        
                        if(sb.isEmpty() || !sb.getLastCode().isValidCodeObject()) //Object
                        {
                            Tuple tuple = parseInstruction(scopeSource, true, ColonMode.ERROR);
                            sb.decode();
                            sb.addCode(MutableLiteral.object(tuple));
                        }
                        else //Scope
                        {
                            Scope scope = parseScope(scopeSource);
                            sb.decode();
                            sb.addCode(scope);
                        }
                    } continue;
                    
                    case '}': throw CompilerError.invalidChar('}');
                    
                    case '\"':
                    case '\'': {
                        final char cend = c;
                        sb.decode();
                        
                        canend = false;
                        for(;;)
                        {
                            c = source.next();
                            if(c == cend)
                                break;
                        }
                        sb.addCode(Literal.valueOf(sb.getAndClear()));
                    } continue;
                    
                    case ',': {
                        sb.decode();
                        sb.addCode(Separator.COMMA);
                    } continue;
                    
                    case ':': {
                        sb.decode();
                        sb.addCode(Separator.TWO_POINTS);
                    } continue;
                    
                    case '/': {
                        if(!source.canPeek(1))
                            throw CompilerError.invalidEndChar('/');
                        switch(source.peek(1))
                        {
                            case '/': {
                                canend = true;
                                skipUntil(source, '\n', true);
                                canend = false;
                            } break;
                            case '*': {
                                canend = true;
                                for(;;)
                                {
                                    skipUntil(source, '*', true);
                                    c = source.next();
                                    if(c == '/')
                                    {
                                        canend = false;
                                        continue base_loop;
                                    }
                                }
                            }
                            case '=': {
                                sb.decode();
                                sb.addCode(AssignationSymbol.ASSIGNATION_DIVIDE);
                            } break;
                            default: {
                                sb.decode();
                                sb.addCode(OperatorSymbol.DIVIDE);
                            } break;
                        }
                    } continue;
                }
            }
            
            if(!sb.isEmpty())
                throw new CompilerError("Unexpected End of File");
        }
        catch(EOFException ex)
        {
            if((!validEnd && !canend) || !canend)
                throw new CompilerError("Unexpected End of File");
            sb.decode();
        }
        
        return sb.build();
    }
    
    private static void skipUntil(CodeReader source, char end, boolean isEndOfFileValid) throws EOFException
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
    
    private static CodeReader extractScope(CodeReader source, char cstart, char cend) throws CompilerError
    {
        int startIndex = source.getCurrentIndex();
        int scope = 0;
        try
        {
            for(;;)
            {
                char c = source.next();
                if(c == cstart)
                    scope++;
                else if(c == cend)
                {
                    if(scope == 0)
                        return source.subpart(startIndex, source.getCurrentIndex());
                    scope--;
                }
            }
        }
        catch(EOFException ex) { throw CompilerError.invalidChar(cstart); }
    }
    
    private enum ColonMode { ENDS, IGNORE, ERROR }
}

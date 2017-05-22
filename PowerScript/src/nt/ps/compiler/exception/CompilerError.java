/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.exception;

import nt.ps.compiler.parser.Code;
import nt.ps.compiler.parser.CommandWord;
import nt.ps.exception.PSException;

/**
 *
 * @author Asus
 */
public class CompilerError extends PSException
{
    public CompilerError(String message)
    {
        super(message);
    }
    
    public CompilerError(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public static final CompilerError invalidIdentifier(String identifier)
    {
        return new CompilerError("Invalid identifier \"" + identifier + "\". Can only contains [a-zA-Z0-9_]");
    }
    public static final CompilerError unexpectedEndOfInstruction()
    {
        return new CompilerError("Unexpected end of instruction");
    }
    public static final CompilerError unexpectedCode(Code code)
    {
        return new CompilerError("Unexpected \"" + code + "\"");
    }
    public static final CompilerError expectedAny(CommandWord name)
    {
        return new CompilerError("Expected any code after \"" + name.getName().name().toLowerCase() + "\" command");
    }
    public static final CompilerError varAlreadyExists(String name)
    {
        return new CompilerError("Variable \"" + name + "\" has already exists");
    }
    public static final CompilerError invalidEndChar(char c)
    {
        return new CompilerError("Invalid end of line with character '" + c + "'");
    }
}

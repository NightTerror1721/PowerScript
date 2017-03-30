/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.exception;

import java.util.LinkedList;
import nt.ps.compiler.parser.Command;

/**
 *
 * @author Asus
 */
public class PSCompilerException extends PSException
{
    private final String command;
    private final int line;
    
    public PSCompilerException(Command cmd, String message)
    {
        super(message(message,cmd.getSourceLine()));
        command = cmd.toString();
        line = cmd.getSourceLine();
    }
    
    public PSCompilerException(Command cmd, Throwable cause)
    {
        super(message(cause.getMessage(),cmd.getSourceLine()),cause);
        command = cmd.toString();
        line = cmd.getSourceLine();
    }
    
    private static String message(String msg, int line)
    {
        return "In line " + line + ":\n" + msg;
    }
    
    public static final PSCompilerException decode(Command cmd, Throwable th)
    {
        if(th instanceof PSCompilerException)
        {
            PSCompilerException ex = (PSCompilerException) th;
            if(ex.command.equals(cmd.toString()))
                return ex;
            return new PSCompilerException(cmd,ex.getCause());
        }
        return new PSCompilerException(cmd,th);
    }
    
    public static final class ErrorList
    {
        private final LinkedList<String> errors = new LinkedList<>();
        
        public final void addError(String error)
        {
            if(error == null || error.isEmpty())
                throw new IllegalArgumentException();
            errors.add(error);
        }
    }
}

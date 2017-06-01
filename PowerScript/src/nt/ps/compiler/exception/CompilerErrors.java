/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.exception;

import java.util.LinkedList;
import nt.ps.compiler.parser.Command;

/**
 *
 * @author Asus
 */
public final class CompilerErrors
{
    final LinkedList<Error> errors = new LinkedList<>();
    
    public final void addError(CompilerError error, Command command)
    {
        errors.add(new Error(command,error.getMessage(),error.getCause()));
    }
    
    public final void addErrors(CompilerErrors errors)
    {
        errors.errors.forEach((error) -> {
            this.errors.add(error);
        });
    }
    
    public final boolean hasErrors() { return !errors.isEmpty(); }
    
    public static final class Error
    {
        final Command command;
        final String message;
        final Throwable cause;
        
        private Error(Command command, String message, Throwable cause)
        {
            if(command == null)
                throw new NullPointerException();
            if(message == null)
                throw new NullPointerException();
            this.command = command;
            this.message = message;
            this.cause = cause;
        }
        
        public final Command getCommand() { return command; }
        public final String getMessage() { return message; }
        public final Throwable getCause() { return cause; }
        public final int getSourceLine() { return command.getSourceLine(); }
    }
}

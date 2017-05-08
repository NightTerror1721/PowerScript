/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.Arrays;
import java.util.List;
import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.parser.Block.Scope;

/**
 *
 * @author Asus
 */
public class FunctionLiteral extends ParsedCode
{
    private final String name;
    private final String varargs;
    private final String[] pars;
    private final Scope scope;
    
    private FunctionLiteral(String name, boolean varargs, String[] parNames, Scope scope)
    {
        this.name = name == null ? "" : name;
        if(varargs)
        {
            if(parNames.length < 1)
                throw new IllegalStateException();
            this.varargs = parNames[parNames.length-1];
            pars = Arrays.copyOf(parNames,parNames.length-1);
        }
        else
        {
            this.varargs = null;
            pars = parNames == null ? new String[]{} : parNames;
        }
        if(scope == null)
            throw new NullPointerException();
        this.scope = scope;
    }
    
    public final boolean isClosure() { return name.isEmpty(); }
    
    public final String getName() { return name; }
    public final boolean isVarargs() { return varargs != null; }
    
    public final int getParameterCount() { return pars.length; }
    public final String getParameterName(int index) { return pars[index]; }
    
    public final String getVarargsParameterName() { return isClosure() ? null : varargs; }
    
    public final Scope getScope() { return scope; }
    
    
    @Override
    public final CodeType getCodeType() { return CodeType.FUNCTION; }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static final FunctionLiteral closure(String[] pars, boolean varargs, Scope scope)
    {
        return new FunctionLiteral(null,varargs,pars,scope);
    }
    public static final FunctionLiteral closure(List<String> pars, boolean varargs, Scope scope)
    {
        return closure(pars.toArray(new String[pars.size()]),varargs,scope);
    }
    
    public static final FunctionLiteral function(String name, String[] pars, boolean varargs, Scope scope) throws CompilerError
    {
        if(name == null || name.isEmpty())
            throw new IllegalStateException();
        Identifier.checkValidIdentifier(name);
        return new FunctionLiteral(null,varargs,pars,scope);
    }
    public static final FunctionLiteral function(String name, List<String> pars, boolean varargs, Scope scope) throws CompilerError
    {
        return function(name,pars.toArray(new String[pars.size()]),varargs,scope);
    }
}

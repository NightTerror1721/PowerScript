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
    private ParsedCode assignation;
    private int assignationMode;
    private final String varargs;
    private final String[] pars;
    private final Scope scope;
    
    private FunctionLiteral(ParsedCode assignation, boolean varargs, String[] parNames, Scope scope) throws CompilerError
    {
        if(assignation == null)
        {
            name = "";
            this.assignation = null;
            assignationMode = -1;
        }
        else
        {
            if(assignation.is(CodeType.IDENTIFIER))
            {
                Identifier idName = (Identifier) assignation;
                name = idName.toString();
                Identifier.checkValidIdentifier(name);
                this.assignation = assignation;
                assignationMode = 0;
            }
            else if(assignation.is(CodeType.OPERATOR))
            {
                Operator op = (Operator) assignation;
                if(op.getSymbol() == OperatorSymbol.ACCESS)
                {
                    name = "";
                    this.assignation = assignation;
                    assignationMode = 1;
                }
                else if(op.getSymbol() == OperatorSymbol.PROPERTY_ACCESS)
                {
                    name = op.getOperand(1).toString();
                    Identifier.checkValidIdentifier(name);
                    this.assignation = assignation;
                    assignationMode = 1;
                }
                else throw new CompilerError("Invalid statement in function name: " + assignation);
            }
            else throw new CompilerError("Invalid statement in function name: " + assignation);
        }
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
    public final boolean hasAssignation() { return assignationMode >= 0; }
    
    public final String getName() { return name; }
    public final boolean isVarargs() { return varargs != null; }
    
    public final int getParameterCount() { return pars.length; }
    public final String getParameterName(int index) { return pars[index]; }
    
    public final String getVarargsParameterName() { return isClosure() ? null : varargs; }
    
    public final Scope getScope() { return scope; }
    
    public final boolean isIdentifierAssignation() { return assignationMode == 0; }
    public final boolean isAccessAssignation() { return assignationMode == 1; }
    public final boolean isPropertyAccessAssignation() { return assignationMode == 2; }
    
    
    @Override
    public final CodeType getCodeType() { return CodeType.FUNCTION; }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static final FunctionLiteral closure(String[] pars, boolean varargs, Scope scope) throws CompilerError
    {
        return new FunctionLiteral(null,varargs,pars,scope);
    }
    public static final FunctionLiteral closure(List<String> pars, boolean varargs, Scope scope) throws CompilerError
    {
        return closure(pars.toArray(new String[pars.size()]),varargs,scope);
    }
    
    public static final FunctionLiteral function(ParsedCode assignation, String[] pars, boolean varargs, Scope scope) throws CompilerError
    {
        if(assignation == null)
            throw new IllegalStateException();
        return new FunctionLiteral(assignation,varargs,pars,scope);
    }
    public static final FunctionLiteral function(ParsedCode assignation, List<String> pars, boolean varargs, Scope scope) throws CompilerError
    {
        return function(assignation,pars.toArray(new String[pars.size()]),varargs,scope);
    }
}

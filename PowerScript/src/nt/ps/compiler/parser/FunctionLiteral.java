/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

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
    
    private FunctionLiteral(ParsedCode assignation, Block<?> pars, Scope scope) throws CompilerError
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
        if(pars == null)
            throw new NullPointerException();
        if(!pars.isArgumentsList())
            throw new CompilerError("Expected a valid arguments list in function definition");
        
        boolean isVarargs = false;
        int count = -1;
        String[] spars = new String[pars.getCodeCount()];
        for(ParsedCode c : pars)
        {
            count++;
            switch(c.getCodeType())
            {
                case IDENTIFIER: {
                    spars[count] = c.toString();
                } break;
                case VARARGS_IDENTIFIER: {
                    if(count != pars.getCodeCount() - 1)
                        throw new CompilerError("Varargs Identifier is valid only in last position of arguments list");
                    isVarargs = true;
                    spars[count] = ((VarargsIdentifier)c).getIdentifier().toString();
                } break;
                default: throw new CompilerError("Unexpected code. Expected only a valid identifier or varargs identifier");
            }
        }
        if(isVarargs)
        {
            varargs = spars[spars.length - 1];
            this.pars = new String[spars.length - 1];
            System.arraycopy(spars,0,this.pars,0,this.pars.length);
        }
        else
        {
            varargs = null;
            this.pars = spars;
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
    
    public static final FunctionLiteral closure(Block<?> pars, Scope scope) throws CompilerError
    {
        return new FunctionLiteral(null,pars,scope);
    }
    
    public static final FunctionLiteral function(ParsedCode assignation, Block<?> pars, Scope scope) throws CompilerError
    {
        if(assignation == null)
            throw new IllegalStateException();
        return new FunctionLiteral(assignation,pars,scope);
    }
}

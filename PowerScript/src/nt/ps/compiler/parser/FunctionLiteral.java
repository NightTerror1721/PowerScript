/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.parser.Assignation.AssignationPart;
import nt.ps.compiler.parser.Block.Scope;

/**
 *
 * @author Asus
 */
public class FunctionLiteral extends ParsedCode
{
    private final String name;
    private final boolean generator;
    private ParsedCode assignation;
    private int assignationMode;
    private final String varargs;
    private final String[] pars;
    private final Literal[] defs;
    private final Scope scope;
    
    private FunctionLiteral(boolean generator, ParsedCode assignation, Block<?> pars, Scope scope) throws CompilerError
    {
        this.generator = generator;
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
        Literal[] ldefs = null;
        String[] spars = new String[pars.getCodeCount()];
        for(ParsedCode c : pars)
        {
            count++;
            switch(c.getCodeType())
            {
                case IDENTIFIER: {
                    if(ldefs != null)
                        throw new CompilerError("Invalid identifier after default parameter in function definition");
                    spars[count] = c.toString();
                } break;
                case VARARGS_IDENTIFIER: {
                    if(ldefs != null)
                        throw new CompilerError("Invalid identifier after default parameter in function definition");
                    if(count != pars.getCodeCount() - 1)
                        throw new CompilerError("Varargs Identifier is valid only in last position of arguments list");
                    isVarargs = true;
                    spars[count] = ((VarargsIdentifier)c).getIdentifier().toString();
                } break;
                case ASSIGNATION: {
                    if(isVarargs)
                        throw new CompilerError("Cannot combine varargs identifier with defualt identifiers in function definition");
                    if(ldefs == null)
                        ldefs = new Literal[spars.length - count];
                    Assignation a = (Assignation) c;
                    if(!a.hasIdentifiersOnly())
                        throw new CompilerError("Only is valid a identifier in left part of default identifier in function definition. <identifier = literal>");
                    if(a.getPartCount() != 1)
                        throw new CompilerError("Only is valid a single literal in right part of default identifier in function definition. <identifier = literal>");
                    AssignationPart ap = a.getPart(0);
                    if(ap.getLocationCount() != 1)
                        throw new CompilerError("Only is valid a single literal in right part of default identifier in function definition. <identifier = literal>");
                    ParsedCode plit = ap.getAssignation();
                    if(!plit.is(CodeType.LITERAL))
                        throw new CompilerError("Only is valid a single literal in right part of default identifier in function definition. <identifier = literal>");
                    spars[count] = ap.getLocation(0).getCode().toString();
                    ldefs[count - (spars.length - ldefs.length)] = (Literal) plit;
                } break;
                default: throw new CompilerError("Unexpected code. Expected only a valid identifier or varargs identifier");
            }
        }
        if(isVarargs)
        {
            varargs = spars[spars.length - 1];
            this.pars = new String[spars.length - 1];
            System.arraycopy(spars,0,this.pars,0,this.pars.length);
            this.defs = null;
        }
        else
        {
            varargs = null;
            this.pars = spars;
            this.defs = ldefs;
        }

        if(scope == null)
            throw new NullPointerException();
        this.scope = scope;
    }
    
    public final boolean isGenerator() { return generator; }
    public final boolean isClosure() { return name.isEmpty(); }
    public final boolean hasAssignation() { return assignationMode >= 0; }
    public final boolean hasDefaults() { return defs != null; }
    
    public final String getName() { return name; }
    public final boolean isVarargs() { return varargs != null; }
    
    public final ParsedCode getAssignation() { return assignation; }
    
    public final int getParameterCount() { return pars.length; }
    public final String getParameterName(int index) { return pars[index]; }
    
    public final String getVarargsParameterName() { return varargs; }
    
    public final int getDefaultCount() { return defs == null ? 0 : defs.length; }
    public final Literal getDefault(int index) { return defs[index]; }
    
    public final Literal[] getDefaults()
    {
        Literal[] copy = new Literal[defs.length];
        System.arraycopy(defs, 0, copy, 0, defs.length);
        return copy;
    }
    
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
    
    public static final FunctionLiteral closure(boolean generator, Block<?> pars, Scope scope) throws CompilerError
    {
        return new FunctionLiteral(generator, null, pars, scope);
    }
    
    public static final FunctionLiteral function(boolean generator, ParsedCode assignation, Block<?> pars, Scope scope) throws CompilerError
    {
        if(assignation == null)
            throw new IllegalStateException();
        return new FunctionLiteral(generator, assignation, pars, scope);
    }
}

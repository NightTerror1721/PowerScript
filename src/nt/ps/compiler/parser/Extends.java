/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import nt.ps.compiler.exception.CompilerError;

/**
 *
 * @author mpasc
 */
public final class Extends extends ParsedCode
{
    private final ParsedCode parent;
    private final MutableLiteral object;
    
    public Extends(ParsedCode parent, MutableLiteral object) throws CompilerError
    {
        this.parent = parent;
        if(!object.isLiteralObject())
            throw new CompilerError("Expected a literal object in extends operator");
        this.object = object;
    }
    
    @Override
    public final String toString() { return "extends " + parent + " " + object; }

    @Override
    public final CodeType getCodeType() { return CodeType.EXTENDS; }
    
    public final ParsedCode getParent() { return parent; }
    public final MutableLiteral getObject() { return object; }
}

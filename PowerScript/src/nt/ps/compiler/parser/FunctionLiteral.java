/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

/**
 *
 * @author Asus
 */
public class FunctionLiteral extends ParsedCode
{
    private final String name;
    private final boolean varargs;
    private final Block args;
    
    @Override
    public final CodeType getCodeType() { return CodeType.FUNCTION; }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

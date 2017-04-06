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
public class Assignation extends ParsedCode
{
    private final AssignationSymbol symbol;
    
    
    @Override
    public final CodeType getCodeType() { return CodeType.ASSIGNATION; }
    
    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

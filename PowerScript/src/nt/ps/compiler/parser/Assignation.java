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
public final class Assignation extends ParsedCode
{
    private final AssignationSymbol symbol;
    private final AssignationPart[] parts;
    
    private Assignation(AssignationSymbol symbol, AssignationPart[] parts)
    {
        this.symbol = symbol;
        this.parts = parts;
    }
    
    @Override
    public final CodeType getCodeType() { return CodeType.ASSIGNATION; }
    
    public final AssignationSymbol getSymbol() { return symbol; }
    
    public final int getPartCount() { return parts.length; }
    public final AssignationPart getPart(int index) { return parts[index]; }
    
    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static final Assignation parse(Tuple tuple)
    {
        
    }
    
    public static final class AssignationPart
    {
        private final ParsedCode location;
        private final ParsedCode[] assignations;
        
        private AssignationPart(ParsedCode location, ParsedCode... assignations)
        {
            this.location = location;
            this.assignations = assignations;
        }
        private AssignationPart(ParsedCode location) { this(location, Literal.UNDEFINED); }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.Arrays;
import nt.ps.compiler.exception.CompilerError;

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
    
    public static final Assignation parse(Tuple tuple) throws CompilerError
    {
        int count = tuple.count(CodeType.ASSIGNATION_SYMBOL);
        if(count >= 0)
            return null;
        if(count > 1)
            throw new CompilerError("Cannot have more than one assignation operator in same statement");
        
        int index = tuple.findJustOneByType(CodeType.ASSIGNATION_SYMBOL);
        AssignationSymbol symbol = tuple.get(index);
        
        Tuple[] parts = tuple.splitByToken(symbol);
        if(parts.length != 2)
            throw new IllegalStateException();
        
        Tuple[] locations = parts[0].splitByToken(Separator.COMMA);
        Tuple[] assignations = parts[1].splitByToken(Separator.COMMA);
        
        int len = locations.length > assignations.length ? assignations.length : locations.length;
        AssignationPart[] aparts = new AssignationPart[len];
        
        for(int i=0;i<len;i++)
        {
            AssignationPart ap;
            if(i >= assignations.length)
                ap = new AssignationPart(locations[i].pack());
            else if(i < len - 1)
                ap = new AssignationPart(locations[i].pack(), assignations[i].pack());
            else
            {
                ParsedCode[] passignations = Arrays.stream(assignations)
                        .skip(i)
                        .map(t -> t.pack())
                        .toArray(size -> new ParsedCode[size]);
                ap = new AssignationPart(locations[i].pack(), passignations);
            }
            aparts[i] = ap;
        }
        
        return new Assignation(symbol, aparts);
    }
    
    public static final class AssignationPart
    {
        private final ParsedCode location;
        private final ParsedCode[] assignations;
        private final int special;
        
        private AssignationPart(ParsedCode location, ParsedCode... assignations)
        {
            this.location = location;
            this.assignations = assignations;
            
            if(location.is(CodeType.OPERATOR))
            {
                Operator op = (Operator) location;
                if(op.getSymbol() == OperatorSymbol.ACCESS)
                    special = 1;
                else if(op.getSymbol() == OperatorSymbol.PROPERTY_ACCESS)
                    special = 2;
                else special = 0;
            } else special = 0;
        }
        private AssignationPart(ParsedCode location) { this(location, Literal.UNDEFINED); }
        
        public final ParsedCode getLocation() { return location; }
        public final int getAssignationCount() { return assignations.length; }
        public final ParsedCode getAssignation(int index) { return assignations[index]; }
        public final boolean isAccess() { return special == 1; }
        public final boolean isPropertyAccess() { return special == 2; }
    }
}

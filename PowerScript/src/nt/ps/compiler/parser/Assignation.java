/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

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
    
    public final boolean hasIdentifiersOnly()
    {
        for(int i=0;i<parts.length;i++)
            if(!parts[i].isIdentifiersOnly())
                return false;
        return true;
    }
    
    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static final Assignation parse(Tuple tuple) throws CompilerError
    {
        int count = tuple.count(CodeType.ASSIGNATION_SYMBOL);
        if(count <= 0)
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
            if(i < len - 1)
                aparts[i] = new AssignationPart(locations[i].pack(), assignations[i].pack());
            else
            {
                if(assignations.length == locations.length)
                    aparts[i] = new AssignationPart(locations[i].pack(), assignations[i].pack());
                else if(assignations.length < locations.length)
                {
                    ParsedCode assignation = assignations[i].pack();
                    if(assignation.is(CodeType.OPERATOR) && ((Operator) assignation).getSymbol().isCallable())
                    {
                        ParsedCode[] plocs = Tuple.mapArray(i, locations, t -> t.pack(), new ParsedCode[locations.length - i]);
                        aparts[i] = new AssignationPart(plocs, assignation);
                    }
                    else
                    {
                        aparts[i] = new AssignationPart(locations[i].pack(), assignation);
                        for(i++;i<locations.length;i++)
                            aparts[i] = new AssignationPart(locations[i].pack());
                    }
                }
                else throw new CompilerError("Cannot assign more operations than assignators");
            }
        }
        
        return new Assignation(symbol, aparts);
    }
    
    public static final class Location
    {
        private final ParsedCode location;
        private final int special;
        
        private Location(ParsedCode location) throws CompilerError
        {
            this.location = location;
            
            if(location.is(CodeType.OPERATOR))
            {
                Operator op = (Operator) location;
                if(op.getSymbol() == OperatorSymbol.ACCESS)
                    special = 1;
                else if(op.getSymbol() == OperatorSymbol.PROPERTY_ACCESS)
                    special = 2;
                else throw new CompilerError("Invalid operator in left assignation part: " + location);
            }
            else if(location.is(CodeType.IDENTIFIER))
                special = 0;
            else throw new CompilerError("Invalid code in left assignation part: " + location);
        }
        
        public final ParsedCode getCode() { return location; }
        public final boolean isIdentifier() { return special == 0; }
        public final boolean isAccess() { return special == 1; }
        public final boolean isPropertyAccess() { return special == 2; }
    }
    
    public static final class AssignationPart
    {
        private final Location[] locations;
        private final ParsedCode assignation;
        
        private AssignationPart(ParsedCode[] locations, ParsedCode assignation) throws CompilerError
        {
            if(locations.length <= 0)
                throw new IllegalStateException();
            this.locations = new Location[locations.length];
            for(int i=0;i<locations.length;i++)
                this.locations[i] = new Location(locations[i]);
            
            this.assignation = assignation;
        }
        private AssignationPart(ParsedCode location) throws CompilerError { this(location, Literal.UNDEFINED); }
        private AssignationPart(ParsedCode location, ParsedCode assignation) throws CompilerError { this(new ParsedCode[]{ location }, assignation); }
        
        public final Location getLocation(int index) { return locations[index]; }
        public final int getLocationCount() { return locations.length; }
        public final ParsedCode getAssignation() { return assignation; }
        public final boolean isIdentifiersOnly()
        {
            for(Location loc : locations)
                if(!loc.isIdentifier())
                    return false;
            return true;
        }
        
    }
}

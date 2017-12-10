/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.HashMap;
import static nt.ps.compiler.parser.Code.collect;

/**
 *
 * @author Asus
 */
public final class AssignationSymbol extends Code
{
    private final String symbol;
    private final OperatorSymbol associatedOperator;
    
    private AssignationSymbol(String symbol, OperatorSymbol associatedOperator)
    {
        this.symbol = symbol;
        this.associatedOperator = associatedOperator;
    }
    
    @Override
    public final CodeType getCodeType() { return CodeType.ASSIGNATION_SYMBOL; }
    
    @Override
    public final String toString() { return symbol; }
    
    public final String getSymbol() { return symbol; }
    
    public final boolean containsOperator() { return associatedOperator != null; }
    public final OperatorSymbol getAssociatedOperatorSymbol() { return associatedOperator; }
    
    public static final AssignationSymbol
            ASSIGNATION = new AssignationSymbol("=",null),
            
            ASSIGNATION_PLUS = new AssignationSymbol("+=",OperatorSymbol.PLUS),
            ASSIGNATION_MINUS = new AssignationSymbol("-=",OperatorSymbol.MINUS),
            ASSIGNATION_MULTIPLY = new AssignationSymbol("*=",OperatorSymbol.MULTIPLY),
            ASSIGNATION_DIVIDE = new AssignationSymbol("/=",OperatorSymbol.DIVIDE),
            ASSIGNATION_MODULE = new AssignationSymbol("%=",OperatorSymbol.MODULE),
            
            ASSIGNATION_SHIFT_LEFT = new AssignationSymbol("<<=",OperatorSymbol.SHIFT_LEFT),
            ASSIGNATION_SHIFT_RIGHT = new AssignationSymbol(">>=",OperatorSymbol.SHIFT_RIGHT),
            
            ASSIGNATION_LOGIC_AND = new AssignationSymbol("&=",OperatorSymbol.LOGIC_AND),
            ASSIGNATION_LOGIC_OR = new AssignationSymbol("|=",OperatorSymbol.LOGIC_OR),
            ASSIGNATION_LOGIC_XOR = new AssignationSymbol("^=",OperatorSymbol.LOGIC_XOR);
    
    private static final HashMap<String, AssignationSymbol> HASH = collect(AssignationSymbol.class,os -> os.symbol);
    
    public static final boolean isAssignation(String str) { return HASH.containsKey(str); }
    public static final boolean isAssignation(char c) { return isAssignation(String.valueOf(c)); }
    
    public static final AssignationSymbol getAssignationSymbol(String str) { return HASH.get(str); }
    public static final AssignationSymbol getAssignationSymbol(char c) { return getAssignationSymbol(String.valueOf(c)); }
}

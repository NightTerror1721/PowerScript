/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.Objects;

/**
 *
 * @author Asus
 */
public final class Operator extends CodeObject
{
    private final OperatorSymbol symbol;
    private final CodeObject[] operands;
    
    public Operator(OperatorSymbol symbol, CodeObject op0, CodeObject op1, CodeObject op2)
    {
        if(symbol == null)
            throw new NullPointerException();
        this.symbol = symbol;
        operands = new CodeObject[]{ Objects.requireNonNull(op0), Objects.requireNonNull(op1), Objects.requireNonNull(op2) };
    }
    
    public Operator(OperatorSymbol symbol, CodeObject op0, CodeObject op1)
    {
        if(symbol == null)
            throw new NullPointerException();
        this.symbol = symbol;
        operands = new CodeObject[]{ Objects.requireNonNull(op0), Objects.requireNonNull(op1) };
    }
    
    public Operator(OperatorSymbol symbol, CodeObject op0)
    {
        if(symbol == null)
            throw new NullPointerException();
        this.symbol = symbol;
        operands = new CodeObject[]{ Objects.requireNonNull(op0) };
    }
    
    public final OperatorSymbol getSymbol() { return symbol; }
    
    public final int getOperandCount() { return operands.length; }
    
    public final CodeObject getOperand(int index) { return operands[index]; }
    public final void setOperand(int index, CodeObject operand)
    {
        if(operand == null)
            throw new NullPointerException();
        operands[index] = operand;
    }
    
    @Override
    public final CodeType getCodeType() { return CodeType.OPERATOR; }
    
    @Override
    public final String toString() { return symbol.toString(); }
    
    /*public boolean isUnaryOperator() { return operands.length == 1; }
    public boolean isBinaryOperator() { return operands.length == 2; }
    public boolean isTernaryOperator() { return operands.length == 3; }*/
}

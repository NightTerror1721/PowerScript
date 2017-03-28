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
public abstract class Operator extends CodeObject
{
    private final OperatorSymbol symbol;
    private final CodeObject[] operands;
    
    private Operator(OperatorSymbol symbol, CodeObject op0, CodeObject op1, CodeObject op2)
    {
        if(symbol == null)
            throw new NullPointerException();
        this.symbol = symbol;
        operands = new CodeObject[]{ op0, op1, op2 };
    }
    
    private Operator(OperatorSymbol symbol, CodeObject op0, CodeObject op1)
    {
        if(symbol == null)
            throw new NullPointerException();
        this.symbol = symbol;
        operands = new CodeObject[]{ op0, op1 };
    }
    
    private Operator(OperatorSymbol symbol, CodeObject op0)
    {
        if(symbol == null)
            throw new NullPointerException();
        this.symbol = symbol;
        operands = new CodeObject[]{ op0 };
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
    public final String toString() { return symbol.toString(); }
    
    /*public boolean isUnaryOperator() { return operands.length == 1; }
    public boolean isBinaryOperator() { return operands.length == 2; }
    public boolean isTernaryOperator() { return operands.length == 3; }*/
}

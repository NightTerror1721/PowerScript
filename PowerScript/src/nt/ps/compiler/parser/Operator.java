/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.Objects;
import nt.ps.compiler.exception.CompilerError;

/**
 *
 * @author Asus
 */
public final class Operator extends CodeObject
{
    private final OperatorSymbol symbol;
    private final ParsedCode[] operands;
    
    public Operator(OperatorSymbol symbol, ParsedCode op0, ParsedCode op1, ParsedCode op2) throws CompilerError
    {
        if(symbol == null)
            throw new NullPointerException();
        this.symbol = symbol;
        operands = new ParsedCode[]{ check(op0), check(op1), check(op2) };
    }
    
    public Operator(OperatorSymbol symbol, ParsedCode op0, ParsedCode op1) throws CompilerError
    {
        if(symbol == null)
            throw new NullPointerException();
        this.symbol = symbol;
        operands = new ParsedCode[]{ check(op0), check(op1) };
    }
    
    public Operator(OperatorSymbol symbol, ParsedCode op0) throws CompilerError
    {
        if(symbol == null)
            throw new NullPointerException();
        this.symbol = symbol;
        operands = new ParsedCode[]{ check(op0) };
    }
    
    private Operator(OperatorSymbol symbol, ParsedCode[] ops)
    {
        if(symbol == null)
            throw new NullPointerException();
        this.symbol = symbol;
        if(ops == null)
            throw new NullPointerException();
        operands = ops;
    }
    
    public static final Operator newOperator(ParsedCode identifier, Block parameters)
    {
        if(identifier == null)
            throw new NullPointerException();
        if(parameters == null)
            throw new NullPointerException();
        if(!parameters.isArgumentsList())
            throw new IllegalStateException();
        ParsedCode[] ops = new ParsedCode[parameters.getCodeCount() + 1];
        ops[0] = identifier;
        parameters.putInArray(ops,1);
        return new Operator(OperatorSymbol.NEW, ops);
    }
    
    public static final Operator callOperator(ParsedCode preOperand, Block parameters)
    {
        if(preOperand == null)
            throw new NullPointerException();
        if(parameters == null)
            throw new NullPointerException();
        if(!parameters.isArgumentsList())
            throw new IllegalStateException();
        ParsedCode[] ops = new ParsedCode[parameters.getCodeCount() + 1];
        ops[0] = preOperand;
        parameters.putInArray(ops,1);
        return new Operator(OperatorSymbol.CALL, ops);
    }
    
    public static final Operator invokeOperator(Identifier identifier, ParsedCode preOperand, Block parameters)
    {
        if(preOperand == null)
            throw new NullPointerException();
        if(parameters == null)
            throw new NullPointerException();
        if(!parameters.isArgumentsList())
            throw new IllegalStateException();
        ParsedCode[] ops = new ParsedCode[parameters.getCodeCount() + 2];
        ops[1] = identifier;
        ops[1] = preOperand;
        parameters.putInArray(ops,2);
        return new Operator(OperatorSymbol.INVOKE, ops);
    }
    
    public final OperatorSymbol getSymbol() { return symbol; }
    
    public final int getOperandCount() { return operands.length; }
    
    public final ParsedCode getOperand(int index) { return operands[index]; }
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
    
    private static ParsedCode check(ParsedCode op) throws CompilerError
    {
        if(op == null)
            throw new NullPointerException();
        if(!op.isValidCodeObject())
            throw CompilerError.unexpectedCode(op);
        return op;
    }
}

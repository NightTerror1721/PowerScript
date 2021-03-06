/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.util.LinkedList;
import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.parser.AssignationSymbol;
import nt.ps.compiler.parser.Code;
import nt.ps.compiler.parser.CommandWord;
import nt.ps.compiler.parser.Identifier;
import nt.ps.compiler.parser.Literal;
import nt.ps.compiler.parser.OperatorSymbol;
import nt.ps.compiler.parser.Tuple;

/**
 *
 * @author Asus
 */
final class InstructionBuilder
{
    private final StringBuilder sb = new StringBuilder(16);
    private final LinkedList<Code> codes = new LinkedList<>();
    
    public final InstructionBuilder append(byte value) { sb.append(value); return this; }
    public final InstructionBuilder append(short value) { sb.append(value); return this; }
    public final InstructionBuilder append(int value) { sb.append(value); return this; }
    public final InstructionBuilder append(long value) { sb.append(value); return this; }
    public final InstructionBuilder append(float value) { sb.append(value); return this; }
    public final InstructionBuilder append(double value) { sb.append(value); return this; }
    public final InstructionBuilder append(boolean value) { sb.append(value); return this; }
    public final InstructionBuilder append(char value) { sb.append(value); return this; }
    public final InstructionBuilder append(CharSequence value) { sb.append(value); return this; }
    public final InstructionBuilder append(String value) { sb.append(value); return this; }
    public final InstructionBuilder append(Object value) { sb.append(value); return this; }
    
    public final int length() { return sb.length(); }
    public final boolean isEmpty() { return sb.length() <= 0; }
    public final void clear() { sb.delete(0, sb.length()); }
    public final String getAndClear()
    {
        String text = sb.toString();
        sb.delete(0, sb.length());
        return text;
    }
    
    public final char getLastChar() { return sb.charAt(sb.length() - 1); }
    
    public final String get() { return sb.toString(); }
    @Override public final String toString() { return sb.toString(); }
    public final boolean equals(String str) { return sb.toString().equals(str); }
    
    public final InstructionBuilder addCode(Code code)
    {
        if(code == null)
            throw new NullPointerException();
        codes.add(code);
        
        return this;
    }
    
    public final InstructionBuilder replaceCode(int index, Code code)
    {
        codes.set(index, code);
        return this;
    }
    
    public final InstructionBuilder decode() throws CompilerError
    {
        if(!isEmpty())
        {
            boolean unary = codes.isEmpty() || !codes.getLast().isValidCodeObject() || codes.getLast().isElevatorCommand();
            Code code = Literal.decode(sb.toString());
            if(code == null)
            {
                code = Identifier.create(sb.toString(), unary);
                if(code == null)
                    throw new CompilerError("Invalid command: " + sb.toString());
                /*if(code.is(CodeType.COMMAND_WORD) && !codes.isEmpty())
                    throw new CompilerError("Invalid command after first possition in instruction: " + code);*/
            }
            codes.add(code);
            clear();
        }
        return this;
    }
    
    public final InstructionBuilder addOperator(String operator) throws CompilerError
    {
        decode();
        boolean unary = !codes.isEmpty() && (!codes.getLast().isValidCodeObject() ||
                codes.getLast() == CommandWord.RETURN ||
                codes.getLast() == CommandWord.YIELD ||
                codes.getLast() == CommandWord.DELEGATOR_YIELD);
        OperatorSymbol symbol = OperatorSymbol.getOperator(operator, unary);
        if(symbol == null)
            throw new CompilerError("Invalid operator: " + symbol);
        return addCode(symbol);
    }
    
    public final InstructionBuilder addOperator(OperatorSymbol operator) throws CompilerError
    {
        decode();
        return addCode(operator);
    }
    
    public final InstructionBuilder addAssignation(AssignationSymbol assignator) throws CompilerError
    {
        decode();
        return addCode(assignator);
    }
    
    public final int getCodeCount() { return codes.size(); }
    public final boolean hasCodes() { return !codes.isEmpty(); }
    
    public final Code getCode(int index) { return codes.get(index); }
    public final Code getFirstCode() { return codes.getFirst(); }
    public final Code getLastCode() { return codes.getLast(); }
    
    public final Tuple build() { return new Tuple(codes); }
}

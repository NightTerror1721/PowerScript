/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.util.LinkedList;
import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.parser.Code;
import nt.ps.compiler.parser.Code.CodeType;
import nt.ps.compiler.parser.Identifier;

/**
 *
 * @author Asus
 */
final class InstructionBuilder
{
    private final StringBuilder sb = new StringBuilder(16);
    
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
    
    public final String get() { return sb.toString(); }
    @Override public final String toString() { return sb.toString(); }
    public final boolean equals(String str) { return sb.toString().equals(str); }
    
    public final InstructionBuilder decode(LinkedList<Code> codes) throws CompilerError
    {
        if(!isEmpty())
        {
            boolean unary = !codes.isEmpty() && !codes.getLast().isValidCodeObject();
            Code code = Identifier.create(sb.toString(), unary);
            if(code == null)
                throw new CompilerError("Invalid command: " + sb.toString());
            if(code.is(CodeType.COMMAND_WORD) && !codes.isEmpty())
                throw new CompilerError("Invalid command after first possition in instruction: " + code);
            codes.add(code);
            clear();
        }
        return this;
    }
}

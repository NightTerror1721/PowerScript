/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.util.HashMap;
import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.parser.Code;
import nt.ps.compiler.parser.Code.CodeType;
import nt.ps.compiler.parser.Literal;
import nt.ps.lang.PSNumber;
import nt.ps.lang.PSValue;
import org.apache.bcel.generic.InstructionHandle;

/**
 *
 * @author Asus
 */
public final class SwitchModel
{
    private final InstructionHandle startHandle;
    private final HashMap<Integer, InstructionHandle> ints;
    private final HashMap<Float, InstructionHandle> floats;
    private final HashMap<String, InstructionHandle> strs;
    private InstructionHandle defaultTarget;
    
    public SwitchModel(InstructionHandle start)
    {
        if(start == null)
            throw new NullPointerException();
        this.startHandle = start;
        
        ints = new HashMap<>();
        floats = new HashMap<>();
        strs = new HashMap<>();
    }
    
    public final boolean isMultiswitch()
    {
        return (
                (ints.isEmpty() ? 0 : 1) +
                (floats.isEmpty() ? 0 : 1) +
                (strs.isEmpty() ? 0 : 1)
        ) > 1;
    }
    
    public final int getIntCaseCount() { return ints.size(); }
    public final int getFloatCaseCount() { return floats.size(); }
    public final int getStringCaseCount() { return strs.size(); }
    
    public final void addDefaultCase(InstructionHandle target) throws CompilerError
    {
        if(target == null)
            throw new NullPointerException();
        if(defaultTarget != null)
            throw new CompilerError("\"default:\" already exists");
        defaultTarget = target;
    }
    
    public final void addCase(InstructionHandle target, Code code) throws CompilerError
    {
        if(!code.is(CodeType.LITERAL))
            throw new CompilerError("Required valid int, float or string literal in switch cases");
        addCase(target, (Literal) code);
    }
    
    public final void addCase(InstructionHandle target, Literal literal) throws CompilerError
    {
        if(target == null)
            throw new NullPointerException();
        PSValue value = literal.getValue();
        if(value.isNumber())
        {
            PSNumber num = (PSNumber) value;
            if(num.isDecimal())
            {
                Float f = num.toJavaFloat();
                if(floats.containsKey(f))
                    throw new CompilerError("\"case " + f + ":\" already exists");
                floats.put(f, target);
            }
            else
            {
                Integer i = num.toJavaInt();
                if(ints.containsKey(i))
                    throw new CompilerError("\"case " + i + ":\" already exists");
                ints.put(i, target);
            }
        }
        else if(value.isString())
        {
            String s = value.toJavaString();
            if(strs.containsKey(s))
                throw new CompilerError("\"case \"" + s + "\":\" already exists");
            strs.put(s, target);
        }
        else throw new CompilerError("Required valid int, float or string literal in switch cases");
    }
}

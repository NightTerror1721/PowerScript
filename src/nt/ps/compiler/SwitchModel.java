/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
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
    
    public final int getSwitchTypeCount()
    {
        return (ints.isEmpty() ? 0 : 1) + (floats.isEmpty() ? 0 : 1) + (strs.isEmpty() ? 0 : 1);
    }
    
    public final InstructionHandle getStartHandle() { return startHandle; }
    
    public final int getIntCaseCount() { return ints.size(); }
    public final int getFloatCaseCount() { return floats.size(); }
    public final int getStringCaseCount() { return strs.size(); }
    
    public final Iterable<Case<Integer>> intCases() { return () -> new CaseIterator(ints); }
    public final Iterable<Case<Float>> floatCases() { return () -> new CaseIterator(floats); }
    public final Iterable<Case<String>> stringCases() { return () -> new CaseIterator(strs); }
    
    public final void addDefaultCase(InstructionHandle target) throws CompilerError
    {
        if(target == null)
            throw new NullPointerException();
        if(defaultTarget != null)
            throw new CompilerError("\"default:\" already exists");
        defaultTarget = target;
    }
    public final InstructionHandle getDefaultCase()
    {
        if(defaultTarget == null)
            throw new NullPointerException();
        return defaultTarget;
    }
    public final boolean hasDefaultCase() { return defaultTarget != null; }
    
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
    
    private static final class CaseIterator<V> implements Iterator<Case<V>>
    {
        private final Iterator<Map.Entry<V, InstructionHandle>> it;
        private final Case<V> currentCase;
        
        private CaseIterator(Map<V, InstructionHandle> map)
        {
            it = map.entrySet().iterator();
            currentCase = new Case<>();
        }

        @Override
        public final boolean hasNext() { return it.hasNext(); }

        @Override
        public final Case<V> next()
        {
            Map.Entry<V, InstructionHandle> e = it.next();
            currentCase.set(e.getKey(), e.getValue());
            return currentCase;
        }
    }
    
    public static final class Case<V>
    {
        private V value;
        private InstructionHandle target;
        private int hashcode;
        
        private void set(V value, InstructionHandle target)
        {
            this.value = value;
            this.target = target;
            this.hashcode = Objects.hashCode(value);
        }
        
        public final V getValue() { return value; }
        public final InstructionHandle getTarget() { return target; }
        public final int getHashCode() { return hashcode; }
        
        @Override
        public final int hashCode() { return getHashCode(); }
    }
}

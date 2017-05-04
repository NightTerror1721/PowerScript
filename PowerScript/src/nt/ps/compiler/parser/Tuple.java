/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.parser.Code.CodeType;

/**
 *
 * @author Asus
 */
public final class Tuple
{
    private final Code[] code;
    
    public Tuple(Code... code)
    {
        this.code = code;
    }
    public Tuple(List<Code> c)
    {
        this(c.toArray(new Code[c.size()]));
    }
    public Tuple(Collection<? extends Code> c)
    {
        this(c.toArray(new Code[c.size()]));
    }
    
    public final void set(int index, Code code)
    {
        if(code == null)
            throw new NullPointerException();
        this.code[index] = code;
    }
    
    public final <C extends Code> C get(int index) { return (C) code[index]; }
    
    public final int length() { return code.length; }
    public final boolean isEmpty() { return code.length == 0; }
    
    public final Tuple subTuple(int offset, int length)
    {
        Code[] array = new Code[length];
        System.arraycopy(code,offset,array,0,length);
        return new Tuple(array);
    }
    public final Tuple subTuple(int offset) { return subTuple(offset,code.length-offset); }
    public final Tuple copy() { return subTuple(0,code.length); }
    
    public final Tuple concat(Tuple ct) { return concat(ct.code); }
    public final Tuple concat(List<Code> code) { return concat(code.toArray(new Code[code.size()])); }
    public final Tuple concat(Code... code)
    {
        Code[] array = new Code[this.code.length + code.length];
        System.arraycopy(this.code,0,array,0,this.code.length);
        System.arraycopy(code,0,array,this.code.length,code.length);
        return new Tuple(array);
    }
    
    public final Tuple wrap(Tuple start, Tuple end) { return wrap(start.code, end.code); }
    public final Tuple wrap(Code start, Code end) { return wrap(new Code[]{start}, new Code[]{end}); }
    public final Tuple wrap(Code[] start, Code[] end)
    {
        Code[] array = new Code[start.length + code.length + end.length];
        System.arraycopy(start,0,array,0,start.length);
        System.arraycopy(code,0,array,start.length,code.length);
        System.arraycopy(end,0,array,start.length+code.length,end.length);
        return new Tuple(array);
    }
    
    public final Tuple extract(Code start, Code end)
    {
        boolean init = false;
        int offset = -1, len = -1, idx = -1;
        for(Code cp : code)
        {
            idx++;
            if(!init)
            {
                if(!cp.equals(start))
                    continue;
                init = true;
                offset = idx;
                continue;
            }
            if(cp.equals(end))
                break;
            len++;
        }
        return subTuple(offset,len);
    }
    
    public final int count(Code codePart)
    {
        int count = 0;
        for(Code cp : code)
            if(cp.equals(codePart))
                count++;
        return count;
    }
    
    public final boolean has(Code codePart)
    {
        for(Code cp : code)
            if(cp.equals(codePart))
                return true;
        return false;
    }
    
    public final int count(CodeType type)
    {
        int count = 0;
        for(Code cp : code)
            if(cp.getCodeType() == type)
                count++;
        return count;
    }
    
    public final int findJustOneByType(CodeType type) throws CompilerError
    {
        int count = 0;
        for(Code cp : code)
        {
            if(cp.getCodeType() == type)
                return count;
            count++;
        }
        return -1;
    }
    
    public final Tuple[] splitByToken(Code sep)
    {
        return splitByToken(sep,-1);
    }
    public final Tuple[] splitByToken(Code sep, int limit)
    {
        LinkedList<Tuple> part = new LinkedList<>();
        LinkedList<Code> tokens = new LinkedList<>();

        for(Code cp : code)
        {
            if(cp.equals(sep) && limit != 0)
            {
                part.add(new Tuple(tokens));
                tokens.clear();
                limit--;
                continue;
            }
            tokens.add(cp);
        }
        if(!part.isEmpty())
            part.add(new Tuple(tokens));
        tokens.clear();
        return part.toArray(new Tuple[part.size()]);
    }
    
    public final boolean isTuple() { return true; }
    
    @Override
    public final String toString() { return code.toString(); }
    
    private static boolean isPostIncDec(Code code)
    {
        return code == OperatorSymbol.INCREMENT || code == OperatorSymbol.DECREMENT;
    }
    
    private ParsedCode packPreUnary(Counter it) throws CompilerError
    {
        Code part = code[it.value];
        it.increase();
        if(part.is(CodeType.OPERATOR_SYMBOL))
        {
            if(it.end())
                throw CompilerError.unexpectedEndOfInstruction();
            OperatorSymbol prefix = (OperatorSymbol) part;
            if(!prefix.canBeUnary() || !prefix.hasUnaryLeftOrder())
                throw new CompilerError("Operator " + prefix + " cannot be an unary prefix operator");
            part = packPreUnary(it);
            if(!part.isValidCodeObject())
                throw CompilerError.unexpectedCode(part);
            return new Operator(prefix, (CodeObject) part);
        }
        if(!part.isParsedCode())
            throw CompilerError.unexpectedCode(part);
        return (ParsedCode) part;
    }
    
    private ParsedCode packPostUnary(Counter it, ParsedCode pre) throws CompilerError
    {
        if(it.end())
            return pre;
        Code part = code[it.value];
        
        if(part.is(CodeType.OPERATOR_SYMBOL))
        {
            OperatorSymbol sufix = (OperatorSymbol) part;
            if(!sufix.canBeUnary())
                return pre;
            it.increase();
            if(!sufix.hasUnaryRightOrder())
                throw new CompilerError("Operator " + sufix + " cannot be an unary sufix operator");
            if(!pre.isValidCodeObject())
                throw CompilerError.unexpectedCode(pre);
            return packPostUnary(it, new Operator(sufix, (CodeObject) pre));
        }
        return pre;
    }
    
    private ParsedCode packPart(Counter it) throws CompilerError
    {
        if(it.end())
            throw CompilerError.unexpectedEndOfInstruction();
        return packPostUnary(it,packPreUnary(it));
    }
    
    private OperatorSymbol findNextOperatorSymbol(int index)
    {
        for(int i=index;i<code.length;i++)
            if(code[i].is(CodeType.OPERATOR_SYMBOL))
                return (OperatorSymbol) code[i];
        return null;
    }
    
    private ParsedCode packOperation(Counter it, ParsedCode operand1) throws CompilerError
    {
        if(!code[it.value].is(CodeType.OPERATOR_SYMBOL))
            throw new CompilerError("Expected a valid operator between operands. \"" + code[it.value] + "\"");
        OperatorSymbol operator = (OperatorSymbol) code[it.value];
        it.increase();
        if(operator.canBeBinary())
        {
            OperatorSymbol nextOperator = findNextOperatorSymbol(it.value);
        }
        
    }
    
    public final ParsedCode pack() throws CompilerError
    {
        Assignation a = Assignation.parse(this);
        if(a != null)
            return a;
        Counter it = new Counter();
        ParsedCode operand = packPart(it);
        if(it.end())
            return operand;
        return packOperation(it, operand);
    }
    
    private final class Counter
    {
        private int value;
        private final int limit;
        
        private Counter(int initialValue)
        {
            this.value = initialValue;
            this.limit = code.length;
        }
        private Counter() { this(0); }
        
        public final int increase(int times) { return value += times; }
        public final int increase() { return increase(1); }
        public final int decrease(int times) { return value -= times; }
        public final int decrease() { return decrease(1); }
        public final int value() { return value; }
        public final boolean end() { return value < limit; }
    }
}

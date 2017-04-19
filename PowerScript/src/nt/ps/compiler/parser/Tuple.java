/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.parser.Code.CodeType;

/**
 *
 * @author Asus
 */
public final class Tuple
{
    private final List<Code> code;
    
    public Tuple(ArrayList<Code> list)
    {
        if(list == null)
            throw new NullPointerException();
        this.code = list;
    }
    public Tuple(Code... code)
    {
        this(Arrays.asList(code));
    }
    public Tuple(List<Code> c)
    {
        this(new ArrayList<>(c));
    }
    public Tuple(Collection<? extends Code> c)
    {
        this(new ArrayList<>(c));
    }
    
    public final void set(int index, Code code)
    {
        if(code == null)
            throw new NullPointerException();
        this.code.set(index,code);
    }
    
    public final <C extends Code> C get(int index) { return (C) code.get(index); }
    
    public final int length() { return code.size(); }
    public final boolean isEmpty() { return code.isEmpty(); }
    
    public final Tuple subTuple(int offset, int length)
    {
        return new Tuple(code.subList(offset,offset + length));
    }
    public final Tuple subTuple(int offset) { return subTuple(offset,code.size()-offset); }
    public final Tuple copy() { return subTuple(0,code.size()); }
    
    public final Tuple concat(Tuple ct)
    {
        List<Code> newCode = new ArrayList<>(code.size() + ct.code.size());
        newCode.addAll(code);
        newCode.addAll(ct.code);
        return new Tuple(newCode);
    }
    
    public final Tuple concat(List<Code> code)
    {
        if(code.isEmpty())
            return this;
        List<Code> newCode = new ArrayList<>(this.code.size() + code.size());
        newCode.addAll(this.code);
        newCode.addAll(code);
        return new Tuple(newCode);
    }
    public final Tuple concat(ParsedCode... code)
    {
        return concat(Arrays.asList(code));
    }
    
    public final Tuple wrap(Tuple start, Tuple end)
    {
        ArrayList<Code> newCode = new ArrayList<>(start.code.size() + code.size() + end.code.size());
        newCode.addAll(start.code);
        newCode.addAll(code);
        newCode.addAll(end.code);
        return new Tuple(newCode);
    }
    
    public final Tuple wrap(Code start, Code end)
    {
        ArrayList<Code> newCode = new ArrayList<>(code.size() + 2);
        newCode.add(start);
        newCode.addAll(code);
        newCode.add(end);
        return new Tuple(newCode);
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
        Code part = code.get(it.value);
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
        Code part = code.get(it.value);
        
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
        ListIterator<Code> it = code.listIterator(index);
        while(it.hasNext())
        {
            Code c = it.next();
            if(c.is(CodeType.OPERATOR_SYMBOL))
                return (OperatorSymbol) c;
        }
        return null;
    }
    
    public final ParsedCode pack()
    {
        
    }
    
    private static final class Counter
    {
        private int value;
        private final int limit;
        
        private Counter(int limit, int initialValue)
        {
            this.value = initialValue;
            this.limit = limit;
        }
        private Counter(int limit) { this(limit,0); }
        
        public final int increase(int times) { return value += times; }
        public final int increase() { return increase(1); }
        public final int decrease(int times) { return value -= times; }
        public final int decrease() { return decrease(1); }
        public final int value() { return value; }
        public final boolean end() { return value < limit; }
    }
}

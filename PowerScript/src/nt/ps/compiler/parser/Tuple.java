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
import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.parser.Code.CodeType;

/**
 *
 * @author Asus
 */
public final class Tuple
{
    private final List<Code> code;
    
    public Tuple(List<Code> list)
    {
        if(list == null)
            throw new NullPointerException();
        this.code = list;
    }
    public Tuple(Code... code)
    {
        this(Arrays.asList(code));
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
    
    public final Code get(int index) { return code.get(index); }
    
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
    
    public final ParsedCode pack()
    {
        
    }
}

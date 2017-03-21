/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author Asus
 */
public class CodeTuple extends CodePart
{
    private final CodePart[] code;
    
    public CodeTuple(CodePart... code)
    {
        if(code == null)
            throw new NullPointerException();
        this.code = code;
    }
    public CodeTuple(Collection<? extends CodePart> c)
    {
        this(c.toArray(new CodePart[c.size()]));
    }
    
    public final void set(int index, CodePart code)
    {
        if(code == null)
            throw new NullPointerException();
        this.code[index] = code;
    }
    
    public final CodePart get(int index) { return code[index]; }
    
    public final int length() { return code.length; }
    public final boolean isEmpty() { return code.length == 0; }
    
    public final CodeTuple subTuple(int offset, int length)
    {
        if(offset < 0 || offset >= code.length)
            throw new IllegalArgumentException("offset out of range");
        if(offset + length > code.length)
            throw new IllegalArgumentException();
        CodePart[] copy = new CodePart[length];
        System.arraycopy(code,offset,copy,0,length);
        return new CodeTuple(copy);
    }
    public final CodeTuple subTuple(int offset) { return subTuple(offset,code.length-offset); }
    public final CodeTuple copy() { return subTuple(0,code.length); }
    
    public final CodeTuple concat(CodeTuple ct)
    {
        CodePart[] newCode = new CodePart[code.length + ct.code.length];
        System.arraycopy(code,0,newCode,0,code.length);
        System.arraycopy(ct.code,0,newCode,code.length,ct.code.length);
        return new CodeTuple(newCode);
    }
    
    public final CodeTuple concat(CodePart... code)
    {
        if(code.length == 0)
            return this;
        CodePart[] newCode = new CodePart[this.code.length + code.length];
        System.arraycopy(this.code,0,newCode,0,this.code.length);
        System.arraycopy(code,0,newCode,this.code.length,code.length);
        return new CodeTuple(newCode);
    }
    
    public final CodeTuple wrap(CodeTuple start, CodeTuple end)
    {
        CodePart[] newCode = new CodePart[start.code.length + code.length + end.code.length];
        System.arraycopy(start.code,0,newCode,0,start.code.length);
        System.arraycopy(code,0,newCode,start.code.length,code.length);
        System.arraycopy(end.code,0,newCode,start.code.length + code.length,code.length);
        return new CodeTuple(newCode);
    }
    
    public final CodeTuple wrap(CodePart start, CodePart end)
    {
        CodePart[] newCode = new CodePart[code.length + 2];
        System.arraycopy(code,0,newCode,1,code.length);
        newCode[0] = start;
        newCode[newCode.length - 1] = end;
        return new CodeTuple(newCode);
    }
    
    public final CodeTuple extract(CodePart start, CodePart end)
    {
        boolean init = false;
        int offset = -1, len = -1, idx = -1;
        for(CodePart cp : code)
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
    
    public final int count(CodePart codePart)
    {
        int count = 0;
        for(CodePart cp : code)
            if(cp.equals(codePart))
                count++;
        return count;
    }
    
    public final CodeTuple[] splitByToken(CodePart sep)
    {
        return splitByToken(sep,-1);
    }
    public final CodeTuple[] splitByToken(CodePart sep, int limit)
    {
        LinkedList<CodeTuple> part = new LinkedList<>();
        LinkedList<CodePart> tokens = new LinkedList<>();

        for(CodePart cp : code)
        {
            if(cp.equals(sep) && limit != 0)
            {
                part.add(new CodeTuple(tokens));
                tokens.clear();
                limit--;
                continue;
            }
            tokens.add(cp);
        }
        if()
        part.add(new CodeTuple(tokens));
        tokens.clear();
        return part.toArray(new CodeTuple[part.size()]);
    }
    
    @Override
    public final boolean isTuple() { return true; }
    
    @Override
    public final String toString() { return Arrays.toString(code); }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import nt.ps.compiler.exception.CompilerError;

/**
 *
 * @author Asus
 */
public final class Stack
{
    private static final int MAX_STACK_CAPACITY = 1 << 12;
    private int total;
    private int temp;
    private int vars;
    
    public Stack()
    {
        total = temp = vars = 0;
    }
    
    public final int push(int amount) throws CompilerError
    {
        if(amount < 1)
            throw new IllegalArgumentException();
        temp += amount;
        if(vars + temp > total)
            total = vars + temp;
        if(total > MAX_STACK_CAPACITY)
            throw new CompilerError("Stack capacity exceded");
        return (vars + temp) - 1;
    }
    public final int push() throws CompilerError { return push(1); }
    
    public final int pop(int amount) throws CompilerError
    {
        if(amount < 1)
            throw new IllegalArgumentException();
        temp -= amount;
        if(temp < 0)
            throw new CompilerError("Stack capacity is under zero");
        return vars + temp;
    }
    public final int pop() throws CompilerError { return pop(1); }
    
    public final int peek() { return vars + temp; }
    
    public final int allocateVariable() throws CompilerError
    {
        vars++;
        if(vars + temp > total)
            total = vars + temp;
        if(total > MAX_STACK_CAPACITY)
            throw new CompilerError("Stack capacity exceded");
        return vars - 1;
    }
    
    public final int deallocateVariables(int amount) throws CompilerError
    {
        if(amount < 1)
            throw new IllegalArgumentException();
        vars -= amount;
        if(vars < 0)
            throw new CompilerError("Variable counter is under zero");
        return vars + temp;
    }
    
    public final int getTotalUsed() { return total; }
    public final int getTempUsed() { return temp; }
    public final int getVarsUsed() { return vars; }
}

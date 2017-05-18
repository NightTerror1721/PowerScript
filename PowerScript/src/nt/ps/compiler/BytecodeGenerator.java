/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import nt.ps.compiler.CompilerBlock.CompilerBlockType;
import nt.ps.lang.PSFunction;

/**
 *
 * @author Asus
 */
public abstract class BytecodeGenerator
{
    public abstract BytecodeGenerator createInstance(String name, int argsLen, int defaultValues, boolean packExtraArgs);
    public abstract PSFunction build(CompilerBlockType type);
    
}

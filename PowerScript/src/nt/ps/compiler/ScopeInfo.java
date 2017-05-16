/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import nt.ps.compiler.parser.Block.Scope;
import org.apache.bcel.generic.InstructionHandle;

/**
 *
 * @author mpasc
 */
public final class ScopeInfo
{
    private final Scope socpe;
    private InstructionHandle startRef;
    private InstructionHandle endRef;
    
    
    public static enum ScopeType
    {
        IF, ELSE, WHILE, FOR, TRY, CATCH
    }
}

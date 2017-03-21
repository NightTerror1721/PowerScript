/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

/**
 *
 * @author Asus
 */
public abstract class Block extends CodePart
{
    public abstract int getInstructionsCount();
    
    @Override
    public final boolean isBlock() { return true; }
    
    
    //public static class 
}

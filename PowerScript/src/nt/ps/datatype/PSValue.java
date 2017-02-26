/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.datatype;

/**
 *
 * @author mpasc
 */
public abstract class PSValue extends PSVarargs
{
    
    @Override public final int numberOfArguments() { return 1; }
    @Override public final PSValue self() { return this; }
    @Override public final PSValue arg(int index) { return index == 0 ? this : UNDEFINED; }
    
    private static final class PSUndefined extends PSValue {}
    public static final PSValue UNDEFINED = new PSUndefined();
    
    private static final class PSNull extends PSValue {}
    public static final PSValue NULL = new PSNull();
}

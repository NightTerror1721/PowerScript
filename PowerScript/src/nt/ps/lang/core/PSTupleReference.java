/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import nt.ps.lang.PSFunction;
import nt.ps.lang.PSTuple;
import nt.ps.lang.PSValue;
import nt.ps.lang.PSVarargs;
import nt.ps.lang.core.ImmutableCoreLibrary.PrimitiveReference;

/**
 *
 * @author Asus
 */
public final class PSTupleReference extends PrimitiveReference
{
    @Override
    public final PSValue createNewInstance() { return new PSTuple(); }
    
    @Override
    public PSValue createNewInstance(PSValue arg0)
    {
        switch(arg0.getPSType())
        {
            default: return createNewInstance();
            case TUPLE: return arg0.toPSTuple().copy();
            case ARRAY: return arg0.toPSArray().asPSTuple();
        }
    }
    
    @Override
    protected final PSVarargs innerCall(PSValue self) { return EMPTY_TUPLE; }

    @Override
    protected final PSVarargs innerCall(PSValue self, PSValue arg0)
    {
        return new PSTuple(arg0.toJavaList());
    }
    
    @Override
    public PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
            case "contract": return CONTRACT;
        }
    }
    
    private static final PSValue CONTRACT = PSFunction.function((args) -> {
        PSValue[] array = new PSValue[args.numberOfArguments()];
        for(int i=0;i<array.length;i++)
            array[i] = args.arg(i);
        return new PSTuple(array);
    });
}

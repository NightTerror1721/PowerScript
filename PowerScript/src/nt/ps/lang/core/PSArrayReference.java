/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import nt.ps.exception.PSRuntimeException;
import nt.ps.lang.PSArray;
import nt.ps.lang.PSFunction;
import nt.ps.lang.PSValue;
import nt.ps.lang.PSVarargs;
import nt.ps.lang.core.ImmutableCoreLibrary.PrimitiveReference;

/**
 *
 * @author Asus
 */
public class PSArrayReference extends PrimitiveReference
{
    @Override
    public final PSValue createNewInstance() { return new PSArray(); }
    
    @Override
    public final PSValue createNewInstance(PSValue arg0)
    {
        switch(arg0.getPSType())
        {
            default: return createNewInstance();
            case TUPLE: return arg0.toPSTuple().asPSArray();
            case ARRAY: return arg0.toPSArray().copy();
            case NUMBER: return new PSArray(arg0.toJavaInt());
        }
    }
    
    @Override
    public final PSValue createNewInstance(PSValue arg0, PSValue arg1) { return createNewInstance(varargsOf(arg0, arg1)); }
    
    @Override
    public final PSValue createNewInstance(PSValue arg0, PSValue arg1, PSValue arg2) { return createNewInstance(varargsOf(arg0, arg1, arg2)); }
    
    @Override
    public final PSValue createNewInstance(PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return createNewInstance(varargsOf(arg0, arg1, arg2, arg3)); }
    
    @Override
    public final PSValue createNewInstance(PSVarargs args)
    {
        int len = args.numberOfArguments();
        if(len < 1)
            return createNewInstance();
        if(len > 255)
            throw new PSRuntimeException("Multidimensional array cannot has more than 255 dimensions");
        return createNestedArray(args, 0, len);
    }
    
    
    private static PSArray createNestedArray(PSVarargs args, int dim, int dimLen)
    {
        int len = args.arg(dim).toJavaInt();
        if(dim + 1 >= dimLen)
            return new PSArray(len);
        
        PSValue[] array = new PSValue[len];
        for(int i=0;i<len;i++)
            array[i] = createNestedArray(args, dim + 1, dimLen);
        return new PSArray(array);
    }
    
    
    @Override
    protected final PSVarargs innerCall(PSValue self) { return new PSArray(); }

    @Override
    protected final PSVarargs innerCall(PSValue self, PSValue arg0)
    {
        return new PSArray(arg0.toJavaList());
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
        return new PSArray(array);
    });
}

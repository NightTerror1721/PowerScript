/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import java.util.StringJoiner;
import nt.ps.exception.PSRuntimeException;
import nt.ps.lang.PSFunction;
import nt.ps.lang.PSString;
import nt.ps.lang.PSValue;
import nt.ps.lang.PSVarargs;
import nt.ps.lang.core.ImmutableCoreLibrary.PrimitiveReference;

/**
 *
 * @author Asus
 */
public final class PSStringReference extends PrimitiveReference
{
    @Override
    public final PSValue createNewInstance() { return new PSString(""); }
    
    @Override
    public PSValue createNewInstance(PSValue arg0) { return new PSString(arg0.toJavaString()); }
    
    @Override
    protected final PSVarargs innerCall(PSValue self) { return new PSString(""); }

    @Override
    protected final PSVarargs innerCall(PSValue self, PSValue arg0) { return new PSString(arg0.toJavaString()); }
    
    @Override
    public PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
            case "join": return JOIN;
        }
    }
    
    private static final PSValue
            JOIN = PSFunction.varFunction((args) -> {
                int len = args.numberOfArguments();
                if(len < 2)
                    throw new PSRuntimeException("Expected 2 o more arguments");
                StringJoiner joiner = new StringJoiner(args.self().toJavaString());
                if(len == 2)
                    for(PSVarargs values : iterate(args.arg(1)))
                        for(PSValue value : iterableVarargs(values))
                            joiner.add(value.toJavaString());
                else for(PSValue value : iterableVarargs(subVarargs(args,1)))
                    joiner.add(value.toJavaString());
                return new PSString(joiner.toString());
            });
}

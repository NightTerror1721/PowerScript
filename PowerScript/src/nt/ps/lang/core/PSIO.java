/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import java.util.StringJoiner;
import nt.ps.lang.PSFunction;
import nt.ps.lang.PSValue;

/**
 *
 * @author Asus
 */
public class PSIO extends ImmutableCoreLibrary
{
    @Override
    public PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
            case "print": return PRINT;
        }
    }
    
    private static final PSValue
            PRINT = PSFunction.voidVarFunction((args) -> {
                switch(args.numberOfArguments())
                {
                    case 0: System.out.println(); break;
                    case 1: System.out.println(args.self()); break;
                    default: {
                        StringJoiner joiner = new StringJoiner(" ");
                        for(PSValue value : iterableVarargs(args))
                            joiner.add(value.toJavaString());
                        System.out.println(joiner.toString());
                    } break;
                }
            });
}

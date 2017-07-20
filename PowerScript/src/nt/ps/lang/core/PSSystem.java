/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import nt.ps.lang.PSFunction;
import nt.ps.lang.PSValue;

/**
 *
 * @author Asus
 */
public final class PSSystem extends ImmutableCoreLibrary
{
    @Override
    public final PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
            case "exit": return EXIT;
            case "getUserDirectory": return USER_DIR;
            case "currentTimeMillis": return TIME_MILLIS;
            case "nanoTime": return NANO_TIME;
        }
    }
    
    private static final PSValue
            EXIT = PSFunction.voidFunction((arg0) -> System.exit(arg0.toJavaInt())),
            USER_DIR = PSFunction.function(() -> valueOf(System.getProperty("user.dir"))),
            TIME_MILLIS = PSFunction.function(() -> valueOf(System.currentTimeMillis())),
            NANO_TIME = PSFunction.function(() -> valueOf(System.nanoTime()));
}

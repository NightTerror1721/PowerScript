/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import java.io.File;
import java.io.FileInputStream;
import nt.ps.PSClassLoader;
import nt.ps.PSState;
import nt.ps.compiler.CompilerUnit;
import nt.ps.exception.PSRuntimeException;
import nt.ps.lang.PSFunction;
import nt.ps.lang.PSValue;

/**
 *
 * @author Asus
 */
public final class PSSystem extends ImmutableCoreLibrary
{
    private PSState state;
    
    public PSSystem(PSState state)
    {
        if(state == null)
            throw new NullPointerException();
        this.state = state;
    }
    
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
            case "executeScript": return EXECUTE_SCRIPT;
        }
    }
    
    private static final PSValue
            EXIT = PSFunction.voidFunction((arg0) -> System.exit(arg0.toJavaInt())),
            USER_DIR = PSFunction.function(() -> valueOf(System.getProperty("user.dir"))),
            TIME_MILLIS = PSFunction.function(() -> valueOf(System.currentTimeMillis())),
            NANO_TIME = PSFunction.function(() -> valueOf(System.nanoTime()));
    
    private final PSValue
            EXECUTE_SCRIPT = PSFunction.function((arg0) -> {
                File file = state.adaptPathToRoot(arg0.toJavaString());
                try(FileInputStream fis = new FileInputStream(file))
                {
                    PSFunction script = CompilerUnit.compile(fis, state, new PSClassLoader(state.getClassLoader()), file.getName().replace('.', '$'), false);
                    return script.call();
                }
                catch(Exception ex) { throw new PSRuntimeException(ex); }
            });
}

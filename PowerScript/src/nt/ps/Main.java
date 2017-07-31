/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps;

import java.io.FileNotFoundException;
import java.io.IOException;
import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.exception.PSCompilerException;

/**
 *
 * @author Asus
 */
public final class Main
{
    public static void main(String[] args) throws CompilerError, FileNotFoundException, PSCompilerException, IOException
    {
        CommandLineInterpreter cli = new CommandLineInterpreter();
        cli.execute(args);
        //cli.execute("test.pws");
        //return;
        
        //CompilerError.setDebugMode(true);
        
        
        /*File file = new File("test.pws");
        FileInputStream fis = new FileInputStream(file);
        PSClassLoader cl = new PSClassLoader(Main.class.getClassLoader());
        PSState state = new PSState();
        
        PSScript script = CompilerUnit.compile(fis, state, cl, "test");
        script.execute();*/
        
        /*CompilerUnit.compileAsJar(new File("test.jar"), new File(System.getProperty("user.dir")),
                new File("test.pws"));
        
        File file = new File("test.pws");
        FileInputStream fis = new FileInputStream(file);
        PSClassLoader cl = new PSClassLoader(Main.class.getClassLoader());
        
        PSState state = PSState.createDefaultInstance();
        state.insertDefaultImportFunction();
        state.insertDefaultIOUtils();
        //state.setGlobalValue("IO", new PSIO(state));
        //state.setGlobalValue("Object", new PSObjectReference());
        
        double t1 = System.nanoTime();
        PSScript script = CompilerUnit.compile(fis, state, cl, "test", false);
        double t2 = System.nanoTime();
        script.execute();
        double t3 = System.nanoTime();
        script.execute();
        double t4 = System.nanoTime();
        
        System.out.println("Time to Compile: " + ((t2 - t1) / 1000000) + "ms");
        System.out.println("Time to Execute: " + ((t4 - t3) / 1000000) + "ms");*/
    }
}

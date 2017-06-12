/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import nt.ps.compiler.CompilerUnit;
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
        CompilerError.setDebugMode(true);
        
        
        /*File file = new File("test.pws");
        FileInputStream fis = new FileInputStream(file);
        PSClassLoader cl = new PSClassLoader(Main.class.getClassLoader());
        PSState state = new PSState();
        
        PSScript script = CompilerUnit.compile(fis, state, cl, "test");
        script.execute();*/
        
        CompilerUnit.compileAsJar(new File("test.jar"), new File(System.getProperty("user.dir")),
                new File("test.pws"));
        
        File file = new File("test.pws");
        FileInputStream fis = new FileInputStream(file);
        PSClassLoader cl = new PSClassLoader(Main.class.getClassLoader());
        PSState state = new PSState();
        
        PSScript script = CompilerUnit.compile(fis, state, cl, "test");
        script.execute();
    }
}

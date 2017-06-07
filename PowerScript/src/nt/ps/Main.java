/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import nt.ps.compiler.CompilerUnit;
import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.exception.PSCompilerException;
import nt.ps.compiler.parser.Literal;

/**
 *
 * @author Asus
 */
public final class Main
{
    public static void main(String[] args) throws CompilerError, FileNotFoundException, PSCompilerException
    {
        System.out.println(Literal.decode("Infinity"));
        
        
        File file = new File("test.pws");
        FileInputStream fis = new FileInputStream(file);
        PSClassLoader cl = new PSClassLoader(Main.class.getClassLoader());
        PSState state = new PSState();
        
        PSScript script = CompilerUnit.compile(fis, state, cl, "test");
        script.execute();
    }
}

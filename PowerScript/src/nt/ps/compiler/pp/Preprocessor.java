/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.pp;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import nt.ps.compiler.CodeReader;

/**
 *
 * @author Asus
 */
public final class Preprocessor
{
    private final CodeReader source;
    private final BufferedWriter output;
    
    public Preprocessor(CodeReader source)
    {
        if(source == null)
            throw new NullPointerException();
        this.source = source;
        output = new BufferedWriter(new OutputStreamWriter(new ByteArrayOutputStream(source.getMaxIndex())));
    }
    
    public final CodeReader compute()
    {
        
    }
}

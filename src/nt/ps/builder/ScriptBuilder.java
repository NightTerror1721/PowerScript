/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 *
 * @author Asus
 */
public final class ScriptBuilder extends FunctionBuilder
{
    public ScriptBuilder()
    {
        super(null, new String[0], true, false, "");
    }
    
    @Override
    public final Statement toStatement()
    {
        throw new UnsupportedOperationException();
    }
    
    public final void write(OutputStream out) throws IOException
    {
        try(OutputStreamWriter osw = new OutputStreamWriter(out))
        {
            osw.write(super.toStatement().toCode());
        }
    }
    
    public final void write(File file) throws IOException
    {
        try(FileOutputStream fos = new FileOutputStream(file))
        {
            write(fos);
        }
    }
}

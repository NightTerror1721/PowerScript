/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import nt.ps.lang.PSFunction;

/**
 *
 * @author u75213
 */
public class PSClassLoader extends ClassLoader
{
    private final HashMap<String,Class<? extends PSFunction>> cache = new HashMap<>();
    
    public PSClassLoader(ClassLoader parent)
    {
        super(parent);
    }
    
    public final Class<? extends PSFunction> createPSClass(String name, byte[] data)
    {
        return createPSClass(name,data,0,data.length);
    }
    
    public final Class<? extends PSFunction> createPSClass(String name,
            byte[] data, int offset, int len)
    {
        Class<? extends PSFunction> c = (Class<? extends PSFunction>) super.defineClass(name,data,offset,len);
        cache.put(name,c);
        return c;
    }
    
    public final Class<? extends PSFunction> findClassInCache(String name)
    {
        return cache.get(name);
    }
    
    public final void writeClass(File dir, String nameFile, byte[] data)
    {
        File file = new File(dir.getAbsolutePath() + "/" + nameFile);
        try(FileOutputStream fos = new FileOutputStream(file))
        {
            fos.write(data);
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }
    }
    
    public final void clearCache()
    {
        cache.clear();
    }
}

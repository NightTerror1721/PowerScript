/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.HashMap;
import nt.ps.PSState;
import nt.ps.exception.PSRuntimeException;
import nt.ps.lang.PSFunction;
import nt.ps.lang.PSObject;
import nt.ps.lang.PSValue;
import nt.ps.lang.PSVarargs;

/**
 *
 * @author Asus
 */
public final class ImportFunction extends PSFunction.PSTwoArgsFunction
{
    private final PSState state;
    private final HashMap<String, PSObject> cache = new HashMap<>();
    private File root = new File(System.getProperty("user.dir"));
    
    public ImportFunction(PSState state)
    {
        if(state == null)
            throw new NullPointerException();
        this.state = state;
    }
    
    public final void setRoot(File file)
    {
        if(file == null)
            throw new NullPointerException();
        if(!file.exists() || !file.isDirectory())
            throw new IllegalArgumentException("Require a valid directory for root import function");
        this.root = file;
    }
    
    @Override
    public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1)
    {
        File file = adapt(arg0.toJavaString());
        return doImport(file, arg1.toJavaBoolean());
    }
    
    private PSValue doImport(File file, boolean force) throws PSRuntimeException
    {
        PSObject wrapper;
        String path = file.getAbsolutePath();
        if(!force)
        {
            wrapper = cache.get(path);
            if(wrapper != null)
                return wrapper;
        }
        else cache.remove(path);
        
        try(FileInputStream fis = new FileInputStream(file))
            {
                wrapper = new PSObject();
                PSFunction callable = state.compile(fis, file.getName().replace('.','_'), wrapper);
                callable.call();
                wrapper.setFrozen(true);
                cache.put(path, wrapper);
                return wrapper;
            }
            catch(Throwable th)
            {
                throw new PSRuntimeException(th);
            }
    }
    
    private File adapt(String strPath)
    {
        File file = new File(strPath);
        Path path = file.toPath();
        if(path.getRoot() != null)
            return file;
        return new File(root.getAbsolutePath() + File.separator + strPath);
    }
}

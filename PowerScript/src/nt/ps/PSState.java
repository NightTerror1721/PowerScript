/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import nt.ps.compiler.CompilerUnit;
import nt.ps.compiler.exception.PSCompilerException;
import nt.ps.lang.PSValue;
import nt.ps.lang.PSVarargs;
import nt.ps.lang.core.EvalFunction;
import nt.ps.lang.core.ImportFunction;
import nt.ps.lang.core.PSArrayReference;
import nt.ps.lang.core.PSBooleanReference;
import nt.ps.lang.core.PSFunctionReference;
import nt.ps.lang.core.PSIteratorReference;
import nt.ps.lang.core.PSMapReference;
import nt.ps.lang.core.PSMath;
import nt.ps.lang.core.PSNumberReference;
import nt.ps.lang.core.PSObjectReference;
import nt.ps.lang.core.PSStringReference;
import nt.ps.lang.core.PSSystem;
import nt.ps.lang.core.PSTupleReference;

/**
 *
 * @author Asus
 */
public final class PSState extends PSGlobals
{
    private final HashMap<String, PSValue> globals = new HashMap<>();
    private final HashMap<String, PSValue> natives = new HashMap<>();
    
    private final PSClassLoader classLoader;
    
    private Writer stdout;
    private Writer stderr;
    private Reader stdin;
    
    private static final PrintWriter DEFAULT_STDOUT = new PrintWriter(System.out);
    private static final PrintWriter DEFAULT_STDERR = new PrintWriter(System.err);
    private static final InputStreamReader DEFAULT_STDIN = new InputStreamReader(System.in);
    
    public PSState()
    {
        super(null);
        classLoader = new PSClassLoader(getClass().getClassLoader());
        
        stdout = DEFAULT_STDOUT;
        stderr = DEFAULT_STDERR;
        stdin = DEFAULT_STDIN;
    }
    
    public final PSClassLoader getClassLoader() { return classLoader; }
    
    public static final PSState createDefaultInstance()
    {
        PSState state = new PSState();
        state.insertDefaultNatives();
        return state;
    }
    
    public final void insertDefaultNatives()
    {
        natives.put("Object", new PSObjectReference());
        natives.put("Array", new PSArrayReference());
        natives.put("Tuple", new PSTupleReference());
        natives.put("Boolean", new PSBooleanReference());
        natives.put("Number", new PSNumberReference());
        natives.put("String", new PSStringReference());
        natives.put("Map", new PSMapReference());
        natives.put("Iterator", new PSIteratorReference());
        natives.put("Function", new PSFunctionReference(this));
        natives.put("Math", new PSMath());
        natives.put("System", new PSSystem());
        natives.put("eval", new EvalFunction(this));
    }
    
    public final void insertDefaultImportFunction()
    {
        natives.put("import", new ImportFunction(this));
    }
    
    public final Writer getStdout() { return stdout; }
    public final Writer getStderr() { return stderr; }
    public final Reader getStdin() { return stdin; }
    
    public final void setStdout(Writer writer) { stdout = Objects.requireNonNull(writer); }
    public final void setStderr(Writer writer) { stderr = Objects.requireNonNull(writer); }
    public final void setStdin(Reader reader) { stdin = Objects.requireNonNull(reader); }
    
    
    public final PSScript compile(InputStream input, String name) throws PSCompilerException
    {
        return CompilerUnit.compile(input, this, classLoader, name, false);
    }
    public final PSScript compile(InputStream input, String name, PSValue globalsWrapped) throws PSCompilerException
    {
        PSGlobals child = PSGlobals.wrap(this, globalsWrapped);
        return CompilerUnit.compile(input, child, classLoader, name, false);
    }
    public final PSScript compile(InputStream input, String name, Map<String, PSValue> globalsWrapped) throws PSCompilerException
    {
        PSGlobals child = PSGlobals.valueOf(this, globalsWrapped);
        return CompilerUnit.compile(input, child, classLoader, name, false);
    }
    
    public final PSScript compile(File file, String name) throws IOException, PSCompilerException
    {
        try(FileInputStream fis = new FileInputStream(file))
        {
            return CompilerUnit.compile(fis, this, classLoader, name, false);
        }
    }
    public final PSScript compile(File file, String name, PSValue globalsWrapped) throws IOException, PSCompilerException
    {
        try(FileInputStream fis = new FileInputStream(file))
        {
            PSGlobals child = PSGlobals.wrap(this, globalsWrapped);
            return CompilerUnit.compile(fis, child, classLoader, name, false);
        }
    }
    public final PSScript compile(File file, String name, Map<String, PSValue> globalsWrapped) throws IOException, PSCompilerException
    {
        try(FileInputStream fis = new FileInputStream(file))
        {
            PSGlobals child = PSGlobals.valueOf(this, globalsWrapped);
            return CompilerUnit.compile(fis, child, classLoader, name, false);
        }
    }
    
    public final PSVarargs eval(String code) throws PSCompilerException
    {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(code.getBytes()))
        {
            PSClassLoader cl = new PSClassLoader(classLoader);
            PSScript script = CompilerUnit.compile(bais, this, cl, randomName(), true);
            return script.call();
        }
        catch(IOException ex) { throw new IllegalArgumentException(ex); }
    }
    
    public final PSVarargs eval(String code, PSValue globalsWrapped) throws PSCompilerException
    {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(code.getBytes()))
        {
            PSClassLoader cl = new PSClassLoader(classLoader);
            PSGlobals child = PSGlobals.wrap(this, globalsWrapped);
            PSScript script = CompilerUnit.compile(bais, child, cl, randomName(), true);
            return script.call();
        }
        catch(IOException ex) { throw new IllegalArgumentException(ex); }
    }
    
    public final PSVarargs eval(String code, Map<String, PSValue> globalsWrapped) throws PSCompilerException
    {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(code.getBytes()))
        {
            PSClassLoader cl = new PSClassLoader(classLoader);
            PSGlobals child = PSGlobals.valueOf(this, globalsWrapped);
            PSScript script = CompilerUnit.compile(bais, child, cl, randomName(), true);
            return script.call();
        }
        catch(IOException ex) { throw new IllegalArgumentException(ex); }
    }
    
    
    @Override
    protected final PSValue innerGetGlobalValue(String name) { return globals.get(name); }

    @Override
    protected final void innerSetGlobalValue(String name, PSValue value) { globals.put(name,value); }

    @Override
    public final void removeGlobalValue(String name) { globals.remove(name); }

    @Override
    public final Collection<String> keys() { return globals.keySet(); }

    @Override
    public final Collection<PSValue> values() { return globals.values(); }
    
    @Override
    final PSValue getNativeValue0(String name)
    {
        PSValue v;
        return (v = natives.get(name)) != null ? v : PSValue.UNDEFINED;
    }
    
    public final void setNativeValue(String name, PSValue value)
    {
        if(natives.containsKey(name))
            throw new IllegalArgumentException(name + " value has already exists");
        natives.put(name, value);
    }
    
    @Override
    final Set<String> getNativeNames0()
    {
        return natives.keySet();
    }
    
    @Override
    final boolean hasNativeValue0(String name)
    {
        return natives.containsKey(name);
    }
    
    private static String randomName()
    {
        long nano = System.nanoTime();
        return "lambda_" + Long.toHexString(nano) + Long.toHexString(System.currentTimeMillis());
    }
}

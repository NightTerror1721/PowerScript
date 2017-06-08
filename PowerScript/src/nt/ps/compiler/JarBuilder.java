/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import nt.ps.PSClassLoader;
import nt.ps.PSState;
import nt.ps.compiler.exception.PSCompilerException;

/**
 *
 * @author Marc
 */
public final class JarBuilder
{
    public static final void createJar(SourceFile[] inputs, File output) throws PSCompilerException, IOException
    {
        Objects.requireNonNull(inputs);
        Objects.requireNonNull(output);
        if(inputs.length < 1)
            throw new IllegalArgumentException();
        
        LinkedList<ClassData> list = new LinkedList<>();
        //ClassData cmain = "";
        ClassAccumulator acc = new ClassAccumulator();
        PSState env = new PSState();
        PSClassLoader classLoader = new PSClassLoader(JarBuilder.class.getClassLoader());
        for(SourceFile input : inputs)
        {
            compileFile(env,input.file,getFileName(input),acc,classLoader);
            acc.classes.values().stream().forEach((cd) -> {
                list.add(cd);
            });
            acc.clearCache();
        }
        /*if(cmain == null)
            throw new IllegalStateException("The main class has not exists");*/
        
        try(JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(output))))
        {
            for(ClassData cd : list)
            {
                ZipEntry entry = new ZipEntry(cd.name + ".class");
                jos.putNextEntry(entry);
                jos.write(cd.data);
                jos.closeEntry();
            }
            ZipEntry metaInfDir = new ZipEntry("META-INF/");
            jos.putNextEntry(metaInfDir);
            jos.closeEntry();
            ZipEntry manifest = new ZipEntry("META-INF/MANIFEST.MF");
            jos.putNextEntry(manifest);
            String text = "Manifest-Version: 1.0" + System.lineSeparator()
                    + "Class-Path: " + System.lineSeparator()/* + "Main-Class: " +
                    cmain.name + System.lineSeparator()*/ + System.lineSeparator();
            jos.write(text.getBytes("utf-8"));
            jos.closeEntry();
        }
    }
    
    public static final void createJar(File output, File root, File... inputs) throws IOException, PSCompilerException
    {
        Path proot = root.getAbsoluteFile().toPath().toAbsolutePath();
        SourceFile[] sources = new SourceFile[inputs.length];
        int count = -1;
        for(File input : inputs)
        {
            count++;
            Path pinput = input.toPath().toAbsolutePath();
            if(!pinput.startsWith(proot))
                throw new IllegalArgumentException("File '" + input.getAbsolutePath() +
                        "' not belongs at root " + root.getAbsolutePath());
            sources[count] = new SourceFile(root,
                    pinput.subpath(proot.getNameCount(),pinput.getNameCount()).toString());
        }
        createJar(sources, output);
    }
    
    private static void compileFile(PSState env, File file, String name,
            ClassAccumulator acc, PSClassLoader classLoader) throws IOException, PSCompilerException
    {
        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file)))
        {
            CompilerUnit.compile(bis, env, classLoader, name, acc);
        }
        catch(Throwable ex)
        {
            ex.printStackTrace(System.err);
        }
    }
    
    private static boolean checkIfMainInputExists(SourceFile[] inputs, SourceFile mainInput)
    {
        for(SourceFile input : inputs)
            if(input.equals(mainInput))
                return true;
        return false;
    }
    
    private static String getFileName(SourceFile file)
    {
        String name = file.relativePath;
        int index = name.lastIndexOf('.');
        if(index >= 0)
            name = name.substring(0,index);
        return name.replace("/","$s$").replace('.','$');
    }
    
    private static final class ClassAccumulator implements ClassRepository
    {
        private final HashMap<String, ClassData> classes;

        public ClassAccumulator()
        {
            classes = new HashMap<>();
        }

        public final void clearCache()
        {
            classes.clear();
        }

        @Override
        public void registerClass(String name, byte[] data)
        {
            if(classes.containsKey(name))
                throw new IllegalStateException("Class '"+name+"' has already exists");
            classes.put(name,new ClassData(name,data));
        }

        public void compileAllData(File dest) throws IOException
        {
            try(DataOutputStream dos =
                    new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dest))))
            {
                dos.writeInt(0x123793D); //Magical number
                dos.writeInt(classes.size());
                for(ClassData cd : classes.values())
                {
                    dos.writeInt(cd.data.length);
                    dos.write(cd.data);
                    dos.writeUTF(cd.name);
                }
            }
        }
    }
    
    private static final class ClassData
    {
        private final String name;
        private final byte[] data;

        private ClassData(String name, byte[] data)
        {
            this.name = name;
            this.data = data;
        }
    }
    
    public static final class SourceFile
    {
        private final File file;
        private final String relativePath;
        
        public SourceFile(File root, String relativePath) throws FileNotFoundException
        {
            String sroot = root.getAbsolutePath() + "/";
            file = new File(sroot + relativePath);
            if(!file.exists() || !file.isFile())
                throw new FileNotFoundException("Expected a valid source file");
            this.relativePath = relativePath;
        }
        
        @Override
        public final boolean equals(Object o)
        {
            return o instanceof SourceFile &&
                    file.equals(((SourceFile)o).file);
        }

        @Override
        public final int hashCode()
        {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.file);
            return hash;
        }
    }
}

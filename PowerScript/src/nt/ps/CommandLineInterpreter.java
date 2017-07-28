/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.exception.PSCompilerException;
import nt.ps.lang.PSValue;

/**
 *
 * @author Asus
 */
public final class CommandLineInterpreter
{
    private final PSState state;
    
    public CommandLineInterpreter()
    {
        state = PSState.createDefaultInstance();
        state.insertDefaultIOUtils();
        state.insertDefaultImportFunction();
    }
    
    public final void execute(String... args)
    {
        if(args.length < 1)
            return;
        boolean hasMods = args.length > 1 && args[0].startsWith("-");
        String filePath = args[hasMods ? 1 : 0];
        int mods = extractModifiers(args);
        CompilerError.setDebugMode(hasModifier(mods, DEBUG_MODIFIER));
        args = Arrays.copyOfRange(args,
                (hasMods ? 2 : 1), args.length);
        
        PSValue psArgs = PSValue.valueOf(false, args);
        HashMap<String, PSValue> globals = new HashMap<>();
        PSScript script;
        File file;
        
        try(FileInputStream fos = new FileInputStream(file = new File(filePath)))
        {
            if(hasModifier(mods, TIME_MODIFIER))
            {
                double t1 = System.nanoTime();
                script = state.compile(fos, file.getName().replace('.', '_'), globals);
                double t2 = System.nanoTime();
                state.getStdout().write("Compilation Time: " + ((t2 - t1) / 1000000d) + "ms\n");
                state.getStdout().flush();
            }
            else script = state.compile(fos, file.getName().replace('.', '_'), globals);
        }
        catch(PSCompilerException ex)
        {
            int count = ex.getErrorCount();
            StringBuilder sb = new StringBuilder();
            sb.append("Compilation failed. ").append(count).append(" errors found:");
            for(int i=0;i<count;i++)
            {
                nt.ps.compiler.exception.CompilerErrors.Error error = ex.getError(i);
                sb.append("\n    Line ").append(error.getSourceLine()).append(": ").append(error.getMessage());
            }
            try
            {
                state.getStderr().write(sb.toString() + "\n");
                state.getStderr().flush();
            }
            catch(IOException ex2) { ex2.printStackTrace(System.err); }
            return;
        }
        catch(IOException ex)
        {
            PrintWriter pw = new PrintWriter(state.getStderr());
            ex.printStackTrace(new PrintWriter(state.getStderr()));
            pw.flush();
            return;
        }
        
        globals.put("ARGS", psArgs);
        if(hasModifier(mods, TIME_MODIFIER))
        {
            double t1 = System.nanoTime();
            script.execute();
            double t2 = System.nanoTime();
            try
            {
                state.getStdout().write("Compilation Time: " + ((t2 - t1) / 1000000d) + "ms\n");
                state.getStdout().flush();
            }
            catch(IOException ex2) { ex2.printStackTrace(System.err); }
        }
        else script.execute();
    }
    
    private int extractModifiers(String[] args)
    {
        if(args.length < 2)
            return 0;
        String smod = args[0];
        if(smod.charAt(0) != '-')
            return 0;
        int mod = 0;
        char[] chars = smod.toCharArray();
        for(int i=1;i<chars.length;i++)
            switch(chars[i])
            {
                case 'd': mod |= DEBUG_MODIFIER; break;
                case 't': mod |= TIME_MODIFIER; break;
            }
        return mod;
    }
    
    private static boolean hasModifier(int moddifiers, int mod)
    {
        return (moddifiers & mod) != 0;
    }
    
    private static final int DEBUG_MODIFIER = 0x1;
    private static final int TIME_MODIFIER = 0x2;
}

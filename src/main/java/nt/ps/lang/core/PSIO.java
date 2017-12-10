/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.StringJoiner;
import nt.ps.PSState;
import nt.ps.exception.PSRuntimeException;
import nt.ps.lang.PSFunction;
import nt.ps.lang.PSTuple;
import nt.ps.lang.PSUserdata;
import nt.ps.lang.PSValue;

/**
 *
 * @author Asus
 */
public class PSIO extends ImmutableCoreLibrary
{
    private PSState state;
    private static final Random TEMP_RANDOM = new Random();
    
    public PSIO(PSState state)
    {
        if(state == null)
            throw new NullPointerException();
        this.state = state;
    }
    
    
    @Override
    public PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
            case "print": return PRINT;
            case "getAbsolutePath": return ABSOLUTE_PATH;
            case "isAbsolutePath": return IS_ABSOLUTE_PATH;
            case "exists": return EXISTS;
            case "isDirectory": return IS_DIR;
            case "lines": return LINES;
            case "listFilenames": return LIST_FILENAMES;
            case "genlistFilenames": return GEN_LIST_FILENAMES;
            case "mkdir": return MKDIR;
            case "mkdirs": return MKDIRS;
            case "open": return OPEN;
            case "read": return READ;
            case "tmpfile": return TMPFILE;
            case "write": return WRITE;
        }
    }
    
    private final PSValue
            PRINT = PSFunction.voidVarFunction((args) -> {
                try
                {
                    switch(args.numberOfArguments())
                    {
                        case 0: state.getStdout().write("\n"); break;
                        case 1: state.getStdout().write(args.self() + "\n"); break;
                        default: {
                            StringJoiner joiner = new StringJoiner(" ");
                            for(PSValue value : iterableVarargs(args))
                                joiner.add(value.toJavaString());
                            state.getStdout().write(joiner.toString() + "\n");
                        } break;
                    }
                    state.getStdout().flush();
                }
                catch(IOException ex) { throw new PSRuntimeException(ex); }
            }),
            WRITE = PSFunction.voidVarFunction((args) -> {
                try
                {
                    switch(args.numberOfArguments())
                    {
                        case 0: return;
                        case 1: state.getStdout().write(args.self().toJavaString()); break;
                        default: {
                            StringJoiner joiner = new StringJoiner(" ");
                            for(PSValue value : iterableVarargs(args))
                                joiner.add(value.toJavaString());
                            state.getStdout().write(joiner.toString());
                        } break;
                    }
                    state.getStdout().flush();
                }
                catch(IOException ex) { throw new PSRuntimeException(ex); }
            }),
            READ = PSFunction.function((arg0) -> {
                try
                {
                    if(arg0 == UNDEFINED)
                    {
                        String line;
                        BufferedReader br = new BufferedReader(state.getStdin());
                        line = br.readLine();
                        return line == null ? UNDEFINED : valueOf(line);
                    }
                    else
                    {
                        char[] buffer = new char[arg0.toJavaInt()];
                        int len = state.getStdin().read(buffer);
                        return len <= 0 ? NULL : valueOf(new String(buffer,0,len));
                    }
                }
                catch(IOException ex) { throw new PSRuntimeException(ex); }
            });;
    
    
    private static final PSValue
            OPEN = PSFunction.function((arg0, arg1) -> {
                File file = new File(arg0.toString());
                switch(arg1.toString())
                {
                    case "w": return new FileImpl(file,FileImpl.OPENMODE_W);
                    case "r": return new FileImpl(file,FileImpl.OPENMODE_R);
                    case "a": return new FileImpl(file,FileImpl.OPENMODE_A);
                    default: throw new PSRuntimeException("Invalid open file mode");
                }
            }), 
            
            LINES = PSFunction.function((arg0) -> {
                File file = new File(arg0.toJavaString());
                return iterateLines(file);
            }),
            
            TMPFILE = PSFunction.function(() -> {
                try
                {
                    File file = File.createTempFile(Long.toString(TEMP_RANDOM.nextLong()),"temp");
                    file.deleteOnExit();
                    return new FileImpl(file,FileImpl.OPENMODE_A);
                }
                catch(IOException ex)
                {
                    return NULL;
                }
            }),
            
            ABSOLUTE_PATH = PSFunction.function((arg0) -> {
                File file = new File(arg0.toJavaString());
                return file.exists() ? valueOf(file.getAbsolutePath()) : NULL;
            }),
            
            IS_ABSOLUTE_PATH = PSFunction.function((arg0) -> {
                File file = new File(arg0.toString());
                return file.isAbsolute() ? TRUE : FALSE;
            }),
            
            LIST_FILENAMES = PSFunction.function((arg0) -> {
                File file = new File(arg0.toString());
                if(!file.exists() || !file.isDirectory())
                    return NULL;
                PSValue[] files = Arrays.stream(file.listFiles())
                        .map(f -> valueOf(f.getName()))
                        .toArray(size -> new PSValue[size]);
                return new PSTuple(files);
            }),
            
            GEN_LIST_FILENAMES = PSFunction.function((arg0) -> {
                File file = new File(arg0.toString());
                if(!file.exists() || !file.isDirectory())
                    return NULL;
                final File[] files = file.listFiles();
                return valueOf(new Iterator<PSValue>() {
                    private int it = 0;
                    
                    @Override
                    public boolean hasNext() { return it < files.length; }

                    @Override
                    public final PSValue next() { return valueOf(files[it++].getName()); }
                });
            }),
            
            MKDIR = PSFunction.voidFunction((arg0) -> {
                File file = new File(arg0.toJavaString());
                file.mkdir();
            }),
            
            MKDIRS = PSFunction.voidFunction((arg0) -> {
                File file = new File(arg0.toJavaString());
                file.mkdirs();
            }),
            
            EXISTS = PSFunction.function((arg0) -> {
                File file = new File(arg0.toJavaString());
                return file.exists() ? TRUE : FALSE;
            }),
            
            IS_DIR = PSFunction.function((arg0) -> {
                File file = new File(arg0.toString());
                return file.isDirectory() ? TRUE : FALSE;
            });
    
    
    
    
    private static final class FileImpl
            extends PSUserdata
            implements Closeable
    {
        public static final int
                OPENMODE_R = 0,
                OPENMODE_W = 1,
                OPENMODE_A = 2;
        
        private final File file;
        public final int mode;
        public final BufferedReader in;
        public final PrintWriter out;
        private final Scanner scan;
        
        private FileImpl(File file, int openMode)
        {
            this.file = file;
            mode = openMode;
            
            try
            {
                switch (mode)
                {
                    case OPENMODE_R:
                        in = new BufferedReader(new FileReader(file));
                        scan = new Scanner(in);
                        out = null;
                        break;
                    case OPENMODE_W:
                        in = null;
                        scan = null;
                        out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
                        break;
                    case OPENMODE_A:
                        in = null;
                        scan = null;
                        out = new PrintWriter(new BufferedWriter(new FileWriter(file,true)));
                        break;
                    default:
                        throw new PSRuntimeException("Invalid open file mode");
                }
            }
            catch(IOException ex)
            {
                throw new PSRuntimeException(ex);
            }
        }
        
        @Override
        public final PSValue shiftLeft(PSValue value)
        {
            if(mode == FileImpl.OPENMODE_R)
                return this;
            out.write(value.toString());
            return this;
        }
        
        @Override
        public final PSValue shiftRight(PSValue value)
        {
            if(mode != FileImpl.OPENMODE_R)
                return NULL;
            if(value == UNDEFINED)
            {
                return valueOf(scan.next());
            }
            switch(value.toString())
            {
                case "int": case "integer": case "int32": case "i": case "I": return valueOf(scan.nextInt());
                case "long": case "int64": case "l": case "L": return valueOf(scan.nextLong());
                case "float": case "float32": case "f": case "F": return valueOf(scan.nextFloat());
                case "double": case "float64": case "d": case "D": return valueOf(scan.nextDouble());
                case "boolean": case "bool": case "b": case "B": return valueOf(scan.nextBoolean());
                default: return UNDEFINED;
            }
        }
        
        @Override
        public final PSValue getProperty(String name)
        {
            switch(name)
            {
                default: return UNDEFINED;
                case "close": return F_CLOSE;
                case "getAbsolutePath": return F_ABSOLUTE_PATH;
                case "flush": return F_FLUSH;
                case "fname": return F_FNAME;
                case "lines": return F_LINES;
                case "read": return F_READ;
                case "write": return F_WRITE;
            }
        }

        @Override
        public final void close() throws IOException
        {
            switch(mode)
            {
                case OPENMODE_R:
                    in.close();
                    break;
                case OPENMODE_W:
                case OPENMODE_A:
                    out.close();
            }
        }
    }
    
    private static final PSValue
        F_WRITE = PSFunction.<FileImpl>varMethod((self, args) -> {
            if(self.mode == FileImpl.OPENMODE_R)
                return FALSE;
            StringBuilder sb = new StringBuilder();
            for(PSValue obj : iterableVarargs(args))
                sb.append(obj).append(' ');
            if(sb.length() > 0)
                sb.deleteCharAt(sb.length() - 1);
            self.out.write(sb.toString());
            return TRUE;
        }),

        F_READ = PSFunction.<FileImpl>method((self, arg0) -> {
            if(self.mode != FileImpl.OPENMODE_R)
                return NULL;
            try
            {
                if(arg0 == UNDEFINED)
                {
                    String line = self.in.readLine();
                    return line == null ? NULL : valueOf(line);
                }
                else
                {
                    char[] buffer = new char[arg0.toJavaInt()];
                    int len = self.in.read(buffer);
                    return len <= 0 ? NULL :
                            valueOf(new String(buffer,0,len));
                }
            }
            catch(IOException ex) { throw new PSRuntimeException(ex); }
        }),

        F_CLOSE = PSFunction.<FileImpl>voidMethod((self) -> {
            try
            {
                self.close();
            }
            catch(IOException ex) { throw new PSRuntimeException(ex); }
        }),

        F_FLUSH = PSFunction.<FileImpl>voidMethod((self) -> {
            if(self.mode != FileImpl.OPENMODE_R)
                self.out.flush();
        }),

        F_LINES = PSFunction.<FileImpl>method((self) -> {
            if(self.mode != FileImpl.OPENMODE_R)
                return NULL;
            return iterateLines(self.file);
        }),

        F_FNAME = PSFunction.<FileImpl>method((self) -> {
            return valueOf(self.file.getName());
        }),

        F_ABSOLUTE_PATH = PSFunction.<FileImpl>method((self) -> {
            return valueOf(self.file.getAbsolutePath());
        });
    
    private static PSValue iterateLines(final File file)
    {
        try
        {
            return PSValue.valueOf(Files.lines(file.toPath()).iterator(), PSValue::valueOf);
        }
        catch(IOException ex)
        {
            throw new PSRuntimeException(ex);
        }
    }
}

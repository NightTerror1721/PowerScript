/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.HashMap;
import nt.ps.compiler.parser.Code.CodeType;
import static nt.ps.compiler.parser.Code.collect;

/**
 *
 * @author Asus
 */
public class CompilerWord extends Code
{
    private final CompilerName name;
    
    private CompilerWord(CompilerName name)
    {
        if(name == null)
            throw new NullPointerException();
        this.name = name;
    }
    
    @Override
    public final String toString() { return name.name; }
    
    @Override
    public final CodeType getCodeType() { return CodeType.COMPILER_WORD; }
    
    public static final CompilerWord
            FUNCTION = new CompilerWord(CompilerName.FUNCTION),
            SELF = new CompilerWord(CompilerName.SELF);
    
    private static final HashMap<String, CompilerWord> HASH = collect(CompilerWord.class, k -> k.name.name);
    
    public static final boolean isCompilerWord(String str) { return HASH.containsKey(str); }
    static final CompilerWord getCompilerWord(String str) { return HASH.get(str); }
    
    public enum CompilerName
    {
        FUNCTION, SELF;
        
        private final String name = name().toLowerCase();
    }
}

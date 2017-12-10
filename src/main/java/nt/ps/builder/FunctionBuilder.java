/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.builder;

import java.util.Objects;

/**
 *
 * @author Asus
 */
public class FunctionBuilder extends BlockBuilder<FunctionBuilder>
{
    private final String name;
    private final String[] args;
    private final boolean script;
    private final boolean varargs;
    
    FunctionBuilder(String name,
            String[] args, boolean script, boolean varargs, String oldsprefix)
    {
        super(oldsprefix,script ? "" : oldsprefix + "    ");
        this.name = name;
        this.args = Objects.requireNonNull(args);
        this.script = script;
        this.varargs = varargs;
    }
    
    @Override
    public final String toCode()
    {
        StringBuilder sb = new StringBuilder();
        if(!script)
        {
            if(name == null)
                sb.append(oldsprefix).append("function");
            else
                sb.append(oldsprefix).append("function ").append(name);
            sb.append("(");
            if(args.length > 0)
            {
                for(String arg : args)
                    sb.append(arg).append(", ");
                sb.delete(sb.length() - 2,sb.length());
                if(varargs)
                    sb.append("...");
            }
            sb.append(") {\n");
        }
        code.stream().forEach((c) -> {
            sb.append(sprefix).append(c.toCode()).append("\n");
        });
        if(!script)
            sb.append(oldsprefix).append("}");
        return sb.toString();
    }
    
    public Statement toStatement()
    {
        return this::toCode;
    }
}

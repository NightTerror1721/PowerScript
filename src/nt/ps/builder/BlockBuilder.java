/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.builder;

import java.util.LinkedList;
import java.util.Objects;

/**
 *
 * @author Asus
 * @param <BB>
 */
public abstract class BlockBuilder<BB extends BlockBuilder> implements InstructionCode
{
    protected final LinkedList<InstructionCode> code;
    protected final String oldsprefix, sprefix;
    
    BlockBuilder(String oldsprefix, String sprefix)
    {
        code = new LinkedList<>();
        this.oldsprefix = oldsprefix;
        this.sprefix = sprefix;
    }
    
    public final BB insertCode(InstructionCode c)
    {
        code.add(c);
        return (BB) this;
    }
    
    public final FunctionBuilder declarateFunction(String name, boolean varargs, String... args)
    {
        Objects.requireNonNull(name);
        Objects.requireNonNull(args);
        FunctionBuilder f = new FunctionBuilder(name, args, false, varargs, sprefix + "    ");
        insertCode(f.toStatement());
        return f;
    }
    
    public final FunctionBuilder createClosure(boolean varargs, String... args)
    {
        Objects.requireNonNull(args);
        return new FunctionBuilder(null, args, false, varargs, sprefix + "    ");
    }
    
    public final ConditionalBlock insertIf(Statement cond)
    {
        Objects.requireNonNull(cond);
        ConditionalBlock cb = new ConditionalBlock(1, cond, sprefix, sprefix + "    ");
        insertCode(cb);
        return cb;
    }
    
    public final WhileBlock insertWhile(Statement cond)
    {
        WhileBlock wb = new WhileBlock(Objects.requireNonNull(cond), sprefix, sprefix + "    ");
        insertCode(wb);
        return wb;
    }
    
    public final ForeachBlock insertForeach(Statement inCond, String... vars)
    {
        if(vars == null || vars.length < 1)
            throw new IllegalArgumentException("Required 1 or more varnames");
        ForeachBlock fb = new ForeachBlock(Objects.requireNonNull(inCond), vars, sprefix, sprefix + "    ");
        insertCode(fb);
        return fb;
    }
    
    public final ForBlock insertFor(Statement init, Statement cond, Statement it)
    {
        ForBlock fb = new ForBlock(init, cond, it, sprefix, sprefix + "    ");
        insertCode(fb);
        return fb;
    }
    
    public final TryBlock insertTryCatch()
    {
        TryBlock tb = new TryBlock(sprefix, sprefix + "    ");
        insertCode(tb);
        return tb;
    }
    
    
    public static final class ConditionalBlock extends BlockBuilder<ConditionalBlock>
    {
        private final int type;
        private final Statement statement;
        private ConditionalBlock endBlock;
        
        ConditionalBlock(int type, Statement statement, String oldsprefix, String sprefix)
        {
            super(oldsprefix, sprefix);
            this.type = type;
            this.statement = statement;
            endBlock = null;
        }
        
        public final ConditionalBlock setElseIf(Statement statement)
        {
            if(type == 0)
                throw new IllegalStateException("Invalid operation in else block");
            Objects.requireNonNull(statement);
            return endBlock = new ConditionalBlock(2,statement,oldsprefix,sprefix);
        }
        
        public final ConditionalBlock setElse()
        {
            if(type == 0)
                throw new IllegalStateException("Invalid operation in else block");
            return endBlock = new ConditionalBlock(0,null,oldsprefix,sprefix);
        }
        
        @Override
        public final String toCode()
        {
            StringBuilder sb = new StringBuilder();
            switch(type)
            {
                case 1:
                    sb.append("if(").append(statement.toCode()).append(") {\n");
                    break;
                case 2:
                    sb.append("else if(").append(statement.toCode()).append(") {\n");
                    break;
                default:
                    sb.append("else {\n");
            }
            
            super.code.stream().forEach((c) -> {
                sb.append(sprefix).append(c.toCode()).append("\n");
            });
            
            if(endBlock != null)
            {
                sb.append(oldsprefix).append("} ").append(endBlock.toCode());
                return sb.toString();
            }
            sb.append(oldsprefix).append("}");
            return sb.toString();
        }
    }
    
    public static final class WhileBlock extends BlockBuilder<WhileBlock>
    {
        private final Statement cond;
        
        private WhileBlock(Statement cond, String oldsprefix, String sprefix)
        {
            super(oldsprefix,sprefix);
            this.cond = cond;
        }
        
        @Override
        public final String toCode()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("while(").append(cond.toCode()).append(") {\n");
            
            code.stream().forEach((c) -> {
                sb.append(sprefix).append(c.toCode()).append("\n");
            });
            
            sb.append(oldsprefix).append("}");
            return sb.toString();
        }
    }
    
    public static final class ForeachBlock extends BlockBuilder<ForeachBlock>
    {
        private final Statement itGen;
        private final String[] vars;
        
        private ForeachBlock(Statement itGen, String[] vars, String oldsprefix, String sprefix)
        {
            super(oldsprefix,sprefix);
            this.itGen = itGen;
            this.vars = vars;
        }
        
        @Override
        public final String toCode()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("for(");
            for(String var : vars)
            {
                var = var.replace("\n","\\n").replace("\r","\\r")
                .replace("\t","\\t").replace("\"","\\\"")
                .replace("\'","\\\'").replace("\\","\\\\");
                if(var.isEmpty())
                    throw new IllegalArgumentException("Invalid empty namevar");
                sb.append(var).append(", ");
            }
            sb.delete(sb.length()-2,sb.length());
            sb.append(" : ").append(itGen.toCodeWrapped()).append(") {\n");
            
            code.stream().forEach((c) -> {
                sb.append(sprefix).append(c.toCode()).append("\n");
            });
            
            sb.append(oldsprefix).append("}");
            return sb.toString();
        }
    }
    
    public static final class ForBlock extends BlockBuilder<ForBlock>
    {
        private final InstructionCode init;
        private final Statement cond, it;
        
        private ForBlock(InstructionCode init, Statement cond, Statement it, String oldsprefix, String sprefix)
        {
            super(oldsprefix,sprefix);
            this.init = init;
            this.cond = cond;
            this.it = it;
        }
        
        @Override
        public final String toCode()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("for(");
            
            if(init != null)
                sb.append(init.toCode());
            sb.append("; ");
            
            if(cond != null)
                sb.append(cond.toCode());
            sb.append("; ");
            
            if(it != null)
                sb.append(it.toCode());
            sb.append(") {\n");
            
            code.stream().forEach((c) -> {
                sb.append(sprefix).append(c.toCode()).append("\n");
            });
            
            sb.append(oldsprefix).append("}");
            return sb.toString();
        }
    }
    
    public static final class TryBlock extends BlockBuilder<TryBlock>
    {
        private CatchBlock ct;
        
        private TryBlock(String oldsprefix, String sprefix)
        {
            super(oldsprefix,sprefix);
        }
        
        public final CatchBlock insertCatch(String exceptionVarname)
        {
            if(ct != null)
                throw new IllegalStateException("Catch block is already present");
            return ct = new CatchBlock(exceptionVarname, oldsprefix, sprefix);
        }
        
        @Override
        public final String toCode()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("try {\n");
            
            code.stream().forEach((c) -> {
                sb.append(sprefix).append(c.toCode()).append("\n");
            });
            
            if(ct == null)
                sb.append(oldsprefix).append("} catch(ex) {}");
            else sb.append(oldsprefix).append("} ").append(ct.toCode());
            return sb.toString();
        }
    }
    
    public static final class CatchBlock extends BlockBuilder<CatchBlock>
    {
        private final String exceptionName;
        
        private CatchBlock(String exceptionName, String oldsprefix, String sprefix)
        {
            super(oldsprefix,sprefix);
            this.exceptionName = Objects.requireNonNull(exceptionName);
        }
        
        @Override
        public final String toCode()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("catch(").append(exceptionName).append(") {\n");
            
            code.stream().forEach((c) -> {
                sb.append(sprefix).append(c.toCode()).append("\n");
            });
            
            sb.append(oldsprefix).append("}");
            return sb.toString();
        }
    }
}

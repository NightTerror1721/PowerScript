/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang;

import java.util.AbstractList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author Asus
 */
public abstract class PSVarargs
{
    public abstract int numberOfArguments();
    public abstract PSValue self();
    public abstract PSValue arg(int index);
    
    /* Varargs Operations */
    public static final PSValue varargsAsPSArray(PSVarargs args, int start)
    {
        if(start >= args.numberOfArguments())
            return new PSArray();
        return new PSArray(new VarargsList(args,start));
    }
    
    public static final List<PSValue> varargsAsList(PSVarargs args, int start)
    {
        if(start >= args.numberOfArguments())
            return Collections.EMPTY_LIST;
        return new VarargsList(args,start);
    }
    
    public static final PSVarargs subVarargs(PSVarargs args, int start)
    {
        if(start >= args.numberOfArguments())
            return EMPTY;
        return new SubVarargs(args,start);
    }
    
    public static final Iterable<PSValue> iterableVarargs(PSVarargs varargs)
    {
        return () -> new Iterator<PSValue>()
        {
            private int it = 0;
            private final int len = varargs.numberOfArguments();
            @Override
            public final boolean hasNext()
            {
                return it < len;
            }

            @Override
            public final PSValue next()
            {
                return varargs.arg(it++);
            }
        };
    }
    
    public static final Iterator<PSValue> varargsIterator(PSVarargs varargs)
    {
        return iterableVarargs(varargs).iterator();
    }
    
    public static final Stream<PSValue> varargsStream(PSVarargs varargs)
    {
        return StreamSupport.stream(Spliterators.spliterator(
                varargsIterator(varargs),varargs.numberOfArguments(),Spliterator.ORDERED),false);
    }
    
    
    
    /* Varargs Of */
    public static final PSVarargs varargsOf() { return EMPTY; }
    
    public static final PSVarargs varargsOf(PSVarargs args0) { return args0; }
    
    public static final PSVarargs varargsOf(PSVarargs args0, PSVarargs args1)
    {
        return new TailVarargs(args0,args1);
    }
    
    public static final PSVarargs varargsOf(PSValue arg0, PSVarargs rest)
    {
        return new PairVarargs(arg0,rest);
    }
    
    public static final PSVarargs varargsOf(PSValue arg0, PSValue arg1,
            PSVarargs rest)
    {
        return new ArrayVarargs(arg0,arg1,rest);
    }
    
    public static final PSVarargs varargsOf(PSValue arg0, PSValue arg1, PSValue arg2,
            PSVarargs rest)
    {
        return new ArrayVarargs(new PSValue[] { arg0, arg1, arg2 }, rest);
    }
    
    public static final PSVarargs varargsOf(PSValue arg0, PSValue arg1, PSValue arg2,
            PSValue arg3, PSVarargs rest)
    {
        return new ArrayVarargs(new PSValue[] { arg0, arg1, arg2, arg3 }, rest);
    }
    
    public static final PSVarargs varargsOf(PSValue[] args, PSVarargs rest)
    {
        return new ArrayVarargs(args,rest);
    }
    
    public static final PSVarargs varargsOf(PSValue... args)
    {
        return new LiteralArrayVarargs(args);
    }
    
    
    /* Default Values in functions */
    static final PSVarargs defaultFunctionVarargs(int min, PSVarargs args, PSVarargs defs)
    {
        if(args.numberOfArguments() >= min + defs.numberOfArguments())
            return args;
        return new PSDefaultVarargs(min,args,defs);
    }
    
    
    /* Class definition */
    private static final class EmptyVarargs extends PSVarargs
    {
        @Override
        public final int numberOfArguments() { return 0; }

        @Override
        public final PSValue self() { return PSValue.UNDEFINED; }

        @Override
        public final PSValue arg(int index) { return PSValue.UNDEFINED; }
    }
    public static final PSVarargs EMPTY = new EmptyVarargs();
    
    static final class SubVarargs extends PSVarargs
    {
        private final PSVarargs args;
        private final int start;
        
        private SubVarargs(PSVarargs args, int start)
        {
            this.args = args;
            this.start = start;
        }

        @Override
        public final PSValue arg(int index) { return args.arg(index + start); }

        @Override
        public final PSValue self() { return args.arg(start); }

        @Override
        public final int numberOfArguments() { return args.numberOfArguments() - start; }
    }
    
    static final class PairVarargs extends PSVarargs
    {
        private final PSValue arg0;
        private final PSVarargs rest;
        
        public PairVarargs(PSValue arg0, PSVarargs rest)
        {
            this.arg0 = arg0;
            this.rest = rest;
        }

        @Override
        public final PSValue arg(int index)
        {
            return index == 0 ? arg0 : rest.arg(index - 1);
        }

        @Override
        public final PSValue self()
        {
            return arg0;
        }

        @Override
        public final int numberOfArguments()
        {
            return rest.numberOfArguments() + 1;
        }
    }
    
    static final class ArrayVarargs extends PSVarargs
    {
        private final PSValue[] args;
        private final PSVarargs rest;
        
        public ArrayVarargs(PSValue[] args, PSVarargs rest)
        {
            this.args = args;
            this.rest = rest;
        }
        public ArrayVarargs(PSValue arg0, PSValue arg1, PSVarargs rest)
        {
            this.args = new PSValue[] { arg0, arg1 };
            this.rest = rest;
        }
        public ArrayVarargs(PSValue arg0, PSValue arg1, PSValue arg2, PSVarargs rest)
        {
            this.args = new PSValue[] { arg0, arg1, arg2 };
            this.rest = rest;
        }

        @Override
        public final PSValue arg(int index)
        {
            switch(index)
            {
                case 0: return self();
                default:
                    return index < args.length ? args[index] : rest.arg(index - args.length);
            }
        }

        @Override
        public final PSValue self()
        {
            return args.length == 0 ? rest.self() : args[0];
        }

        @Override
        public final int numberOfArguments()
        {
            return args.length + rest.numberOfArguments();
        }
    }
    
    static final class LiteralArrayVarargs extends PSVarargs
    {
        private final PSValue[] args;
        
        public LiteralArrayVarargs(PSValue[] args)
        {
            this.args = args;
        }

        @Override
        public final PSValue arg(int index)
        {
            return index < args.length ? args[index] : PSValue.UNDEFINED;
        }

        @Override
        public final PSValue self()
        {
            return args.length == 0 ? PSValue.UNDEFINED : args[0];
        }

        @Override
        public final int numberOfArguments()
        {
            return args.length;
        }
    }
    
    static final class TailVarargs extends PSVarargs
    {
        private final PSVarargs head, tail;
        
        private TailVarargs(PSVarargs head, PSVarargs tail)
        {
            this.head = head;
            this.tail = tail;
        }
        
        @Override
        public final PSValue arg(int index)
        {
            return index < head.numberOfArguments() ? head.arg(index) : tail.arg(index - head.numberOfArguments());
        }

        @Override
        public final PSValue self()
        {
            return head.numberOfArguments() > 0 ? head.self() : tail.self();
        }

        @Override
        public final int numberOfArguments()
        {
            return head.numberOfArguments() + tail.numberOfArguments();
        }
        
    }
    
    private static final class VarargsList extends AbstractList<PSValue>
    {
        private final PSVarargs args;
        private final int start;
        
        private VarargsList(PSVarargs args, int start)
        {
            if(start > args.numberOfArguments())
                throw new IllegalStateException();
            this.args = args;
            this.start = start;
        }

        @Override
        public final PSValue get(int index)
        {
            return args.arg(index + start);
        }

        @Override
        public final int size()
        {
            return args.numberOfArguments() - start;
        }
    }
    
    private static final class PSDefaultVarargs extends PSVarargs
    {
        private final int min;
        private final PSVarargs base, defs;
        
        private PSDefaultVarargs(int min, PSVarargs base, PSVarargs defs)
        {
            this.min = min;
            this.base = base;
            this.defs = defs;
        }
        
        @Override
        public final PSValue arg(int index)
        {
            return index < base.numberOfArguments()
                    ? base.arg(index)
                    : index < min
                        ? PSValue.UNDEFINED
                        : defs.arg(index - min);
        }

        @Override
        public final PSValue self()
        {
            return arg(0);
        }

        @Override
        public final int numberOfArguments()
        {
            return min + defs.numberOfArguments();
        }
    }
}

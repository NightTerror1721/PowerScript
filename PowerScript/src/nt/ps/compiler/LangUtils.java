/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import nt.ps.exception.PSException;
import nt.ps.exception.PSRuntimeException;
import nt.ps.lang.PSDataType;
import nt.ps.lang.PSFunction;
import nt.ps.lang.PSIterator;
import nt.ps.lang.PSNumber;
import nt.ps.lang.PSObject;
import nt.ps.lang.PSObject.Property;
import nt.ps.lang.PSString;
import nt.ps.lang.PSTuple;
import nt.ps.lang.PSUserdata;
import nt.ps.lang.PSValue;
import nt.ps.lang.PSVarargs;
import nt.ps.lang.core.PSObjectReference;

/**
 *
 * @author Asus
 */
public final class LangUtils
{
    private LangUtils() {}
    
    public static final class ProtoMap extends HashMap<PSValue, PSValue>
    {
        public ProtoMap() { super(); }
        public ProtoMap(int initialCapacity) { super(initialCapacity); }
        public ProtoMap(int initialCapacity, float loadFactor) { super(initialCapacity, loadFactor); }
    }
    
    public static final class ProtoObject extends HashMap<String, Property>
    {
        public ProtoObject() { super(); }
        public ProtoObject(int initialCapacity) { super(initialCapacity); }
        public ProtoObject(int initialCapacity, float loadFactor) { super(initialCapacity, loadFactor); }
        
        public final void put(String name, PSValue value) { put(name, new Property(value, false)); }
    }
    
    public static final PSValue operatorTypeof(PSValue value)
    {
        return new PSString(value.getPSType().getTypeName());
    }
    
    public static final PSValue operatorInstanceof(PSValue object, PSValue parent)
    {
        if(!object.isObject())
            return PSValue.FALSE;
        PSObject obj = object.toPSObject();
        return obj.getParent()== parent ? PSValue.TRUE : PSValue.FALSE;
    }
    
    public static final PSValue operatorEqualsReference(PSValue value0, PSValue value1)
    {
        return value0.getPSType() == value1.getPSType()
                ? value0.equals(value1)
                : PSValue.FALSE;
    }
    
    public static final PSValue operatorNotEqualsReference(PSValue value0, PSValue value1)
    {
        return value0.getPSType() == value1.getPSType()
                ? value0.notEquals(value1)
                : PSValue.TRUE;
    }
    
    public static final boolean switchComparisonInteger(PSValue value)
    {
        return value.getPSType() == PSDataType.NUMBER && !((PSNumber)value).isDecimal();
    }
    
    public static final PSValue wrapThrowable(Throwable th)
    {
        ProtoObject map = new ProtoObject();
        map.put("message", new PSString(th.getMessage()));
        Throwable cause = th.getCause();
        map.put("cause", cause == null ? PSValue.NULL : wrapThrowable(cause));
        map.put("name", new PSString(th.getClass().getName()));
        map.put("getStackTrace", PSFunction.method((self) -> {
            StringWriter w = new StringWriter();
            PrintWriter p = new PrintWriter(w);
            th.printStackTrace(p);
            p.flush();
            return new PSString(w.toString());
        }));
        map.put("toString", PSFunction.method((self) -> new PSString(th.toString())));
        map.put("generatedNatively", th instanceof PSException || th instanceof PSRuntimeException
                ? PSValue.FALSE
                : PSValue.TRUE);
        ThrowableWrapper wrapper = new ThrowableWrapper(th);
        map.put("<throwable>", wrapper);
        if(th instanceof PSThrowable)
            map.put("raised", ((PSThrowable)th).errors);
        else map.put("raised", new PSTuple(new PSString(th.getLocalizedMessage())));
        
        return new PSObject(map);
    }
    
    public static Throwable varargsToThrowable(PSVarargs args)
    {
        PSValue[] objs = new PSValue[args.numberOfArguments()];
        StringBuilder sb = new StringBuilder();
        ThrowableWrapper cause = null;
        
        int count = 0;
        for(PSValue value : PSVarargs.iterableVarargs(args))
        {
            objs[count++] = value;
            if(value instanceof PSObject)
            {
                PSObject obj = value.toPSObject();
                if(cause == null && obj.hasProperty("<throwable>"))
                {
                    PSValue prop = obj.getProperty("<throwable>");
                    if(prop instanceof ThrowableWrapper)
                    {
                        cause = (ThrowableWrapper) prop;
                        sb.append(" ");
                        continue;
                    }
                }
                sb.append(PSObjectReference.toString(obj, false)).append(" ");
            }
            else sb.append(value.toJavaString()).append(" ");
        }
        
        return cause == null
                ? new PSThrowable(new PSTuple(objs), sb.toString())
                : new PSThrowable(new PSTuple(objs), sb.toString(), cause);
    }
    
    private static final class ThrowableWrapper extends PSUserdata
    {
        private final Throwable throwable;
        
        private ThrowableWrapper(Throwable throwable) { this.throwable = throwable; }
    }
    
    private static final class PSThrowable extends PSRuntimeException
    {
        private final PSTuple errors;
        
        private PSThrowable(PSTuple errors, String message)
        {
            super(message);
            this.errors = errors;
        }
        
        private PSThrowable(PSTuple errors, String message, ThrowableWrapper cause)
        {
            super(message, cause.throwable);
            this.errors = errors;
        }
    }
    
    public static final class GeneratorState
    {
        private final PSValue[] vars;
        private int state;
        private boolean end;
        
        public GeneratorState(PSValue[] vars)
        {
            this.vars = vars;
            this.state = 0;
            this.end = false;
        }
        
        public final void update(int state) { this.state = state; }
        public final void finish()
        {
            state = -1;
            end = true;
        }
        
        public final int getState() { return state; }
        
        public final void setLocalVariable(int reference, PSValue value) { vars[reference] = value; }
        public final PSValue getLocalVariable(int reference) { return vars[reference]; }
    }
    
    public static interface GeneratorCallable
    {
        GeneratorState createState(PSVarargs args);
        PSVarargs call(PSValue self, GeneratorState state);
    }
    
    public static final class GeneratorIterator extends PSIterator
    {
        private final GeneratorCallable callable;
        private final GeneratorState state;
        private final PSValue self;
        private PSVarargs next;
        
        public GeneratorIterator(GeneratorCallable callable, PSValue self, PSVarargs args)
        {
            this.callable = callable;
            this.state = callable.createState(args);
            this.self = self;
        }
        
        @Override
        public final boolean hasNext()
        {
            if(state.end)
                return false;
            if(next == null)
                next = callable.call(self, state);
            return !state.end;
        }

        @Override
        public final PSVarargs next()
        {
            if(state.end)
                return EMPTY;
            if(next == null)
                return callable.call(self, state);
            PSVarargs temp = next;
            next = null;
            return temp;
        }
    }
    
    public static final class Generator extends PSFunction
    {
        private final GeneratorCallable callable;
        
        public Generator(PSValue callable)
        {
            this.callable = (GeneratorCallable) callable;
        }
        
        @Override
        public final PSVarargs innerCall(PSValue self)
        {
            return new GeneratorIterator(callable, self, EMPTY);
        }
        
        @Override
        public final PSVarargs innerCall(PSValue self, PSValue arg0)
        {
            return new GeneratorIterator(callable, self, arg0);
        }
        
        @Override
        public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1)
        {
            return new GeneratorIterator(callable, self, varargsOf(arg0, arg1));
        }
        
        @Override
        public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2)
        {
            return new GeneratorIterator(callable, self, varargsOf(arg0, arg1, arg2));
        }
        
        @Override
        public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3)
        {
            return new GeneratorIterator(callable, self, varargsOf(arg0, arg1, arg2, arg3));
        }
        
        @Override
        public final PSVarargs innerCall(PSValue self, PSVarargs args)
        {
            return new GeneratorIterator(callable, self, args);
        }
    }
    
    public static final class GeneratorDefault extends PSFunction
    {
        private final GeneratorCallable callable;
        private int nodefaultArgs;
        private PSVarargs defs;
        
        public GeneratorDefault(PSValue callable)
        {
            this.callable = (GeneratorCallable) callable;
        }
        
        private PSVarargs insertDefaults(PSVarargs args)
        {
            return defaultFunctionVarargs(nodefaultArgs,args,defs);
        }
        
        public static final GeneratorDefault insertDefaults(PSVarargs defaults, int totalArgs, GeneratorDefault generator)
        {
            generator.defs = defaults;
            generator.nodefaultArgs = totalArgs - defaults.numberOfArguments();
            return generator;
        }
        
        @Override
        public final PSVarargs innerCall(PSValue self)
        {
            return new GeneratorIterator(callable, self, insertDefaults(EMPTY));
        }
        
        @Override
        public final PSVarargs innerCall(PSValue self, PSValue arg0)
        {
            return new GeneratorIterator(callable, self, insertDefaults(arg0));
        }
        
        @Override
        public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1)
        {
            return new GeneratorIterator(callable, self, insertDefaults(varargsOf(arg0, arg1)));
        }
        
        @Override
        public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2)
        {
            return new GeneratorIterator(callable, self, insertDefaults(varargsOf(arg0, arg1, arg2)));
        }
        
        @Override
        public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3)
        {
            return new GeneratorIterator(callable, self, insertDefaults(varargsOf(arg0, arg1, arg2, arg3)));
        }
        
        @Override
        public final PSVarargs innerCall(PSValue self, PSVarargs args)
        {
            return new GeneratorIterator(callable, self, insertDefaults(args));
        }
    }
}

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
import nt.ps.lang.PSNumber;
import nt.ps.lang.PSObject;
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
    
    public static final class ProtoObject extends HashMap<String, PSValue>
    {
        public ProtoObject() { super(); }
        public ProtoObject(int initialCapacity) { super(initialCapacity); }
        public ProtoObject(int initialCapacity, float loadFactor) { super(initialCapacity, loadFactor); }
    }
    
    public static final PSValue operatorTypeof(PSValue value)
    {
        return new PSString(value.getPSType().getTypeName());
    }
    
    public static final PSValue operatorInstanceof(PSValue object, PSValue parent)
    {
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
        HashMap<String, PSValue> map = new HashMap<>();
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
}

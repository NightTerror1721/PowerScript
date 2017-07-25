/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang;

import java.util.List;

/**
 *
 * @author Asus
 */
public abstract class PSFunction extends PSValue
{
    @Override
    public final PSDataType getPSType() { return PSDataType.FUNCTION; }
    
    @Override
    public final PSFunction toPSFunction() { return this; }
    
    @Override
    public final boolean equals(Object o) { return this == o; }
    
    @Override
    public int hashCode() { return superHashCode(); }
    
    @Override
    public final PSValue setProperty(String name, PSValue value) { return super.setProperty(name, value); }
    
    @Override
    public final PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
            case "call": return CALL;
            case "apply": return APPLY;
        }
    }
    
    
    
    
    public static final PSFunction voidFunction(VoidZeroArgsF closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSZeroArgsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self)
            {
                closure.call();
                return EMPTY;
            }
        };
    }
    public static final <T extends PSValue> PSFunction voidMethod(VoidZeroArgsM<T> closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSZeroArgsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self)
            {
                closure.invoke((T)self);
                return EMPTY;
            }
        };
    }
    public static final PSFunction function(ZeroArgsF closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSZeroArgsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self)
            {
                return closure.call();
            }
        };
    }
    public static final <T extends PSValue> PSFunction method(ZeroArgsM<T> closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSZeroArgsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self)
            {
                return closure.invoke((T)self);
            }
        };
    }
    
    
    public static final PSFunction voidFunction(VoidOneArgF closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSOneArgFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSValue arg0)
            {
                closure.call(arg0);
                return EMPTY;
            }
        };
    }
    public static final <T extends PSValue> PSFunction voidMethod(VoidOneArgM<T> closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSOneArgFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSValue arg0)
            {
                closure.invoke((T)self, arg0);
                return EMPTY;
            }
        };
    }
    public static final PSFunction function(OneArgF closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSOneArgFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSValue arg0)
            {
                return closure.call(arg0);
            }
        };
    }
    public static final <T extends PSValue> PSFunction method(OneArgM<T> closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSOneArgFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSValue arg0)
            {
                return closure.invoke((T)self, arg0);
            }
        };
    }
    
    
    public static final PSFunction voidFunction(VoidTwoArgsF closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSTwoArgsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1)
            {
                closure.call(arg0, arg1);
                return EMPTY;
            }
        };
    }
    public static final <T extends PSValue> PSFunction voidMethod(VoidTwoArgsM<T> closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSTwoArgsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1)
            {
                closure.invoke((T)self, arg0, arg1);
                return EMPTY;
            }
        };
    }
    public static final PSFunction function(TwoArgsF closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSTwoArgsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1)
            {
                return closure.call(arg0, arg1);
            }
        };
    }
    public static final <T extends PSValue> PSFunction method(TwoArgsM<T> closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSTwoArgsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1)
            {
                return closure.invoke((T)self, arg0, arg1);
            }
        };
    }
    
    
    public static final PSFunction voidFunction(VoidThreeArgsF closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSThreeArgsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2)
            {
                closure.call(arg0, arg1, arg2);
                return EMPTY;
            }
        };
    }
    public static final <T extends PSValue> PSFunction voidMethod(VoidThreeArgsM<T> closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSThreeArgsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2)
            {
                closure.invoke((T)self, arg0, arg1, arg2);
                return EMPTY;
            }
        };
    }
    public static final PSFunction function(ThreeArgsF closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSThreeArgsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2)
            {
                return closure.call(arg0, arg1, arg2);
            }
        };
    }
    public static final <T extends PSValue> PSFunction method(ThreeArgsM<T> closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSThreeArgsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2)
            {
                return closure.invoke((T)self, arg0, arg1, arg2);
            }
        };
    }
    
    
    public static final PSFunction voidFunction(VoidFourArgsF closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSFourArgsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3)
            {
                closure.call(arg0, arg1, arg2, arg3);
                return EMPTY;
            }
        };
    }
    public static final <T extends PSValue> PSFunction voidMethod(VoidFourArgsM<T> closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSFourArgsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3)
            {
                closure.invoke((T)self, arg0, arg1, arg2, arg3);
                return EMPTY;
            }
        };
    }
    public static final PSFunction function(FourArgsF closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSFourArgsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3)
            {
                return closure.call(arg0, arg1, arg2, arg3);
            }
        };
    }
    public static final <T extends PSValue> PSFunction method(FourArgsM<T> closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSFourArgsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3)
            {
                return closure.invoke((T)self, arg0, arg1, arg2, arg3);
            }
        };
    }
    
    
    public static final PSFunction voidVarFunction(VoidVarargsF closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSVarargsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSVarargs args)
            {
                closure.call(args);
                return EMPTY;
            }
        };
    }
    public static final <T extends PSValue> PSFunction voidVarMethod(VoidVarargsM<T> closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSVarargsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSVarargs args)
            {
                closure.invoke((T)self, args);
                return EMPTY;
            }
        };
    }
    public static final PSFunction varFunction(VarargsF closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSVarargsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSVarargs args)
            {
                return closure.call(args);
            }
        };
    }
    public static final <T extends PSValue> PSFunction varMethod(VarargsM<T> closure)
    {
        if(closure == null)
            throw new IllegalArgumentException();
        return new PSVarargsFunction()
        {
            @Override
            public PSVarargs innerCall(PSValue self, PSVarargs args)
            {
                return closure.invoke((T)self, args);
            }
        };
    }
    
    
    
    
    public static abstract class PSZeroArgsFunction extends PSFunction
    {
        @Override public abstract PSVarargs innerCall(PSValue self);
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0) { return innerCall(self); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1) { return innerCall(self); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2) { return innerCall(self); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return innerCall(self); }
        @Override public final PSVarargs innerCall(PSValue self, PSVarargs args) { return innerCall(self); }
    }
    
    public static abstract class PSOneArgFunction extends PSFunction
    {
        @Override public final PSVarargs innerCall(PSValue self) { return innerCall(self,UNDEFINED); }
        @Override public abstract PSVarargs innerCall(PSValue self, PSValue arg0);
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1) { return innerCall(self,arg0); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2) { return innerCall(self,arg0); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return innerCall(self,arg0); }
        @Override public final PSVarargs innerCall(PSValue self, PSVarargs args) { return innerCall(self,args.self()); }
    }
    
    public static abstract class PSDefaultOneArgFunction extends PSFunction
    {
        private PSValue def0 = UNDEFINED;
        
        public static final PSDefaultOneArgFunction insertDefaults(PSValue def0, PSDefaultOneArgFunction base)
        {
            base.def0 = def0;
            return base;
        }
        
        @Override public final PSVarargs innerCall(PSValue self) { return innerCall(self,def0); }
        @Override public abstract PSVarargs innerCall(PSValue self, PSValue arg0);
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1) { return innerCall(self,arg0); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2) { return innerCall(self,arg0); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return innerCall(self,arg0); }
        @Override public final PSVarargs innerCall(PSValue self, PSVarargs args) { return innerCall(self,args.self()); }
    }
    
    
    public static abstract class PSTwoArgsFunction extends PSFunction
    {
        @Override public final PSVarargs innerCall(PSValue self) { return innerCall(self,UNDEFINED,UNDEFINED); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0) { return innerCall(self,arg0,UNDEFINED); }
        @Override public abstract PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1);
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2) { return innerCall(self,arg0,arg1); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return innerCall(self,arg0,arg1); }
        @Override public final PSVarargs innerCall(PSValue self, PSVarargs args) { return innerCall(self,args.self(),args.arg(1)); }
    }
    
    public static abstract class PSDefaultTwoArgsFunction extends PSFunction
    {
        private PSValue def0 = UNDEFINED;
        private PSValue def1 = UNDEFINED;
        
        public static final PSDefaultTwoArgsFunction insertDefaults(PSValue def0, PSValue def1, PSDefaultTwoArgsFunction base)
        {
            base.def0 = def0;
            base.def1 = def1;
            return base;
        }
        public static final PSDefaultTwoArgsFunction insertDefaults(PSValue def1, PSDefaultTwoArgsFunction base)
        {
            base.def1 = def1;
            return base;
        }
        
        @Override public final PSVarargs innerCall(PSValue self) { return innerCall(self,def0,def1); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0) { return innerCall(self,arg0,def1); }
        @Override public abstract PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1);
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2) { return innerCall(self,arg0,arg1); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return innerCall(self,arg0,arg1); }
        @Override public final PSVarargs innerCall(PSValue self, PSVarargs args) { return innerCall(self,args.self(),args.arg(1)); }
    }
    
    
    public static abstract class PSThreeArgsFunction extends PSFunction
    {
        @Override public final PSVarargs innerCall(PSValue self) { return innerCall(self,UNDEFINED,UNDEFINED,UNDEFINED); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0) { return innerCall(self,arg0,UNDEFINED,UNDEFINED); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1) { return innerCall(self,arg0,arg1,UNDEFINED); }
        @Override public abstract PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2);
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return innerCall(self,arg0,arg1,arg2); }
        @Override public final PSVarargs innerCall(PSValue self, PSVarargs args) { return innerCall(self,args.self(),args.arg(1),args.arg(2)); }
    }
    
    public static abstract class PSDefaultThreeArgsFunction extends PSFunction
    {
        private PSValue def0 = UNDEFINED;
        private PSValue def1 = UNDEFINED;
        private PSValue def2 = UNDEFINED;
        
        public static final PSDefaultThreeArgsFunction insertDefaults(PSValue def0, PSValue def1, PSValue def2, PSDefaultThreeArgsFunction base)
        {
            base.def0 = def0;
            base.def1 = def1;
            base.def2 = def2;
            return base;
        }
        public static final PSDefaultThreeArgsFunction insertDefaults(PSValue def1, PSValue def2, PSDefaultThreeArgsFunction base)
        {
            base.def1 = def1;
            base.def2 = def2;
            return base;
        }
        public static final PSDefaultThreeArgsFunction insertDefaults(PSValue def2, PSDefaultThreeArgsFunction base)
        {
            base.def2 = def2;
            return base;
        }
        
        @Override public final PSVarargs innerCall(PSValue self) { return innerCall(self,def0,def1,def2); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0) { return innerCall(self,arg0,def1,def2); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1) { return innerCall(self,arg0,arg1,def2); }
        @Override public abstract PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2);
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return innerCall(self,arg0,arg1,arg2); }
        @Override public final PSVarargs innerCall(PSValue self, PSVarargs args) { return innerCall(self,args.self(),args.arg(1),args.arg(2)); }
    }
    
    
    public static abstract class PSFourArgsFunction extends PSFunction
    {
        @Override public final PSVarargs innerCall(PSValue self) { return innerCall(self,UNDEFINED,UNDEFINED,UNDEFINED,UNDEFINED); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0) { return innerCall(self,arg0,UNDEFINED,UNDEFINED,UNDEFINED); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1) { return innerCall(self,arg0,arg1,UNDEFINED,UNDEFINED); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2) { return innerCall(self,arg0,arg1,arg2,UNDEFINED); }
        @Override public abstract PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3);
        @Override public final PSVarargs innerCall(PSValue self, PSVarargs args) { return innerCall(self,args.self(),args.arg(1),args.arg(2),args.arg(3)); }
    }
    
    public static abstract class PSDefaultFourArgsFunction extends PSFunction
    {
        private PSValue def0 = UNDEFINED;
        private PSValue def1 = UNDEFINED;
        private PSValue def2 = UNDEFINED;
        private PSValue def3 = UNDEFINED;
        
        public static final PSDefaultFourArgsFunction insertDefaults(PSValue def0, PSValue def1, PSValue def2, PSValue def3, PSDefaultFourArgsFunction base)
        {
            base.def0 = def0;
            base.def1 = def1;
            base.def2 = def2;
            base.def3 = def3;
            return base;
        }
        public static final PSDefaultFourArgsFunction insertDefaults(PSValue def1, PSValue def2, PSValue def3, PSDefaultFourArgsFunction base)
        {
            base.def1 = def1;
            base.def2 = def2;
            base.def3 = def3;
            return base;
        }
        public static final PSDefaultFourArgsFunction insertDefaults(PSValue def2, PSValue def3, PSDefaultFourArgsFunction base)
        {
            base.def2 = def2;
            base.def3 = def3;
            return base;
        }
        public static final PSDefaultFourArgsFunction insertDefaults(PSValue def3, PSDefaultFourArgsFunction base)
        {
            base.def3 = def3;
            return base;
        }
        
        @Override public final PSVarargs innerCall(PSValue self) { return innerCall(self,def0,def1,def2,def3); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0) { return innerCall(self,arg0,def1,def2,def3); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1) { return innerCall(self,arg0,arg1,def2,def3); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2) { return innerCall(self,arg0,arg1,arg2,def3); }
        @Override public abstract PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3);
        @Override public final PSVarargs innerCall(PSValue self, PSVarargs args) { return innerCall(self,args.self(),args.arg(1),args.arg(2),args.arg(3)); }
    }
    
    
    public static abstract class PSVarargsFunction extends PSFunction
    {
        @Override public final PSVarargs innerCall(PSValue self) { return innerCall(self,EMPTY); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0) { return innerCall(self,(PSVarargs)arg0); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1) { return innerCall(self,new PSVarargs.PairVarargs(arg0,arg1)); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2) { return innerCall(self,new PSVarargs.ArrayVarargs(arg0,arg1,arg2)); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return innerCall(self,new PSVarargs.ArrayVarargs(arg0,arg1,arg2,arg3)); }
        @Override public abstract PSVarargs innerCall(PSValue self, PSVarargs args);
    }
    
    public static abstract class PSDefaultVarargsFunction extends PSFunction
    {
        private int nodefaultArgs;
        private PSVarargs defs;
        
        public static final PSDefaultVarargsFunction insertDefaults(PSVarargs defaults, int totalArgs, PSDefaultVarargsFunction base)
        {
            base.defs = defaults;
            base.nodefaultArgs = totalArgs - defaults.numberOfArguments();
            return base;
        }
        protected final PSVarargs insertDefaults(PSVarargs args)
        {
            return defaultFunctionVarargs(nodefaultArgs,args,defs);
        }
        
        @Override public final PSVarargs innerCall(PSValue self) { return innerCall(self,EMPTY); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0) { return innerCall(self,(PSVarargs)arg0); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1) { return innerCall(self,new PSVarargs.PairVarargs(arg0,arg1)); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2) { return innerCall(self,new PSVarargs.ArrayVarargs(arg0,arg1,arg2)); }
        @Override public final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return innerCall(self,new PSVarargs.ArrayVarargs(arg0,arg1,arg2,arg3)); }
        @Override public abstract PSVarargs innerCall(PSValue self, PSVarargs args);
    }
    
    
    @FunctionalInterface
    public interface VoidZeroArgsF { void call(); }
    @FunctionalInterface
    public interface ZeroArgsF { PSVarargs call(); }
    @FunctionalInterface
    public interface VoidZeroArgsM<T extends PSValue> { void invoke(T self); }
    @FunctionalInterface
    public interface ZeroArgsM<T extends PSValue> { PSVarargs invoke(T self); }
    
    @FunctionalInterface
    public interface VoidOneArgF { void call(PSValue arg0); }
    @FunctionalInterface
    public interface OneArgF { PSVarargs call(PSValue arg0); }
    @FunctionalInterface
    public interface VoidOneArgM<T extends PSValue> { void invoke(T self, PSValue arg0); }
    @FunctionalInterface
    public interface OneArgM<T extends PSValue> { PSVarargs invoke(T self, PSValue arg0); }
    
    @FunctionalInterface
    public interface VoidTwoArgsF { void call(PSValue arg0, PSValue arg1); }
    @FunctionalInterface
    public interface TwoArgsF { PSVarargs call(PSValue arg0, PSValue arg1); }
    @FunctionalInterface
    public interface VoidTwoArgsM<T extends PSValue> { void invoke(T self, PSValue arg0, PSValue arg1); }
    @FunctionalInterface
    public interface TwoArgsM<T extends PSValue> { PSVarargs invoke(T self, PSValue arg0, PSValue arg1); }
    
    @FunctionalInterface
    public interface VoidThreeArgsF { void call(PSValue arg0, PSValue arg1, PSValue arg2); }
    @FunctionalInterface
    public interface ThreeArgsF { PSVarargs call(PSValue arg0, PSValue arg1, PSValue arg2); }
    @FunctionalInterface
    public interface VoidThreeArgsM<T extends PSValue> { void invoke(T self, PSValue arg0, PSValue arg1, PSValue arg2); }
    @FunctionalInterface
    public interface ThreeArgsM<T extends PSValue> { PSVarargs invoke(T self, PSValue arg0, PSValue arg1, PSValue arg2); }
    
    @FunctionalInterface
    public interface VoidFourArgsF { void call(PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3); }
    @FunctionalInterface
    public interface FourArgsF { PSVarargs call(PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3); }
    @FunctionalInterface
    public interface VoidFourArgsM<T extends PSValue> { void invoke(T self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3); }
    @FunctionalInterface
    public interface FourArgsM<T extends PSValue> { PSVarargs invoke(T self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3); }
    
    @FunctionalInterface
    public interface VoidVarargsF { void call(PSVarargs args); }
    @FunctionalInterface
    public interface VarargsF { PSVarargs call(PSVarargs args); }
    @FunctionalInterface
    public interface VoidVarargsM<T extends PSValue> { void invoke(T self, PSVarargs args); }
    @FunctionalInterface
    public interface VarargsM<T extends PSValue> { PSVarargs invoke(T self, PSVarargs args); }
    
    
    private static final PSValue CALL = varMethod((self, args) -> self.innerCall(args.self(), subVarargs(args, 1)));
    private static final PSValue APPLY = method((self, arg0, arg1) -> {
        List<PSValue> list = arg1.toJavaList();
        PSVarargs args = varargsOf(list.toArray(new PSValue[list.size()]));
        arg0 = arg0 == UNDEFINED ? NULL : arg0;
        return self.innerCall(arg0, args);
    });
}

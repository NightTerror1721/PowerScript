/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang;

import java.util.function.Function;

/**
 *
 * @author Asus
 */
final class Utils
{
    private Utils() {}
    
    static abstract class NativeObjectLibOneArg extends PSUserdata.ImmutableStruct
    {
        public NativeObjectLibOneArg(Function<String, PSValue> selector)
        {
            super(selector);
        }
        
        @Override protected abstract PSVarargs innerCall(PSValue self);
        @Override protected abstract PSVarargs innerCall(PSValue self, PSValue arg0);
        @Override protected final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1) { return innerCall(self,arg0); }
        @Override protected final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2) { return innerCall(self,arg0); }
        @Override protected final PSVarargs innerCall(PSValue self, PSValue arg0, PSValue arg1, PSValue arg2, PSValue arg3) { return innerCall(self,arg0); }
        @Override protected final PSVarargs innerCall(PSValue self, PSVarargs args) { return innerCall(self,args.self()); }
    }
}

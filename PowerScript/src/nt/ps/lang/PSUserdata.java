/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang;

import java.util.function.Function;
import nt.ps.exception.PSUnsupportedOperationException;

/**
 *
 * @author Asus
 */
public class PSUserdata extends PSValue
{
    @Override
    public final PSDataType getPSType() { return PSDataType.USERDATA; }
    
    @Override
    public final <U extends PSUserdata> U toPSUserdata() { return (U) this; }

    @Override
    public boolean equals(Object o) { return this == o; }

    @Override
    public int hashCode() { return superHashCode(); }
    
    @Override
    public final PSValue concat(PSValue value) { return new PSString(toJavaString().concat(value.toJavaString())); }
    
    @Override
    public final PSValue setPointerValue(PSValue value) { throw new PSUnsupportedOperationException(this,"setPointerValue"); }
    
    @Override
    public final PSValue getPointerValue() { throw new PSUnsupportedOperationException(this,"getPointerValue"); }
    
    
    public static class ImmutableStruct extends PSUserdata
    {
        private final Function<String, PSValue> selector;
        
        public ImmutableStruct(Function<String, PSValue> selector)
        {
            if(selector == null)
                throw new IllegalArgumentException();
            this.selector = selector;
        }
        
        @Override
        public final PSValue getProperty(String name)
        {
            PSValue value;
            return (value = selector.apply(name)) == null ? UNDEFINED : value;
        }
    }
}

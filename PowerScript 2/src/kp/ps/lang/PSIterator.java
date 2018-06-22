/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.ps.lang;

import java.util.LinkedList;

/**
 *
 * @author Asus
 */
public abstract class PSIterator extends PSValue
{
    
    @Override
    public final PSDataType getPSType() { return PSDataType.ITERATOR; }
    
    @Override
    public final PSIterator toPSIterator() { return this; }

    @Override
    public final boolean equals(Object o) { return this == o; }

    @Override
    public final int hashCode() { return superHashCode(); }
    
    @Override
    public final PSValue increase() { return next().arg0(); }
    
    @Override
    public final PSValue negate() { return hasNext() ? TRUE : FALSE; }
    
    @Override
    public final PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
            case "hasNext": return HAS_NEXT;
            case "next": return NEXT;
            case "forEach": return FOR_EACH;
            case "pack": return PACK;
        }
    }
    
    @Override public final PSIterator createIterator() { return this; }
    @Override public abstract boolean hasNext();
    @Override public abstract PSVarargs next();
    
    
    private static final PSValue HAS_NEXT = PSFunction.method((self) -> self.hasNext() ? TRUE : FALSE);
    private static final PSValue NEXT = PSFunction.method((self) -> self.next());
    private static final PSValue FOR_EACH = PSFunction.voidMethod((self, consumer) -> {
        while(self.hasNext())
            consumer.call(self.next());
    });
    private static final PSValue PACK = PSFunction.method((self) -> {
        LinkedList<PSValue> list = new LinkedList<>();
        while(self.hasNext())
        {
            PSVarargs result = self.next();
            switch(result.numberOfArguments())
            {
                case 0: list.add(NULL); break;
                case 1: list.add(result.self()); break;
                default: list.add(varargsAsPSArray(result, 0)); break;
            }
        }
        return new PSArray(list);
    });
}

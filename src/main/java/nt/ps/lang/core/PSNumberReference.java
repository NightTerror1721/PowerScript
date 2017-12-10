/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import nt.ps.lang.PSFunction;
import nt.ps.lang.PSNumber;
import nt.ps.lang.PSValue;
import nt.ps.lang.PSVarargs;
import nt.ps.lang.core.ImmutableCoreLibrary.PrimitiveReference;

/**
 *
 * @author Asus
 */
public final class PSNumberReference extends PrimitiveReference
{
    @Override
    public final PSValue createNewInstance() { return ZERO; }
    
    @Override
    public PSValue createNewInstance(PSValue arg0) { return new PSNumber.PSDouble(arg0.toJavaDouble()); }
    
    @Override
    protected final PSVarargs innerCall(PSValue self) { return ZERO; }

    @Override
    protected final PSVarargs innerCall(PSValue self, PSValue arg0) { return new PSNumber.PSDouble(arg0.toJavaDouble()); }
    
    @Override
    public PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
            case "int": case "int32": case "integer": case "toInteger": return INT;
            case "long": case "int64": case "toLong": return LONG;
            case "float": case "float32": case "toFloat": return FLOAT;
            case "double": case "float64": case "toDouble": return DOUBLE;
            case "MAX_INTEGER": return MAX_INTEGER;
            case "MIN_INTEGER": return MIN_INTEGER;
            case "MAX_FLOAT": return MAX_FLOAT;
            case "MIN_FLOAT": return MIN_FLOAT;
            case "MAX_EXPONENT": return MAX_EXPONENT;
            case "MIN_EXPONENT": return MIN_EXPONENT;
            case "POSITIVE_INFINITE": return POSITIVE_INFINITE;
            case "NEGATIVE_INFINITE": return NEGATIVE_INFINITE;
            case "NAN": return NAN;
            case "isNaN": return IS_NAN;
            case "isFinite": return IS_FINITE;
            case "isInfinite": return IS_INFINITE;
        }
    }
    
    private static final PSValue
            INT = PSFunction.function((arg0) -> new PSNumber.PSInteger(arg0.toJavaInt())),
            LONG = PSFunction.function((arg0) -> new PSNumber.PSLong(arg0.toJavaLong())),
            FLOAT = PSFunction.function((arg0) -> new PSNumber.PSFloat(arg0.toJavaFloat())),
            DOUBLE = PSFunction.function((arg0) -> new PSNumber.PSDouble(arg0.toJavaDouble())),
            MAX_INTEGER = new PSNumber.PSLong(Long.MAX_VALUE),
            MIN_INTEGER = new PSNumber.PSLong(Long.MIN_VALUE),
            MAX_FLOAT = new PSNumber.PSDouble(Double.MAX_VALUE),
            MIN_FLOAT = new PSNumber.PSDouble(Double.MIN_VALUE),
            MAX_EXPONENT = new PSNumber.PSInteger(Double.MAX_EXPONENT),
            MIN_EXPONENT = new PSNumber.PSInteger(Double.MIN_EXPONENT),
            POSITIVE_INFINITE = new PSNumber.PSDouble(Double.POSITIVE_INFINITY),
            NEGATIVE_INFINITE = new PSNumber.PSDouble(Double.NEGATIVE_INFINITY),
            NAN = new PSNumber.PSDouble(Double.NaN),
            IS_NAN = PSFunction.function((arg0) -> Double.isNaN(arg0.toJavaDouble()) ? TRUE : FALSE),
            IS_FINITE = PSFunction.function((arg0) -> Double.isFinite(arg0.toJavaDouble()) ? TRUE : FALSE),
            IS_INFINITE = PSFunction.function((arg0) -> Double.isInfinite(arg0.toJavaDouble()) ? TRUE : FALSE);
}

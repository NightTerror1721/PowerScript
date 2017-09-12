/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import java.util.Random;
import nt.ps.lang.PSFunction;
import nt.ps.lang.PSNumber;
import nt.ps.lang.PSValue;

/**
 *
 * @author Asus
 */
public final class PSMath extends ImmutableCoreLibrary
{
    private final Random rand = new Random();
    
    @Override
    public final PSValue getProperty(String name)
    {
        switch(name)
        {
            default: return UNDEFINED;
            case "E": return E;
            case "LN2": return LN2;
            case "LN10": return LN10;
            case "LOG10E": return LOG10E;
            case "PI": return PI;
            case "SQRT1_2": return SQRT1_2;
            case "SQRT2": return SQRT2;
            
            case "abs": return ABS;
            case "acos": return ACOS;
            case "acosh": return ACOSH;
            case "asin": return ASIN;
            case "asinh": return ASINH;
            case "atan": return ATAN;
            case "atanh": return ATANH;
            case "atan2": return ATAN2;
            case "cbrt": return CBRT;
            case "ceil": return CEIL;
            case "cos": return COS;
            case "cosh": return COSH;
            case "exp": return EXP;
            case "expm1": return EXPM1;
            case "floor": return FLOOR;
            case "hypot": return HYPOT;
            case "log": return LOG;
            case "log1p": return LOG1P;
            case "log10": return LOG10;
            case "max": return MAX;
            case "min": return MIN;
            case "pow": return POW;
            case "random": return RANDOM;
            case "round": return ROUND;
            case "sign": return SIGN;
            case "sin": return SIN;
            case "sinh": return SINH;
            case "sqrt": return SQRT;
            case "tan": return TAN;
            case "tanh": return TANH;
            case "trunc": return TRUNC;
        }
    }
    
    private static final PSValue
            E = new PSNumber.PSDouble(StrictMath.E),
            LN2 = new PSNumber.PSDouble(StrictMath.log(2)),
            LN10 = new PSNumber.PSDouble(StrictMath.log(10)),
            LOG10E = new PSNumber.PSDouble(StrictMath.E),
            PI = new PSNumber.PSDouble(StrictMath.PI),
            SQRT1_2 = new PSNumber.PSDouble(StrictMath.sqrt(0.5d)),
            SQRT2 = new PSNumber.PSDouble(StrictMath.sqrt(2)),
            
            ABS = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.abs(arg0.toJavaDouble()))),
            ACOS = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.acos(arg0.toJavaDouble()))),
            ACOSH = PSFunction.function((arg0) -> {
                double x = arg0.toJavaDouble();
                return new PSNumber.PSDouble(StrictMath.log(x + StrictMath.sqrt(x * x - 1d)));
            }),
            ASIN = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.asin(arg0.toJavaDouble()))),
            ASINH = PSFunction.function((arg0) -> {
                double x = arg0.toJavaDouble();
                return new PSNumber.PSDouble(StrictMath.log(x + StrictMath.sqrt(x * x + 1d)));
            }),
            ATAN = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.atan(arg0.toJavaDouble()))),
            ATANH = PSFunction.function((arg0) -> {
                double x = arg0.toJavaDouble();
                return new PSNumber.PSDouble(0.5d * (StrictMath.log(1 + x) - StrictMath.log(1 - x)));
            }),
            ATAN2 = PSFunction.function((arg0, arg1) -> new PSNumber.PSDouble(StrictMath.atan2(arg0.toJavaDouble(), arg1.toJavaDouble()))),
            CBRT = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.cbrt(arg0.toJavaDouble()))),
            CEIL = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.ceil(arg0.toJavaDouble()))),
            COS = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.cos(arg0.toJavaDouble()))),
            COSH = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.cosh(arg0.toJavaDouble()))),
            EXP = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.exp(arg0.toJavaDouble()))),
            EXPM1 = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.expm1(arg0.toJavaDouble()))),
            FLOOR = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.floor(arg0.toJavaDouble()))),
            HYPOT = PSFunction.function((arg0, arg1) -> new PSNumber.PSDouble(StrictMath.hypot(arg0.toJavaDouble(), arg1.toJavaDouble()))),
            LOG = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.log(arg0.toJavaDouble()))),
            LOG1P = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.log1p(arg0.toJavaDouble()))),
            LOG10 = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.log10(arg0.toJavaDouble()))),
            MAX = PSFunction.varFunction((args) -> {
                switch(args.numberOfArguments())
                {
                    case 0: case 1: return args;
                    case 2: return new PSNumber.PSDouble(StrictMath.max(args.self().toJavaDouble(), args.arg(1).toJavaDouble()));
                    default:
                        return varargsStream(args).max((a0, a1) -> Double.compare(a0.toJavaDouble(), a1.toJavaDouble())).get();
                }
            }),
            MIN = PSFunction.varFunction((args) -> {
                switch(args.numberOfArguments())
                {
                    case 0: case 1: return args;
                    case 2: return new PSNumber.PSDouble(StrictMath.max(args.self().toJavaDouble(), args.arg(1).toJavaDouble()));
                    default:
                        return varargsStream(args).min((a0, a1) -> Double.compare(a0.toJavaDouble(), a1.toJavaDouble())).get();
                }
            }),
            POW = PSFunction.function((arg0, arg1) -> new PSNumber.PSDouble(StrictMath.pow(arg0.toJavaDouble(), arg1.toJavaDouble()))),
            ROUND = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.round(arg0.toJavaDouble()))),
            SIGN = PSFunction.function((arg0) -> arg0.toJavaDouble() >= 0 ? ONE : MINUSONE),
            SIN = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.sin(arg0.toJavaDouble()))),
            SINH = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.sinh(arg0.toJavaDouble()))),
            SQRT = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.sqrt(arg0.toJavaDouble()))),
            TAN = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.tan(arg0.toJavaDouble()))),
            TANH = PSFunction.function((arg0) -> new PSNumber.PSDouble(StrictMath.tanh(arg0.toJavaDouble()))),
            TRUNC = PSFunction.function((arg0) -> new PSNumber.PSLong(arg0.toJavaLong()));
    
    private final PSValue
            RANDOM = PSFunction.function((arg0) -> {
                if(arg0 == UNDEFINED)
                    return new PSNumber.PSDouble(rand.nextDouble());
                return new PSNumber.PSLong((long) (rand.nextDouble() * arg0.toJavaLong()));
            });
}

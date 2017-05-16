/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.util.HashMap;
import java.util.LinkedList;
import nt.ps.PSGlobals;
import nt.ps.PSScript;
import nt.ps.lang.*;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

/**
 *
 * @author mpasc
 */
public final class JavaBytecodeGenerator extends BytecodeGenerator
{
    public static final String
            STR_TYPE_VALUE = PSValue.class.getName(),
            STR_TYPE_UNDEFINED = PSValue.UNDEFINED.getClass().getName(),
            STR_TYPE_NULL = PSValue.NULL.getClass().getName(),
            STR_TYPE_NUMBER = PSNumber.class.getName(),
            STR_TYPE_INTEGER = PSNumber.PSInteger.class.getName(),
            STR_TYPE_LONG = PSNumber.PSLong.class.getName(),
            STR_TYPE_FLOAT = PSNumber.PSFloat.class.getName(),
            STR_TYPE_DOUBLE = PSNumber.PSDouble.class.getName(),
            STR_TYPE_BOOLEAN = PSBoolean.class.getName(),
            STR_TYPE_STRING = PSString.class.getName(),
            STR_TYPE_ARRAY = PSArray.class.getName(),
            STR_TYPE_TUPLE = PSTuple.class.getName(),
            STR_TYPE_MAP = PSMap.class.getName(),
            STR_TYPE_OBJECT = PSObject.class.getName(),
            STR_TYPE_FUNCTION = PSFunction.class.getName(),
            STR_TYPE_ITERATOR = PSIterator.class.getName(),
            STR_TYPE_USERDATA = PSUserdata.class.getName(),
            STR_TYPE_POINTER = PSPointer.class.getName(),
            STR_TYPE_GLOBALS = PSGlobals.class.getName(),
            STR_TYPE_VARARGS = PSVarargs.class.getName(),
            STR_TYPE_HASHMAP = HashMap.class.getName(),
            STR_TYPE_LINKEDLIST = LinkedList.class.getName();
    
    public static final Type
            TYPE_VALUE = new ObjectType(STR_TYPE_VALUE),
            TYPE_UNDEFINED = new ObjectType(STR_TYPE_UNDEFINED),
            TYPE_NULL = new ObjectType(STR_TYPE_NULL),
            TYPE_NUMBER = new ObjectType(STR_TYPE_NUMBER),
            TYPE_INTEGER = new ObjectType(STR_TYPE_INTEGER),
            TYPE_LONG = new ObjectType(STR_TYPE_LONG),
            TYPE_FLOAT = new ObjectType(STR_TYPE_FLOAT),
            TYPE_DOUBLE = new ObjectType(STR_TYPE_DOUBLE),
            TYPE_BOOLEAN = new ObjectType(STR_TYPE_BOOLEAN),
            TYPE_STRING = new ObjectType(STR_TYPE_STRING),
            TYPE_ARRAY = new ObjectType(STR_TYPE_ARRAY),
            TYPE_TUPLE = new ObjectType(STR_TYPE_TUPLE),
            TYPE_MAP = new ObjectType(STR_TYPE_MAP),
            TYPE_OBJECT = new ObjectType(STR_TYPE_OBJECT),
            TYPE_FUNCTION = new ObjectType(STR_TYPE_FUNCTION),
            TYPE_ITERATOR = new ObjectType(STR_TYPE_ITERATOR),
            TYPE_USERDATA = new ObjectType(STR_TYPE_USERDATA),
            TYPE_POINTER = new ObjectType(STR_TYPE_POINTER),
            TYPE_GLOBALS = new ObjectType(STR_TYPE_GLOBALS),
            TYPE_VARARGS = new ObjectType(STR_TYPE_VARARGS),
            TYPE_HASHMAP = new ObjectType(STR_TYPE_HASHMAP),
            TYPE_LINKEDLIST = new ObjectType(STR_TYPE_LINKEDLIST);
    
    public static final String
            STR_FUNC_CALL = "call",
            STR_FUNC_SELF = "self",
            STR_FUNC_SET_GLOBALS = "setGlobals",
            STR_GLOBALS_ATTRIBUTE = "__G";
    
    public static final Type[]
            NO_ARGS = {},
            ARGS_VALUE_1 = { TYPE_VALUE },
            ARGS_VALUE_2 = { TYPE_VALUE, TYPE_VALUE },
            ARGS_VALUE_3 = { TYPE_VALUE, TYPE_VALUE, TYPE_VALUE },
            ARGS_VALUE_4 = { TYPE_VALUE, TYPE_VALUE, TYPE_VALUE, TYPE_VALUE },
            ARGS_VARARGS = { TYPE_VARARGS },
            ARGS_STRING = { TYPE_STRING },
            ARGS_STRING_VALUE_1 = { TYPE_STRING, TYPE_VALUE },
            ARGS_STRING_VALUE_2 = { TYPE_STRING, TYPE_VALUE, TYPE_VALUE },
            ARGS_STRING_VALUE_3 = { TYPE_STRING, TYPE_VALUE, TYPE_VALUE, TYPE_VALUE },
            ARGS_STRING_VALUE_4 = { TYPE_STRING, TYPE_VALUE, TYPE_VALUE, TYPE_VALUE, TYPE_VALUE },
            ARGS_STRING_VARARGS = { TYPE_STRING, TYPE_VARARGS },
            ARGS_VALUE_VARARGS = { TYPE_VALUE, TYPE_VARARGS },
            ARGS_A_VALUE = { new ArrayType(TYPE_VALUE,1) };
    
    
    
    
    
    
    
    
    public enum FunctionId
    {
        FUNC0(0,   PSFunction.PSZeroArgsFunction.class,         NO_ARGS,      ARGS_STRING),
        FUNC1(1,   PSFunction.PSOneArgFunction.class,           ARGS_VALUE_1, ARGS_STRING_VALUE_1),
        FUNC2(2,   PSFunction.PSTwoArgsFunction.class,          ARGS_VALUE_2, ARGS_STRING_VALUE_2),
        FUNC3(3,   PSFunction.PSThreeArgsFunction.class,        ARGS_VALUE_3, ARGS_STRING_VALUE_3),
        FUNC4(4,   PSFunction.PSFourArgsFunction.class,         ARGS_VALUE_4, ARGS_STRING_VALUE_4),
        FUNCV(-1,  PSFunction.PSVarargsFunction.class,          ARGS_VARARGS, ARGS_STRING_VARARGS),
        FUNC1D(1,  PSFunction.PSDefaultOneArgFunction.class,    ARGS_VALUE_1, ARGS_STRING_VALUE_1),
        FUNC2D(2,  PSFunction.PSDefaultTwoArgsFunction.class,   ARGS_VALUE_2, ARGS_STRING_VALUE_2),
        FUNC3D(3,  PSFunction.PSDefaultThreeArgsFunction.class, ARGS_VALUE_3, ARGS_STRING_VALUE_3),
        FUNC4D(4,  PSFunction.PSDefaultFourArgsFunction.class,  ARGS_VALUE_4, ARGS_STRING_VALUE_4),
        FUNCVD(-1, PSFunction.PSDefaultVarargsFunction.class,   ARGS_VARARGS, ARGS_STRING_VARARGS),
        SCRIPT(0,  PSScript.class,                              NO_ARGS,      ARGS_STRING);
        
        private final String name;
        private final int numArgs;
        private final Type[] callArgs, invokeArgs;
        
        private FunctionId(int numArgs, Class<? extends PSFunction> clazz, Type[] callArgs, Type[] invokeArgs)
        {
            name = clazz.getName();
            this.numArgs = numArgs;
            this.callArgs = callArgs;
            this.invokeArgs = invokeArgs;
        }
        
        private String getName() { return name; }
        private int getNumArgs() { return numArgs; }
        private boolean isVarargs() { return numArgs < 0; }
        private Type[] getCallArgs() { return callArgs; }
        private Type[] getInvokeArgs() { return invokeArgs; }
    }
}

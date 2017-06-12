/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import nt.ps.PSClassLoader;
import nt.ps.PSGlobals;
import nt.ps.PSScript;
import nt.ps.compiler.CompilerBlock.CompilerBlockType;
import nt.ps.compiler.VariablePool.Variable;
import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.parser.Literal;
import nt.ps.compiler.parser.MutableLiteral;
import nt.ps.compiler.parser.Operator;
import nt.ps.compiler.parser.OperatorSymbol;
import nt.ps.lang.*;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.*;

/**
 *
 * @author mpasc
 */
final class BytecodeGenerator
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
            STR_TYPE_LINKEDLIST = LinkedList.class.getName(),
            STR_TYPE_UTILS = LangUtils.class.getName(),
            STR_TYPE_PROTOMAP = LangUtils.ProtoMap.class.getName(),
            STR_TYPE_PROTOOBJECT = LangUtils.ProtoObject.class.getName();
    
    public static final ObjectType
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
            TYPE_LINKEDLIST = new ObjectType(STR_TYPE_LINKEDLIST),
            TYPE_UTILS = new ObjectType(STR_TYPE_UTILS),
            TYPE_PROTOMAP = new ObjectType(STR_TYPE_PROTOMAP),
            TYPE_PROTOOBJECT = new ObjectType(STR_TYPE_PROTOOBJECT);
    
    public static final ArrayType
            TYPE_ARRAY_VALUE = new ArrayType(TYPE_VALUE, 1);
    
    public static final String
            STR_FUNC_CALL = "call",
            STR_FUNC_SELF = "self",
            STR_FUNC_NAME = "innerCall",
            STR_FUNC_SET_GLOBALS = "setGlobals",
            STR_FUNC_ARG = "arg",
            STR_EXPAND_TEMP = "__exp",
            STR_GLOBALS_ATTRIBUTE = "__G",
            STR_UP_POINTERS = "__upptrs";
    
    public static final Type[]
            NO_ARGS = {},
            ARGS_VALUE_1 = { TYPE_VALUE },
            ARGS_VALUE_2 = { TYPE_VALUE, TYPE_VALUE },
            ARGS_VALUE_3 = { TYPE_VALUE, TYPE_VALUE, TYPE_VALUE },
            ARGS_VALUE_4 = { TYPE_VALUE, TYPE_VALUE, TYPE_VALUE, TYPE_VALUE },
            ARGS_VALUE_5 = { TYPE_VALUE, TYPE_VALUE, TYPE_VALUE, TYPE_VALUE, TYPE_VALUE },
            ARGS_VARARGS = { TYPE_VARARGS },
            ARGS_STRING = { Type.STRING },
            ARGS_STRING_VALUE_1 = { Type.STRING, TYPE_VALUE },
            ARGS_STRING_VALUE_2 = { Type.STRING, TYPE_VALUE, TYPE_VALUE },
            ARGS_STRING_VALUE_3 = { Type.STRING, TYPE_VALUE, TYPE_VALUE, TYPE_VALUE },
            ARGS_STRING_VALUE_4 = { Type.STRING, TYPE_VALUE, TYPE_VALUE, TYPE_VALUE, TYPE_VALUE },
            ARGS_STRING_VARARGS = { Type.STRING, TYPE_VARARGS },
            ARGS_VALUE_VARARGS = { TYPE_VALUE, TYPE_VARARGS },
            ARGS_A_VALUE = { new ArrayType(TYPE_VALUE, 1) },
            ARGS_GLOBALS = { TYPE_GLOBALS },
            ARGS_INT = { Type.INT },
            ARGS_LONG = { Type.LONG },
            ARGS_FLOAT = { Type.FLOAT },
            ARGS_DOUBLE = { Type.DOUBLE },
            ARGS_VARARGS_INT = { TYPE_VARARGS, Type.INT },
            ARGS_STRING_VALUE = ARGS_STRING_VALUE_1,
            ARGS_VALUE_INT = { TYPE_VALUE, Type.INT },
            ARGS_JAVAMAP = { new ObjectType(Map.class.getName()) },
            ARGS_HASHMAP = { TYPE_HASHMAP };
    
    private static final Type[][][] FUNC_ARGS = {
        { NO_ARGS, ARGS_VALUE_1, ARGS_VALUE_2, ARGS_VALUE_3, ARGS_VALUE_4, ARGS_VARARGS },
        { ARGS_STRING, ARGS_STRING_VALUE_1, ARGS_STRING_VALUE_2, ARGS_STRING_VALUE_3, ARGS_STRING_VALUE_4, ARGS_STRING_VARARGS }
    };
    
    public static final String
            STR_VAR_PREFIX = "var",
            STR_TEMP_PREFIX = "temp",
            STR_CONSTANT_PREFIX = "cnt",
            STR_SELF = "self";
    
    private static final int LOCAL_FIRST_ID = 2;
    private static final int SELF_ID = 1;
    private static final int INTERNAL_THIS_ID = 0;
    private static final int LOCAL_FIRST_REFERENCE = 1;
    private static final int SELF_REFERENCE = 0;
    
    
    private CompilerBlock compiler;
    
    private final PSClassLoader classLoader;
    private final ClassGen mainClass;
    private final ConstantPoolGen constantPool;
    private final InstructionList mainInst;
    private final InstructionList constInst;
    private final InstructionFactory factory;
    private final MethodGen mainMethod;
    private byte[] bytecode;
    
    private final String className;
    private final String fileName;
    private final FunctionId functionId;
    private final int argsLen;
    private final int defaultValues;
    private final boolean packExtraArgs;
    
    private final HashMap<Integer, Integer> localVars = new HashMap<>();
    private int localVarsCount = 0;
    
    private final HashSet<Integer> pointerVars = new HashSet<>();
    private final LinkedList<BranchInfo> branchInfo = new LinkedList<>();
    private final HashMap<PSValue,String> constants = new HashMap<>();
    private final HashMap<String, TempInfo> tempVars = new HashMap<>();
    private int expandId = -1;
    
    private long inheritedFunctionsId = 0;
    
    public BytecodeGenerator(PSClassLoader classLoader, String name, int argsLen, int defaultValues, boolean packExtraArgs)
    {
        this(classLoader, name, FunctionId.select(argsLen, defaultValues, packExtraArgs), argsLen, defaultValues, packExtraArgs);
    }
    
    public BytecodeGenerator(PSClassLoader classLoader, String name) { this(classLoader, name, FunctionId.SCRIPT, 0, 0, false); }
    
    private BytecodeGenerator(PSClassLoader classLoader, String className, FunctionId functionId, int argsLen, int defaultValues, boolean packExtraArgs)
    {
        if(classLoader == null)
            throw new NullPointerException();
        if(className == null)
            throw new NullPointerException();
        if(className.isEmpty())
            throw new IllegalArgumentException();
        if(functionId == null)
            throw new NullPointerException();
        if(argsLen < -1)
            throw new IllegalArgumentException();
        if(!functionId.isVarargs() && defaultValues > argsLen)
            throw new IllegalArgumentException();
        
        this.classLoader = classLoader;
        this.className = className;
        this.fileName = className + ".class";
        this.functionId = functionId;
        this.argsLen = argsLen;
        this.defaultValues = defaultValues;
        this.packExtraArgs = packExtraArgs;
        
        this.mainClass = new ClassGen(
                className,
                functionId.getName(),
                this.fileName,
                Constants.ACC_PUBLIC | Constants.ACC_FINAL | Constants.ACC_SUPER,
                new String[] {}
        );
        this.constantPool = this.mainClass.getConstantPool();
        this.mainInst = new InstructionList();
        this.constInst = new InstructionList();
        this.factory = new InstructionFactory(this.mainClass, this.constantPool);
        this.mainMethod = functionId.createMethod(className, this.mainInst, this.constantPool);
        
        initLocalVariables();
        createGlobalsSetter();
    }
    
    final void setCompiler(CompilerBlock compiler)
    {
        if(compiler == null)
            throw new NullPointerException();
        if(this.compiler != null)
            throw new IllegalStateException();
        this.compiler = compiler;
    }
    
    private void createGlobalsSetter()
    {
        InstructionList envs = new InstructionList();
        FieldGen field = new FieldGen(
                Constants.ACC_PRIVATE,
                TYPE_GLOBALS,
                STR_GLOBALS_ATTRIBUTE,
                constantPool
        );
        mainClass.addField(field.getField());
        MethodGen mg = new MethodGen(
                Constants.ACC_PUBLIC | Constants.ACC_FINAL,
                Type.VOID,
                ARGS_GLOBALS,
                new String[] { "g" },
                STR_FUNC_SET_GLOBALS,
                STR_TYPE_FUNCTION,
                envs,
                constantPool
        );
        envs.append(InstructionConstants.THIS);
        envs.append(new ALOAD(1));
        envs.append(factory.createFieldAccess(className,STR_GLOBALS_ATTRIBUTE,
                TYPE_GLOBALS, Constants.PUTFIELD));
        envs.append(InstructionConstants.RETURN);
        mg.setMaxStack();
        mainClass.addMethod(mg.getMethod());
        envs.dispose();
    }
    
    private void initLocalVariables()
    {
        //localVars.put(SELF_REFERENCE, SELF_ID);
        
        if(!functionId.isVarargs())
        {
            for(int i=0;i<argsLen;i++)
                localVars.put(i + LOCAL_FIRST_REFERENCE, i + LOCAL_FIRST_ID);
        }
        else
        {
            if(!packExtraArgs && defaultValues > 0)
            {
                mainInst.append(InstructionConstants.THIS);
                mainInst.append(new ALOAD(LOCAL_FIRST_ID));
                mainInst.append(factory.createInvoke(mainClass.getSuperclassName(), "insertDefaults",
                        TYPE_VARARGS, ARGS_VARARGS, Constants.INVOKEVIRTUAL));
                mainInst.append(new ASTORE(LOCAL_FIRST_ID));
            }
            int len = packExtraArgs ? argsLen - 1 : argsLen;
            mainInst.append(new ALOAD(LOCAL_FIRST_ID));
            for(int i=0;i<len;i++)
            {
                if(i < argsLen - 1)
                    mainInst.append(InstructionConstants.DUP);
                if(i == 0)
                    mainInst.append(factory.createInvoke(STR_TYPE_VARARGS, STR_FUNC_SELF,
                            TYPE_VALUE, NO_ARGS, Constants.INVOKEVIRTUAL));
                else
                {
                    mainInst.append(new PUSH(constantPool, i));
                    mainInst.append(factory.createInvoke(STR_TYPE_VARARGS, STR_FUNC_ARG,
                        TYPE_VALUE, ARGS_INT, Constants.INVOKEVIRTUAL));
                }
                createLocal(i);
                storeLocal(i);
            }
            if(packExtraArgs)
            {
                mainInst.append(new PUSH(constantPool, len));
                mainInst.append(factory.createInvoke(STR_TYPE_VARARGS, "varargsAsPSArray",
                        TYPE_VALUE, ARGS_VARARGS_INT, Constants.INVOKESTATIC));
                createLocal(len);
                storeLocal(len);
            }
        }
    }
    
    private byte[] completeClass(CompilerBlockType type)
    {
        if(!constInst.isEmpty())
        {
            MethodGen mg = new MethodGen(
                    Constants.ACC_STATIC,
                    Type.VOID,
                    NO_ARGS,
                    new String[] {},
                    "<clinit>", 
                    mainClass.getClassName(),
                    constInst,
                    mainClass.getConstantPool());
            constInst.append(InstructionConstants.RETURN);
            mg.setMaxStack();
            mainClass.addMethod(mg.getMethod());
            constInst.dispose();
        }
        
        if(type == CompilerBlockType.SCRIPT)
            mainClass.addEmptyConstructor(Constants.ACC_PUBLIC);
        resolveAllBranches();
        mainMethod.setMaxStack();
        mainClass.addMethod(mainMethod.getMethod());
        mainInst.dispose();
        
        try
        {
            JavaClass javaClass = mainClass.getJavaClass();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javaClass.dump(baos);
            return baos.toByteArray();
        }
        catch(IOException ex)
        {
            throw new RuntimeException("JavaClass.dump() threw " + ex);
        }
    }
    
    public final void createUpPointerSlots()
    {
        FieldGen field = new FieldGen(
                Constants.ACC_PRIVATE,
                TYPE_ARRAY_VALUE,
                STR_UP_POINTERS,
                constantPool
        );
        mainClass.addField(field.getField());
        /*InstructionList instr = new InstructionList();
        MethodGen mg = new MethodGen(
                Constants.ACC_PUBLIC | Constants.ACC_FINAL,
                Type.VOID,
                ARGS_VALUE_INT,
                new String[] { "value", "slot" },
                "insertUpPointer",
                STR_TYPE_FUNCTION,
                instr,
                constantPool
        );
        instr.append(InstructionConstants.THIS);
        instr.append(factory.createGetField(className,STR_UP_POINTERS,TYPE_ARRAY_VALUE));
        instr.append(new ILOAD(2));
        instr.append(new ALOAD(1));
        instr.append(InstructionConstants.AASTORE);
        instr.append(InstructionConstants.RETURN);
        mg.setMaxStack();
        mainClass.addMethod(mg.getMethod());
        instr.dispose();*/
    }
    
    public final void initiateUpPointersArray(int count)
    {
        InstructionList il = new InstructionList();
        il.append(InstructionConstants.THIS);
        il.append(new INVOKESPECIAL(constantPool.addMethodref(mainClass.getSuperclassName(),"<init>","()V")));
        il.append(InstructionConstants.THIS);
        il.append(new ALOAD(1));
        il.append(factory.createPutField(className, STR_UP_POINTERS, TYPE_ARRAY_VALUE));
        il.append(InstructionConstants.RETURN);
        MethodGen mg = new MethodGen(Constants.ACC_PUBLIC, Type.VOID, ARGS_A_VALUE,
                new String[]{},"<init>",className,il,constantPool);
        mg.setMaxStack();
        mainClass.addMethod(mg.getMethod());
    }
    
    private InstructionHandle createInheritedUpPointers(List<Variable> vars)
    {
        InstructionHandle ih = mainInst.append(factory.createNewArray(TYPE_VALUE, (short) 1));
        int count = 0;
        for(Variable var : vars)
        {
            mainInst.append(InstructionConstants.DUP);
            Variable pref = var.getUpPointerReference();
            if(pref == null)
                throw new IllegalStateException();
            if(!pref.isLocalPointer() && !pref.isUpPointer())
            {
                castLocaltoPointer(pref.getReference());
                pref.switchToLocalPointer();
            }
            if(pref.isUpPointer())
            {
                mainInst.append(InstructionConstants.THIS);
                mainInst.append(factory.createGetField(className, STR_UP_POINTERS, TYPE_ARRAY_VALUE));
                mainInst.append(new PUSH(constantPool,pref.getReference()));
                mainInst.append(InstructionConstants.AALOAD);
            }
            else loadLocal(pref.getReference());
            mainInst.append(new PUSH(constantPool, count++));
            ih = mainInst.append(new AASTORE());
        }
        return ih;
    }
    
    private InstructionHandle castLocaltoPointer(int reference)
    {
        //Integer idx = localVars.get(slot);
        if(pointerVars.contains(reference)/* || idx == null*/)
            throw new IllegalStateException();
        mainInst.append(factory.createNew(TYPE_POINTER));
        mainInst.append(InstructionConstants.DUP);
        loadLocal(reference);
        mainInst.append(factory.createInvoke(
                STR_TYPE_POINTER,
                "<init>",
                Type.VOID,
                new Type[] { TYPE_VALUE },
                Constants.INVOKESPECIAL));
        InstructionHandle ih = storeLocal(reference);
        pointerVars.add(reference);
        return ih;
    }
    
    private InstructionHandle insertClosureDefaults(BytecodeGenerator closure, InstructionHandle last)
    {
        if(closure.defaultValues <= 0 || closure.argsLen <= 0)
            return last;
        ObjectType supert = new ObjectType(closure.mainClass.getSuperclassName());
        if(closure.argsLen < 5)
        {
            Type[] args;
            switch(closure.defaultValues)
            {
                case 1: args = new Type[] { TYPE_VALUE, supert }; break;
                case 2: args = new Type[] { TYPE_VALUE, TYPE_VALUE, supert }; break;
                case 3: args = new Type[] { TYPE_VALUE, TYPE_VALUE, TYPE_VALUE, supert }; break;
                case 4: args = new Type[] { TYPE_VALUE, TYPE_VALUE, TYPE_VALUE, TYPE_VALUE, supert }; break;
                default: throw new IllegalStateException();
            }
            return mainInst.append(factory.createInvoke(closure.mainClass.getSuperclassName(),"insertDefaults",
                    supert,args,Constants.INVOKESTATIC));
        }
        //createTemp("defs_temp");
        //storeTemp("defs_temp");
        //LocalVariableGen temp = mainMethod.addLocalVariable("defs_temp",TYPE_VALUE,last,null);
        //mainInst.append(new ASTORE(temp.getIndex()));
        wrapArgsToArray(closure.defaultValues);
        mainInst.append(new PUSH(constantPool,closure.argsLen));
        mainInst.append(InstructionConstants.SWAP);
        //append(new ALOAD(temp.getIndex()));
        InstructionHandle ih = mainInst.append(factory.createInvoke(closure.mainClass.getSuperclassName(),"insertDefaults",
                    new ObjectType(closure.mainClass.getSuperclassName()),new Type[]{TYPE_VARARGS,Type.INT,supert},Constants.INVOKESTATIC));
        //temp.setEnd(ih);
        return ih;
    }
    
    
    
    
    /* FUNCTIONS */
    public final InstructionHandle createFunction(Class<? extends PSFunction> functionClass,
            BytecodeGenerator functionBytecodeGenerator, List<Variable> upPointers) throws CompilerError
    {
        mainInst.append(factory.createNew(new ObjectType(functionClass.getName())));
        mainInst.append(InstructionConstants.DUP);
        createInheritedUpPointers(upPointers);
        mainInst.append(factory.createInvoke(
                functionClass.getName(),
                "<init>",
                Type.VOID,
                ARGS_A_VALUE,
                Constants.INVOKESPECIAL));
        mainInst.append(InstructionConstants.DUP);
        mainInst.append(InstructionConstants.THIS);
        mainInst.append(factory.createGetField(className,STR_GLOBALS_ATTRIBUTE,TYPE_GLOBALS));
        InstructionHandle ih = mainInst.append(factory.createInvoke(functionClass.getName(),STR_FUNC_SET_GLOBALS,
                Type.VOID,new Type[]{TYPE_GLOBALS},Constants.INVOKEVIRTUAL));
        ih = insertClosureDefaults(functionBytecodeGenerator, ih);
        if(functionBytecodeGenerator.defaultValues > 0)
            compiler.getStack().pop(functionBytecodeGenerator.defaultValues);
        compiler.getStack().push();
        return ih;
    }
    
    
    
    
    /* ASSIGNATIONS */
    public final InstructionHandle callStoreAccess() throws CompilerError
    {
        compiler.getStack().pop(3);
        compiler.getStack().push();
        return mainInst.append(factory.createInvoke(STR_TYPE_VALUE, "set",
                TYPE_VALUE, ARGS_VALUE_2, Constants.INVOKEVIRTUAL));
    }
    
    public final InstructionHandle callStorePropertyAccess(String property) throws CompilerError
    {
        compiler.getStack().pop(3);
        compiler.getStack().push();
        mainInst.append(new PUSH(constantPool, property));
        mainInst.append(InstructionConstants.SWAP);
        return mainInst.append(factory.createInvoke(STR_TYPE_VALUE, "setProperty",
                TYPE_VALUE, ARGS_STRING_VALUE, Constants.INVOKEVIRTUAL));
    }
    
    
    
    
    
    /* VARIABLES */
    private InstructionHandle loadLocal(int reference)
    {
        Integer id = localVars.get(reference);
        if(id == null)
            throw new IllegalStateException("Variable with reference " + reference + " does not exists");
        return mainInst.append(new ALOAD(id));
    }
    
    public final InstructionHandle loadSelf()
    {
        return mainInst.append(new ALOAD(SELF_ID));
    }
    
    private InstructionHandle storeLocal(int reference)
    {
        if(pointerVars.contains(reference))
            throw new IllegalStateException();
        Integer id = localVars.get(reference);
        if(id == null)
            throw new IllegalStateException("Variable with reference " + reference + " does not exists");
        return mainInst.append(new ASTORE(id));
    }
    
    private void createLocal(int reference)
    {
        if(localVars.containsKey(reference))
            throw new IllegalStateException();
        
        String name = STR_VAR_PREFIX + (localVarsCount++);
        LocalVariableGen local = mainMethod.addLocalVariable(name, TYPE_VALUE, null, null);
        localVars.put(reference, local.getIndex());
    }
    
    private InstructionHandle loadLocalPointer(int reference)
    {
        if(!localVars.containsKey(reference) || !pointerVars.contains(reference))
            throw new IllegalStateException();
        loadLocal(reference);
        return mainInst.append(factory.createInvoke(STR_TYPE_VALUE,"getPointerValue",
                TYPE_VALUE,NO_ARGS,Constants.INVOKEVIRTUAL));
    }
    
    private InstructionHandle storeLocalPointer(int reference)
    {
        if(!localVars.containsKey(reference) || !pointerVars.contains(reference))
            throw new IllegalStateException();
        loadLocal(reference);
        mainInst.append(InstructionConstants.SWAP);
        return mainInst.append(factory.createInvoke(STR_TYPE_VALUE,"setPointerValue",
                Type.VOID,ARGS_VALUE_1,Constants.INVOKEVIRTUAL));
    }
    
    private InstructionHandle loadUpPointer(int reference)
    {
        /*if(!localVars.containsKey(slot) || !pointerVars.contains(slot))
            throw new IllegalStateException();*/
        mainInst.append(InstructionConstants.THIS);
        mainInst.append(factory.createGetField(className,STR_UP_POINTERS,TYPE_ARRAY_VALUE));
        mainInst.append(new PUSH(constantPool,reference));
        mainInst.append(InstructionConstants.AALOAD);
        return mainInst.append(factory.createInvoke(STR_TYPE_VALUE,"getPointerValue",
                TYPE_VALUE,NO_ARGS,Constants.INVOKEVIRTUAL));
    }
    
    private InstructionHandle storeUpPointer(int reference)
    {
        /*if(!localVars.containsKey(slot) || !pointerVars.contains(slot))
            throw new IllegalStateException();*/
        mainInst.append(InstructionConstants.THIS);
        mainInst.append(factory.createGetField(className,STR_UP_POINTERS,TYPE_ARRAY_VALUE));
        mainInst.append(new PUSH(constantPool,reference));
        mainInst.append(InstructionConstants.AALOAD);
        mainInst.append(InstructionConstants.SWAP);
        
        return mainInst.append(factory.createInvoke(STR_TYPE_VALUE,"setPointerValue",
                Type.VOID,ARGS_VALUE_1,Constants.INVOKEVIRTUAL));
    }
    
    private InstructionHandle loadGlobal(String namevar)
    {
        mainInst.append(InstructionConstants.THIS);
        mainInst.append(factory.createGetField(className,STR_GLOBALS_ATTRIBUTE,TYPE_GLOBALS));
        mainInst.append(new PUSH(constantPool,namevar));
        return mainInst.append(factory.createInvoke(STR_TYPE_GLOBALS,"getGlobalValue",
                TYPE_VALUE,new Type[] { Type.STRING },
                Constants.INVOKEVIRTUAL));
    }
    
    private InstructionHandle storeGlobal(String namevar)
    {
        mainInst.append(InstructionConstants.THIS);
        mainInst.append(factory.createGetField(className,STR_GLOBALS_ATTRIBUTE,TYPE_GLOBALS));
        mainInst.append(InstructionConstants.SWAP);
        mainInst.append(new PUSH(constantPool,namevar));
        mainInst.append(InstructionConstants.SWAP);
        return mainInst.append(factory.createInvoke(STR_TYPE_GLOBALS,"setGlobalValue",
                Type.VOID,new Type[] { Type.STRING, TYPE_VALUE },
                Constants.INVOKEVIRTUAL));
    }
    
    public final InstructionHandle load(Variable var) throws CompilerError
    {
        compiler.getStack().push();
        switch(var.getVariableType())
        {
            case LOCAL: return loadLocal(var.getReference());
            case LOCAL_POINTER: return loadLocalPointer(var.getReference());
            case UP_POINTER: return loadUpPointer(var.getReference());
            case GLOBAL: return loadGlobal(var.getName());
            default: throw new IllegalStateException();
        }
    }
    
    public final InstructionHandle store(Variable var) throws CompilerError
    {
        compiler.getStack().pop();
        switch(var.getVariableType())
        {
            case LOCAL:
                if(!localVars.containsKey(var.getReference()))
                    createLocal(var.getReference());
                return storeLocal(var.getReference());
            case LOCAL_POINTER: return storeLocalPointer(var.getReference());
            case UP_POINTER: return storeUpPointer(var.getReference());
            case GLOBAL: return storeGlobal(var.getName());
            default: throw new IllegalStateException();
        }
    }
    
    public final InstructionHandle storeUndefined(Variable var) throws CompilerError
    {
        loadUndefined();
        return store(var);
    }
    
    public final InstructionHandle storeNull(Variable var) throws CompilerError
    {
        loadNull();
        return store(var);
    }
    
    public final InstructionHandle storeLiteral(Variable var, Literal literal) throws CompilerError
    {
        loadLiteral(literal);
        return store(var);
    }
    
    public final InstructionHandle storeLiteral(Variable var, boolean literal) throws CompilerError
    {
        loadBoolean(literal);
        return store(var);
    }
    
    public final InstructionHandle storeLiteral(Variable var, int literal) throws CompilerError
    {
        loadConstant(PSValue.valueOf(literal));
        return store(var);
    }
    public final InstructionHandle storeLiteral(Variable var, long literal) throws CompilerError
    {
        loadConstant(PSValue.valueOf(literal));
        return store(var);
    }
    public final InstructionHandle storeLiteral(Variable var, float literal) throws CompilerError
    {
        loadConstant(PSValue.valueOf(literal));
        return store(var);
    }
    public final InstructionHandle storeLiteral(Variable var, double literal) throws CompilerError
    {
        loadConstant(PSValue.valueOf(literal));
        return store(var);
    }
    
    public final InstructionHandle storeLiteral(Variable var, String literal) throws CompilerError
    {
        loadConstant(PSValue.valueOf(literal));
        return store(var);
    }
    
    
    
    
    
    /* TEMP VARS */
    public final InstructionHandle loadTemp(String name)
    {
        TempInfo temp = tempVars.get(name);
        if(temp == null || !temp.enabled)
            throw new IllegalStateException("Temporal Variable " + name + " does not exists");
        return mainInst.append(new ALOAD(temp.id));
    }
    
    public final InstructionHandle storeTemp(String name)
    {
        TempInfo temp = tempVars.get(name);
        if(temp == null || !temp.enabled)
            throw new IllegalStateException("Temporal Variable " + name + " does not exists");
        return mainInst.append(new ASTORE(temp.id));
    }
    
    public final void createTemp(String name)
    {
        TempInfo temp = tempVars.get(name);
        if(temp != null)
        {
            if(temp.enabled)
                throw new IllegalStateException("Temporal Variable " + name + " already exists");
            temp.enabled = true;
        }
        else
        {
            String varName = STR_TEMP_PREFIX + name;
            LocalVariableGen local = mainMethod.addLocalVariable(varName, TYPE_VALUE, null, null);
            tempVars.put(name, new TempInfo(local.getIndex()));
        }
    }
    
    public final boolean existsTemp(String name) { return tempVars.containsKey(name); }
    
    public final void removeTemp(String name)
    {
        TempInfo temp = tempVars.get(name);
        if(temp == null || !temp.enabled)
            throw new IllegalStateException("Temporal Variable " + name + " does not exists");
        temp.enabled = false;
    }
    
    private int createExpand()
    {
        if(expandId >= 0)
            return expandId;
        
        LocalVariableGen local = mainMethod.addLocalVariable(STR_EXPAND_TEMP, TYPE_VARARGS, null, null);
        return expandId = local.getIndex();
    }
    
    public final InstructionHandle storeExpand()
    {
        return mainInst.append(new ASTORE(createExpand()));
    }
    
    public final InstructionHandle loadExpand(int index)
    {
        mainInst.append(new ALOAD(createExpand()));
        String func = index == 0 ? STR_FUNC_SELF : STR_FUNC_ARG;
        Type[] args = index == 0 ? NO_ARGS : ARGS_INT;
        return mainInst.append(factory.createInvoke(STR_TYPE_VARARGS, func,
                TYPE_VALUE, args, Constants.INVOKEVIRTUAL));
    }
    
    
    
    
    
    /* CONSTANTS */
    public final InstructionHandle dup()
    {
        return mainInst.append(InstructionConstants.DUP);
    }

    public final InstructionHandle pop()
    {
        return mainInst.append(InstructionConstants.POP);
    }
    
    public final InstructionHandle swap()
    {
        return mainInst.append(InstructionConstants.SWAP);
    }
    
    public final InstructionHandle loadNativeString(String string)
    {
        return mainInst.append(new PUSH(constantPool, string));
    }
    
    public final InstructionHandle loadUndefined()
    {
        return mainInst.append(factory.createFieldAccess(STR_TYPE_VALUE,"UNDEFINED",TYPE_VALUE,Constants.GETSTATIC));
    }
    
    public final InstructionHandle loadNull()
    {
        return mainInst.append(factory.createFieldAccess(STR_TYPE_VALUE,"NULL",TYPE_VALUE,Constants.GETSTATIC));
    }
    
    public final InstructionHandle loadEmpty()
    {
        return mainInst.append(factory.createFieldAccess(STR_TYPE_VARARGS,"EMPTY",TYPE_VARARGS,Constants.GETSTATIC));
    }
    
    public final InstructionHandle loadBoolean(boolean b)
    {
        return mainInst.append(factory.createFieldAccess(STR_TYPE_VALUE,
                (b ? "TRUE" : "FALSE"),TYPE_VALUE, Constants.GETSTATIC));
    }
    
    private String createConstant()
    {
        String name = STR_CONSTANT_PREFIX + constants.size();
        FieldGen field = new FieldGen(
                Constants.ACC_PRIVATE | Constants.ACC_STATIC | Constants.ACC_FINAL,
                TYPE_VALUE,
                name,
                constantPool
        );
        mainClass.addField(field.getField());
        return name;
    }
    
    private String createIntConstant(int value)
    {
        String name = createConstant();
        constInst.append(factory.createNew(TYPE_INTEGER));
        constInst.append(InstructionConstants.DUP);
        constInst.append(new PUSH(constantPool, value));
        constInst.append(factory.createInvoke(
                STR_TYPE_INTEGER,
                "<init>",
                Type.VOID,
                ARGS_INT,
                Constants.INVOKESPECIAL));
        constInst.append(factory.createPutStatic(className,name,TYPE_VALUE));
        return name;
    }
    
    private String createLongConstant(long value)
    {
        String name = createConstant();
        constInst.append(factory.createNew(TYPE_LONG));
        constInst.append(InstructionConstants.DUP);
        constInst.append(new PUSH(constantPool,value));
        constInst.append(factory.createInvoke(
                STR_TYPE_LONG,
                "<init>",
                Type.VOID,
                ARGS_LONG,
                Constants.INVOKESPECIAL));
        constInst.append(factory.createPutStatic(className,name,TYPE_VALUE));
        return name;
    }
    
    private String createFloatConstant(float value)
    {
        String name = createConstant();
        constInst.append(factory.createNew(TYPE_FLOAT));
        constInst.append(InstructionConstants.DUP);
        constInst.append(new PUSH(constantPool,value));
        constInst.append(factory.createInvoke(
                STR_TYPE_FLOAT,
                "<init>",
                Type.VOID,
                ARGS_FLOAT,
                Constants.INVOKESPECIAL));
        constInst.append(factory.createPutStatic(className,name,TYPE_VALUE));
        return name;
    }
    
    private String createDoubleConstant(double value)
    {
        String name = createConstant();
        constInst.append(factory.createNew(TYPE_DOUBLE));
        constInst.append(InstructionConstants.DUP);
        constInst.append(new PUSH(constantPool,value));
        constInst.append(factory.createInvoke(
                STR_TYPE_DOUBLE,
                "<init>",
                Type.VOID,
                ARGS_DOUBLE,
                Constants.INVOKESPECIAL));
        constInst.append(factory.createPutStatic(className,name,TYPE_VALUE));
        return name;
    }
    
    private String createStringConstant(String value)
    {
        String name = createConstant();
        constInst.append(factory.createNew(TYPE_STRING));
        constInst.append(InstructionConstants.DUP);
        constInst.append(new PUSH(constantPool,value));
        constInst.append(factory.createInvoke(
                STR_TYPE_STRING,
                "<init>",
                Type.VOID,
                ARGS_STRING,
                Constants.INVOKESPECIAL));
        constInst.append(factory.createPutStatic(className,name,TYPE_VALUE));
        return name;
    }
    
    private InstructionHandle loadConstant(PSValue value)
    {
        switch(value.getPSType())
        {
            case UNDEFINED:
                return loadUndefined();
            case NULL:
                return loadNull();
            case BOOLEAN:
                return loadBoolean(value.toJavaBoolean());
            case NUMBER:
                String name = constants.get(value);
                if(name == null)
                {
                    PSNumber n = (PSNumber) value;
                    if(n.isInteger())
                        constants.put(value,name = createIntConstant(value.toJavaInt()));
                    else if(n.isLong())
                        constants.put(value,name = createLongConstant(value.toJavaLong()));
                    else if(n.isFloat())
                        constants.put(value,name = createFloatConstant(value.toJavaFloat()));
                    else if(n.isDouble())
                        constants.put(value,name = createDoubleConstant(value.toJavaDouble()));
                    else throw new IllegalStateException();
                }
                return __GetConstant(name);
            case STRING:
                name = constants.get(value);
                if(name == null)
                    constants.put(value,name = createStringConstant(value.toString()));
                return __GetConstant(name);
            default:
                throw new IllegalArgumentException("Bad constant value");
        }
    }
    private InstructionHandle __GetConstant(String constName)
    {
        return mainInst.append(factory.createGetStatic(className,constName,TYPE_VALUE));
    }
    
    public final InstructionHandle loadLiteral(Literal literal) throws CompilerError
    {
        compiler.getStack().push();
        return loadConstant(literal.getValue());
    }
    
    
    
    
    
    /* ARRAY LITERAL */
    public final InstructionHandle emptyArrayLiteral() throws CompilerError
    {
        compiler.getStack().push();
        mainInst.append(factory.createNew(TYPE_ARRAY));
        mainInst.append(InstructionConstants.DUP);
        return mainInst.append(factory.createInvoke(
                STR_TYPE_ARRAY,
                "<init>",
                Type.VOID,
                NO_ARGS,
                Constants.INVOKESPECIAL));
    }
    
    public final InstructionHandle initArrayLiteral(MutableLiteral literal) throws CompilerError
    {
        compiler.getStack().push();
        mainInst.append(factory.createNew(TYPE_ARRAY));
        mainInst.append(InstructionConstants.DUP);
        mainInst.append(new PUSH(constantPool, literal.getItemCount()));
        return mainInst.append(factory.createNewArray(TYPE_VALUE, (short) 1));
    }
    
    public final InstructionHandle insertArrayLiteralItem(int index, VoidOperation element) throws CompilerError
    {
        compiler.getStack().pop();
        mainInst.append(InstructionConstants.DUP);
        mainInst.append(new PUSH(constantPool, index));
        element.doOperation();
        return mainInst.append(new AASTORE());
    }
    
    public final InstructionHandle endArrayLiteral()
    {
        return mainInst.append(factory.createInvoke(
                STR_TYPE_ARRAY,
                "<init>",
                Type.VOID,
                ARGS_A_VALUE,
                Constants.INVOKESPECIAL));
    }
    
    /* TUPLE LITERAL */
    public final InstructionHandle emptyTupleLiteral() throws CompilerError
    {
        compiler.getStack().push();
        return mainInst.append(factory.createFieldAccess(STR_TYPE_VALUE, "EMPTY_TUPLE", TYPE_VALUE, Constants.GETSTATIC));
    }
    
    public final InstructionHandle initTupleLiteral(MutableLiteral literal) throws CompilerError
    {
        compiler.getStack().push();
        mainInst.append(factory.createNew(TYPE_TUPLE));
        mainInst.append(InstructionConstants.DUP);
        mainInst.append(new PUSH(constantPool, literal.getItemCount()));
        mainInst.append(factory.createNewArray(TYPE_VALUE, (short) 1));
        return mainInst.append(InstructionConstants.DUP);
    }
    
    public final InstructionHandle insertTupleLiteralItem(int index, VoidOperation element) throws CompilerError
    {
        compiler.getStack().pop();
        mainInst.append(InstructionConstants.DUP);
        mainInst.append(new PUSH(constantPool, index));
        element.doOperation();
        return mainInst.append(new AASTORE());
    }
    
    public final InstructionHandle endTupleLiteral()
    {
        return mainInst.append(factory.createInvoke(
                STR_TYPE_TUPLE,
                "<init>",
                Type.VOID,
                ARGS_A_VALUE,
                Constants.INVOKESPECIAL));
    }
    
    /* MAP LITERAL */
    public final InstructionHandle emptyMapLiteral() throws CompilerError
    {
        compiler.getStack().push();
        mainInst.append(factory.createNew(TYPE_MAP));
        mainInst.append(InstructionConstants.DUP);
        return mainInst.append(factory.createInvoke(
                STR_TYPE_MAP,
                "<init>",
                Type.VOID,
                NO_ARGS,
                Constants.INVOKESPECIAL));
    }
    
    public final InstructionHandle initMapLiteral(MutableLiteral literal) throws CompilerError
    {
        compiler.getStack().push();
        mainInst.append(factory.createNew(TYPE_MAP));
        mainInst.append(InstructionConstants.DUP);
        mainInst.append(factory.createNew(TYPE_PROTOMAP));
        mainInst.append(InstructionConstants.DUP);
        mainInst.append(new PUSH(constantPool, literal.getItemCount()));
        mainInst.append(factory.createInvoke(
                STR_TYPE_PROTOMAP,
                "<init>",
                Type.VOID,
                ARGS_INT,
                Constants.INVOKESPECIAL));
        return mainInst.append(InstructionConstants.DUP);
    }
    
    public final InstructionHandle insertMapLiteralItem(int index, VoidOperation element) throws CompilerError
    {
        compiler.getStack().pop();
        element.doOperation();
        mainInst.append(factory.createInvoke(STR_TYPE_HASHMAP, "put",
                TYPE_VALUE, ARGS_VALUE_1, Constants.INVOKEVIRTUAL));
        return mainInst.append(InstructionConstants.POP);
    }
    
    public final InstructionHandle endMapLiteral()
    {
        return mainInst.append(factory.createInvoke(
                STR_TYPE_MAP,
                "<init>",
                Type.VOID,
                ARGS_JAVAMAP,
                Constants.INVOKESPECIAL));
    }
    
    /* OBJECT LITERAL */
    public final InstructionHandle emptyObjectLiteral() throws CompilerError
    {
        compiler.getStack().push();
        mainInst.append(factory.createNew(TYPE_OBJECT));
        mainInst.append(InstructionConstants.DUP);
        return mainInst.append(factory.createInvoke(
                STR_TYPE_OBJECT,
                "<init>",
                Type.VOID,
                NO_ARGS,
                Constants.INVOKESPECIAL));
    }
    
    public final InstructionHandle initObjectLiteral(MutableLiteral literal) throws CompilerError
    {
        compiler.getStack().push();
        mainInst.append(factory.createNew(TYPE_OBJECT));
        mainInst.append(InstructionConstants.DUP);
        mainInst.append(factory.createNew(TYPE_PROTOOBJECT));
        mainInst.append(InstructionConstants.DUP);
        mainInst.append(new PUSH(constantPool, literal.getItemCount()));
        mainInst.append(factory.createInvoke(
                STR_TYPE_PROTOOBJECT,
                "<init>",
                Type.VOID,
                ARGS_INT,
                Constants.INVOKESPECIAL));
        return mainInst.append(InstructionConstants.DUP);
    }
    
    public final InstructionHandle insertObjectLiteralItem(int index, VoidOperation element) throws CompilerError
    {
        compiler.getStack().pop();
        element.doOperation();
        mainInst.append(factory.createInvoke(STR_TYPE_HASHMAP, "put",
                TYPE_VALUE, ARGS_VALUE_1, Constants.INVOKEVIRTUAL));
        return mainInst.append(InstructionConstants.POP);
    }
    
    public final InstructionHandle endObjectLiteral()
    {
        return mainInst.append(factory.createInvoke(
                STR_TYPE_OBJECT,
                "<init>",
                Type.VOID,
                ARGS_HASHMAP,
                Constants.INVOKESPECIAL));
    }
    
    
    
    
    
    
    /* OPERATORS */
    public final InstructionHandle callOperator(OperatorSymbol symbol) throws CompilerError
    {
        if(symbol.isUnary())
        {
            compiler.getStack().pop();
            compiler.getStack().push();
            if(!symbol.hasAssociatedFunction())
            {
                if(symbol == OperatorSymbol.TYPEOF)
                    return callTypeofOperator();
                throw new IllegalStateException();
            }
            return mainInst.append(factory.createInvoke(STR_TYPE_VALUE, symbol.getAssociatedFunction(),
                TYPE_VALUE, NO_ARGS, Constants.INVOKEVIRTUAL));
        }
        else if(symbol.isBinary())
        {
            compiler.getStack().pop(2);
            compiler.getStack().push();
            if(!symbol.hasAssociatedFunction())
            {
                if(symbol == OperatorSymbol.INSTANCEOF)
                    return callInstanceofOperator();
                if(symbol == OperatorSymbol.EQUALS_REFERENCE)
                    return callEqualsReferenceOperator();
                if(symbol == OperatorSymbol.NOT_EQUALS_REFERENCE)
                    return callNotEqualsReferenceOperator();
                throw new IllegalStateException();
            }
            return mainInst.append(factory.createInvoke(STR_TYPE_VALUE, symbol.getAssociatedFunction(),
                TYPE_VALUE, ARGS_VALUE_1, Constants.INVOKEVIRTUAL));
        }
        else throw new IllegalStateException();
    }
    
    private InstructionHandle callTypeofOperator()
    {
        return mainInst.append(factory.createInvoke(STR_TYPE_UTILS, "operatorTypeof",
                TYPE_VALUE, ARGS_VALUE_1, Constants.INVOKESTATIC));
    }
    
    private InstructionHandle callInstanceofOperator()
    {
        return mainInst.append(factory.createInvoke(STR_TYPE_UTILS, "operatorInstanceof",
                TYPE_VALUE, ARGS_VALUE_2, Constants.INVOKESTATIC));
    }
    
    private InstructionHandle callEqualsReferenceOperator()
    {
        return mainInst.append(factory.createInvoke(STR_TYPE_UTILS, "operatorEqualsReference",
                TYPE_VALUE, ARGS_VALUE_2, Constants.INVOKESTATIC));
    }
    
    private InstructionHandle callNotEqualsReferenceOperator()
    {
        return mainInst.append(factory.createInvoke(STR_TYPE_UTILS, "operatorNotEqualsReference",
                TYPE_VALUE, ARGS_VALUE_2, Constants.INVOKESTATIC));
    }
    
    public final InstructionHandle callAccessOperator() throws CompilerError
    {
        compiler.getStack().pop(2);
        compiler.getStack().push();
        return mainInst.append(factory.createInvoke(STR_TYPE_VALUE, "get",
                TYPE_VALUE, ARGS_VALUE_1, Constants.INVOKEVIRTUAL));
    }
    
    public final InstructionHandle callPropertyAccessOperator(String property) throws CompilerError
    {
        compiler.getStack().pop();
        compiler.getStack().push();
        mainInst.append(new PUSH(constantPool, property));
        return mainInst.append(factory.createInvoke(STR_TYPE_VALUE, "getProperty",
                TYPE_VALUE, ARGS_STRING, Constants.INVOKEVIRTUAL));
    }
    
    
    public final InstructionHandle doCall(Operator operator, boolean isInvoke, boolean multiresult) throws CompilerError
    {
        int args = operator.getOperandCount() - (isInvoke ? 2 : 1);
        if(args > 0)
            compiler.getStack().pop(args);
        compiler.getStack().pop();
        compiler.getStack().push();
        return doCall(args, isInvoke, multiresult);
    }
    public final InstructionHandle doTailedCall(Operator operator, boolean isInvoke, boolean multiresult) throws CompilerError
    {
        int args = -(operator.getOperandCount() - (isInvoke ? 2 : 1));
        compiler.getStack().pop(2);
        compiler.getStack().push();
        return doCall(args, isInvoke, multiresult);
    }
    
    private InstructionHandle doCall(int args, boolean isInvoke, boolean multiresult)
    {
        String method = isInvoke ? "invoke" : "call";
        Type[] targs = FUNC_ARGS[isInvoke ? 1 : 0][args < 0 || args > 5 ? 5 : args];
        
        if(args > 4)
            wrapArgsToArray(args);
        InstructionHandle ih = mainInst.append(factory.createInvoke(STR_TYPE_VALUE, method,
                TYPE_VARARGS, targs, Constants.INVOKEVIRTUAL));
        
        if(!multiresult)
            ih = mainInst.append(factory.createInvoke(STR_TYPE_VARARGS, STR_FUNC_SELF,
                    TYPE_VALUE, NO_ARGS, Constants.INVOKEVIRTUAL));
        return ih;
    }
    
    
    public final InstructionHandle callNewOperator(Operator operator, boolean isTailed) throws CompilerError
    {
        int args;
        Type[] targs;
        if(isTailed)
        {
            args = -1;
            targs = FUNC_ARGS[0][5];
            compiler.getStack().pop(2);
        }
        else
        {
            args = operator.getOperandCount() - 1;
            targs = FUNC_ARGS[0][args > 5 ? 5 : args];
            if(args > 0)
                compiler.getStack().pop(args);
            compiler.getStack().pop();
        }
        compiler.getStack().push();
        
        if(isTailed)
            wrapArgsToArray(args);
        return mainInst.append(factory.createInvoke(STR_TYPE_VALUE, "createNewInstance",
                TYPE_VARARGS, targs, Constants.INVOKEVIRTUAL));
    }
    
    
    
    
    
    
    
    
    /* BRANCHES */
    public final InstructionHandle computeIf() throws CompilerError
    {
        if(compiler.getStack().getTempUsed() <= 0)
            throw new IllegalStateException();
        compiler.getStack().pop();
        asJavaBoolean();
        return doIf(false);
    }
    
    public final InstructionHandle computeInverseIf() throws CompilerError
    {
        if(compiler.getStack().getTempUsed() <= 0)
            throw new IllegalStateException();
        compiler.getStack().pop();
        asJavaBoolean();
        return doIf(true);
    }
    
    private InstructionHandle doIf(boolean inverse)
    {
        if(!inverse)
        {
            IFEQ ifeq = new IFEQ(null);
            return mainInst.append(ifeq);
        }
        IFNE ifne = new IFNE(null);
        return mainInst.append(ifne);
    }
    
    private InstructionHandle doIfLsThan()
    {
        return mainInst.append(new IF_ICMPLE(null));
    }
    
    public final InstructionHandle emptyJump()
    {
        return createEmptyGoto();
    }
    public final void modifyJump(InstructionHandle instrId)
    {
        markBranch(instrId);
    }
    
    final InstructionHandle jump(InstructionHandle instrTo)
    {
        return createGoto(instrTo);
    }
    
    private InstructionHandle createEmptyGoto()
    {
        GOTO g = new GOTO(null);
        return mainInst.append(g);
    }
    
    private InstructionHandle createGoto(InstructionHandle ih)
    {
        return mainInst.append(new GOTO(ih));
    }
    
    private void markBranch(InstructionHandle igoto)
    {
        Instruction i = igoto.getInstruction();
        if(!(i instanceof BranchInstruction))
            throw new IllegalStateException();
        branchInfo.add(new BranchInfo((BranchInstruction)i,mainInst.getEnd()));
    }
    
    private void resolveAllBranches()
    {
        for(BranchInfo b : branchInfo)
        {
            InstructionHandle ih = b.target.getNext();
            if(ih == null)
                throw new IllegalStateException();
            b.instr.setTarget(ih);
        }
    }
    
    public final InstructionHandle asJavaBoolean()
    {
        return mainInst.append(factory.createInvoke(STR_TYPE_VALUE, "toJavaBoolean",
                Type.BOOLEAN, NO_ARGS, Constants.INVOKEVIRTUAL));
    }
    
    
    
    private InstructionHandle doReturn(boolean returnAnything)
    {
        if(!returnAnything)
            loadEmpty();
        return mainInst.append(InstructionConstants.ARETURN);
    }
    public final InstructionHandle Return() throws CompilerError
    {
        if(compiler.getStack().getTempUsed() == 0)
            return doReturn(false);
        compiler.getStack().pop();
        return doReturn(true);
    }
    
    
    

    
    BytecodeGenerator createInstance(int argsLen, int defaultValues, boolean packExtraArgs)
    {
        String name = className + '$' + (inheritedFunctionsId++);
        return new BytecodeGenerator(classLoader, name, argsLen, defaultValues, packExtraArgs);
    }

    
    public final Class<? extends PSFunction> build(CompilerBlockType type, ClassRepository repository)
    {
        if(bytecode == null)
            bytecode = completeClass(type);
        if(repository != null)
            repository.registerClass(className, bytecode);
        return classLoader.createPSClass(className, bytecode);
    }
    
    public final void storeBytecode(OutputStream output) throws IOException
    {
        output.write(bytecode);
    }
    
    
    
    
    
    
    
    public final InstructionHandle getValueFromVarargs(int index)
    {
        if(index == 0)
            return mainInst.append(factory.createInvoke(STR_TYPE_VARARGS, STR_FUNC_SELF,
                    TYPE_VALUE, NO_ARGS, Constants.INVOKEVIRTUAL));
        mainInst.append(new PUSH(constantPool, index));
        return mainInst.append(factory.createInvoke(STR_TYPE_VARARGS, STR_FUNC_ARG,
                    TYPE_VALUE, ARGS_INT, Constants.INVOKEVIRTUAL));
    }
    
    private InstructionHandle createTailVarargs()
    {
        return mainInst.append(factory.createInvoke(STR_TYPE_VARARGS, "varargsOf",
                TYPE_VARARGS, new Type[]{ TYPE_VARARGS, TYPE_VARARGS }, Constants.INVOKESTATIC));
    }
    
    public final InstructionHandle wrapVarargsTail(int argsLen) throws CompilerError
    {
        InstructionHandle ih = getLastHandle();
        compiler.getStack().pop(argsLen);
        compiler.getStack().push();
        for(;argsLen>1;argsLen--)
            ih = createTailVarargs();
        return ih;
    }
    
    public final InstructionHandle wrapArgsToArray(int argsLen)
    {
        switch(argsLen)
        {
            case 0:
                return mainInst.append(factory.createGetStatic(STR_TYPE_VARARGS,"EMPTY",TYPE_VARARGS));
            case 1:
                return getLastHandle();
            case 2:
                return mainInst.append(factory.createInvoke(STR_TYPE_VARARGS,"varargsOf",
                        TYPE_VARARGS,
                        new Type[] { TYPE_VALUE, TYPE_VARARGS },
                        Constants.INVOKESTATIC));
            case 3:
                return mainInst.append(factory.createInvoke(STR_TYPE_VARARGS,"varargsOf",
                        TYPE_VARARGS,
                        new Type[] { TYPE_VALUE, TYPE_VALUE, TYPE_VARARGS },
                        Constants.INVOKESTATIC));
            case 4:
                return mainInst.append(factory.createInvoke(STR_TYPE_VARARGS,"varargsOf",
                        TYPE_VARARGS,
                        new Type[] { TYPE_VALUE, TYPE_VALUE, TYPE_VALUE, TYPE_VARARGS },
                        Constants.INVOKESTATIC));
            case 5:
                return mainInst.append(factory.createInvoke(STR_TYPE_VARARGS,"varargsOf",
                        TYPE_VARARGS,
                        new Type[] { TYPE_VALUE, TYPE_VALUE, TYPE_VALUE, TYPE_VALUE, TYPE_VARARGS },
                        Constants.INVOKESTATIC));
            default: {
                createTemp("wrap_varargs");
                mainInst.append(new PUSH(constantPool,argsLen));
                mainInst.append(factory.createNewArray(TYPE_VALUE, (short) 1));
                storeTemp("wrap_varargs");
                for(int i=0;i<argsLen-1;i++)
                {
                    loadTemp("wrap_varargs");
                    mainInst.append(InstructionConstants.SWAP);
                    mainInst.append(new PUSH(constantPool, i));
                    mainInst.append(InstructionConstants.SWAP);
                    mainInst.append(InstructionConstants.AASTORE);
                }
                loadTemp("wrap_varargs");
                mainInst.append(InstructionConstants.SWAP);
                removeTemp("wrap_varargs");
                return mainInst.append(factory.createInvoke(STR_TYPE_VARARGS,"varargsOf",
                        TYPE_VARARGS,new Type[]{ TYPE_ARRAY_VALUE, TYPE_VARARGS },Constants.INVOKESTATIC));
            }
        }
    }
    
    
    
    
    
    
    public final InstructionHandle getLastHandle() { return mainInst.getEnd(); }
    public final InstructionHandle getFirstHandle() { return mainInst.getStart(); }
    
    
    
    
    
    
    
    
    
    public enum FunctionId
    {
        FUNC0(0,   PSFunction.PSZeroArgsFunction.class,         ARGS_VALUE_1),
        FUNC1(1,   PSFunction.PSOneArgFunction.class,           ARGS_VALUE_2),
        FUNC2(2,   PSFunction.PSTwoArgsFunction.class,          ARGS_VALUE_3),
        FUNC3(3,   PSFunction.PSThreeArgsFunction.class,        ARGS_VALUE_4),
        FUNC4(4,   PSFunction.PSFourArgsFunction.class,         ARGS_VALUE_5),
        FUNCV(-1,  PSFunction.PSVarargsFunction.class,          ARGS_VALUE_VARARGS),
        FUNC1D(1,  PSFunction.PSDefaultOneArgFunction.class,    ARGS_VALUE_2),
        FUNC2D(2,  PSFunction.PSDefaultTwoArgsFunction.class,   ARGS_VALUE_3),
        FUNC3D(3,  PSFunction.PSDefaultThreeArgsFunction.class, ARGS_VALUE_4),
        FUNC4D(4,  PSFunction.PSDefaultFourArgsFunction.class,  ARGS_VALUE_5),
        FUNCVD(-1, PSFunction.PSDefaultVarargsFunction.class,   ARGS_VALUE_VARARGS),
        SCRIPT(0,  PSScript.class,                              ARGS_VALUE_1);
        
        private static final FunctionId[] VALUES = values();
        private final String name;
        private final int numArgs;
        private final Type[] typeArgs;
        private final String[] nameArgs;
        
        private FunctionId(int numArgs, Class<? extends PSFunction> clazz, Type[] typeArgs)
        {
            name = clazz.getName();
            this.numArgs = numArgs;
            this.typeArgs = typeArgs;
            this.nameArgs = new String[typeArgs.length];
            for(int i=0;i<this.nameArgs.length-1;i++)
                this.nameArgs[i+1] = STR_VAR_PREFIX + i;
            this.nameArgs[0] = STR_SELF;
            
        }
        
        private String getName() { return name; }
        private int getNumArgs() { return numArgs; }
        private boolean isVarargs() { return numArgs < 0; }
        
        private MethodGen createMethod(String className, InstructionList instructionsList, ConstantPoolGen constantPool)
        {
            return new MethodGen(
                    Constants.ACC_PUBLIC | Constants.ACC_FINAL,
                    TYPE_VARARGS,
                    typeArgs,
                    nameArgs,
                    STR_FUNC_NAME,
                    className,
                    instructionsList,
                    constantPool
            );
        }
        
        private static FunctionId select(int argsLen, int defaultValues, boolean packExtraArgs)
        {
            if(argsLen < 1)
                return FUNC0;
            return VALUES[(argsLen > 5 || packExtraArgs ? 5 : argsLen) + defaultValues > 0 ? 5 : 0];
        }
    }
    
    private static final class BranchInfo
    {
        private final BranchInstruction instr;
        private final InstructionHandle target;
        
        private BranchInfo(BranchInstruction i, InstructionHandle t)
        {
            instr = i;
            target = t;
        }
    }
    
    private static final class TempInfo
    {
        private final int id;
        private boolean enabled;
        
        private TempInfo(int id)
        {
            this.id = id;
            this.enabled = true;
        }
    }
    
    @FunctionalInterface
    public static interface VoidOperation { void doOperation() throws CompilerError; }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import nt.ps.PSClassLoader;
import nt.ps.PSGlobals;
import nt.ps.PSScript;
import nt.ps.compiler.CompilerBlock.CompilerBlockType;
import nt.ps.compiler.exception.CompilerError;
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
            STR_TYPE_LINKEDLIST = LinkedList.class.getName();
    
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
            TYPE_LINKEDLIST = new ObjectType(STR_TYPE_LINKEDLIST);
    
    public static final ArrayType
            TYPE_ARRAY_VALUE = new ArrayType(TYPE_VALUE, 1);
    
    public static final String
            STR_FUNC_CALL = "call",
            STR_FUNC_SELF = "self",
            STR_FUNC_NAME = "innerCall",
            STR_FUNC_SET_GLOBALS = "setGlobals",
            STR_FUNC_ARG = "arg",
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
            ARGS_VALUE_VARARGS = { TYPE_VALUE, TYPE_VARARGS },
            ARGS_A_VALUE = { new ArrayType(TYPE_VALUE,1) },
            ARGS_GLOBALS = { TYPE_GLOBALS },
            ARGS_INT = { Type.INT },
            ARGS_LONG = { Type.LONG },
            ARGS_FLOAT = { Type.FLOAT },
            ARGS_DOUBLE = { Type.DOUBLE },
            ARGS_STRING = { Type.STRING },
            ARGS_VARARGS_INT = { TYPE_VARARGS, Type.INT },
            ARGS_VALUE_INT = { TYPE_VALUE, Type.INT };
    
    public static final String
            STR_VAR_PREFIX = "var",
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
        localVars.put(SELF_REFERENCE, SELF_ID);
        
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
        InstructionList instr = new InstructionList();
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
        instr.dispose();
    }
    
    public final void initiateUpPointersArray(int count)
    {
        InstructionList il = new InstructionList();
        il.append(InstructionConstants.THIS);
        il.append(new INVOKESPECIAL(constantPool.addMethodref(mainClass.getSuperclassName(),"<init>","()V")));
        il.append(InstructionConstants.THIS);
        il.append(new PUSH(constantPool,count));
        il.append(new ANEWARRAY(constantPool.addClass(TYPE_VALUE)));
        il.append(factory.createPutField(className,"__upptrs",TYPE_ARRAY_VALUE));
        il.append(InstructionConstants.RETURN);
        MethodGen mg = new MethodGen(Constants.ACC_PUBLIC, Type.VOID, Type.NO_ARGS,
                new String[]{},"<init>",className,il,constantPool);
        mg.setMaxStack();
        mainClass.addMethod(mg.getMethod());
    }
    
    
    
    
    /* VARIABLES */
    private InstructionHandle loadLocal(int reference)
    {
        Integer id = localVars.get(reference);
        if(id == null)
            throw new IllegalStateException("Variable with reference " + reference + " does not exists");
        return mainInst.append(new ALOAD(id));
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
    
    public final InstructionHandle loadConstant(PSValue value)
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
    
    
    
    /* BRANCHES */
    private void markBranch(InstructionHandle igoto, int line)
    {
        Instruction i = igoto.getInstruction();
        if(!(i instanceof BranchInstruction))
            throw new IllegalStateException();
        branchInfo.add(new BranchInfo((BranchInstruction)i,mainInst.getEnd(),line));
    }
    
    private void resolveAllBranches()
    {
        for(BranchInfo b : branchInfo)
        {
            InstructionHandle ih = b.target.getNext();
            if(ih == null)
                throw new IllegalStateException("In line: " + b.line);
            b.instr.setTarget(ih);
        }
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
    
    
    

    
    BytecodeGenerator createInstance(String name, int argsLen, int defaultValues, boolean packExtraArgs)
    {
        return new BytecodeGenerator(classLoader, name, argsLen, defaultValues, packExtraArgs);
    }

    
    public final Class<? extends PSFunction> build(CompilerBlockType type)
    {
        byte[] bytecode = completeClass(type);
        return classLoader.createPSClass(className, bytecode);
    }
    
    
    
    
    
    
    
    
    
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
        private final int line;
        
        private BranchInfo(BranchInstruction i, InstructionHandle t, int line)
        {
            instr = i;
            target = t;
            this.line = line;
        }
    }
}

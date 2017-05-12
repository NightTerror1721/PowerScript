/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.parser.Block.Scope;
import nt.ps.compiler.parser.Code.CodeType;

/**
 *
 * @author Asus
 */
public final class Tuple
{
    private final Code[] code;
    
    public Tuple(Code... code)
    {
        this.code = code;
    }
    public Tuple(List<Code> c)
    {
        this(c.toArray(new Code[c.size()]));
    }
    public Tuple(Collection<? extends Code> c)
    {
        this(c.toArray(new Code[c.size()]));
    }
    
    public final void set(int index, Code code)
    {
        if(code == null)
            throw new NullPointerException();
        this.code[index] = code;
    }
    
    public final <C extends Code> C get(int index) { return (C) code[index]; }
    
    public final int length() { return code.length; }
    public final boolean isEmpty() { return code.length == 0; }
    
    public final Tuple subTuple(int offset, int length)
    {
        Code[] array = new Code[length];
        System.arraycopy(code,offset,array,0,length);
        return new Tuple(array);
    }
    public final Tuple subTuple(int offset) { return subTuple(offset,code.length-offset); }
    public final Tuple copy() { return subTuple(0,code.length); }
    
    public final Tuple concat(Tuple ct) { return concat(ct.code); }
    public final Tuple concat(List<Code> code) { return concat(code.toArray(new Code[code.size()])); }
    public final Tuple concat(Code... code)
    {
        Code[] array = new Code[this.code.length + code.length];
        System.arraycopy(this.code,0,array,0,this.code.length);
        System.arraycopy(code,0,array,this.code.length,code.length);
        return new Tuple(array);
    }
    
    public final Tuple wrap(Tuple start, Tuple end) { return wrap(start.code, end.code); }
    public final Tuple wrap(Code start, Code end) { return wrap(new Code[]{start}, new Code[]{end}); }
    public final Tuple wrap(Code[] start, Code[] end)
    {
        Code[] array = new Code[start.length + code.length + end.length];
        System.arraycopy(start,0,array,0,start.length);
        System.arraycopy(code,0,array,start.length,code.length);
        System.arraycopy(end,0,array,start.length+code.length,end.length);
        return new Tuple(array);
    }
    
    public final Tuple extract(Code start, Code end)
    {
        boolean init = false;
        int offset = -1, len = -1, idx = -1;
        for(Code cp : code)
        {
            idx++;
            if(!init)
            {
                if(!cp.equals(start))
                    continue;
                init = true;
                offset = idx;
                continue;
            }
            if(cp.equals(end))
                break;
            len++;
        }
        return subTuple(offset,len);
    }
    
    public final int count(Code codePart)
    {
        int count = 0;
        for(Code cp : code)
            if(cp.equals(codePart))
                count++;
        return count;
    }
    
    public final boolean has(Code codePart)
    {
        for(Code cp : code)
            if(cp.equals(codePart))
                return true;
        return false;
    }
    
    public final int count(CodeType type)
    {
        int count = 0;
        for(Code cp : code)
            if(cp.getCodeType() == type)
                count++;
        return count;
    }
    
    public final int findJustOneByType(CodeType type) throws CompilerError
    {
        int count = 0;
        for(Code cp : code)
        {
            if(cp.getCodeType() == type)
                return count;
            count++;
        }
        return -1;
    }
    
    public final Tuple[] splitByToken(Code sep)
    {
        return splitByToken(sep,-1);
    }
    public final Tuple[] splitByToken(Code sep, int limit)
    {
        LinkedList<Tuple> part = new LinkedList<>();
        LinkedList<Code> tokens = new LinkedList<>();

        for(Code cp : code)
        {
            if(cp.equals(sep) && limit != 0)
            {
                part.add(new Tuple(tokens));
                tokens.clear();
                limit--;
                continue;
            }
            tokens.add(cp);
        }
        if(!part.isEmpty())
            part.add(new Tuple(tokens));
        tokens.clear();
        return part.toArray(new Tuple[part.size()]);
    }
    
    public final boolean isTuple() { return true; }
    
    @Override
    public final String toString() { return Arrays.toString(code); }
    
    private ParsedCode packNewOperator(Counter it) throws CompilerError
    {
        Code ident = code[it.value];
        if(!ident.isValidCodeObject())
            throw new CompilerError("Expected a valid identifier in new operator");
        it.increase();
        Code cpars = code[it.value];
        if(!cpars.is(CodeType.BLOCK))
            throw new CompilerError("Expected a valid list of arguments in new operator");
        Block pars = (Block) cpars;
        if(!pars.isArgumentsList())
            throw new CompilerError("Expected a valid list of arguments in new operator");
        it.increase();
        return Operator.newOperator(pars, pars);
    }
    
    private ParsedCode packFunctionOperator(Counter it) throws CompilerError
    {
        int start = it.value;
        Block<?> pars = null;
        for(;!it.end();it.increase())
        {
            Code c = code[it.value];
            if(!c.is(CodeType.BLOCK))
                continue;
            pars = (Block) c;
            if(!pars.isArgumentsList())
                continue;
            break;
        }
        if(it.end() || pars == null)
            throw new CompilerError("Expected arguments list in function definition");
        ParsedCode identifier = subTuple(start, it.value - 1).pack();
        
        it.increase();
        if(it.end())
            throw new CompilerError("Expected a function implementation after function definition");
        Code cscope = code[it.value];
        if(!cscope.is(CodeType.BLOCK) || !((Block)cscope).isScope())
            throw new CompilerError("Expected a function implementation after function definition");
        it.increase();
        Scope scope = (Scope) cscope;
        
        FunctionLiteral function;
        if(start == it.value)
            function = FunctionLiteral.closure(pars, scope);
        else function = FunctionLiteral.function(identifier, pars, scope);
        
        return function;
    }
    
    private ParsedCode packPreUnary(Counter it) throws CompilerError
    {
        Code part = code[it.value];
        it.increase();
        if(part.is(CodeType.OPERATOR_SYMBOL))
        {
            if(it.end())
                throw CompilerError.unexpectedEndOfInstruction();
            OperatorSymbol prefix = (OperatorSymbol) part;
            if(!prefix.isUnary())
            {
                if(prefix.isNew())
                    return packNewOperator(it);
                if(prefix.isFunction())
                    return packFunctionOperator(it);
                throw new CompilerError("Operator " + prefix + " cannot be a non unary prefix operator");
            }
            part = packPreUnary(it);
            if(!part.isValidCodeObject())
                throw CompilerError.unexpectedCode(part);
            return new Operator(prefix, (ParsedCode) part);
        }
        if(!part.isParsedCode())
            throw CompilerError.unexpectedCode(part);
        return (ParsedCode) part;
    }
    
    private ParsedCode packPostUnary(Counter it, ParsedCode pre) throws CompilerError
    {
        if(it.end())
            return pre;
        Code part = code[it.value];
        
        if(part.is(CodeType.OPERATOR_SYMBOL))
        {
            OperatorSymbol sufix = (OperatorSymbol) part;
            if(!sufix.isUnary())
                return pre;
            it.increase();
            if(!sufix.canBeBothUnaryOrder())
                throw new CompilerError("Operator " + sufix + " cannot be an unary sufix operator");
            if(!pre.isValidCodeObject())
                throw CompilerError.unexpectedCode(pre);
            return packPostUnary(it, new Operator(sufix, pre));
        }
        return pre;
    }
    
    private ParsedCode packPart(Counter it) throws CompilerError
    {
        if(it.end())
            throw CompilerError.unexpectedEndOfInstruction();
        return packPostUnary(it,packPreUnary(it));
    }
    
    private OperatorSymbol findNextOperatorSymbol(int index)
    {
        for(int i=index;i<code.length;i++)
            if(code[i].is(CodeType.OPERATOR_SYMBOL))
                return (OperatorSymbol) code[i];
        return null;
    }
    
    private ParsedCode getSuperOperatorScope(Counter it, OperatorSymbol opBase) throws CompilerError
    {
        int start = it.value;
        for(;!it.end();it.increase())
        {
            if(!code[it.value].is(CodeType.OPERATOR_SYMBOL))
                continue;
            OperatorSymbol op = (OperatorSymbol) code[it.value];
            if(opBase.comparePriority(op) > 0)
            {
                it.decrease();
                return subTuple(start,start - it.value).pack();
            }
        }
        return subTuple(start).pack();
    }
    
    private ParsedCode packOperation(Counter it, ParsedCode operand1) throws CompilerError
    {
        if(!code[it.value].is(CodeType.OPERATOR_SYMBOL))
            throw new CompilerError("Expected a valid operator between operands. \"" + code[it.value] + "\"");
        OperatorSymbol operator = (OperatorSymbol) code[it.value];
        it.increase();
        Operator operation;
        
        if(operator.isInvoke())
        {
            Code identifier = code[it.value];
            if(!identifier.is(CodeType.IDENTIFIER))
                throw new CompilerError("Expected a valid identifier in invoke operator: " + identifier);
            it.increase();
            Code cpars = code[it.value];
            if(!cpars.is(CodeType.BLOCK))
                throw new CompilerError("Expected a valid list of arguments in call operator");
            Block pars = (Block) cpars;
            if(!pars.isArgumentsList())
                throw new CompilerError("Expected a valid list of arguments in call operator");
            it.increase();
            operation = Operator.invokeOperator((Identifier) identifier, operand1, pars);
        }
        else if(operator.isCall())
        {
            Code cpars = code[it.value];
            if(!cpars.is(CodeType.BLOCK))
                throw new CompilerError("Expected a valid list of arguments in call operator");
            Block pars = (Block) cpars;
            if(!pars.isArgumentsList())
                throw new CompilerError("Expected a valid list of arguments in call operator");
            it.increase();
            operation = Operator.callOperator(operand1, pars);
        }
        else if(operator.isTernary())
        {
            int start = it.value;
            int terOp = 0;
            for(;!it.end();it.increase())
            {
                Code c = code[it.value];
                if(c == OperatorSymbol.TERNARY_CONDITION)
                    terOp++;
                else if(c == Separator.TWO_POINTS)
                {
                    if(terOp == 0)
                        break;
                    terOp--;
                }
            }
            if(it.end())
                throw new CompilerError("Expected a : in ternary operator");
            ParsedCode response1 = subTuple(start,it.value - 1).pack();
            it.increase();
            ParsedCode response2 = subTuple(it.value).pack();
            it.value = it.limit;
            return new Operator(OperatorSymbol.TERNARY_CONDITION, operand1, response1, response2);
        }
        else if(operator.isBinary())
        {
            OperatorSymbol nextOperator = findNextOperatorSymbol(it.value);
            if(nextOperator != null && operator.comparePriority(nextOperator) > 0)
                nextOperator = null;
            
            ParsedCode operand2;
            if(nextOperator != null)
                operand2 = getSuperOperatorScope(it, operator);
            else operand2 = packPart(it);
            if(operator == OperatorSymbol.PROPERTY_ACCESS &&
                    !operand2.is(CodeType.IDENTIFIER))
                throw new CompilerError("Expected a valid identifier in PropertyAccess operator: " + operand2);
            operation = new Operator(operator, operand1, operand2);
        }
        else throw new CompilerError("Invalid operator type: " + operator);
        
        
        if(it.end())
            return operation;
        return packOperation(it, operation);
    }
    
    public final ParsedCode pack() throws CompilerError
    {
        Assignation a = Assignation.parse(this);
        if(a != null)
            return a;
        Counter it = new Counter();
        ParsedCode operand = packPart(it);
        if(it.end())
            return operand;
        return packOperation(it, operand);
    }
    
    private final class Counter
    {
        private int value;
        private final int limit;
        
        private Counter(int initialValue)
        {
            this.value = initialValue;
            this.limit = code.length;
        }
        private Counter() { this(0); }
        
        public final int increase(int times) { return value += times; }
        public final int increase() { return increase(1); }
        public final int decrease(int times) { return value -= times; }
        public final int decrease() { return decrease(1); }
        public final int value() { return value; }
        public final boolean end() { return value < limit; }
    }
    
    @FunctionalInterface
    interface Mapper<I, O> { O map(I input) throws CompilerError; }
    
    static final <IT, OT> OT[] mapArray(IT[] input, Mapper<IT, OT> mapper, OT[] output) throws CompilerError
    {
        int end = input.length > output.length ? output.length : input.length;
        for(int i=0;i<end;i++)
            output[i] = mapper.map(input[i]);
        return output;
    }
    
    static final <IT, OT> OT[] mapArray(int offset, IT[] input, Mapper<IT, OT> mapper, OT[] output) throws CompilerError
    {
        int end = input.length > output.length ? output.length : input.length;
        for(int i=offset;i<end;i++)
            output[i] = mapper.map(input[i]);
        return output;
    }
}

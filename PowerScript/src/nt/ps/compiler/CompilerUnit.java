/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Objects;
import nt.ps.PSClassLoader;
import nt.ps.PSGlobals;
import nt.ps.PSScript;
import nt.ps.compiler.CompilerBlock.CompilerBlockType;
import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.exception.CompilerErrors;
import nt.ps.compiler.exception.PSCompilerException;
import nt.ps.compiler.parser.AssignationSymbol;
import nt.ps.compiler.parser.Block;
import nt.ps.compiler.parser.Block.Scope;
import nt.ps.compiler.parser.Code;
import nt.ps.compiler.parser.Code.CodeType;
import nt.ps.compiler.parser.Command;
import nt.ps.compiler.parser.CommandWord;
import nt.ps.compiler.parser.CommandWord.CommandName;
import nt.ps.compiler.parser.GeneratorIdentifier;
import nt.ps.compiler.parser.Identifier;
import nt.ps.compiler.parser.Literal;
import nt.ps.compiler.parser.MutableLiteral;
import nt.ps.compiler.parser.OperatorSymbol;
import nt.ps.compiler.parser.Separator;
import nt.ps.compiler.parser.Tuple;
import nt.ps.compiler.parser.VarargsIdentifier;
import nt.ps.lang.PSFunction;

/**
 *
 * @author Asus
 */
public final class CompilerUnit
{
    private final CodeReader source;
    private final PSClassLoader classLoader;
    private final String name;
    private final PSGlobals globals;
    private final ClassRepository repository;
    private final boolean eval;
    
    private CompilerUnit(CodeReader source, PSClassLoader classLoader, String name, PSGlobals globals, ClassRepository repository, boolean eval)
    {
        this.source = Objects.requireNonNull(source);
        this.classLoader = Objects.requireNonNull(classLoader);
        this.name = Objects.requireNonNull(name);
        this.globals = Objects.requireNonNull(globals);
        this.repository = repository;
        this.eval = eval;
    }
    
    public static final PSScript compile(InputStream input, PSGlobals globals, PSClassLoader classLoader, String name, ClassRepository repository, boolean eval) throws PSCompilerException
    {
        CodeReader sourceBase = new CodeReader(input);
        CompilerUnit compiler = new CompilerUnit(sourceBase, classLoader, name, globals, repository, eval);
        
        PSScript script = compiler.compile(eval);
        return script;
    }
    public static final PSScript compile(InputStream input, PSGlobals globals, PSClassLoader classLoader, String name, boolean eval) throws PSCompilerException
    {
        return compile(input, globals, classLoader, name, null, eval);
    }
    
    public static final void compileAsJar(File jarFile, File sourceRoot, File... sources) throws IOException, PSCompilerException
    {
        JarBuilder.createJar(jarFile, sourceRoot, sources);
    }
    
    public static final PSFunction compileFunction(PSGlobals globals, PSClassLoader classLoader, String name, String code, String... args) throws IOException, PSCompilerException
    {
        CodeReader sourceBase;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(code.getBytes()))
        {
            sourceBase = new CodeReader(bais);
        }
        CompilerUnit compiler = new CompilerUnit(sourceBase, classLoader, name, globals, null, false);
        
        PSFunction func = compiler.compileFunction(args);
        return func;
    }
    
    private PSFunction compileFunction(String[] args) throws PSCompilerException
    {
        CompilerErrors errors = new CompilerErrors();
        
        Scope base = parseAllInstructions(errors);
        if(errors.hasErrors())
            throw new PSCompilerException(errors);
        
        ScopeInfo baseInfo = new ScopeInfo(base, ScopeInfo.ScopeType.BASE);
        BytecodeGenerator bytecode = new BytecodeGenerator(classLoader, name, args.length, 0, false, false);
        CompilerBlock compilerBlock = new CompilerBlock(baseInfo, globals, CompilerBlockType.FUNCTION, bytecode, errors, null, false, repository);
        VariablePool vars = compilerBlock.getVariables();
        
        try
        {
            for(String arg : args)
            {
                Identifier.checkValidIdentifier(arg);
                vars.createParameter(arg);
            }
        }
        catch(CompilerError ex)
        {
            errors.addError(ex, Command.parseErrorCommand(0));
        }
        if(errors.hasErrors())
            throw new PSCompilerException(errors);
        
        compilerBlock.compile(true, true);
        if(errors.hasErrors())
            throw new PSCompilerException(errors);
        
        Class<? extends PSFunction> baseClass = compilerBlock.getCompiledClass();
        return (PSScript) CompilerBlock.buildFunctionInstance(baseClass, globals);
    }
    
    private PSScript compile(boolean eval) throws PSCompilerException
    {
        CompilerErrors errors = new CompilerErrors();
        
        Scope base = parseAllInstructions(errors);
        if(errors.hasErrors())
            throw new PSCompilerException(errors);
        
        ScopeInfo baseInfo = new ScopeInfo(base, ScopeInfo.ScopeType.BASE);
        BytecodeGenerator bytecode = new BytecodeGenerator(classLoader, name);
        CompilerBlock compilerBlock = new CompilerBlock(baseInfo, globals, CompilerBlockType.SCRIPT, bytecode, errors, null, eval, repository);
        
        compilerBlock.compile(true, true);
        if(errors.hasErrors())
            throw new PSCompilerException(errors);
        
        Class<? extends PSFunction> baseClass = compilerBlock.getCompiledClass();
        return (PSScript) CompilerBlock.buildFunctionInstance(baseClass, globals);
    }
    
    private Scope parseAllInstructions(final CompilerErrors errors) { return parseScope(errors, source); }
    
    private Scope parseScope(final CompilerErrors errors, CodeReader source)
    {
        LinkedList<Command> commands = new LinkedList<>();
        
        while(source.hasNext())
        {
            int currentLine = source.getCurrentLine();
            try
            {
                Tuple tuple = parseInstruction(errors, source, false, ColonMode.ENDS);
                Command command = Command.decode(currentLine, tuple);
                if(command != null)
                    commands.add(command);
            }
            catch(CompilerError error) { errors.addError(error, Command.parseErrorCommand(currentLine)); }
        }
        
        return Block.scope(commands);
    }
    
    private Tuple parseInstruction(final CompilerErrors errors, CodeReader source, boolean validEnd, ColonMode colonMode) throws CompilerError
    {
        final InstructionBuilder sb = new InstructionBuilder();
        boolean canend = true;
        
        try
        {
            base_loop:
            for(;;)
            {
                char c = source.next();
                
                main_switch:
                switch(c)
                {
                    case '\t':
                    case '\r':
                    case '\n':
                    case ' ': {
                        sb.decode();
                    } break;
                    
                    case ';': {
                        sb.decode();
                        switch(colonMode)
                        {
                            case ENDS: break base_loop;
                            case IGNORE:
                                sb.addCode(Separator.COLON);
                                break main_switch;
                            case ERROR: throw new CompilerError("Unexpected End of Instruction ';'");
                            default: throw new IllegalStateException();
                        }
                    }
                    
                    case '(': {
                        CodeReader scopeSource = extractScope(source, '(', ')');
                        sb.decode();
                        if(sb.getCodeCount() > 0 && sb.getLastCode().is(Code.CodeType.COMMAND_WORD) && !sb.getLastCode().isElevatorCommand())
                        {
                            int line = scopeSource.getCurrentLine();
                            Tuple tuple = parseInstruction(errors, scopeSource, true, ColonMode.IGNORE);
                            if(((CommandWord)sb.getLastCode()).getName() == CommandName.FOR)
                                sb.addCode(Block.argumentsToFor(tuple, line));
                            else sb.addCode(Block.arguments(tuple, Separator.COLON));
                        }
                        else if(sb.getCodeCount() > 0 &&
                                (sb.getLastCode() == OperatorSymbol.FUNCTION ||  sb.getLastCode().is(CodeType.GENERATOR_IDENTIFIER)))
                        {
                            Tuple tuple = parseInstruction(errors, scopeSource, true, ColonMode.ERROR);
                            sb.addCode(Block.arguments(tuple, Separator.COMMA));
                        }
                        else
                        {
                            Tuple tuple = parseInstruction(errors, scopeSource, true, ColonMode.ERROR);

                            if(sb.getCodeCount() <= 0 || !sb.getLastCode().isValidCodeObject() || sb.getLastCode().isElevatorCommand()) //Parenthesis or Tuple
                            {
                                if(tuple.has(Separator.COMMA))
                                    sb.addCode(MutableLiteral.tuple(tuple));
                                else sb.addCode(Block.parenthesis(tuple));
                            }
                            else //Arguments list
                            {
                                int codesCount = sb.getCodeCount();
                                if(codesCount < 1)
                                    throw new CompilerError("Required any before call/invoke operators");
                                if(codesCount > 2 && sb.getCode(codesCount - 2) == OperatorSymbol.PROPERTY_ACCESS)
                                    sb.replaceCode(codesCount - 2, OperatorSymbol.INVOKE);
                                else sb.addOperator(OperatorSymbol.CALL);
                                sb.addCode(Block.arguments(tuple, Separator.COMMA));
                            }
                        }
                    } break;
                    
                    case ')': throw CompilerError.invalidChar(')');
                    
                    case '[': {
                        CodeReader scopeSource = extractScope(source, '[', ']');
                        Tuple tuple = parseInstruction(errors, scopeSource, true, ColonMode.ERROR);
                        sb.decode();
                        
                        if(sb.getCodeCount() <= 0 || !sb.getLastCode().isValidCodeObject() || sb.getLastCode().isElevatorCommand()) //Array or Map
                        {
                            if(tuple.has(Separator.TWO_POINTS))
                                sb.addCode(MutableLiteral.map(tuple));
                            else sb.addCode(MutableLiteral.array(tuple));
                        }
                        else //Access Operator
                            sb.addCode(OperatorSymbol.ACCESS)
                              .addCode(tuple.pack());
                    } break;
                    
                    case ']': throw CompilerError.invalidChar(']');
                    
                    case '{': {
                        CodeReader scopeSource = extractScope(source, '{', '}');
                        
                        if(sb.getCodeCount() <= 0 || !sb.getLastCode().isValidCodeObject() ||
                                sb.getLastCode().isElevatorCommand() || sb.getLastCode() == CommandWord.CONST) //Object
                        {
                            Tuple tuple = parseInstruction(errors, scopeSource, true, ColonMode.ERROR);
                            sb.decode();
                            if(sb.getCodeCount() > 0 && sb.getLastCode() == CommandWord.CONST)
                                sb.replaceCode(sb.getCodeCount() - 1, MutableLiteral.object(tuple, true));
                            else sb.addCode(MutableLiteral.object(tuple, false));
                        }
                        else //Scope
                        {
                            Scope scope = parseScope(errors, scopeSource);
                            sb.decode();
                            sb.addCode(scope);
                            if(sb.getCodeCount() > 2 && (sb.getCode(sb.getCodeCount() - 3) == OperatorSymbol.FUNCTION ||
                                    sb.getCode(sb.getCodeCount() - 3) == GeneratorIdentifier.GENERATOR))
                                break;
                            switch(colonMode)
                            {
                                case ENDS: break base_loop;
                                case IGNORE:
                                    sb.addCode(Separator.COLON);
                                    break main_switch;
                                case ERROR: throw new CompilerError("Unexpected End of Instruction ';'");
                                default: throw new IllegalStateException();
                            }
                        }
                    } break;
                    
                    case '}': throw CompilerError.invalidChar('}');
                    
                    case '\"':
                    case '\'': {
                        final char cend = c;
                        sb.decode();
                        
                        canend = false;
                        for(;;)
                        {
                            c = source.next();
                            if(c == cend)
                                break;
                            if(c == '\\')
                            {
                                c = source.next();
                                switch(c)
                                {
                                    case 'n': sb.append('\n'); break;
                                    case 'r': sb.append('\r'); break;
                                    case 't': sb.append('\t'); break;
                                    case 'u': {
                                        if(!source.canPeek(4))
                                            throw new CompilerError("Invalid unicode scape");
                                        String hexCode = new String(source.nextArray(4));
                                        sb.append(HexadecimalDecoder.decodeUnicode(hexCode));
                                    } break;
                                    case '\\': sb.append('\\'); break;
                                    case '$': sb.append('$'); break;
                                    case '\'': sb.append('\''); break;
                                    case '\"': sb.append('\"'); break;
                                }
                                continue;
                            }
                            sb.append(c);
                        }
                        canend = true;
                        sb.addCode(Literal.valueOf(sb.getAndClear()));
                    } break;
                    
                    case ',': {
                        sb.decode();
                        sb.addCode(Separator.COMMA);
                    } break;
                    
                    case ':': {
                        sb.decode();
                        sb.addCode(Separator.TWO_POINTS);
                        if(sb.getCodeCount() > 1 && sb.getFirstCode().is(Code.CodeType.COMMAND_WORD))
                        {
                            CommandWord cw = (CommandWord) sb.getFirstCode();
                            if(cw == CommandWord.CASE || cw == CommandWord.DEFAULT)
                                break base_loop;
                        }
                    } break;
                    
                    case '?': {
                        if(!source.canPeek(0))
                            throw CompilerError.invalidEndChar('?');
                        sb.addOperator(OperatorSymbol.TERNARY_CONDITION);
                    } break;
                    
                    case '|': {
                        if(!source.canPeek(0))
                            throw CompilerError.invalidEndChar('|');
                        c = source.next();
                        switch(c)
                        {
                            default: {
                                sb.addOperator(OperatorSymbol.LOGIC_OR);
                                source.move(-1);
                            } break;
                            case '|': {
                                if(!source.canPeek(0))
                                    throw CompilerError.invalidEndChar('|');
                                sb.addOperator(OperatorSymbol.OR);
                            } break;
                            case '=': {
                                if(!source.canPeek(0))
                                    throw CompilerError.invalidEndChar('|');
                                sb.addAssignation(AssignationSymbol.ASSIGNATION_LOGIC_OR);
                            } break;
                        }
                    } break;
                    
                    case '&': {
                        if(!source.canPeek(0))
                            throw CompilerError.invalidEndChar('&');
                        c = source.next();
                        switch(c)
                        {
                            default: {
                                sb.addOperator(OperatorSymbol.LOGIC_AND);
                                source.move(-1);
                            } break;
                            case '&': {
                                if(!source.canPeek(0))
                                    throw CompilerError.invalidEndChar('&');
                                sb.addOperator(OperatorSymbol.AND);
                            } break;
                            case '=': {
                                if(!source.canPeek(0))
                                    throw CompilerError.invalidEndChar('&');
                                sb.addAssignation(AssignationSymbol.ASSIGNATION_LOGIC_AND);
                            } break;
                        }
                    } break;
                    
                    case '^': {
                        if(!source.canPeek(0))
                            throw CompilerError.invalidEndChar('^');
                        c = source.next();
                        if(c == '=')
                        {
                            if(!source.canPeek(0))
                                throw CompilerError.invalidEndChar('=');
                            sb.addAssignation(AssignationSymbol.ASSIGNATION_LOGIC_XOR);
                        }
                        else
                        {
                            sb.addOperator(OperatorSymbol.LOGIC_XOR);
                            source.move(-1);
                        }
                    } break;
                    
                    case '.': {
                        if(!source.canPeek(0))
                            throw CompilerError.invalidEndChar('.');
                        c = source.next();
                        if(c == '.')
                        {
                            if(!source.canPeek(0))
                                throw CompilerError.invalidEndChar('.');
                            c = source.next();
                            if(c == '.')
                            {
                                sb.decode();
                                if(!sb.hasCodes() || !sb.getLastCode().is(CodeType.IDENTIFIER))
                                    throw new CompilerError("Expected a valid identifier before '...' operator: " + sb.getLastCode());
                                sb.replaceCode(sb.getCodeCount() - 1, new VarargsIdentifier((Identifier) sb.getLastCode()));
                            }
                            else
                            {
                                sb.addOperator(OperatorSymbol.STRING_CONCAT);
                                source.move(-1);
                            }
                        }
                        else
                        {
                            if(!sb.isEmpty() && isInteger(sb.get()))
                                sb.append('.');
                            else sb.addOperator(OperatorSymbol.PROPERTY_ACCESS);
                            source.move(-1);
                        }
                    } break;
                    
                    case '!': {
                        if(!source.canPeek(0))
                            throw CompilerError.invalidEndChar('!');
                        c = source.next();
                        if(c == '=')
                        {
                            if(!source.canPeek(0))
                                throw CompilerError.invalidEndChar('=');
                            c = source.next();
                            if(c == '=')
                            {
                                if(!source.canPeek(0))
                                    throw CompilerError.invalidEndChar('=');
                                sb.addOperator(OperatorSymbol.NOT_EQUALS_REFERENCE);
                            }
                            else
                            {
                                sb.addOperator(OperatorSymbol.NOT_EQUALS);
                                source.move(-1);
                            }
                        }
                        else
                        {
                            sb.addOperator(OperatorSymbol.NEGATE);
                            source.move(-1);
                        }
                    } break;
                    
                    case '=': {
                        if(!source.canPeek(0))
                            throw CompilerError.invalidEndChar('=');
                        c = source.next();
                        if(c == '=')
                        {
                            if(!source.canPeek(0))
                                throw CompilerError.invalidEndChar('=');
                            c = source.next();
                            if(c == '=')
                            {
                                if(!source.canPeek(0))
                                    throw CompilerError.invalidEndChar('=');
                                sb.addOperator(OperatorSymbol.EQUALS_REFERENCE);
                            }
                            else
                            {
                                sb.addOperator(OperatorSymbol.EQUALS);
                                source.move(-1);
                            }
                        }
                        else
                        {
                            sb.addAssignation(AssignationSymbol.ASSIGNATION);
                            source.move(-1);
                        }
                    } break;
                    
                    case '>': {
                        if(!source.canPeek(0))
                            throw CompilerError.invalidEndChar('>');
                        c = source.next();
                        switch(c)
                        {
                            default: {
                                sb.addOperator(OperatorSymbol.GREATER_THAN);
                                source.move(-1);
                            } break;
                            case '=': {
                                if(!source.canPeek(0))
                                    throw CompilerError.invalidEndChar('>');
                                sb.addOperator(OperatorSymbol.GREATER_THAN_EQUALS);
                            } break;
                            case '>': {
                                if(!source.canPeek(0))
                                    throw CompilerError.invalidEndChar('>');
                                sb.addOperator(OperatorSymbol.SHIFT_RIGHT);
                            } break;
                        }
                    } break;
                    
                    case '<': {
                        if(!source.canPeek(0))
                            throw CompilerError.invalidEndChar('<');
                        c = source.next();
                        switch(c)
                        {
                            default: {
                                sb.addOperator(OperatorSymbol.LESS_THAN);
                                source.move(-1);
                            } break;
                            case '=': {
                                if(!source.canPeek(0))
                                    throw CompilerError.invalidEndChar('<');
                                sb.addOperator(OperatorSymbol.LESS_THAN_EQUALS);
                            } break;
                            case '<': {
                                if(!source.canPeek(0))
                                    throw CompilerError.invalidEndChar('<');
                                sb.addOperator(OperatorSymbol.SHIFT_LEFT);
                            } break;
                        }
                    } break;
                    
                    case '-': {
                        if(!source.canPeek(0))
                            throw CompilerError.invalidEndChar('-');
                        c = source.next();
                        switch(c)
                        {
                            default: {
                                sb.addOperator("-");
                                source.move(-1);
                            } break;
                            case '-': {
                                if(!source.canPeek(0))
                                    throw CompilerError.invalidEndChar('-');
                                sb.addOperator(OperatorSymbol.DECREMENT);
                            } break;
                            case '=': {
                                if(!source.canPeek(0))
                                    throw CompilerError.invalidEndChar('=');
                                sb.addAssignation(AssignationSymbol.ASSIGNATION_MINUS);
                            } break;
                        }
                    } break;
                    
                    case '+': {
                        if(!source.canPeek(0))
                            throw CompilerError.invalidEndChar('+');
                        c = source.next();
                        switch(c)
                        {
                            default: {
                                sb.addOperator(OperatorSymbol.PLUS);
                                source.move(-1);
                            } break;
                            case '+': {
                                sb.addOperator(OperatorSymbol.INCREMENT);
                            } break;
                            case '=': {
                                if(!source.canPeek(0))
                                    throw CompilerError.invalidEndChar('=');
                                sb.addAssignation(AssignationSymbol.ASSIGNATION_PLUS);
                            } break;
                        }
                    } break;
                    
                    case '%': {
                        if(!source.canPeek(0))
                            throw CompilerError.invalidEndChar('%');
                        c = source.next();
                        if(c == '=')
                        {
                            sb.addOperator(OperatorSymbol.MODULE);
                            source.move(-1);
                        }
                        else
                        {
                            if(!source.canPeek(0))
                                throw CompilerError.invalidEndChar('=');
                            sb.addAssignation(AssignationSymbol.ASSIGNATION_MODULE);
                        }
                    } break;
                    
                    case '/': {
                        if(!source.canPeek(0))
                            throw CompilerError.invalidEndChar('/');
                        c = source.next();
                        switch(c)
                        {
                            case '/': {
                                sb.decode();
                                skipUntil(source, '\n', true);
                            } break;
                            case '*': {
                                for(;;)
                                {
                                    sb.decode();
                                    skipUntil(source, '*', true);
                                    c = source.next();
                                    if(c == '/')
                                        continue base_loop;
                                }
                            }
                            case '=': {
                                sb.addAssignation(AssignationSymbol.ASSIGNATION_DIVIDE);
                            } break;
                            default: {
                                sb.addOperator(OperatorSymbol.DIVIDE);
                                source.move(-1);
                            } break;
                        }
                    } break;
                    
                    case '*': {
                        if(!source.canPeek(0))
                            throw CompilerError.invalidEndChar('*');
                        c = source.next();
                        if(c == '=')
                        {
                            if(!source.canPeek(0))
                                throw CompilerError.invalidEndChar('=');
                            sb.addAssignation(AssignationSymbol.ASSIGNATION_MULTIPLY);
                        }
                        else
                        {
                            sb.decode();
                            if(sb.getCodeCount() > 0 && sb.getLastCode() == OperatorSymbol.FUNCTION)
                                sb.addCode(GeneratorIdentifier.GENERATOR);
                            else if(sb.getCodeCount() > 0 && sb.getLastCode() == CommandWord.YIELD)
                                sb.replaceCode(sb.getCodeCount() - 1, CommandWord.DELEGATOR_YIELD);
                            else sb.addOperator(OperatorSymbol.MULTIPLY);
                            source.move(-1);
                        }
                    } break;
                    
                    case '~': {
                        if(!source.canPeek(0))
                            throw CompilerError.invalidEndChar('~');
                        sb.addOperator(OperatorSymbol.LOGIC_NOT);
                    } break;
                    
                    
                    default: {
                        sb.append(c);
                    } break;
                }
            }
            
            if(!sb.isEmpty() && !eval)
                throw new CompilerError("Unexpected End of File");
        }
        catch(EOFException ex)
        {
            if((!validEnd && !canend) || !canend)
                throw new CompilerError("Unexpected End of File");
            sb.decode();
        }
        
        return sb.build();
    }
    
    private static void skipUntil(CodeReader source, char end, boolean isEndOfFileValid) throws EOFException
    {
        try
        {
            for(;;)
            {
                char c = source.next();
                if(c == end)
                    return;
            }
        }
        catch(EOFException ex)
        {
            if(!isEndOfFileValid)
                throw ex;
        }
    }
    
    private static CodeReader extractScope(CodeReader source, char cstart, char cend) throws CompilerError
    {
        int startIndex = source.getCurrentIndex();
        int scope = 0;
        try
        {
            for(;;)
            {
                char c = source.next();
                if(c == cstart)
                    scope++;
                else if(c == cend)
                {
                    if(scope == 0)
                        return source.subpart(startIndex, source.getCurrentIndex() - 1);
                    scope--;
                }
            }
        }
        catch(EOFException ex) { throw CompilerError.invalidChar(cstart); }
    }
    
    private static boolean isInteger(String str)
    {
        for(char c : str.toCharArray())
            if(!Character.isDigit(c))
                return false;
        return true;
    }
    
    private enum ColonMode { ENDS, IGNORE, ERROR }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

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
import nt.ps.compiler.parser.Command;
import nt.ps.compiler.parser.CommandWord;
import nt.ps.compiler.parser.Literal;
import nt.ps.compiler.parser.MutableLiteral;
import nt.ps.compiler.parser.OperatorSymbol;
import nt.ps.compiler.parser.Separator;
import nt.ps.compiler.parser.Tuple;
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
    
    private CompilerUnit(CodeReader source, PSClassLoader classLoader, String name, PSGlobals globals, ClassRepository repository)
    {
        this.source = Objects.requireNonNull(source);
        this.classLoader = Objects.requireNonNull(classLoader);
        this.name = Objects.requireNonNull(name);
        this.globals = Objects.requireNonNull(globals);
        this.repository = repository;
    }
    
    public static final PSScript compile(InputStream input, PSGlobals globals, PSClassLoader classLoader, String name, ClassRepository repository) throws PSCompilerException
    {
        CodeReader sourceBase = new CodeReader(input);
        CompilerUnit compiler = new CompilerUnit(sourceBase, classLoader, name, globals, repository);
        
        PSScript script = compiler.compile();
        return script;
    }
    public static final PSScript compile(InputStream input, PSGlobals globals, PSClassLoader classLoader, String name) throws PSCompilerException
    {
        return compile(input, globals, classLoader, name, null);
    }
    
    public static final void compileAsJar(File jarFile, File sourceRoot, File... sources) throws IOException, PSCompilerException
    {
        JarBuilder.createJar(jarFile, sourceRoot, sources);
    }
    
    private PSScript compile() throws PSCompilerException
    {
        CompilerErrors errors = new CompilerErrors();
        
        Scope base = parseAllInstructions(errors);
        if(errors.hasErrors())
            throw new PSCompilerException(errors);
        
        ScopeInfo baseInfo = new ScopeInfo(base, ScopeInfo.ScopeType.BASE);
        BytecodeGenerator bytecode = new BytecodeGenerator(classLoader, name);
        CompilerBlock compilerBlock = new CompilerBlock(baseInfo, globals, CompilerBlockType.SCRIPT, bytecode, errors, null, repository);
        
        compilerBlock.compile();
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
                            case IGNORE: break;
                            case ERROR: throw new CompilerError("Unexpected End of Instruction ';'");
                            default: throw new IllegalStateException();
                        }
                    }
                    
                    case '(': {
                        CodeReader scopeSource = extractScope(source, '(', ')');
                        if(sb.getCodeCount() > 0 && sb.getLastCode() == CommandWord.FOR)
                        {
                            Tuple tuple = parseInstruction(errors, scopeSource, true, ColonMode.IGNORE);
                            sb.decode();
                            sb.addCode(Block.arguments(tuple, Separator.COLON));
                        }
                        else
                        {
                            Tuple tuple = parseInstruction(errors, scopeSource, true, ColonMode.ERROR);
                            sb.decode();

                            if(sb.getCodeCount() <= 0 || !sb.getLastCode().isValidCodeObject()) //Parenthesis or Tuple
                            {
                                if(tuple.has(Separator.COMMA))
                                    sb.addCode(MutableLiteral.tuple(tuple));
                                else sb.addCode(Block.parenthesis(tuple));
                            }
                            else //Arguments list
                            {
                                sb.decode();
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
                        
                        if(sb.getCodeCount() <= 0 || !sb.getLastCode().isValidCodeObject()) //Array or Map
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
                        
                        if(sb.getCodeCount() <= 0 || !sb.getLastCode().isValidCodeObject()) //Object
                        {
                            Tuple tuple = parseInstruction(errors, scopeSource, true, ColonMode.ERROR);
                            sb.decode();
                            sb.addCode(MutableLiteral.object(tuple));
                        }
                        else //Scope
                        {
                            Scope scope = parseScope(errors, scopeSource);
                            sb.decode();
                            sb.addCode(scope);
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
                    } break;
                    
                    case '?': {
                        if(!source.canPeek(1))
                            throw CompilerError.invalidEndChar('?');
                        sb.addOperator(OperatorSymbol.TERNARY_CONDITION);
                    } break;
                    
                    case '|': {
                        if(!source.canPeek(1))
                            throw CompilerError.invalidEndChar('|');
                        c = source.next();
                        switch(c)
                        {
                            default: {
                                sb.addOperator(OperatorSymbol.LOGIC_OR);
                                source.move(-1);
                            } break;
                            case '|': {
                                if(!source.canPeek(1))
                                    throw CompilerError.invalidEndChar('|');
                                sb.addOperator(OperatorSymbol.OR);
                            } break;
                            case '=': {
                                if(!source.canPeek(1))
                                    throw CompilerError.invalidEndChar('|');
                                sb.addAssignation(AssignationSymbol.ASSIGNATION_LOGIC_OR);
                            } break;
                        }
                    } break;
                    
                    case '&': {
                        if(!source.canPeek(1))
                            throw CompilerError.invalidEndChar('&');
                        c = source.next();
                        switch(c)
                        {
                            default: {
                                sb.addOperator(OperatorSymbol.LOGIC_AND);
                                source.move(-1);
                            } break;
                            case '&': {
                                if(!source.canPeek(1))
                                    throw CompilerError.invalidEndChar('&');
                                sb.addOperator(OperatorSymbol.AND);
                            } break;
                            case '=': {
                                if(!source.canPeek(1))
                                    throw CompilerError.invalidEndChar('&');
                                sb.addAssignation(AssignationSymbol.ASSIGNATION_LOGIC_AND);
                            } break;
                        }
                    } break;
                    
                    case '^': {
                        if(!source.canPeek(1))
                            throw CompilerError.invalidEndChar('^');
                        c = source.next();
                        if(c == '=')
                        {
                            if(!source.canPeek(1))
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
                        if(!source.canPeek(1))
                            throw CompilerError.invalidEndChar('.');
                        c = source.next();
                        if(c == '.')
                        {
                            if(!source.canPeek(1))
                                throw CompilerError.invalidEndChar('.');
                            sb.addOperator(OperatorSymbol.STRING_CONCAT);
                        }
                        else
                        {
                            if(!sb.isEmpty() && Character.isDigit(sb.getLastChar()))
                                sb.append('.');
                            else sb.addOperator(OperatorSymbol.PROPERTY_ACCESS);
                            source.move(-1);
                        }
                    } break;
                    
                    case '!': {
                        if(!source.canPeek(1))
                            throw CompilerError.invalidEndChar('!');
                        c = source.next();
                        if(c == '=')
                        {
                            if(!source.canPeek(1))
                                throw CompilerError.invalidEndChar('=');
                            c = source.next();
                            if(c == '=')
                            {
                                if(!source.canPeek(1))
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
                        if(!source.canPeek(1))
                            throw CompilerError.invalidEndChar('=');
                        c = source.next();
                        if(c == '=')
                        {
                            if(!source.canPeek(1))
                                throw CompilerError.invalidEndChar('=');
                            c = source.next();
                            if(c == '=')
                            {
                                if(!source.canPeek(1))
                                    throw CompilerError.invalidEndChar('=');
                                sb.addOperator(OperatorSymbol.EQUALS_REFERENCE);
                            }
                            else
                            {
                                sb.addOperator(OperatorSymbol.EQUALS);
                                source.move(-1);
                            }
                        }
                        else sb.addAssignation(AssignationSymbol.ASSIGNATION);
                    } break;
                    
                    case '>': {
                        if(!source.canPeek(1))
                            throw CompilerError.invalidEndChar('>');
                        c = source.next();
                        switch(c)
                        {
                            default: {
                                sb.addOperator(OperatorSymbol.GREATER_THAN);
                                source.move(-1);
                            } break;
                            case '=': {
                                if(!source.canPeek(1))
                                    throw CompilerError.invalidEndChar('>');
                                sb.addOperator(OperatorSymbol.GREATER_THAN_EQUALS);
                            } break;
                            case '>': {
                                if(!source.canPeek(1))
                                    throw CompilerError.invalidEndChar('>');
                                sb.addOperator(OperatorSymbol.SHIFT_RIGHT);
                            } break;
                        }
                    } break;
                    
                    case '<': {
                        if(!source.canPeek(1))
                            throw CompilerError.invalidEndChar('<');
                        c = source.next();
                        switch(c)
                        {
                            default: {
                                sb.addOperator(OperatorSymbol.LESS_THAN);
                                source.move(-1);
                            } break;
                            case '=': {
                                if(!source.canPeek(1))
                                    throw CompilerError.invalidEndChar('<');
                                sb.addOperator(OperatorSymbol.LESS_THAN_EQUALS);
                            } break;
                            case '<': {
                                if(!source.canPeek(1))
                                    throw CompilerError.invalidEndChar('<');
                                sb.addOperator(OperatorSymbol.SHIFT_LEFT);
                            } break;
                        }
                    } break;
                    
                    case '-': {
                        if(!source.canPeek(1))
                            throw CompilerError.invalidEndChar('-');
                        c = source.next();
                        switch(c)
                        {
                            default: {
                                sb.addOperator("-");
                                source.move(-1);
                            } break;
                            case '-': {
                                if(!source.canPeek(1))
                                    throw CompilerError.invalidEndChar('-');
                                sb.addOperator(OperatorSymbol.DECREMENT);
                            } break;
                            case '=': {
                                if(!source.canPeek(1))
                                    throw CompilerError.invalidEndChar('=');
                                sb.addAssignation(AssignationSymbol.ASSIGNATION_MINUS);
                            } break;
                        }
                    } break;
                    
                    case '+': {
                        if(!source.canPeek(1))
                            throw CompilerError.invalidEndChar('+');
                        c = source.next();
                        switch(c)
                        {
                            default: {
                                sb.addOperator(OperatorSymbol.PLUS);
                                source.move(-1);
                            } break;
                            case '-': {
                                if(!source.canPeek(1))
                                    throw CompilerError.invalidEndChar('+');
                                sb.addOperator(OperatorSymbol.INCREMENT);
                            } break;
                            case '=': {
                                if(!source.canPeek(1))
                                    throw CompilerError.invalidEndChar('=');
                                sb.addAssignation(AssignationSymbol.ASSIGNATION_PLUS);
                            } break;
                        }
                    } break;
                    
                    case '%': {
                        if(!source.canPeek(1))
                            throw CompilerError.invalidEndChar('%');
                        c = source.next();
                        if(c == '=')
                        {
                            sb.addOperator(OperatorSymbol.MODULE);
                            source.move(-1);
                        }
                        else
                        {
                            if(!source.canPeek(1))
                                throw CompilerError.invalidEndChar('=');
                            sb.addAssignation(AssignationSymbol.ASSIGNATION_MODULE);
                        }
                    } break;
                    
                    case '/': {
                        if(!source.canPeek(1))
                            throw CompilerError.invalidEndChar('/');
                        c = source.next();
                        switch(c)
                        {
                            case '/': {
                                canend = true;
                                skipUntil(source, '\n', true);
                                canend = false;
                            } break;
                            case '*': {
                                canend = true;
                                for(;;)
                                {
                                    skipUntil(source, '*', true);
                                    c = source.next();
                                    if(c == '/')
                                    {
                                        canend = false;
                                        continue base_loop;
                                    }
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
                        if(!source.canPeek(1))
                            throw CompilerError.invalidEndChar('*');
                        c = source.next();
                        if(c == '=')
                        {
                            if(!source.canPeek(1))
                                throw CompilerError.invalidEndChar('=');
                            sb.addAssignation(AssignationSymbol.ASSIGNATION_MULTIPLY);
                        }
                        else
                        {
                            sb.addOperator(OperatorSymbol.MULTIPLY);
                            source.move(-1);
                        }
                    } break;
                    
                    case '~': {
                        if(!source.canPeek(1))
                            throw CompilerError.invalidEndChar('~');
                        sb.addOperator(OperatorSymbol.LOGIC_NOT);
                    } break;
                    
                    
                    default: {
                        sb.append(c);
                    } break;
                }
            }
            
            if(!sb.isEmpty())
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
    
    private enum ColonMode { ENDS, IGNORE, ERROR }
}

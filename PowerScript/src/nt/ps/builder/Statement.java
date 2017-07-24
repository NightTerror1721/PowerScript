/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.builder;

import java.util.LinkedList;
import java.util.Objects;
import java.util.StringJoiner;

/**
 *
 * @author Asus
 */
public interface Statement extends InstructionCode
{
    public static Literal literal(int i)
    {
        switch(i)
        {
            case -1: return Literal.MINUSONE;
            case 0: return Literal.ZERO;
            case 1: return Literal.ONE;
            default: return () -> Integer.toString(i);
        }
    }
    
    public static Literal literal(long l) { return () -> Long.toString(l) + "L"; }
    
    public static Literal literal(float f) { return () -> Float.toString(f) + "f"; }
    
    public static Literal literal(double d) { return () -> Double.toString(d); }
    
    public static Literal literal(boolean b) { return b ? Literal.TRUE : Literal.FALSE; }
    
    public static Literal literal(String s)
    {
        if(s == null)
            return Literal.NULL;
        if(s.isEmpty())
            return Literal.EMPTY_STRING;
        final String s2 = "\"" + s.replace("\n","\\n").replace("\r","\\r")
                .replace("\t","\\t").replace("\"","\\\"")
                .replace("\'","\\\'").replace("\\","\\\\") + "\"";
        return () -> s2;
    }
    
    public static Literal literal(Enum<?> e)
    {
        if(e == null)
            return Literal.NULL;
        return literal(e.ordinal());
    }
    
    public static Statement variable(String name) { return () -> name; }
    
    public default InstructionCode assign(String... varnames)
    {
        if(varnames.length <= 0)
            throw new IllegalArgumentException("varnames.length cannot be 0");
        final Statement self = this;
        return () -> {
            StringJoiner joiner = new StringJoiner(", ");
            for(String varname : varnames)
                joiner.add(varname);
            return joiner + " = " + self.toCode();
        };
    }
    
    public default InstructionCode assign(boolean global, String... varnames)
    {
        if(varnames.length <= 0)
            throw new IllegalArgumentException("varnames.length cannot be 0");
        final Statement self = this;
        return () -> {
            StringJoiner joiner = new StringJoiner(", ");
            for(String varname : varnames)
                joiner.add(varname);
            return (global ? "" : "local ") + joiner + " = " + self.toCode();
        };
    }
    
    public default InstructionCode declare(boolean global, String... varnames)
    {
        if(varnames.length <= 0)
            throw new IllegalArgumentException("varnames.length cannot be 0");
        return () -> {
            StringJoiner joiner = new StringJoiner(", ");
            for(String varname : varnames)
                joiner.add(varname);
            return (global ? "" : "local ") + joiner;
        };
    }
    
    
    
    public default Statement opPlus(Statement statement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + " + " + statement.toCodeWrapped();
    }
    
    public default Statement opMinus(Statement statement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + " - " + statement.toCodeWrapped();
    }
    
    public default Statement opMult(Statement statement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + " * " + statement.toCodeWrapped();
    }
    
    public default Statement opDiv(Statement statement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + " / " + statement.toCodeWrapped();
    }
    
    public default Statement opMod(Statement statement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + " % " + statement.toCodeWrapped();
    }
    
    public default Statement opAnd(Statement statement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + " && " + statement.toCodeWrapped();
    }
    
    public default Statement opOr(Statement statement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + " || " + statement.toCodeWrapped();
    }
    
    public default Statement opNot()
    {
        final Statement self = this;
        return () -> "!" + self.toCodeWrapped();
    }
    
    public default Statement opEq(Statement statement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + " == " + statement.toCodeWrapped();
    }
    
    public default Statement opNoeq(Statement statement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + " != " + statement.toCodeWrapped();
    }
    
    public default Statement opGrThan(Statement statement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + " > " + statement.toCodeWrapped();
    }
    
    public default Statement opLsThan(Statement statement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + " < " + statement.toCodeWrapped();
    }
    
    public default Statement opGrEqThan(Statement statement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + " >= " + statement.toCodeWrapped();
    }
    
    public default Statement opLsEqThan(Statement statement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + " <= " + statement.toCodeWrapped();
    }
    
    public default Statement opRefEq(Statement statement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + " === " + statement.toCodeWrapped();
    }
    
    public default Statement opRefNoeq(Statement statement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + " !== " + statement.toCodeWrapped();
    }
    
    public default Statement opSet(Statement keyStatement, Statement valueStatement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + "[" + keyStatement.toCodeWrapped() + "] = " +
                valueStatement.toCodeWrapped();
    }
    
    public default Statement opGet(Statement keyStatement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + "[" + keyStatement.toCodeWrapped() + "]";
    }
    
    public default Statement opSetProp(String property, Statement valueStatement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + "." + property + " = " + valueStatement.toCodeWrapped();
    }
    
    public default Statement opGetProp(String property)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + "." + property;
    }
    
    public default Statement opIn(Statement statement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + " in " + statement.toCodeWrapped();
    }
    
    public default Statement opNeg()
    {
        final Statement self = this;
        return () -> "(-" + self.toCodeWrapped() + ")";
    }
    
    public default Statement opConcat(Statement statement)
    {
        final Statement self = this;
        return () -> self.toCodeWrapped() + ".." + statement.toCodeWrapped();
    }
    
    public default Statement opNew(Statement... args)
    {
        final Statement self = this;
        return () -> {
            StringBuilder sb = new StringBuilder();
            sb.append("new ").append(self.toCodeWrapped()).append("(");
            if(args.length > 0)
            {
                StringJoiner joiner = new StringJoiner(", ");
                for(Statement arg : args)
                    joiner.add(arg.toCodeWrapped());
                sb.append(joiner);
            }
            sb.append(")");
            return sb.toString();
        };
    }
    
    public default Statement opCall(Statement... args)
    {
        final Statement self = this;
        return () -> {
            StringBuilder sb = new StringBuilder();
            sb.append(self.toCodeWrapped()).append("(");
            if(args.length > 0)
            {
                StringJoiner joiner = new StringJoiner(", ");
                for(Statement arg : args)
                    joiner.add(arg.toCodeWrapped());
                sb.append(joiner);
            }
            sb.append(")");
            return sb.toString();
        };
    }
    
    public default Statement opInvoke(String property, Statement... args)
    {
        final Statement self = this;
        final String member2 = property.replace("\n","\\n").replace("\r","\\r")
                .replace("\t","\\t").replace("\"","\\\"")
                .replace("\'","\\\'").replace("\\","\\\\");
        return () -> {
            StringBuilder sb = new StringBuilder();
            sb.append(self.toCodeWrapped()).append(".")
                    .append(literal(member2).toCode()).append("(");
            if(args.length > 0)
            {
                StringJoiner joiner = new StringJoiner(", ");
                for(Statement arg : args)
                    joiner.add(arg.toCodeWrapped());
                sb.append(joiner);
            }
            sb.append(")");
            return sb.toString();
        };
    }
    
    
    public static class MetaList
    {
        private final boolean tuple;
        private final LinkedList<Statement> list;
        
        private MetaList(boolean tuple)
        {
            this.tuple = tuple;
            list = new LinkedList<>();
        }
        
        public final MetaList add(Statement statement)
        {
            list.add(Objects.requireNonNull(statement));
            return this;
        }
        
        public final Literal end()
        {
            if(list.isEmpty())
                return tuple ? Literal.EMPTY_TUPLE : Literal.EMPTY_LIST;
            return () -> {
                StringBuilder sb = new StringBuilder(list.size() * 10);
                sb.append(tuple ? "(" : "[");
                list.stream().forEach((s) -> {
                    sb.append(s.toCodeWrapped()).append(", ");
                });
                if(!tuple)
                    sb.delete(sb.length() - 2, sb.length());
                sb.append(tuple ? ")" : "]");
                return sb.toString();
            };
        }
        
        public static final class MetaArray extends MetaList
        {
            private MetaArray() { super(false); }
        }
        
        public static final class MetaTuple extends MetaList
        {
            private MetaTuple() { super(true); }
        }
    }
    public static MetaList.MetaArray array() { return new MetaList.MetaArray(); }
    public static MetaList.MetaTuple tuple() { return new MetaList.MetaTuple(); }
    
    
    public static class MetaPairs<T>
    {
        private final boolean object;
        private final LinkedList<Entry> map;
        
        private MetaPairs(boolean object)
        {
            this.object = object;
            map = new LinkedList<>();
        }
        
        public final MetaPairs<T> put(T key, Statement value)
        {
            map.add(new Entry(
                    Objects.requireNonNull(object ? Statement.variable((String) key) : (Statement) key),
                    Objects.requireNonNull(value)
            ));
            return this;
        }
        
        public final Literal end()
        {
            if(map.isEmpty())
                return object ? Literal.EMPTY_OBJECT : Literal.EMPTY_MAP;
            return () -> {
                StringBuilder sb = new StringBuilder(map.size() * 12);
                sb.append(object ? "{ " : "[ ");
                map.stream().forEach((e) -> {
                    sb.append(e.key.toCodeWrapped()).append(": ")
                            .append(e.value.toCodeWrapped()).append(", ");
                });
                sb.delete(sb.length() - 2, sb.length()).append(object ? " }" : " ]");
                return sb.toString();
            };
        }
        
        private final class Entry
        {
            private final Statement key, value;
            private Entry(Statement key, Statement value)
            {
                this.key = key;
                this.value = value;
            }
        }
        
        public static final class MetaMap extends MetaPairs<Statement>
        {
            private MetaMap() { super(false); }
        }
        
        public static final class MetaObject extends MetaPairs<String>
        {
            private MetaObject() { super(true); }
        }
    }
    public static MetaPairs.MetaMap map() { return new MetaPairs.MetaMap(); }
    public static MetaPairs.MetaObject object() { return new MetaPairs.MetaObject(); }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler.parser;

import java.util.Objects;

/**
 *
 * @author Asus
 */
public abstract class ParsedCode extends Code
{
    @Override
    public abstract String toString();
    
    @Override
    public final boolean isParsedCode() { return true; }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(toString());
        return hash;
    }
    
}

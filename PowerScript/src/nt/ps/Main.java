/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps;

import nt.ps.compiler.exception.CompilerError;
import nt.ps.compiler.parser.Literal;

/**
 *
 * @author Asus
 */
public final class Main
{
    public static void main(String[] args) throws CompilerError
    {
        System.out.println(Literal.decode("Infinity"));
    }
}

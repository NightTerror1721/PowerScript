/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps;

import nt.ps.compiler.parser.Literal;

/**
 *
 * @author Asus
 */
public final class Main
{
    public static void main(String[] args)
    {
        System.out.println(Literal.isNumber("56"));
        System.out.println(Literal.isNumber("0x1"));
        System.out.println(Literal.isNumber(".6"));
        System.out.println(Literal.isNumber("6D"));
    }
}

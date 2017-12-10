/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps;

import nt.ps.lang.PSFunction.PSZeroArgsFunction;
import nt.ps.lang.PSValue;

/**
 *
 * @author mpasc
 */
public abstract class PSScript extends PSZeroArgsFunction
{
    public final PSValue execute()
    {
        return innerCall(NULL).self();
    }
}

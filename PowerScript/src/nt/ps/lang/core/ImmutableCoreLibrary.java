/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.lang.core;

import nt.ps.lang.PSUserdata;
import nt.ps.lang.PSValue;

/**
 *
 * @author Asus
 */
abstract class ImmutableCoreLibrary extends PSUserdata
{
    @Override
    public abstract PSValue getProperty(String name);
    
    @Override
    public final PSValue setProperty(String name, PSValue value)
    {
        return super.setProperty(name, value);
    }
}

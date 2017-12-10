/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.exception;

import nt.ps.lang.PSDataType;
import nt.ps.lang.PSValue;

/**
 *
 * @author Asus
 */
public class PSCastException extends PSRuntimeException
{
    public PSCastException(PSDataType selfType, PSDataType targetType)
    {
        super("Cannot cast " + selfType.getTypeName() + " to " + targetType.getTypeName());
    }
    
    public PSCastException(PSValue self, PSDataType targetType)
    {
        this(self.getPSType(),targetType);
    }
}

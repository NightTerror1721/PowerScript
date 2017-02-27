/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.exception;

import nt.ps.datatype.PSDataType;
import nt.ps.datatype.PSValue;

/**
 *
 * @author Asus
 */
public class PSUnsupportedOperationException extends PSRuntimeException
{
    public PSUnsupportedOperationException(PSDataType selfType, String opName)
    {
        super(selfType.getTypeName() + " cannot support " + opName + " operation");
    }
    
    public PSUnsupportedOperationException(PSValue self, String opName)
    {
        this(self.getPSType(),opName);
    }
}

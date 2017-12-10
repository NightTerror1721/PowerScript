/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.exception;

/**
 *
 * @author Asus
 */
public class PSRuntimeException extends RuntimeException
{
    public PSRuntimeException() {}
    public PSRuntimeException(String msg) { super(msg); }
    public PSRuntimeException(Throwable th) { super(th); }
    public PSRuntimeException(String msg, Throwable th) { super(msg,th); }
}

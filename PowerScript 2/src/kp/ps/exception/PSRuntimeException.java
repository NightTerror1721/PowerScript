/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.ps.exception;

/**
 *
 * @author Asus
 */
public class PSRuntimeException extends RuntimeException
{
    public PSRuntimeException() { super(); }
    public PSRuntimeException(String msg) { super(msg); }
    public PSRuntimeException(Throwable ex) { super(ex); }
    public PSRuntimeException(String msg, Throwable ex) { super(msg, ex); }
}

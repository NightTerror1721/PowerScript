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
public class PSException extends Exception
{
    public PSException() {}
    public PSException(String msg) { super(msg); }
    public PSException(Throwable th) { super(th); }
    public PSException(String msg, Throwable th) { super(msg,th); }
}

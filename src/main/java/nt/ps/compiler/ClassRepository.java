/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nt.ps.compiler;

/**
 *
 * @author Asus
 */
public interface ClassRepository
{
    void registerClass(String name, byte[] data);
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.ps.lang;

/**
 *
 * @author Asus
 */
public interface PSIteratorResult
{
    int resultElementsCount();
    PSValue resultElement(int index);
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.automatalib.words;

/**
 *
 * @author jeroen
 */
public class InfiniteWordException extends IllegalStateException {
    
    public InfiniteWordException() {
        super("Can not perform operation, because this is an infite word.");
    }
}

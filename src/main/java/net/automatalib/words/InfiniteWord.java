/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.automatalib.words;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.automatalib.ts.simple.SimpleDTS;

/**
 *
 * @author jeroen
 */
public class InfiniteWord<I, S> extends Word<I> {
    
    @Getter
    private final SimpleDTS<S, I> ts;
    
    private final Alphabet<I> alphabet;
    
    @Getter
    private final Word<I> prefix;
    
    @Getter
    private final Word<I> loop;
    
    public <S> InfiniteWord(SimpleDTS ts, Alphabet alphabet) throws IllegalArgumentException {
        this.ts = ts;
        this.alphabet = alphabet;
        
        final Word one = unroll(1);
        final Word two = unroll(2);        
        loop = two.subWord(one.length());
        prefix = one.prefix(one.length() - loop.length());
    }
    
    public final Word<I> unroll(int count) throws IllegalArgumentException {
        final Map<S, Integer> visitCount = new HashMap<>();
                
        S currentState = ts.getInitialState();
        
        Word<I> input = Word.epsilon();
        
        if (currentState != null) {
        
            visitCount.put(currentState, 1);

            do {
                I letter = null;
                S nextState = null;
                for (I i : alphabet) {
                    S tryState = ts.getSuccessor(currentState, i);
                    if (tryState != null) {
                        if (letter != null) {
                            throw new IllegalArgumentException(String.format("DTS does not contain a single word, old = %s - %s -> ?, new = %s - %s -> %s", currentState, letter, currentState, i, nextState));
                        } else {
                            letter = i;
                            nextState = tryState;
                        }
                    }
                }
                if (letter == null || nextState == null) throw new IllegalArgumentException("DTS does not contain an infinite word.");
                else {                    
                    currentState = nextState;
                    visitCount.put(currentState, visitCount.getOrDefault(currentState, 0) + 1);
                    input = input.append(letter);
                }
            } while (visitCount.get(currentState) <= count);
        }
                
        return input;
    }

    @Override
    public I getSymbol(int index) {
        throw new InfiniteWordException();
//        if (!isEmpty()) {
//            
//        } else throw new IndexOutOfBoundsException(Integer.toString(index));
    }

    @Override
    public int length() {
        if (prefix.isEmpty()) return 0;
        throw new InfiniteWordException();
    }

    @Override
    public Word<I> append(I symbol) {
        throw new InfiniteWordException();
    }

    @Override
    public boolean equals(Object other) {
        /* to do this correctly we must check if <other>'s automaton
        represents the same infite word */
        return false;
    }

    @Override
    public int hashCode() {
        return ts.hashCode();
    }

    @Override
    public void print(Appendable a) throws IOException {
        throw new InfiniteWordException();
    }
        
	private class Iterator implements java.util.Iterator<I> {

        private final java.util.Iterator<I> prefixIterator = prefix.iterator();
        private java.util.Iterator<I> loopIterator = loop.iterator();
        
        @Override
        public boolean hasNext() {
            return !isEmpty();
        }

        @Override
        public I next() {
            final I next;
            if (prefixIterator.hasNext()) next = prefixIterator.next();
            else {
                if (!loopIterator.hasNext()) loopIterator = loop.iterator();
                next = loopIterator.next();
            }
            return next;
        }
    }

    @Override
    public java.util.Iterator<I> iterator() {
        return new Iterator();
    }

    @Override
    public List<I> asList() {
        throw new InfiniteWordException();
    }

    @Override
    public Word<I> canonicalNext(Alphabet<I> sigma) {
        throw new InfiniteWordException();
    }

    @Override
    public boolean isEmpty() {
        try {
            return length() == 0;
        } catch (InfiniteWordException iwe) {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("prefix: %s, loop: %s", prefix, loop);
    }    
}

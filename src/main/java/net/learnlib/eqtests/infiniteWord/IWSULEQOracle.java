/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.learnlib.eqtests.infiniteWord;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.SUL;
import de.learnlib.oracles.DefaultQuery;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import net.automatalib.automata.transout.TransitionOutputAutomaton;
import net.automatalib.words.InfiniteWord;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 *
 * @author jeroen
 */
public abstract class IWSULEQOracle<S,I,T,O> implements EquivalenceOracle<TransitionOutputAutomaton<S,I,T,O>, I, Word<O>> {
    
    @Getter
    @Setter
    private SUL<I,O> sul;
    
    @Getter
    @Setter
    private InfiniteWord<I, S> infiniteWord;
    
    @Getter
    @Setter
    private boolean removeUnsuccessful;
    
    public IWSULEQOracle(SUL sul, boolean removeUnsuccessful) {
        this(sul, null, removeUnsuccessful);
    }
    
    public IWSULEQOracle(SUL sul, InfiniteWord infiniteWord, boolean removeUnsuccessful) {
        this.infiniteWord = infiniteWord;
        this.sul = sul;
        this.removeUnsuccessful = removeUnsuccessful;
    }
    
    protected boolean stop() {
        return false;
    }

    @Override
    public DefaultQuery<I, Word<O>> findCounterExample(TransitionOutputAutomaton<S, I, T, O> hypothesis, Collection<? extends I> inputs) {
                        
        DefaultQuery<I, Word<O>> counterExample = null;
        
        if (infiniteWord != null)  {
            S currentState = hypothesis.getInitialState();

            sul.pre();

            final WordBuilder<I> finiteInputBuilder = new WordBuilder();
            final WordBuilder<O> finiteOutputBuilder = new WordBuilder();

            try {
                
                Iterator<I> infiniteWordIterator = infiniteWord.iterator();

                boolean validInput = true;
                                
                while (infiniteWordIterator.hasNext() && counterExample == null && validInput && !stop()) {

                    final I input = infiniteWordIterator.next();
                    
                    if (validInput = inputs.contains(input)) {                
                        finiteInputBuilder.append(input);

                        final O outputHyp = hypothesis.getOutput(currentState, input);
                        final O outputSUL = sul.step(input);
                        finiteOutputBuilder.append(outputSUL);

                        if (!Objects.equals(outputHyp, outputSUL)) {
                            counterExample = new DefaultQuery(Word.epsilon(), finiteInputBuilder.toWord());
                            counterExample.answer(finiteOutputBuilder.toWord());
                        } else {
                            currentState = hypothesis.getSuccessor(currentState, input);
                        }
                    }
                }

            } finally {
                sul.post();
            }
            
            if (removeUnsuccessful) infiniteWord = null;
        }

        return counterExample;
    }
}

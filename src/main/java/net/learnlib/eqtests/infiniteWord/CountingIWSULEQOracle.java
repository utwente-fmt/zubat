/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.learnlib.eqtests.infiniteWord;

import de.learnlib.api.SUL;
import de.learnlib.oracles.DefaultQuery;
import java.util.Collection;
import lombok.Getter;
import net.automatalib.automata.transout.TransitionOutputAutomaton;
import net.automatalib.words.InfiniteWord;

/**
 *
 * @author jeroen
 */
public class CountingIWSULEQOracle extends IWSULEQOracle {

    @Getter
    private int maxInputs;

    @Getter
    private int maxUnrolls;

    @Getter
    private int inputCount = 0;

    public CountingIWSULEQOracle(SUL sul, InfiniteWord infiniteWord, boolean removeUnsuccessful, int maxUnrolls) {
        super(sul, null, removeUnsuccessful);
        this.maxUnrolls = maxUnrolls;
        setInfiniteWord(infiniteWord);
    }

    public CountingIWSULEQOracle(SUL sul, boolean removeUnsuccessful, int maxUnrolls) {
        this(sul, null, removeUnsuccessful, maxUnrolls);
    }

    public CountingIWSULEQOracle(SUL sul, boolean removeUnsuccessful) {
        this(sul, removeUnsuccessful, -2);
    }

    public CountingIWSULEQOracle(SUL sul, InfiniteWord infiniteWord, boolean removeUnsuccessful) {
        this(sul, infiniteWord, removeUnsuccessful, -2);
    }

    private void setMaxInputs() {
        setMaxInputs(maxUnrolls);
    }

    private void setMaxInputs(int maxUnrolls) {
        if (maxUnrolls == -1) maxInputs = -1;
        else if (maxUnrolls >= 0 && getInfiniteWord() != null) {
            final int prefix = getInfiniteWord().getPrefix().length();
            final int loop = getInfiniteWord().getLoop().length();

            try {
                maxInputs = Math.addExact(prefix, Math.multiplyExact(maxUnrolls, loop));
            } catch (ArithmeticException ae) {
                maxInputs = Integer.MAX_VALUE;
            }
        }
    }

    public final void setMaxUnrolls(int maxUnrolls) {
        this.maxUnrolls = maxUnrolls;
        setMaxInputs();
    }

    @Override
    public final void setInfiniteWord(InfiniteWord infiniteWord) {
        super.setInfiniteWord(infiniteWord);
        setMaxInputs();
    }

    @Override
    protected boolean stop() {

        final boolean stop;
        if (maxUnrolls == -1) stop = false;
        else stop = inputCount == maxInputs;

        inputCount++;

        return stop;
    }

    @Override
    public DefaultQuery findCounterExample(TransitionOutputAutomaton hypothesis, Collection inputs) {
        this.inputCount = 0;
        if (this.maxUnrolls == -2) setMaxInputs(hypothesis.size());
        return super.findCounterExample(hypothesis, inputs);
    }

    public boolean isStopped() {
        return inputCount < maxInputs;
    }
}

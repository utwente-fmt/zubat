/* Copyright (C) 2013-2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.utwente.fmt.etfexporter.example;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.util.graphs.dot.GraphDOT;
import nl.utwente.fmt.etfexporter.ETF;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.drivers.reflect.AbstractMethodInput;
import de.learnlib.drivers.reflect.AbstractMethodOutput;
import de.learnlib.drivers.reflect.SimplePOJOTestDriver;
import de.learnlib.eqtests.basic.mealy.RandomWalkEQOracle;
import de.learnlib.experiments.Experiment.MealyExperiment;
import de.learnlib.oracles.SULOracle;

/**
 * This example shows how a model of a Java class can be learned using the SUL
 * (system under learning) interfaces and the random walks equivalence test.
 *
 * @author falkhowar
 */
public class Learnlib_Example_Minimal {

    /*
     * The BoundedStringQueue is the class of which we are going to 
     * infer a model. It wraps an ordinary queue of Strings, limiting
     * its size to MAX_SIZE (3). Once the queue is full, additional 
     * offers will be ignored.
     * <p>
     * However, the implementation uses the underlying queue in a strange
     * way as the model will reveal.
     */
    public static class BoundedStringQueue {

        // capacity
        public static final int MAX_SIZE = 2;
        // storage
        private Deque<String> data = new ArrayDeque<>(MAX_SIZE);

        // add a String to the queue if capacity allows
        public void offer(String s) {
            if (data.size() < MAX_SIZE) {
                data.offerFirst(s);
            }
        }

        // get next element from queue (null for empty queue)
        public String poll() {
            return data.poll();
        }
    }
    

    public static <S,I,T> void main(String[] args) throws NoSuchMethodException, IOException {

        // instantiate test driver
        SimplePOJOTestDriver driver = new SimplePOJOTestDriver(
                BoundedStringQueue.class.getConstructor());
                
        // create learning alphabet
        Method mOffer = BoundedStringQueue.class.getMethod(
                "offer", new Class<?>[]{String.class});
        Method mPoll = BoundedStringQueue.class.getMethod(
                "poll", new Class<?>[]{});
                
        // offer
        AbstractMethodInput offer_a = driver.addInput("push_a", mOffer, "a");
        AbstractMethodInput offer_b = driver.addInput("push_b", mOffer, "b");

        // poll
        AbstractMethodInput poll = driver.addInput("poll", mPoll);
        
        SULOracle<AbstractMethodInput, AbstractMethodOutput> mqOracle = new SULOracle<>(driver);


        // construct L* instance (almost classic Mealy version)
        // almost: we use words (Word<String>) in cells of the table 
        // instead of single outputs.
        MealyLearner<AbstractMethodInput, AbstractMethodOutput> lstar
        	= new ExtensibleLStarMealyBuilder<AbstractMethodInput,AbstractMethodOutput>()
        		.withAlphabet(driver.getInputs()) // input alphabet
        		.withOracle(mqOracle)			  // membership oracle
        		.create();
                

        // create random walks equivalence test
        MealyEquivalenceOracle<AbstractMethodInput, AbstractMethodOutput> randomWalks =
                new RandomWalkEQOracle<>(
                0.05, // reset SUL w/ this probability before a step 
                10000, // max steps (overall)
                false, // reset step count after counterexample 
                new Random(46346293), // make results reproducible 
                driver // system under learning
                );

        // construct a learning experiment from
        // the learning algorithm and the random walks test.
        // The experiment will execute the main loop of
        // active learning
        MealyExperiment<AbstractMethodInput, AbstractMethodOutput> experiment =
                new MealyExperiment<>(lstar, randomWalks, driver.getInputs());


        // run experiment
        experiment.run();

        // get learned model
        MealyMachine<S, AbstractMethodInput, ?, AbstractMethodOutput> result = 
                (MealyMachine<S, AbstractMethodInput, ?, AbstractMethodOutput>) experiment.getFinalHypothesis();

        // report results
        System.out.println("-------------------------------------------------------");

        // Export ETF model in sys.out
        ETF.export(result, driver.getInputs(), System.out);

        // Show model in GraphDOT
        Writer w = DOT.createDotWriter(true);
        GraphDOT.write(result, driver.getInputs(), w);
        w.close();
        
        System.out.println("-------------------------------------------------------");

    }
}
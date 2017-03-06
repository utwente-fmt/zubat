package nl.utwente.fmt.etfexporter.wgc;

import java.io.IOException;
import java.io.Writer;
import java.util.Random;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.SimpleAlphabet;
import nl.utwente.fmt.etfexporter.ETF;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealy;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.SUL;
import de.learnlib.eqtests.basic.WMethodEQOracle;
import de.learnlib.eqtests.basic.WpMethodEQOracle;
import de.learnlib.eqtests.basic.mealy.RandomWalkEQOracle;
import de.learnlib.experiments.Experiment.MealyExperiment;
import de.learnlib.oracles.SULOracle;

/**
 * General learning testing framework. The most important parameters are the input alphabet and the SUL (The
 * first two static attributes). Other settings can also be configured.
 */
public class WGC_Example {
	//*****************//
 	// SUL information //
	//*****************//
	// Defines the input alphabet, adapt for your socket (you can even use other types than string, if you 
	// change the generic-values, e.g. make your SUL of type SUL<Integer, Float> for int-input and float-output
	private static final Alphabet<String> inputAlphabet = new SimpleAlphabet<String>(ImmutableSet.of("cabbage?", "goat?", "nothing?", "wolf?"));	


	//*******************//
	//*******************//
	// the learning and testing algorithms. LStar is the basic algorithm, TTT performs much faster
	// but is a bit more inaccurate and produces more intermediate hypotheses, so test well)
	private static final LearningMethod learningAlgorithm = LearningMethod.LStar;
	public enum LearningMethod { LStar, RivestSchapire, TTT, KearnsVazirani }
	// Random walk is the simplest, but performs badly on large models: the chance of hitting a
	// erroneous long trace is very small
	private static final TestingMethod testMethod = TestingMethod.WMethod;
	public enum TestingMethod { RandomWalk, WMethod, WpMethod }
	// for random walk, the chance to do a reset after an input and the number of
	// inputs to test before accepting a hypothesis
	private static final double chanceOfResetting = 0.1; 
	private static final int numberOfSymbols = 100;
	
	public static void main(String [] args) throws IOException {

		// Define the System Under Learning.
		SUL<String,String> sul = new WGCSUL();

		// Most testing/learning-algorithms want a membership-oracle instead of a SUL directly in order to optimize system queries.
        final MembershipOracle<String, Word<String>> sulOracle = new SULOracle<>(sul);

		 		
        // Choosing the EQ oracle
		EquivalenceOracle<MealyMachine<?, String, ?, String>, String, Word<String>> eqOracle = null;           
		switch (testMethod){
			case RandomWalk:
				eqOracle = new RandomWalkEQOracle<>(chanceOfResetting, numberOfSymbols, true, new Random(123456l), sul);
				break;
			// Other methods are somewhat smarter than random testing: state coverage, trying to distinguish states, etc.
			case WMethod:
				eqOracle = new WMethodEQOracle.MealyWMethodEQOracle<>(3, sulOracle);
				break;
			case WpMethod:
				eqOracle = new WpMethodEQOracle.MealyWpMethodEQOracle<>(3, sulOracle);
				break;
			default:
				throw new RuntimeException("No test oracle selected!");
		}

		// Choosing a learning algorithm
		LearningAlgorithm<MealyMachine<?, String, ?, String>, String, Word<String>> learner = null;
		switch (learningAlgorithm){
			case LStar:
				learner = new ExtensibleLStarMealy<>(inputAlphabet, sulOracle, Lists.<Word<String>>newArrayList(), ObservationTableCEXHandlers.CLASSIC_LSTAR, ClosingStrategies.CLOSE_SHORTEST);
				break;
			case RivestSchapire:
				learner = new ExtensibleLStarMealy<>(inputAlphabet, sulOracle, Lists.<Word<String>>newArrayList(), ObservationTableCEXHandlers.RIVEST_SCHAPIRE, ClosingStrategies.CLOSE_SHORTEST);
				break;
			default:
				throw new RuntimeException("No learner selected");
		}
		
		
		// Setup and run the experiment.
		MealyExperiment<String, String> experiment = new MealyExperiment<String, String>(learner, eqOracle, inputAlphabet);
		experiment.run();

        // report results
        System.out.println("-------------------------------------------------------");
        
		// Output the results as ETF and GraphDOT 
		ETF.writeRaw4(experiment.getFinalHypothesis(), experiment.getFinalHypothesis().transitionGraphView(inputAlphabet), System.out);

		Writer w = DOT.createDotWriter(true);
	    GraphDOT.write(experiment.getFinalHypothesis(), inputAlphabet, w);
	    w.close();
	}
	

}

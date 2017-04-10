package net.automatalib.serialization.fsm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

public class FSM {
    
    public static CompactMealy<String, String> parseMealy(File f) throws FileNotFoundException {
        
        final Scanner s = new Scanner(new FileReader(f));

        /* Keep track of which part in the FSM we are:        
         *  0: State definition
         *  1: State vectors
         *  2: Transitions between states
         * We are only interested in part 3. */
        int currentPart = 0;
        
        Set<String> inputs = new HashSet<>();
        Set<Transition> transitions = new HashSet<>();
        Set<String> states = new HashSet<>();        
        while (s.hasNextLine()) {
            final String line = s.nextLine();
            
            if (line.equals("---")) currentPart++;
            else if (currentPart == 2) {
                final String[] split = line.split(" ");  
   
                	/*
                	 * If the split has length three, we assume one label per edge and merge two edges/lines.
                	 * Otherwise we obtain the transition from one edge/label.
                	 */
                	if(split.length == 3){
                		String line2 = s.nextLine();
                		String[] split2 = line2.split(" ");  
                		final Transition t = new Transition(split[0], split2[1], split[2], split2[2]);
                        inputs.add(t.getInput());
                        states.add(t.getSource());
                        states.add(t.getTarget());
                        transitions.add(t);
                	} else {
                		final Transition t = new Transition(split[0], split[1], split[2], split[3]);
                        inputs.add(t.getInput());
                        states.add(t.getSource());
                        states.add(t.getTarget());
                        transitions.add(t);
                	}
                
            }
        }

        
        final Alphabet<String> a = Alphabets.fromCollection(inputs);
        
        CompactMealy<String, String> cm = new CompactMealy(a, states.size());
        
        Map<String, Integer> stateMap = new HashMap<>();
        for (String state : states) stateMap.put(state, cm.addState());
        
        cm.setInitial(stateMap.get("1"), true);
        
        for (Transition t : transitions) {
            cm.addTransition(stateMap.get(t.getSource()), t.getInput(), stateMap.get(t.getTarget()), t.getOutput());
        }
        
        s.close();
        
        return cm;
    }
    
    @EqualsAndHashCode
    public static class Transition {
        
        @Getter private final String source;
        @Getter private final String target;
        @Getter private final String input;
        @Getter private final String output;
        
        public Transition(final String source, final String target,
            final String input, final String output) {
            this.source = source;
            this.target = target;
            this.input = input.substring(1, input.length() - 1);
            this.output = output.substring(1, output.length() - 1);
        }
    }
}

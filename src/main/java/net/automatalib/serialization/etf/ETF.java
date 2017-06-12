package net.automatalib.serialization.etf;

import java.io.Flushable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.TransitionOutputAutomaton;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.UndirectedGraph;
import net.automatalib.graphs.dot.AggregateDOTHelper;
import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;

public class ETF {




	public static <I,O> void export(MealyMachine<?,I,?,O> machine, Collection<I> inputs, Appendable a, Collection<O> skipOutputs, boolean singleLabelEdge){

		try {
			writeRaw4(machine, machine.transitionGraphView(inputs), a, skipOutputs, singleLabelEdge);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static <I,O> void export(MealyMachine<?,I,?,O> machine, Collection<I> inputs, Appendable a, Collection<O> skipOutputs){

		try {
			writeRaw4(machine, machine.transitionGraphView(inputs), a, skipOutputs, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static <I> void export(MealyMachine machine, Collection<? extends I> inputs, Appendable a, boolean singleLabelEdge ){

		try {
			writeRaw4(machine, machine.transitionGraphView(inputs), a, null, singleLabelEdge);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static <I,O> void export(MealyMachine<?,I,?,O> machine, Collection<I> inputs, Appendable a){

		try {
			writeRaw4(machine, machine.transitionGraphView(inputs), a, null, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	//--------------------------------------------------------------------------------------

	public static <N,E,O,T> void writeRaw4(TransitionOutputAutomaton<?,?,T,O> automaton, Graph<N, E> graph,
			Appendable a, Collection<O> skipOutputs, boolean singleLabelEdge) throws IOException {

		GraphDOTHelper<N, E> dotHelper = new DefaultDOTHelper<>();

		boolean directed = true;
		if(graph instanceof UndirectedGraph)
			directed = false;


		Map<String,String> props = new HashMap<>();


		MutableMapping<N,String> nodeNames = graph.createStaticNodeMapping();


		Set inputs = new HashSet<>();
		Set outputs = new HashSet<>();


		int i = 0;

		for(N node : graph) {
			if(!dotHelper.getNodeProperties(node, props))
				continue;
			String id = i++ +"";
			nodeNames.put(node, id);
		}

		Set<ETFEdge> edges = new HashSet<>();

		for(N node : graph) {
			String srcId = nodeNames.get(node);
			if(srcId == null)
				continue;
			Collection<? extends E> outEdges = graph.getOutgoingEdges(node);
			if(outEdges.isEmpty())
				continue;
			for(E e : outEdges) {
				N tgt = graph.getTarget(e);
				String tgtId = nodeNames.get(tgt);
				if(tgtId == null)
					continue;

				if(!directed && tgtId.compareTo(srcId) < 0)
					continue;

				props.clear();
				if(!dotHelper.getEdgeProperties(node, e, tgt, props))
					continue;


				TransitionEdge edge = (TransitionEdge) e;
				String input = String.valueOf(edge.getInput());
				O output = automaton.getTransitionOutput((T) edge.getTransition());
				if (skipOutputs == null || !skipOutputs.contains(output)) {
                    inputs.add(input);
                    outputs.add(output.toString());

                    ETFEdge etfEdge = new ETFEdge(srcId, tgtId, input, output.toString());
                    edges.add(etfEdge);
                }
			}
		}

		List<String> inputList = new ArrayList<>();
		inputList.addAll(inputs);

		List<String> outputList = new ArrayList();
		outputList.addAll(outputs);

//		System.out.println("-------------------------------------------------------");

		boolean doubleEdge = !singleLabelEdge;
		if(doubleEdge){
			appendDoubleEdgeLabel(graph.size(), edges, inputList, outputList, a);
		} else {
			appendSingleEdgeLabel(graph.size(), edges, inputList, outputList, a);
		}


		if (a instanceof Flushable) {
			((Flushable) a).flush();
		}
	}

	/*
	 * Produces an ETF model with input and output label on the same edge.
	 */
	public static void appendDoubleEdgeLabel(int nodes, Set<ETFEdge> edges, List<String> inputList, List<String> outputList, Appendable a) throws IOException{
		a.append("begin state\nlabel:label\nend state\n");
		a.append("begin edge\ninput:input output:output\nend edge\n");
		a.append("begin init\n0\nend init\n");


		a.append("begin trans"); a.append("\n");
		for (ETFEdge edge : edges) {
			a.append(edge.getSrc()+"/" +edge.getTgt()+" "+inputList.indexOf(edge.getInput())+" "+outputList.indexOf(edge.getOutput()));
		    a.append("\n");
		}
		a.append("end trans");a.append("\n");

		a.append("begin sort label");a.append("\n");
		for (int s = 0; s < nodes; s++) {
            a.append("\""+s+"\"");a.append("\n");
        }
		a.append("end sort");a.append("\n");

		a.append("begin sort input");a.append("\n");
		for (String string : inputList) {
			a.append("\""+string+"\"");a.append("\n");
		}
		a.append("end sort");a.append("\n");

		a.append("begin sort output");a.append("\n");
		for (String string : outputList){
			a.append("\""+string+"\"");a.append("\n");
		}
		a.append("end sort");a.append("\n");
	}

	/*
	 * Produces an ETF model with seperate edges for input and output labels
	 */
	public static void appendSingleEdgeLabel(int nodes, Set<ETFEdge> edges, List<String> inputList, List<String> outputList, Appendable a) throws IOException{
		a.append("begin state\nlabel:label\nend state\n");
		a.append("begin edge\nvalue:value\nend edge\n");
		a.append("begin init\n0\nend init\n");



		a.append("begin trans"); a.append("\n");
		for (ETFEdge edge : edges) {

			a.append(edge.getSrc()+"/" +nodes+" "+inputList.indexOf(edge.getInput()));
		    a.append("\n");
		    a.append(nodes+"/" +edge.getTgt()+" "+(inputList.size()+outputList.indexOf(edge.getOutput())));
		    a.append("\n");
		    nodes++;
		}
		a.append("end trans");a.append("\n");

		a.append("begin sort label");a.append("\n");
		for (int s = 0; s < nodes; s++) {
            a.append("\""+s+"\"");a.append("\n");
        }
		a.append("end sort");a.append("\n");

		a.append("begin sort value");a.append("\n");
		for (String string : inputList) {
			a.append("\""+string+"\"");a.append("\n");
		}
		for (String string : outputList){
			a.append("\""+string+"\"");a.append("\n");
		}
		a.append("end sort");a.append("\n");

	}

}

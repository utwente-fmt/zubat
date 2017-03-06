package nl.utwente.fmt.etfexporter.wgc;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;
import nl.utwente.fmt.etfexporter.wgc.model.Model;
import nl.utwente.fmt.etfexporter.wgc.model.Model.Move;
import nl.utwente.fmt.etfexporter.wgc.model.Model.Result;

public class WGCSUL implements SUL<String, String> {    
    
    public static final boolean[] INITIAL = { Model.LEFT, Model.LEFT, Model.LEFT, Model.LEFT };
    
    private final Model model = new Model(INITIAL, 0);
    
    long resets = 0;
	
    public long getResets() {
        return resets;
    }
    
	@Override
	public void pre() {
		model.reset();
        resets++;
	}
	
	@Override
	public void post() {
	}

	@Override
	public String step(String input) throws SULException {
        Move move;
        switch (input) {
            case "cabbage?":
                move = Move.CABBAGE;
                break;
            case "goat?":
                move = Move.GOAT;
                break;
            case "nothing?":
                move = Move.NOTHING;
                break;
            case "wolf?":
                move = Move.WOLF;
                break;
            default:
                throw new SULException(new IllegalArgumentException("invalid input"));
        }
                        
        Result result = model.doTransition(move);
        
        switch(result) {
            case FINISHED:
            case EATEN:
                model.reset();
        }
        
        String res = null;
        
        switch (result) {
            case EATEN:
                res = "eaten!";
                break;
            case FINISHED:
                res = "finished!";
                break;
            case INIT:
                res = "init!";
                break;
            case NORMALMOVE:
                res = "ok!";
                break;
            case RETRY:
                res = "retry!";
                break;
            default:
                throw new SULException(new IllegalArgumentException("invalid output"));
        }
                
        return res;
	}
}

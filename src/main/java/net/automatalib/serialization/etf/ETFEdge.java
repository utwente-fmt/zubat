package net.automatalib.serialization.etf;

public class ETFEdge {

	private String src;
	private String tgt;
	private String input;
	private String output;

	public ETFEdge(String src, String tgt, String input, String output) {
		super();
		this.src = src;
		this.tgt = tgt;
		this.input = input;
		this.output = output;
	}

	public String getSrc() {
		return src;
	}

	public String getTgt() {
		return tgt;
	}

	public String getInput() {
		return input;
	}

	public String getOutput() {
		return output;
	}

}

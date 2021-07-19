package io.github.bananapuncher714.cartographer.core.api.command;

public class SplitCommand {
	protected String[] input;
	protected String[] arguments;
	
	public SplitCommand( String[] input, String[] arguments ) {
		this.input = input;
		this.arguments = arguments;
	}

	public String[] getInput() {
		return input;
	}

	public void setInput( String[] input ) {
		this.input = input;
	}

	public String[] getArguments() {
		return arguments;
	}

	public void setArguments( String[] arguments ) {
		this.arguments = arguments;
	}
}

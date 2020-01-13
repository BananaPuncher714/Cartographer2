package io.github.bananapuncher714.cartographer.core.api.command.validator;

import java.util.Collection;

public class InputValidatorInt implements InputValidator< Integer > {
	protected int min = Integer.MIN_VALUE;
	protected int max = Integer.MAX_VALUE;
	
	public InputValidatorInt() {
	}
	
	public InputValidatorInt( int min, int max ) {
		this.min = min;
		this.max = max;
	}
	
	@Override
	public Collection< String > getTabCompletes() {
		return null;
	}

	@Override
	public boolean isValid( String input, String[] args ) {
		try {
			int i = Integer.valueOf( input );
			return i >= min && i <= max;
		} catch ( Exception exception ) {
			return false;
		}
	}

	@Override
	public Integer get( String input ) {
		return Integer.parseInt( input );
	}
}

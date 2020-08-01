package io.github.bananapuncher714.cartographer.core.api.command.validator;

import java.util.Collection;

public class InputValidatorPattern implements InputValidator< String > {
	protected String pattern;
	
	public InputValidatorPattern( String pattern ) {
		this.pattern = pattern;
	}
	
	@Override
	public Collection< String > getTabCompletes() {
		return null;
	}

	@Override
	public boolean isValid( String input, String[] args ) {
		return input.matches( pattern );
	}

	@Override
	public String get( String input ) {
		return input;
	}
}

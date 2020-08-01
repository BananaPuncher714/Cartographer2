package io.github.bananapuncher714.cartographer.core.command.validator;

import java.util.Collection;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator;

public class InputValidatorCreateMinimap implements InputValidator< String > {
	protected Cartographer plugin;
	
	public InputValidatorCreateMinimap( Cartographer plugin ) {
		this.plugin = plugin;
	}
	
	@Override
	public Collection< String > getTabCompletes() {
		return null;
	}

	@Override
	public boolean isValid( String input, String[] args ) {
		return !plugin.getMapManager().getMinimaps().containsKey( input );
	}

	@Override
	public String get( String input ) {
		return input;
	}

}

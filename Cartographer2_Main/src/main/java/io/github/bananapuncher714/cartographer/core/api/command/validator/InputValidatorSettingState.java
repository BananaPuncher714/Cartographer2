package io.github.bananapuncher714.cartographer.core.api.command.validator;

import java.util.Collection;

import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;

public class InputValidatorSettingState implements InputValidator< String > {
	private SettingState< ? > state;
	
	public InputValidatorSettingState( SettingState< ? > state ) {
		this.state = state;
	}
	
	@Override
	public Collection< String > getTabCompletes() {
		return state.getValues();
	}

	@Override
	public boolean isValid( String input, String[] args ) {
		return state.getFrom( input ).isPresent();
	}

	@Override
	public String get( String input ) {
		return input;
	}
}

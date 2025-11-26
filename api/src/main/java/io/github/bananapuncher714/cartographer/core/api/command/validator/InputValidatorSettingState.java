package io.github.bananapuncher714.cartographer.core.api.command.validator;

import java.util.Collection;

import org.bukkit.command.CommandSender;

import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;

public class InputValidatorSettingState implements InputValidator< String > {
	private SettingState< ? > state;
	
	public InputValidatorSettingState( SettingState< ? > state ) {
		this.state = state;
	}
	
	@Override
	public Collection< String > getTabCompletes( CommandSender sender, String[] input ) {
		return state.getValues();
	}

	@Override
	public boolean isValid( CommandSender sender, String[] input, String[] args ) {
		return state.getFrom( input[ 0 ] ).isPresent();
	}

	@Override
	public String get( CommandSender sender, String[] input ) {
		return input[ 0 ];
	}
}

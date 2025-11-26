package io.github.bananapuncher714.cartographer.core.api.command.validator;

import java.util.Collection;

import org.bukkit.command.CommandSender;

public class InputValidatorPattern implements InputValidator< String > {
	protected String pattern;
	
	public InputValidatorPattern( String pattern ) {
		this.pattern = pattern;
	}
	
	@Override
	public Collection< String > getTabCompletes( CommandSender sender, String[] input ) {
		return null;
	}

	@Override
	public boolean isValid( CommandSender sender, String input[], String[] args ) {
		return input[ 0 ].matches( pattern );
	}

	@Override
	public String get( CommandSender sender, String input[] ) {
		return input[ 0 ];
	}
}

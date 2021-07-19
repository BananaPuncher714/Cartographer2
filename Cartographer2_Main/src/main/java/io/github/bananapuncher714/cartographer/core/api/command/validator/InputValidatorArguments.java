package io.github.bananapuncher714.cartographer.core.api.command.validator;

import java.util.Collection;

import org.bukkit.command.CommandSender;

public class InputValidatorArguments implements InputValidator< Void > {
	protected int min;
	protected int max;
	
	public InputValidatorArguments( int amount ) {
		this.min = amount;
		this.max = amount;
	}
	
	public InputValidatorArguments( int min, int max ) {
		this.min = min;
		this.max = max;
	}
	
	@Override
	public Collection< String > getTabCompletes( CommandSender sender, String[] input ) {
		return null;
	}

	@Override
	public boolean isValid( CommandSender sender, String input[], String[] args ) {
		int len = args.length;
		return len >= min && len <= max;
	}

	@Override
	public Void get( CommandSender sender, String input[] ) {
		return null;
	}

}

package io.github.bananapuncher714.cartographer.core.api.command.validator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

public class InputValidatorString implements InputValidator< String > {
	protected Set< String > values = new HashSet< String >();
	
	public InputValidatorString( String value ) {
		values.add( value.toLowerCase() );
	}

	public InputValidatorString( String... values ) {
		Validate.isTrue( values.length > 0, "Must provide at least 1 argument!" );
		for ( String str : values ) {
			this.values.add( str.toLowerCase() );
		}
	}
	
	@Override
	public Collection< String > getTabCompletes( CommandSender sender, String[] input ) {
		return values;
	}

	@Override
	public boolean isValid( CommandSender sender, String input[], String[] args ) {
		return values.contains( input[ 0 ].toLowerCase() );
	}

	@Override
	public String get( CommandSender sender, String input[] ) {
		return input[ 0 ];
	}
}

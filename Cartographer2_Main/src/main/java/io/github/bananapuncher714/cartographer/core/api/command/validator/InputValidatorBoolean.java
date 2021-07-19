package io.github.bananapuncher714.cartographer.core.api.command.validator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.CommandSender;

public class InputValidatorBoolean implements InputValidator< Boolean > {
	protected Set< String > trueVals = new HashSet< String >();
	protected Set< String > falseVals = new HashSet< String >();

	public InputValidatorBoolean( String[] trueVal, String[] falseVal ) {
		for ( String str : trueVal ) {
			trueVals.add( str.toLowerCase() );
		}
		
		for ( String str : falseVal ) {
			falseVals.add( str.toLowerCase() );
		}
	}
	
	public InputValidatorBoolean() {
		trueVals.add( "true" );
		falseVals.add( "false" );
	}
	
	@Override
	public Collection< String > getTabCompletes( CommandSender sender, String[] input ) {
		Set< String > combined = new HashSet< String >( trueVals );
		combined.addAll( falseVals );
		return combined;
	}

	@Override
	public boolean isValid( CommandSender sender, String input[], String[] args ) {
		String lowercase = input[ 0 ].toLowerCase();
		return trueVals.contains( lowercase ) || falseVals.contains( lowercase );
	}

	@Override
	public Boolean get( CommandSender sender, String input[] ) {
		String lowercase = input[ 0 ].toLowerCase();
		return trueVals.contains( lowercase );
	}

}

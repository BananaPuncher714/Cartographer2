package io.github.bananapuncher714.cartographer.core.api.command.validator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.CommandSender;

import io.github.bananapuncher714.cartographer.core.api.BooleanOption;

public class InputValidatorBooleanOption implements InputValidator< BooleanOption > {
	protected Set< String > trueVals = new HashSet< String >();
	protected Set< String > falseVals = new HashSet< String >();
	protected Set< String > unsetVals = new HashSet< String >();

	public InputValidatorBooleanOption( String[] trueVal, String[] falseVal, String[] unsetVal ) {
		for ( String str : trueVal ) {
			trueVals.add( str.toLowerCase() );
		}
		
		for ( String str : falseVal ) {
			falseVals.add( str.toLowerCase() );
		}
		
		for ( String str : unsetVal ) {
			unsetVals.add( str.toLowerCase() );
		}
	}
	
	public InputValidatorBooleanOption() {
		trueVals.add( "true" );
		falseVals.add( "false" );
		unsetVals.add( "unset" );
	}
	
	@Override
	public Collection< String > getTabCompletes( CommandSender sender, String[] input ) {
		Set< String > combined = new HashSet< String >( trueVals );
		combined.addAll( falseVals );
		combined.addAll( unsetVals );
		return combined;
	}

	@Override
	public boolean isValid( CommandSender sender, String[] input, String[] args ) {
		String option = input[ 0 ].toLowerCase();
		return trueVals.contains( option ) || falseVals.contains( option ) || unsetVals.contains( option );
	}

	@Override
	public BooleanOption get( CommandSender sender, String[] input ) {
		String option = input[ 0 ].toLowerCase();
		if ( trueVals.contains( option ) ) {
			return BooleanOption.TRUE;
		} else if ( unsetVals.contains( option ) ) {
			return BooleanOption.UNSET;
		}
		return BooleanOption.FALSE;
	}

}

package io.github.bananapuncher714.cartographer.core.api.command.validator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
	public Collection< String > getTabCompletes() {
		Set< String > combined = new HashSet< String >( trueVals );
		combined.addAll( falseVals );
		combined.addAll( unsetVals );
		return combined;
	}

	@Override
	public boolean isValid( String input, String[] args ) {
		input = input.toLowerCase();
		return trueVals.contains( input ) || falseVals.contains( input ) || unsetVals.contains( input );
	}

	@Override
	public BooleanOption get( String input ) {
		input = input.toLowerCase();
		if ( trueVals.contains( input ) ) {
			return BooleanOption.TRUE;
		} else if ( unsetVals.contains( input ) ) {
			return BooleanOption.UNSET;
		}
		return BooleanOption.FALSE;
	}

}

package io.github.bananapuncher714.cartographer.core.api.command.validator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
	public Collection< String > getTabCompletes() {
		Set< String > combined = new HashSet< String >( trueVals );
		combined.addAll( falseVals );
		return combined;
	}

	@Override
	public boolean isValid( String input, String[] args ) {
		input = input.toLowerCase();
		return trueVals.contains( input ) || falseVals.contains( input );
	}

	@Override
	public Boolean get( String input ) {
		input = input.toLowerCase();
		return trueVals.contains( input );
	}

}

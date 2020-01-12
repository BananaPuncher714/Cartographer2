package io.github.bananapuncher714.cartographer.core.api.command.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InputValidatorChain< T > implements InputValidator< T > {
	protected InputValidator< T > primaryValidator;
	protected List< InputValidator< ? > > validators = new ArrayList< InputValidator< ? > >();
	
	public InputValidatorChain( InputValidator< T > primaryValidator ) {
		this.primaryValidator = primaryValidator;
	}
	
	public InputValidatorChain< T > addValidator( InputValidator< ? > validator ) {
		validators.add( validator );
		return this;
	}
	
	@Override
	public Collection< String > getTabCompletes() {
		return primaryValidator.getTabCompletes();
	}

	@Override
	public boolean isValid( String input, String[] args ) {
		if ( !primaryValidator.isValid( input, args ) ) {
			return false;
		}
		for ( InputValidator< ? > validator : validators ) {
			if ( !validator.isValid( input, args ) ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public T get( String input ) {
		return primaryValidator.get( input );
	}
}

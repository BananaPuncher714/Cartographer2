package io.github.bananapuncher714.cartographer.core.api.command.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.command.CommandSender;

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
	public Collection< String > getTabCompletes( CommandSender sender, String[] input ) {
		return primaryValidator.getTabCompletes( sender, input );
	}

	@Override
	public boolean isValid( CommandSender sender, String input[], String[] args ) {
		if ( !primaryValidator.isValid( sender, input, args ) ) {
			return false;
		}
		for ( InputValidator< ? > validator : validators ) {
			if ( !validator.isValid( sender, input, args ) ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public T get( CommandSender sender, String input[] ) {
		return primaryValidator.get( sender, input );
	}
}

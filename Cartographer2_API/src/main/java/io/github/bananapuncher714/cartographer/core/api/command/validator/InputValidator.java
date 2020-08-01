package io.github.bananapuncher714.cartographer.core.api.command.validator;

import java.util.Collection;

public interface InputValidator< T > {
	Collection< String > getTabCompletes();
	boolean isValid( String input, String[] args );
	T get( String input );
}

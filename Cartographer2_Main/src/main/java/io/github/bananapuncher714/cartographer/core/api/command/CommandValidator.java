package io.github.bananapuncher714.cartographer.core.api.command;

import java.util.Collection;

public interface CommandValidator {
	Collection< String > getTabCompletes();
	boolean isValid( String input );
}

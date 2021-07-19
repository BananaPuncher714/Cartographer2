package io.github.bananapuncher714.cartographer.core.api.command.validator;

import java.util.Collection;

import org.bukkit.command.CommandSender;

public interface InputValidator< T > {
	Collection< String > getTabCompletes( CommandSender sender, String[] input );
	boolean isValid( CommandSender sender, String[] input, String[] args );
	T get( CommandSender sender, String[] input );
	default int getArgumentCount() {
		return 1;
	}
}

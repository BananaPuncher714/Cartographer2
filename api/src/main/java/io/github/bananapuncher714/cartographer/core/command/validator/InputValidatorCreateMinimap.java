package io.github.bananapuncher714.cartographer.core.command.validator;

import java.util.Collection;

import org.bukkit.command.CommandSender;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator;

public class InputValidatorCreateMinimap implements InputValidator< String > {
	protected Cartographer plugin;
	
	public InputValidatorCreateMinimap( Cartographer plugin ) {
		this.plugin = plugin;
	}
	
	@Override
	public Collection< String > getTabCompletes( CommandSender sender, String[] input ) {
		return null;
	}

	@Override
	public boolean isValid( CommandSender sender, String[] input, String[] args ) {
		return !plugin.getMapManager().getMinimaps().containsKey( input[ 0 ] );
	}

	@Override
	public String get( CommandSender sender, String[] input ) {
		return input[ 0 ];
	}

}

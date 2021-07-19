package io.github.bananapuncher714.cartographer.core.command.validator;

import java.util.Collection;

import org.bukkit.command.CommandSender;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidator;
import io.github.bananapuncher714.cartographer.core.map.Minimap;

public class InputValidatorMinimap implements InputValidator< Minimap > {
	protected Cartographer plugin;
	
	public InputValidatorMinimap( Cartographer plugin ) {
		this.plugin = plugin;
	}
	
	@Override
	public Collection< String > getTabCompletes( CommandSender sender, String[] input ) {
		return plugin.getMapManager().getMinimaps().keySet();
	}

	@Override
	public boolean isValid( CommandSender sender, String[] input, String[] args ) {
		return plugin.getMapManager().getMinimaps().keySet().contains( input[ 0 ] );
	}

	@Override
	public Minimap get( CommandSender sender, String[] input ) {
		return plugin.getMapManager().getMinimaps().get( input[ 0 ] );
	}
}

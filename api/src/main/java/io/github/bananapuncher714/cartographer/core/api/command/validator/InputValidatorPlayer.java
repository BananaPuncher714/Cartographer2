package io.github.bananapuncher714.cartographer.core.api.command.validator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InputValidatorPlayer implements InputValidator< Player > {
	@Override
	public Collection< String > getTabCompletes( CommandSender sender, String[] input ) {
		Set< String > playerNames = new HashSet< String >();
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			playerNames.add( player.getName() );
		}
		return playerNames;
	}

	@Override
	public boolean isValid( CommandSender sender, String input[], String[] args ) {
		return Bukkit.getPlayerExact( input[ 0 ] ) != null;
	}

	@Override
	public Player get( CommandSender sender, String input[] ) {
		return Bukkit.getPlayerExact( input[ 0 ] );
	}

}

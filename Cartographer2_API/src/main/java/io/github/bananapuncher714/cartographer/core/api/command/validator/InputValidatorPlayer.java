package io.github.bananapuncher714.cartographer.core.api.command.validator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class InputValidatorPlayer implements InputValidator< Player > {
	@Override
	public Collection< String > getTabCompletes() {
		Set< String > playerNames = new HashSet< String >();
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			playerNames.add( player.getName() );
		}
		return playerNames;
	}

	@Override
	public boolean isValid( String input, String[] args ) {
		return Bukkit.getPlayerExact( input ) != null;
	}

	@Override
	public Player get( String input ) {
		return Bukkit.getPlayerExact( input );
	}

}

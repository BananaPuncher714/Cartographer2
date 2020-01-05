package io.github.bananapuncher714.cartographer.core.api;

import org.bukkit.entity.Player;

/**
 * Takes in a player and whatever else may be required.
 * 
 * @author BananaPuncher714
 */
public interface PlayerRunnable {
	/**
	 * Execute with the player and optional objects.
	 * 
	 * @param player
	 * The player.
	 * @param objects
	 * Optional objects.
	 * @return
	 * Whether or not it was successful.
	 */
	public boolean execute( Player player, Object... objects );
}

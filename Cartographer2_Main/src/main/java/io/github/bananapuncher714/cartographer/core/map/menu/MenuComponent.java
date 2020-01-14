package io.github.bananapuncher714.cartographer.core.map.menu;

import org.bukkit.entity.Player;

public interface MenuComponent {
	
	Frame getFrame();
	
	boolean isDirty();
	
	/**
	 * Called when a player is viewing a menu.
	 * 
	 * @param Player
	 * The player viewing the menu.
	 * @param x
	 * The X coordinate of the cursor relative to the top left of this icon, from 0 to 255.
	 * @param y
	 * The Y coordinate of the cursor relative to the top left of this icon, from 0 to 255.
	 * @return
	 * If this menu should be closed.
	 */
	boolean onView( Player player, double x, double y );
	
	/**
	 * Called when a player interacts with a menu, either from pressing Q, or CTRL+Q.
	 * 
	 * @param player
	 * The player viewing the menu.
	 * @param x
	 * The X coordinate of the cursor relative to the top left of this icon, from 0 to 255.
	 * @param y
	 * The Y coordinate of the cursor relative to the top left of this icon, from 0 to 255.
	 * @param main
	 * Whether or not the interaction was by pressing Q.
	 * @return
	 * If this menu should be closed.
	 */
	boolean onInteract( Player player, double x, double y, MapInteraction interaction );
}

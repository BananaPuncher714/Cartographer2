/**
 * A wrapper used to identify a real world location, MapCursor type, and whether to hide when out of bounds.
 * 
 * @author BananaPuncher714
 */
package io.github.bananapuncher714.cartographer.core.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;

public class RealWorldCursor {
	private String name;
	private final Location l;
	MapCursor.Type type;
	boolean oob;
	private PlayerRunnable haction = null, aaction = null;

	/**
	 * Constructor for a RealWorldCursor
	 * 
	 * @param l
	 * The current location of whatever the cursor is tracking
	 * @param type
	 * The MapCursor type that will represent this RealWorldCursor
	 * @param hideWhenOOB
	 * Whether or not to hide this cursor when it is not on the map.
	 */
	public RealWorldCursor( String name, Location l, MapCursor.Type type, boolean hideWhenOOB ) {
		this.name = name;
		this.l = l;
		this.type = type;
		oob = hideWhenOOB;
	}

	/**
	 * Gets the location of the tracked location
	 * 
	 * @return
	 * The real world location of the location; Does not mean location on the map
	 */
	public Location getLocation() {
		return l;
	}

	/**
	 * Gets the MapCursor type of the cursor
	 * 
	 * @return
	 * Returns the type that will be displayed on the map
	 */
	public MapCursor.Type getType() {
		return type;
	}
	
	/**
	 * Out Of Bounds means when the cursor is not directly on the map.
	 * 
	 * @return
	 * Gets if this cursor should be hidden when out of bounds.
	 */
	public boolean hideWhenNotOnMap() {
		return oob;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setHoverAction( PlayerRunnable action ) {
		haction = action;
	}
	
	public void setActivateAction( PlayerRunnable action ) {
		aaction = action;
	}
	
	public boolean executeHover( Player player, Object... objects ) {
		return haction != null ? haction.run( player, objects ) : true;
	}
	
	public boolean executeActivate( Player player, Object... objects ) {
		return aaction != null ? aaction.run( player, objects ) : true;
	}
}
/**
 * A wrapper used to identify a real world location, MapCursor type, and whether to hide when out of bounds.
 * 
 * @author BananaPuncher714
 */
package io.github.bananapuncher714.cartographer.core.api;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.map.MapCursor.Type;

public class WorldCursor {
	protected String name;
	protected final Location location;
	protected Type type;
	protected boolean global;

	/**
	 * Constructor for a RealWorldCursor.
	 * 
	 * @param l
	 * The current location of whatever the cursor is tracking. Cannot be null.
	 * @param type
	 * The MapCursor type that will represent this RealWorldCursor. Cannot be null.
	 * @param global
	 * Whether or not to show this cursor if it is off-map.
	 */
	public WorldCursor( String name, Location l, Type type, boolean global ) {
		Validate.notNull( l );
		Validate.notNull( type );
		this.name = name;
		this.location = l;
		this.type = type;
		this.global = global;
	}
	
	/**
	 * Construct a WorldCursor with the arguments provided.
	 * 
	 * @param location
	 * The current location of whatever the cursor is tracking. Cannot be null.
	 */
	public WorldCursor( Location location ) {
		this( null, location, Type.WHITE_POINTER, true );
	}
	
	/**
	 * Construct a WorldCursor with the arguments provided.
	 * 
	 * @param location
	 * The current location of whatever the cursor is tracking. Cannot be null.
	 * @param type
	 * The MapCursor type that will represent this RealWorldCursor. Cannot be null.
	 */
	public WorldCursor( Location location, Type type ) {
		this( null, location, type, true );
	}
	
	/**
	 * Construct a WorldCursor with the arguments provided.
	 * 
	 * @param name
	 * Name of the cursor, can be null for none.
	 * @param location
	 * The current location of whatever the cursor is tracking. Cannot be null.
	 * @param type
	 * The MapCursor type that will represent this RealWorldCursor. Cannot be null.
	 */
	public WorldCursor( String name, Location location, Type type ) {
		this( name, location, type, true );
	}

	/**
	 * Get the location.
	 * 
	 * @return
	 * The mutable location of this cursor. Does not mean location on the map.
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Get the MapCursor type of the cursor.
	 * 
	 * @return
	 * The type that will be displayed on the map.
	 */
	public Type getType() {
		return type;
	}
	
	/**
	 * Set the MapCursor type of the cursor.
	 * 
	 * @param type
	 * The type that will be displayed on the map. Cannot be null.
	 */
	public void setType( Type type ) {
		Validate.notNull( type );
		this.type = type;
	}
	
	/**
	 * Get if the cursor is global.
	 * 
	 * @return
	 * If this cursor should be hidden when not on the map.
	 */
	public boolean isGlobal() {
		return global;
	}
	
	/**
	 * Set if the cursor is global.
	 */
	public void setGlobal( boolean hide ) {
		global = hide;
	}
	
	/**
	 * Get the name.
	 * 
	 * @return
	 * May be null indicating no name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name.
	 * 
	 * @param name
	 * Can be null for none.
	 */
	public void setName(String name) {
		this.name = name;
	}
}
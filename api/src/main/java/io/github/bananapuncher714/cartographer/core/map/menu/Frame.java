package io.github.bananapuncher714.cartographer.core.map.menu;

/**
 * Represents a portion of map data at a specific time
 * 
 * @author BananaPuncher714
 */
public class Frame {
	/**
	 * The time of creation
	 */
	public final long timestamp = System.currentTimeMillis();
	
	// Simple final variables
	public final int x;
	public final int y;
	public final int width;
	protected final int[] display;
	
	/**
	 * Create a new non-centered frame
	 * 
	 * @param x
	 * The top left X of the given frame, can be any number
	 * @param y
	 * The top left Y of the given frame, can be any number
	 * @param map
	 * The map data, in MinecraftColor bytes
	 * @param width
	 * The width of this frame
	 */
	public Frame( int x, int y, int[] map, int width ) {
		this.x = x;
		this.y = y;
		this.display = map;
		this.width = width;
	}
	
	/**
	 * Creates a new centered frame
	 * 
	 * @param map
	 * The map data, in integer color.
	 * @param width
	 * The width of this frame
	 */
	public Frame( int[] map, int width ) {
		this( 0, 0, map, width );
	}
	
	/**
	 * Gives a mutable array of bytes
	 * 
	 * @return
	 * Contains an integer color frame.
	 */
	public int[] getDisplay() {
		return display;
	}
}

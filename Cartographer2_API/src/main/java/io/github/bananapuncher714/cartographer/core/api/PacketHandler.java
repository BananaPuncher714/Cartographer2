package io.github.bananapuncher714.cartographer.core.api;

import java.util.UUID;

import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursor.Type;

import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;

/**
 * Handle NMS methods and packet manipulation.
 * 
 * @author BananaPuncher714
 */
public interface PacketHandler {
	/**
	 * Called before a packet gets sent from server to player.
	 * 
	 * @param player
	 * The player receiving the packet.
	 * @param packet
	 * The packet being sent.
	 * @return
	 * The packet that should be sent, null indicates nothing should be sent.
	 */
	Object onPacketInterceptOut( Player player, Object packet );
	
	/**
	 * Called before a packet gets sent from player to server.
	 * 
	 * @param player
	 * The player sending the packet.
	 * @param packet
	 * The packet being received.
	 * @return
	 * The packet that should be received, null indicates nothing should be received.
	 */
	Object onPacketInterceptIn( Player player, Object packet );
	
	/**
	 * Check if the id provided is a Cartographer2 map.
	 * 
	 * @param id
	 * The id of a Bukkit MapView.
	 * @return
	 * If the MapView with the given id is currently being used for Cartographer2.
	 */
	boolean isMapRegistered( int id );
	
	/**
	 * Unregister a MapView with Cartographer2.
	 * 
	 * @param id
	 * The id of a Bukkit MapView.
	 */
	void unregisterMap( int id );
	
	/**
	 * Register a MapView with Cartographer2.
	 * 
	 * @param id
	 * The id of a Bukkit MapView
	 */
	void registerMap( int id );
	
	/**
	 * Send map data to the client.
	 * 
	 * @param id
	 * The id of the Bukkit MapView.
	 * @param data
	 * An array of data with size of 128 * 128.
	 * @param cursors
	 * An array of MapCursors on the map.
	 * @param uuids
	 * An array of the UUID of players receiving this data.
	 */
	void sendDataTo( int id, byte[] data, MapCursor[] cursors, UUID... uuids );
	
	/**
	 * Get the vanilla MinimapPalette, per version.
	 * 
	 * @return
	 * A new MinimapPalette constructed from NMS block and colors.
	 */
	MinimapPalette getVanillaPalette();
	
	/**
	 * Check if the Minecraft version contains freezing maps.
	 * 
	 * @return
	 * If the version is 1.10.2 or below.
	 */
	default boolean mapBug() {
		return false;
	}
	
	// More mundane methods
	/**
	 * Construct a MapCursor with the following params.
	 * 
	 * @param x
	 * The x position on the map, from -128 to 127.
	 * @param y
	 * The y position on the map, from -128 to 127.
	 * @param yaw
	 * The yaw of the cursor, in degrees.
	 * @param cursorType
	 * The type of the cursor. Cannot be null.
	 * @param name
	 * The name that will be displayed by the cursor, if applicable. Can be null.
	 * @return
	 * A Bukkit MapCursor.
	 */
	MapCursor constructMapCursor( int x, int y, double yaw, Type cursorType, String name );
	
	/**
	 * Get the current TPS of the Minecraft server.
	 * 
	 * @return
	 * May fluctuate highly and quickly, and go above 20.
	 */
	double getTPS();
	
	/**
	 * Register a command with the fallback prefix indicated.
	 * 
	 * @param fallbackPrefix
	 * The prefix to use if another command shares the same value.
	 * @param command
	 * Cannot be null.
	 * @return
	 * Whether the command was registered successfully.
	 */
	boolean registerCommand( String fallbackPrefix, PluginCommand command );
	
	/**
	 * Register a command with the default fallback prefix.
	 * 
	 * @param command
	 * Cannot be null. The fallback prefix will be the name of the plugin of the command.
	 * @return
	 * Whether the command was registered successfully.
	 */
	boolean registerCommand( PluginCommand command );
	
	/**
	 * Unregister a command.
	 * 
	 * @param command
	 * The command to unregister, cannot be null.
	 */
	void unregisterCommand( PluginCommand command );
	
	/**
	 * Get the {@link GeneralUtil} for this PacketHandler.
	 * 
	 * @return
	 * A version specific {@link GeneralUtil}.
	 */
	GeneralUtil getUtil();
}

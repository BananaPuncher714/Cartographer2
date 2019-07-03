package io.github.bananapuncher714.cartographer.core.api;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursor.Type;

public interface PacketHandler {
	Object onPacketInterceptOut( Player player, Object packet );
	Object onPacketInterceptIn( Player player, Object packet );
	boolean isMapRegistered( int id );
	void unregisterMap( int id );
	void registerMap( int id );
	void sendDataTo( int id, byte[] data, MapCursor[] cursors, UUID... uuids );
	
	// More mundane methods
	MapCursor constructMapCursor( int x, int y, double yaw, Type cursorType, String name );
	
	double getTPS();
	
	GeneralUtil getUtil();
}

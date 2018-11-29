package io.github.bananapuncher714.cartographer.core.api;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;

public interface PacketHandler {
	Object onPacketInterceptOut( Player player, Object packet );
	Object onPacketInterceptIn( Player player, Object packet );
	boolean isMapRegistered( int id );
	void unregisterMap( int id );
	void registerMap( int id );
	void sendDataTo( int id, byte[] data, MapCursor[] cursors, UUID... uuids );
}

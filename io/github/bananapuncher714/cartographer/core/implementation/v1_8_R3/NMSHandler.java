package io.github.bananapuncher714.cartographer.core.implementation.v1_8_R3;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursor.Type;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.GeneralUtil;
import io.github.bananapuncher714.cartographer.core.api.PacketHandler;
import io.github.bananapuncher714.cartographer.core.internal.Util_1_8;
import io.github.bananapuncher714.cartographer.core.util.MapUtil;
import io.github.bananapuncher714.cartographer.tinyprotocol.TinyProtocol;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.MapIcon;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayOutMap;

public class NMSHandler implements PacketHandler {
	private static Field[] MAP_FIELDS = new Field[ 8 ];
	
	static {
		try {
			MAP_FIELDS[ 0 ] = PacketPlayOutMap.class.getDeclaredField( "a" );
			MAP_FIELDS[ 1 ] = PacketPlayOutMap.class.getDeclaredField( "b" );
			MAP_FIELDS[ 2 ] = PacketPlayOutMap.class.getDeclaredField( "c" );
			MAP_FIELDS[ 3 ] = PacketPlayOutMap.class.getDeclaredField( "d" );
			MAP_FIELDS[ 4 ] = PacketPlayOutMap.class.getDeclaredField( "e" );
			MAP_FIELDS[ 5 ] = PacketPlayOutMap.class.getDeclaredField( "f" );
			MAP_FIELDS[ 6 ] = PacketPlayOutMap.class.getDeclaredField( "g" );
			MAP_FIELDS[ 7 ] = PacketPlayOutMap.class.getDeclaredField( "h" );

			for ( Field field : MAP_FIELDS ) {
				field.setAccessible( true );
			}
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}

	private final Set< Integer > maps = new TreeSet< Integer >();
	private Util_1_8 util = new Util_1_8();
	
	@Override
	public void sendDataTo( int id, byte[] data, @Nullable MapCursor[] cursors, UUID... uuids ) {
		MapIcon[] icons;
		if ( cursors == null ) {
			icons = new MapIcon[ 0 ];
		} else {
			icons = new MapIcon[ cursors.length ];
			
			for ( int index = 0; index < cursors.length; index++ ) {
				MapCursor cursor = cursors[ index ];
				
				icons[ index ] = new MapIcon( cursor.getType().getValue(), cursor.getX(), cursor.getY(), cursor.getDirection() );
			}
		}
		
		PacketPlayOutMap packet = new PacketPlayOutMap();
		
		try {
			MAP_FIELDS[ 0 ].set( packet, id );
			MAP_FIELDS[ 1 ].set( packet, ( byte ) 0 );
			MAP_FIELDS[ 2 ].set( packet, icons );
			MAP_FIELDS[ 3 ].set( packet, 0 );
			MAP_FIELDS[ 4 ].set( packet, 0 );
			MAP_FIELDS[ 5 ].set( packet, 128 );
			MAP_FIELDS[ 6 ].set( packet, 128 );
			MAP_FIELDS[ 7 ].set( packet, data );
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
		
		PacketPlayOutMinimap mapPacket = new PacketPlayOutMinimap( packet );
		
		TinyProtocol protocol = Cartographer.getInstance().getProtocol();
		for ( UUID uuid : uuids ) {
			if ( uuid != null ) {
				Channel channel = protocol.getChannel( uuid, null );
				if ( channel != null ) {
					protocol.sendPacket( channel, mapPacket );
				}
			}
		}
	}

	@Override
	public Object onPacketInterceptOut( Player viewer, Object packet ) {
		if ( packet instanceof PacketPlayOutMinimap ) {
			return ( ( PacketPlayOutMinimap ) packet ).packet;
		} else if ( packet instanceof PacketPlayOutMap ) {
			if ( packet.getClass().equals( PacketPlayOutMap.class ) ) {
				try {
					int id = MAP_FIELDS[ 0 ].getInt( packet );
					if ( maps.contains( id ) ) {
						return null;
					}
				} catch ( IllegalArgumentException | IllegalAccessException e ) {
					e.printStackTrace();
				}
			}
		}
		return packet;
	}
	
	@Override
	public Object onPacketInterceptIn( Player viewer, Object packet ) {
		return packet;
	}
	
	@Override
	public boolean isMapRegistered( int id ) {
		return maps.contains( id );
	}
	
	@Override
	public void registerMap( int id ) {
		maps.add( id );
	}
	
	@Override
	public void unregisterMap( int id ) {
		maps.remove( id );
	}
	
	@Override
	public boolean mapBug() {
		return true;
	}
	
	@Override
	public MapCursor constructMapCursor( int x, int y, double yaw, Type cursorType, String name ) {
		return new MapCursor( ( byte ) x, ( byte ) y, MapUtil.getDirection( yaw ), cursorType.getValue(), true );
	}
	
	@Override
	public double getTPS() {
		return MinecraftServer.getServer().recentTps[ 0 ];
	}
	
	@Override
	public boolean registerCommand( PluginCommand command ) {
		return registerCommand( command.getPlugin().getName(), command );
	}
	
	@Override
	public boolean registerCommand( String fallbackPrefix, PluginCommand command ) {
		return ( ( CraftServer ) Bukkit.getServer() ).getCommandMap().register( command.getPlugin().getName(), command );
	}
	
	@Override
	public GeneralUtil getUtil() {
		return util;
	}
	
	private class PacketPlayOutMinimap extends PacketPlayOutMap {
		protected final PacketPlayOutMap packet;
		
		protected PacketPlayOutMinimap( PacketPlayOutMap packet ) {
			this.packet = packet;
		}
	}
}

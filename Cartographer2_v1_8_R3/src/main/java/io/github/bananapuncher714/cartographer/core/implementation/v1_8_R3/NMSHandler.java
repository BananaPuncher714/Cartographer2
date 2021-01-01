package io.github.bananapuncher714.cartographer.core.implementation.v1_8_R3;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursor.Type;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.GeneralUtil;
import io.github.bananapuncher714.cartographer.core.api.PacketHandler;
import io.github.bananapuncher714.cartographer.core.internal.Util_1_8;
import io.github.bananapuncher714.cartographer.core.map.menu.MapInteraction;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;
import io.github.bananapuncher714.cartographer.core.util.MapUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.MapIcon;
import net.minecraft.server.v1_8_R3.MinecraftKey;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig.EnumPlayerDigType;
import net.minecraft.server.v1_8_R3.PacketPlayInSettings;
import net.minecraft.server.v1_8_R3.PacketPlayOutMap;
import net.minecraft.server.v1_8_R3.PlayerConnection;

public class NMSHandler implements PacketHandler {
	private static final AtomicInteger HANDLER_INDEX = new AtomicInteger();
	
	private static Field[] MAP_FIELDS = new Field[ 8 ];
	private static Field SIMPLECOMMANDMAP_COMMANDS;
	
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
			
			SIMPLECOMMANDMAP_COMMANDS = SimpleCommandMap.class.getDeclaredField( "knownCommands" );
			SIMPLECOMMANDMAP_COMMANDS.setAccessible( true );
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}

	private final Map< UUID, Channel > channels = new ConcurrentHashMap< UUID, Channel >();
	private final Set< Integer > maps = new TreeSet< Integer >();
	private Util_1_8 util = new Util_1_8();
	private final String handler_name;
	
	private final Set< PacketPlayOutMap > whitelisted = Collections.synchronizedSet( Collections.newSetFromMap( new WeakHashMap< PacketPlayOutMap, Boolean >() ) );
	
	public NMSHandler() {
		handler_name = "cartographer2_handler_" + HANDLER_INDEX.getAndIncrement();
	}
	
	@Override
	public void inject( Player player ) {
		PlayerConnection conn = ( ( CraftPlayer ) player ).getHandle().playerConnection;
		NetworkManager manager = conn.networkManager;
		Channel channel = manager.channel;
		
		if ( channel != null ) {
			channels.put( player.getUniqueId(), channel );
			if ( channel.pipeline().get( handler_name ) != null ) {
				channel.pipeline().remove( handler_name );
			}
			channel.pipeline().addBefore( "packet_handler", handler_name, new PacketInterceptor( player ) );
		}
	}
	
	@Override
	public void uninject( Player player ) {
		PlayerConnection conn = ( ( CraftPlayer ) player ).getHandle().playerConnection;
		NetworkManager manager = conn.networkManager;
		Channel channel = manager.channel;
		channels.remove( player.getUniqueId() );
		
		if ( channel != null ) {
			if ( channel.pipeline().get( handler_name ) != null ) {
				channel.pipeline().remove( handler_name );
			}
		}
	}
	
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
		
		whitelisted.add( packet );
		
		for ( UUID uuid : uuids ) {
			if ( uuid != null ) {
				Channel channel = channels.get( uuid );
				if ( channel != null ) {
					channel.pipeline().writeAndFlush( packet );
				}
			}
		}
	}

	private Object onPacketInterceptOut( Player viewer, Object packet ) {
		if ( packet instanceof PacketPlayOutMap && !whitelisted.contains( packet ) ) {
			try {
				int id = MAP_FIELDS[ 0 ].getInt( packet );
				if ( maps.contains( id ) ) {
					return null;
				}
			} catch ( IllegalArgumentException | IllegalAccessException e ) {
				e.printStackTrace();
			}
		}
		return packet;
	}
	
	private Object onPacketInterceptIn( Player viewer, Object packet ) {
		if ( viewer != null ) {
			if ( packet instanceof PacketPlayInBlockDig && Cartographer.getInstance().getSettings().isPreventDrop() && Cartographer.getInstance().getSettings().isUseDropPacket() ) {
				// Check for the drop packet
				PacketPlayInBlockDig digPacket = ( PacketPlayInBlockDig ) packet;
	
				EnumPlayerDigType type = digPacket.c();
				if ( type == EnumPlayerDigType.DROP_ALL_ITEMS || type == EnumPlayerDigType.DROP_ITEM ) {
					ItemStack item = viewer.getEquipment().getItemInHand();
					if ( Cartographer.getInstance().getMapManager().isMinimapItem( item ) ) {
						// Update the player's hand
						viewer.getEquipment().setItemInHand( item );
						
						// Activate the drop
						Cartographer.getInstance().getMapManager().activate( viewer, type == EnumPlayerDigType.DROP_ALL_ITEMS ? MapInteraction.CTRLQ : MapInteraction.Q );
						return null;
					}
				}
			} else if ( packet instanceof PacketPlayInSettings ) {
				PacketPlayInSettings settings = ( PacketPlayInSettings ) packet;
				Cartographer.getInstance().getPlayerManager().setLocale( viewer.getUniqueId(), settings.a() );
			}
		}
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
	public MinimapPalette getVanillaPalette() {
		MinimapPalette palette = new MinimapPalette();
		for ( MinecraftKey key : Block.REGISTRY.keySet() ) {
			Block block = Block.REGISTRY.get( key );
			for ( IBlockData data : block.P().a() ) {
				boolean transparent = !block.c();
				CrossVersionMaterial material = new CrossVersionMaterial( CraftMagicNumbers.getMaterial( block ), block.toLegacyData( data ) );
				
				if ( transparent ) {
					palette.addTransparentMaterial( material );
				} else {
					int color = block.g( data ).L;
					palette.setColor( material, color );
				}
			}
		}
		return palette;
	}
	
	@Override
	public double getTPS() {
		return MinecraftServer.getServer().recentTps[ 0 ];
	}
	
	@Override
	public boolean registerCommand( PluginCommand command ) {
		Validate.notNull( command );
		return registerCommand( command.getPlugin().getName(), command );
	}
	
	@Override
	public boolean registerCommand( String fallbackPrefix, PluginCommand command ) {
		Validate.notNull( fallbackPrefix );
		Validate.notNull( command );
		return ( ( CraftServer ) Bukkit.getServer() ).getCommandMap().register( command.getPlugin().getName(), command );
	}
	
	@Override
	public void unregisterCommand( PluginCommand command ) {
		Validate.notNull( command );
		try {
			SimpleCommandMap map = ( ( CraftServer ) Bukkit.getServer() ).getCommandMap();
			Map< String, Command > commands = ( Map< String, Command > ) SIMPLECOMMANDMAP_COMMANDS.get( map );
			for ( Iterator< Entry< String, Command > > iterator = commands.entrySet().iterator(); iterator.hasNext(); ) {
				Entry< String, Command > entry = iterator.next();
				if ( entry.getValue() == command ) {
					iterator.remove();
				}
			}
		} catch ( IllegalArgumentException | IllegalAccessException e ) {
			e.printStackTrace();
		}
	}
	
	@Override
	public GeneralUtil getUtil() {
		return util;
	}
	
	private class PacketInterceptor extends ChannelDuplexHandler {
		public volatile Player player;
		
		private PacketInterceptor( Player player ) {
			this.player = player;
		}
		
		@Override
		public void channelRead( ChannelHandlerContext ctx, Object msg ) throws Exception {
			try {
				msg = onPacketInterceptIn( player, msg );
			} catch ( Exception e ) {
				Cartographer.getPlugin( Cartographer.class ).getLogger().log( Level.SEVERE, "Error in onPacketInAsync().", e );
			}

			if ( msg != null ) {
				super.channelRead( ctx, msg );
			}
		}

		@Override
		public void write( ChannelHandlerContext ctx, Object msg, ChannelPromise promise ) throws Exception {
			try {
				msg = onPacketInterceptOut( player, msg );
			} catch ( Exception e ) {
				Cartographer.getPlugin( Cartographer.class ).getLogger().log( Level.SEVERE, "Error in onPacketOutAsync().", e );
			}

			if ( msg != null ) {
				super.write( ctx, msg, promise );
			}
		}
	}
}

package io.github.bananapuncher714.cartographer.core.implementation.v1_14_R1;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumMap;
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
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursor.Type;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.GeneralUtil;
import io.github.bananapuncher714.cartographer.core.api.PacketHandler;
import io.github.bananapuncher714.cartographer.core.internal.Util_1_14;
import io.github.bananapuncher714.cartographer.core.map.menu.MapInteraction;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;
import io.github.bananapuncher714.cartographer.core.util.MapUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_14_R1.Block;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.EnumRenderType;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.IRegistry;
import net.minecraft.server.v1_14_R1.MapIcon;
import net.minecraft.server.v1_14_R1.MinecraftKey;
import net.minecraft.server.v1_14_R1.MinecraftServer;
import net.minecraft.server.v1_14_R1.NetworkManager;
import net.minecraft.server.v1_14_R1.PacketPlayInBlockDig;
import net.minecraft.server.v1_14_R1.PacketPlayInBlockDig.EnumPlayerDigType;
import net.minecraft.server.v1_14_R1.PacketPlayInSettings;
import net.minecraft.server.v1_14_R1.PacketPlayOutMap;
import net.minecraft.server.v1_14_R1.PlayerConnection;

public class NMSHandler implements PacketHandler {
	private static final AtomicInteger HANDLER_INDEX = new AtomicInteger();
	
	private static Field[] MAP_FIELDS = new Field[ 10 ];
	private static Map< MapCursor.Type, MapIcon.Type > CURSOR_TYPES = new EnumMap< MapCursor.Type, MapIcon.Type >( MapCursor.Type.class );
	private static Field SIMPLECOMMANDMAP_COMMANDS;
	private static Method CRAFTSERVER_SYNCCOMMANDS;
	
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
			MAP_FIELDS[ 8 ] = PacketPlayOutMap.class.getDeclaredField( "i" );
			MAP_FIELDS[ 9 ] = PacketPlayOutMap.class.getDeclaredField( "j" );

			for ( Field field : MAP_FIELDS ) {
				field.setAccessible( true );
			}
			
			SIMPLECOMMANDMAP_COMMANDS = SimpleCommandMap.class.getDeclaredField( "knownCommands" );
			SIMPLECOMMANDMAP_COMMANDS.setAccessible( true );
			
			CRAFTSERVER_SYNCCOMMANDS = CraftServer.class.getDeclaredMethod( "syncCommands" );
			CRAFTSERVER_SYNCCOMMANDS.setAccessible( true );
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
		
		CURSOR_TYPES.put( MapCursor.Type.WHITE_POINTER, MapIcon.Type.PLAYER );
		CURSOR_TYPES.put( MapCursor.Type.GREEN_POINTER, MapIcon.Type.FRAME );
		CURSOR_TYPES.put( MapCursor.Type.RED_POINTER, MapIcon.Type.TARGET_POINT );
		CURSOR_TYPES.put( MapCursor.Type.BLUE_POINTER, MapIcon.Type.BLUE_MARKER );
		CURSOR_TYPES.put( MapCursor.Type.WHITE_CROSS, MapIcon.Type.TARGET_X );
		CURSOR_TYPES.put( MapCursor.Type.RED_MARKER, MapIcon.Type.RED_MARKER );
		CURSOR_TYPES.put( MapCursor.Type.WHITE_CIRCLE, MapIcon.Type.PLAYER_OFF_MAP );
		CURSOR_TYPES.put( MapCursor.Type.SMALL_WHITE_CIRCLE, MapIcon.Type.PLAYER_OFF_LIMITS );
		CURSOR_TYPES.put( MapCursor.Type.MANSION, MapIcon.Type.MANSION );
		CURSOR_TYPES.put( MapCursor.Type.TEMPLE, MapIcon.Type.MONUMENT );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_WHITE, MapIcon.Type.BANNER_WHITE );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_ORANGE, MapIcon.Type.BANNER_ORANGE );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_MAGENTA, MapIcon.Type.BANNER_MAGENTA );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_LIGHT_BLUE, MapIcon.Type.BANNER_LIGHT_BLUE );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_YELLOW, MapIcon.Type.BANNER_YELLOW );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_LIME, MapIcon.Type.BANNER_LIME );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_PINK, MapIcon.Type.BANNER_PINK );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_GRAY, MapIcon.Type.BANNER_GRAY );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_LIGHT_GRAY, MapIcon.Type.BANNER_LIGHT_GRAY );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_CYAN, MapIcon.Type.BANNER_CYAN );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_PURPLE, MapIcon.Type.BANNER_PURPLE );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_BLUE, MapIcon.Type.BANNER_BLUE );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_BROWN, MapIcon.Type.BANNER_BROWN );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_GREEN, MapIcon.Type.BANNER_GREEN );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_RED, MapIcon.Type.BANNER_RED );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_BLACK, MapIcon.Type.BANNER_BLACK );
		CURSOR_TYPES.put( MapCursor.Type.RED_X, MapIcon.Type.RED_X );
	}

	private final Map< UUID, Channel > channels = new ConcurrentHashMap< UUID, Channel >();
	private final Set< Integer > maps = new TreeSet< Integer >();
	private Util_1_14 util = new Util_1_14();
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
				
				icons[ index ] = new MapIcon( CURSOR_TYPES.get( cursor.getType() ), cursor.getX(), cursor.getY(), cursor.getDirection(), cursor.getCaption() != null ? new ChatComponentText( cursor.getCaption() ) : null );
			}
		}
		
		PacketPlayOutMap packet = new PacketPlayOutMap();
		
		try {
			MAP_FIELDS[ 0 ].set( packet, id );
			MAP_FIELDS[ 1 ].set( packet, ( byte ) 0 );
			MAP_FIELDS[ 2 ].set( packet, false );
			MAP_FIELDS[ 3 ].set( packet, false );
			MAP_FIELDS[ 4 ].set( packet, icons );
			MAP_FIELDS[ 5 ].set( packet, 0 );
			MAP_FIELDS[ 6 ].set( packet, 0 );
			MAP_FIELDS[ 7 ].set( packet, 128 );
			MAP_FIELDS[ 8 ].set( packet, 128 );
			MAP_FIELDS[ 9 ].set( packet, data );
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
	
				EnumPlayerDigType type = digPacket.d();
				if ( type == EnumPlayerDigType.DROP_ALL_ITEMS || type == EnumPlayerDigType.DROP_ITEM ) {
					ItemStack item = viewer.getEquipment().getItemInMainHand();
					if ( Cartographer.getInstance().getMapManager().isMinimapItem( item ) ) {
						// Update the player's hand
						viewer.getEquipment().setItemInMainHand( item );
						
						// Activate the drop
						Cartographer.getInstance().getMapManager().activate( viewer, type == EnumPlayerDigType.DROP_ALL_ITEMS ? MapInteraction.CTRLQ : MapInteraction.Q );
						return null;
					}
				}
			} else if ( packet instanceof PacketPlayInSettings ) {
				PacketPlayInSettings settings = ( PacketPlayInSettings ) packet;
				Cartographer.getInstance().getPlayerManager().setLocale( viewer.getUniqueId(), settings.b() );
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
	public MapCursor constructMapCursor( int x, int y, double yaw, Type cursorType, String name ) {
		return new MapCursor( ( byte ) x, ( byte ) y, MapUtil.getDirection( yaw ), cursorType, true, name );
	}
	
	@Override
	public MinimapPalette getVanillaPalette() {
		MinimapPalette palette = new MinimapPalette();
		for ( MinecraftKey key : IRegistry.BLOCK.keySet() ) {
			Block block = IRegistry.BLOCK.get( key );
			for ( IBlockData data : block.getStates().a() ) {
				boolean transparent = data.k() == EnumRenderType.INVISIBLE;
				CrossVersionMaterial material = new CrossVersionMaterial( CraftMagicNumbers.getMaterial( data ).getItemType() );
				
				if ( transparent ) {
					palette.addTransparentMaterial( material );
				} else {
					int color = block.e( data, null, null ).rgb;
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
		boolean registered = ( ( CraftServer ) Bukkit.getServer() ).getCommandMap().register( fallbackPrefix, command );
		
		try {
			CRAFTSERVER_SYNCCOMMANDS.invoke( Bukkit.getServer() );
		} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			e.printStackTrace();
		}
		
		return registered;
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
			
			try {
				CRAFTSERVER_SYNCCOMMANDS.invoke( Bukkit.getServer() );
			} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
				e.printStackTrace();
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

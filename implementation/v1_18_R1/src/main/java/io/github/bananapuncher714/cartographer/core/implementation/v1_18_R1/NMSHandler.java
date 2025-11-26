package io.github.bananapuncher714.cartographer.core.implementation.v1_18_R1;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.legacy.CraftLegacy;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursor.Type;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.GeneralUtil;
import io.github.bananapuncher714.cartographer.core.api.PacketHandler;
import io.github.bananapuncher714.cartographer.core.internal.Util_1_17;
import io.github.bananapuncher714.cartographer.core.map.menu.MapInteraction;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;
import io.github.bananapuncher714.cartographer.core.util.MapUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.core.IRegistry;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.protocol.game.PacketPlayInBlockDig;
import net.minecraft.network.protocol.game.PacketPlayInBlockDig.EnumPlayerDigType;
import net.minecraft.network.protocol.game.PacketPlayInSettings;
import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnumRenderType;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockBase.Info;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.MaterialMapColor;
import net.minecraft.world.level.saveddata.maps.MapIcon;
import net.minecraft.world.level.saveddata.maps.WorldMap;

public class NMSHandler implements PacketHandler {
	private static final AtomicInteger HANDLER_INDEX = new AtomicInteger();
	
	private static Map< MapCursor.Type, MapIcon.Type > CURSOR_TYPES = new EnumMap< MapCursor.Type, MapIcon.Type >( MapCursor.Type.class );
	private static Field SIMPLECOMMANDMAP_COMMANDS;
	private static Method CRAFTSERVER_SYNCCOMMANDS;
	private static Field BLOCKBASE_INFO;
	private static Field INFO_FUNCTION;
	
	static {
		try {
			SIMPLECOMMANDMAP_COMMANDS = SimpleCommandMap.class.getDeclaredField( "knownCommands" );
			SIMPLECOMMANDMAP_COMMANDS.setAccessible( true );
			
			CRAFTSERVER_SYNCCOMMANDS = CraftServer.class.getDeclaredMethod( "syncCommands" );
			CRAFTSERVER_SYNCCOMMANDS.setAccessible( true );
			
			BLOCKBASE_INFO = BlockBase.class.getDeclaredField( "aP" );
			BLOCKBASE_INFO.setAccessible( true );
			
			INFO_FUNCTION = Info.class.getDeclaredField( "b" );
			INFO_FUNCTION.setAccessible( true );
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
		
		CURSOR_TYPES.put( MapCursor.Type.WHITE_POINTER, MapIcon.Type.a );
		CURSOR_TYPES.put( MapCursor.Type.GREEN_POINTER, MapIcon.Type.b );
		CURSOR_TYPES.put( MapCursor.Type.RED_MARKER, MapIcon.Type.c );
		CURSOR_TYPES.put( MapCursor.Type.BLUE_POINTER, MapIcon.Type.d );
		CURSOR_TYPES.put( MapCursor.Type.WHITE_CROSS, MapIcon.Type.e );
		CURSOR_TYPES.put( MapCursor.Type.RED_POINTER, MapIcon.Type.f );
		CURSOR_TYPES.put( MapCursor.Type.WHITE_CIRCLE, MapIcon.Type.g );
		CURSOR_TYPES.put( MapCursor.Type.SMALL_WHITE_CIRCLE, MapIcon.Type.h );
		CURSOR_TYPES.put( MapCursor.Type.MANSION, MapIcon.Type.i );
		CURSOR_TYPES.put( MapCursor.Type.TEMPLE, MapIcon.Type.j );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_WHITE, MapIcon.Type.k );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_ORANGE, MapIcon.Type.l );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_MAGENTA, MapIcon.Type.m );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_LIGHT_BLUE, MapIcon.Type.n );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_YELLOW, MapIcon.Type.o );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_LIME, MapIcon.Type.p );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_PINK, MapIcon.Type.q );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_GRAY, MapIcon.Type.r );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_LIGHT_GRAY, MapIcon.Type.s );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_CYAN, MapIcon.Type.t );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_PURPLE, MapIcon.Type.u );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_BLUE, MapIcon.Type.v );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_BROWN, MapIcon.Type.w );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_GREEN, MapIcon.Type.x );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_RED, MapIcon.Type.y );
		CURSOR_TYPES.put( MapCursor.Type.BANNER_BLACK, MapIcon.Type.z );
		CURSOR_TYPES.put( MapCursor.Type.RED_X, MapIcon.Type.A );
	}

	private final Map< UUID, Channel > channels = new ConcurrentHashMap< UUID, Channel >();
	private final Set< Integer > maps = new TreeSet< Integer >();
	private Util_1_17 util = new Util_1_17();
	private final String handler_name;
	
	private final Set< PacketPlayOutMap > whitelisted = Collections.synchronizedSet( Collections.newSetFromMap( new WeakHashMap< PacketPlayOutMap, Boolean >() ) );

	public NMSHandler() {
		handler_name = "cartographer2_handler_" + HANDLER_INDEX.getAndIncrement();
	}
	
	@Override
	public void inject( Player player ) {
		PlayerConnection conn = ( ( CraftPlayer ) player ).getHandle().b;
		NetworkManager manager = conn.a;
		Channel channel = manager.k;
		
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
		PlayerConnection conn = ( ( CraftPlayer ) player ).getHandle().b;
		NetworkManager manager = conn.a;
		Channel channel = manager.k;
		channels.remove( player.getUniqueId() );
		
		if ( channel != null ) {
			if ( channel.pipeline().get( handler_name ) != null ) {
				channel.pipeline().remove( handler_name );
			}
		}
	}
	
	@Override
	public void sendDataTo( int id, byte[] data, @Nullable MapCursor[] cursors, UUID... uuids ) {
		List< MapIcon > icons = null;
		if ( cursors != null ) {
			icons = new LinkedList< MapIcon >();
			
			for ( int index = 0; index < cursors.length; index++ ) {
				MapCursor cursor = cursors[ index ];
				
				icons.add( new MapIcon( CURSOR_TYPES.get( cursor.getType() ), cursor.getX(), cursor.getY(), cursor.getDirection(), cursor.getCaption() != null ? new ChatComponentText( cursor.getCaption() ) : null ) );
			}
		}
		
		PacketPlayOutMap packet = new PacketPlayOutMap( id, ( byte ) 0, false, icons, new WorldMap.b( 0, 0, 128, 128, data ) );
		
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
				int id = ( ( PacketPlayOutMap ) packet ).b();
				if ( maps.contains( id ) ) {
					return null;
				}
			} catch ( IllegalArgumentException e ) {
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
				if ( type == EnumPlayerDigType.e || type == EnumPlayerDigType.d ) {
					ItemStack item = viewer.getEquipment().getItemInMainHand();
					if ( Cartographer.getInstance().getMapManager().isMinimapItem( item ) ) {
						// Update the player's hand
						viewer.getEquipment().setItemInMainHand( item );
						
						// Activate the drop
						Cartographer.getInstance().getMapManager().activate( viewer, type == EnumPlayerDigType.d ? MapInteraction.CTRLQ : MapInteraction.Q );
						return null;
					}
				}
			} else if ( packet instanceof PacketPlayInSettings ) {
				PacketPlayInSettings settings = ( PacketPlayInSettings ) packet;
				Cartographer.getInstance().getPlayerManager().setLocale( viewer.getUniqueId(), settings.b );
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
		// For some reason IRegistry.BLOCK contains only a few blocks, and it's not a consistent amount.
		for ( Block block : IRegistry.X ) {
			CrossVersionMaterial material = new CrossVersionMaterial( CraftLegacy.fromLegacy( CraftMagicNumbers.getMaterial( block ) ) );
			boolean transparent = block.b_( block.n() ) == EnumRenderType.a;
			if ( transparent ) {
				palette.addTransparentMaterial( material );
			} else {
				try {
					Info info = ( Info ) BLOCKBASE_INFO.get( block );
					Function< IBlockData, MaterialMapColor > function = ( Function< IBlockData, MaterialMapColor > ) INFO_FUNCTION.get( info );
					int color = function.apply( block.n() ).ak;
					if ( color == 0 ) {
						palette.addTransparentMaterial( material );
					} else {
						palette.setColor( material, color );
					}
				} catch ( IllegalArgumentException | IllegalAccessException e ) {
					e.printStackTrace();
				}
			}
		}
		palette.getTransparentBlocks().remove( new CrossVersionMaterial( org.bukkit.Material.WATER ) );
		palette.getTransparentBlocks().remove( new CrossVersionMaterial( org.bukkit.Material.LAVA ) );
		palette.setColor( new CrossVersionMaterial( org.bukkit.Material.WATER ), new Color( 64, 64, 255 ) );
		palette.setColor( new CrossVersionMaterial( org.bukkit.Material.LAVA ), new Color( 255, 0, 0 ) );
		
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
			// Pretty dumb, but apparently you need to re-sync the commands after you do your business or else it won't tab complete properly for players
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

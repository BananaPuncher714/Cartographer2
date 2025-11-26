package io.github.bananapuncher714.cartographer.core.implementation.v1_21_R3;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
import org.bukkit.craftbukkit.v1_21_R3.CraftServer;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R3.legacy.CraftLegacy;
import org.bukkit.craftbukkit.v1_21_R3.util.CraftMagicNumbers;
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
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.PacketPlayInBlockDig;
import net.minecraft.network.protocol.game.PacketPlayInBlockDig.EnumPlayerDigType;
import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnumRenderType;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockBase.Info;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.MaterialMapColor;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapIcon;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.WorldMap;

public class NMSHandler implements PacketHandler {
    private static final AtomicInteger HANDLER_INDEX = new AtomicInteger();
    
    private static Map< MapCursor.Type, Holder<MapDecorationType> > CURSOR_TYPES = new HashMap< MapCursor.Type, Holder<MapDecorationType> >();
    private static Field SIMPLECOMMANDMAP_COMMANDS;
    private static Method CRAFTSERVER_SYNCCOMMANDS;
    private static Field BLOCKBASE_INFO;
    private static Field INFO_FUNCTION;
    private static Field NETWORK_MANAGER;
    
    static {
        try {
            SIMPLECOMMANDMAP_COMMANDS = SimpleCommandMap.class.getDeclaredField( "knownCommands" );
            SIMPLECOMMANDMAP_COMMANDS.setAccessible( true );
            
            CRAFTSERVER_SYNCCOMMANDS = CraftServer.class.getDeclaredMethod( "syncCommands" );
            CRAFTSERVER_SYNCCOMMANDS.setAccessible( true );
            
            BLOCKBASE_INFO = BlockBase.class.getDeclaredField( "aQ" );
            BLOCKBASE_INFO.setAccessible( true );
            
            INFO_FUNCTION = Info.class.getDeclaredField( "b" );
            INFO_FUNCTION.setAccessible( true );
            
            NETWORK_MANAGER = ServerCommonPacketListenerImpl.class.getDeclaredField( "e" );
            NETWORK_MANAGER.setAccessible( true );
        } catch ( Exception exception ) {
            exception.printStackTrace();
        }
        
        CURSOR_TYPES.put( MapCursor.Type.PLAYER, MapDecorationTypes.a );
        CURSOR_TYPES.put( MapCursor.Type.FRAME, MapDecorationTypes.b );
        CURSOR_TYPES.put( MapCursor.Type.RED_MARKER, MapDecorationTypes.c );
        CURSOR_TYPES.put( MapCursor.Type.BLUE_MARKER, MapDecorationTypes.d );
        CURSOR_TYPES.put( MapCursor.Type.TARGET_X, MapDecorationTypes.e );
        CURSOR_TYPES.put( MapCursor.Type.TARGET_POINT, MapDecorationTypes.f );
        CURSOR_TYPES.put( MapCursor.Type.PLAYER_OFF_MAP, MapDecorationTypes.g );
        CURSOR_TYPES.put( MapCursor.Type.PLAYER_OFF_LIMITS, MapDecorationTypes.h );
        CURSOR_TYPES.put( MapCursor.Type.MANSION, MapDecorationTypes.i );
        CURSOR_TYPES.put( MapCursor.Type.MONUMENT, MapDecorationTypes.j );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_WHITE, MapDecorationTypes.k );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_ORANGE, MapDecorationTypes.l );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_MAGENTA, MapDecorationTypes.m );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_LIGHT_BLUE, MapDecorationTypes.n );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_YELLOW, MapDecorationTypes.o );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_LIME, MapDecorationTypes.p );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_PINK, MapDecorationTypes.q );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_GRAY, MapDecorationTypes.r );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_LIGHT_GRAY, MapDecorationTypes.s );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_CYAN, MapDecorationTypes.t );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_PURPLE, MapDecorationTypes.u );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_BLUE, MapDecorationTypes.v );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_BROWN, MapDecorationTypes.w );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_GREEN, MapDecorationTypes.x );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_RED, MapDecorationTypes.y );
        CURSOR_TYPES.put( MapCursor.Type.BANNER_BLACK, MapDecorationTypes.z );
        CURSOR_TYPES.put( MapCursor.Type.RED_X, MapDecorationTypes.A );
        CURSOR_TYPES.put( MapCursor.Type.VILLAGE_DESERT, MapDecorationTypes.B );
        CURSOR_TYPES.put( MapCursor.Type.VILLAGE_PLAINS, MapDecorationTypes.C );
        CURSOR_TYPES.put( MapCursor.Type.VILLAGE_SAVANNA, MapDecorationTypes.D );
        CURSOR_TYPES.put( MapCursor.Type.VILLAGE_SNOWY, MapDecorationTypes.E );
        CURSOR_TYPES.put( MapCursor.Type.VILLAGE_TAIGA, MapDecorationTypes.F );
        CURSOR_TYPES.put( MapCursor.Type.JUNGLE_TEMPLE, MapDecorationTypes.G );
        CURSOR_TYPES.put( MapCursor.Type.SWAMP_HUT, MapDecorationTypes.H );
        CURSOR_TYPES.put( MapCursor.Type.TRIAL_CHAMBERS, MapDecorationTypes.I );
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
        PlayerConnection conn = ( ( CraftPlayer ) player ).getHandle().f;
        NetworkManager manager = null;
        try {
            manager = ( NetworkManager ) NETWORK_MANAGER.get( conn );
        } catch ( IllegalArgumentException | IllegalAccessException e ) {
            e.printStackTrace();
        }
        Channel channel = manager.n;
        
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
        PlayerConnection conn = ( ( CraftPlayer ) player ).getHandle().f;
        NetworkManager manager = null;
        try {
            manager = ( NetworkManager ) NETWORK_MANAGER.get( conn );
        } catch ( IllegalArgumentException | IllegalAccessException e ) {
            e.printStackTrace();
        }
        Channel channel = manager.n;
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
                
                icons.add( new MapIcon( CURSOR_TYPES.get( cursor.getType() ), cursor.getX(), cursor.getY(), cursor.getDirection(), Optional.ofNullable( cursor.getCaption() != null ? IChatBaseComponent.c( cursor.getCaption() ) : null ) ) );
            }
        }
        
        PacketPlayOutMap packet = new PacketPlayOutMap( new MapId( id ), ( byte ) 0, false, icons, new WorldMap.c( 0, 0, 128, 128, data ) );
        
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
                int id = ( ( PacketPlayOutMap ) packet ).b().b();
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
    
                EnumPlayerDigType type = digPacket.f();
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
            } else if ( packet instanceof ServerboundClientInformationPacket ) {
                ServerboundClientInformationPacket settings = ( ServerboundClientInformationPacket ) packet;
                Cartographer.getInstance().getPlayerManager().setLocale( viewer.getUniqueId(), settings.b().b() );
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
        for ( Block block : BuiltInRegistries.e ) {
            CrossVersionMaterial material = new CrossVersionMaterial( CraftLegacy.fromLegacy( CraftMagicNumbers.getMaterial( block ) ) );
            boolean transparent = block.m().o() == EnumRenderType.a;
            if ( transparent ) {
                palette.addTransparentMaterial( material );
            } else {
                try {
                    Info info = ( Info ) BLOCKBASE_INFO.get( block );
                    Function< IBlockData, MaterialMapColor > function = ( Function< IBlockData, MaterialMapColor > ) INFO_FUNCTION.get( info );
                    int color = function.apply( block.m() ).ak;
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

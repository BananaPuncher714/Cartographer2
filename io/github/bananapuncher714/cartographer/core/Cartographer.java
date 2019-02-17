package io.github.bananapuncher714.cartographer.core;

import java.awt.Color;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.bananapuncher714.cartographer.core.api.PacketHandler;
import io.github.bananapuncher714.cartographer.core.map.MapDataCache;
import io.github.bananapuncher714.cartographer.core.map.MapSettings;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.map.SimpleChunkProcessor;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;
import io.github.bananapuncher714.cartographer.core.util.ReflectionUtil;
import io.github.bananapuncher714.cartographer.tinyprotocol.TinyProtocol;
import io.netty.channel.Channel;

public class Cartographer extends JavaPlugin implements Listener {
	private static Cartographer INSTANCE;
	
	private TinyProtocol protocol;
	private PacketHandler handler;
	
	private MinimapManager mapManager;
	
	private Set< Integer > invalidIds = new HashSet< Integer >();
	
	private MinimapPalette palette = new MinimapPalette( new Color( 0, 0, 0, 255 ) );
	
	private Set< CartographerRenderer > renderers = new HashSet< CartographerRenderer >();
	
	private CartographerCommand command;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		
		JetpImageUtil.init();
		
		saveDefaultConfig();
		loadConfig();
		
		handler = ReflectionUtil.getNewPacketHandlerInstance();
		if ( handler == null ) {
			getLogger().severe( "This version(" + ReflectionUtil.VERSION + ") is not supported currently!" );
			getLogger().severe( "Disabling..." );
			Bukkit.getPluginManager().disablePlugin( this );
			return;
		}
		protocol = new TinyProtocol( this ) {
			@Override
			public Object onPacketOutAsync( Player player, Channel channel, Object packet ) {
				return handler.onPacketInterceptOut( player, packet );
			}

			@Override
			public Object onPacketInAsync( Player player, Channel channel, Object packet ) {
				return handler.onPacketInterceptIn( player, packet );
			}
		};
		
		// TODO make this palette more customizable
		FileConfiguration config = YamlConfiguration.loadConfiguration( new InputStreamReader( getResource( "data/colors-1.13.2.yml" ) ) );
		for ( String key : config.getConfigurationSection( "colors" ).getKeys( false ) ) {
			String[] data = config.getString( "colors." + key ).split( "\\D+" );
			Color color = new Color( Integer.parseInt( data[ 0 ] ), Integer.parseInt( data[ 1 ] ), Integer.parseInt( data[ 2 ] ) );
						
			palette.setColor( Material.valueOf( key.toUpperCase() ), color );
		}
		
		for ( String val : config.getStringList( "transparent-blocks" ) ) {
			palette.addTransparentMaterial( Material.valueOf( val.toUpperCase() ) );
		}
		
		mapManager = new MinimapManager( this );
		
		command = new CartographerCommand();
		getCommand( "cartographer" ).setExecutor( command );
		getCommand( "cartographer" ).setTabCompleter( command );
		
		Bukkit.getPluginManager().registerEvents( this, this );
		
		Bukkit.getScheduler().runTaskTimer( this, this::update, 5, 1 );
		Bukkit.getScheduler().runTaskTimer( this, ChunkLoadListener.INSTANCE::update, 5, 4 );
		
		Bukkit.getPluginManager().registerEvents( new PlayerListener(), this );
		Bukkit.getPluginManager().registerEvents( ChunkLoadListener.INSTANCE, this );
		
		loadMaps();
	}
	
	@Override
	public void onDisable() {
		for ( CartographerRenderer renderer : renderers ) {
			renderer.terminate();
		}
		mapManager.terminate();
	}
	
	private void update() {
		mapManager.update();
	}
	
	private void loadConfig() {
		FileConfiguration config = YamlConfiguration.loadConfiguration( new File( getDataFolder() + "/" + "config.yml" ) );
		for ( String string : config.getStringList( "skip-ids" ) ) {
			invalidIds.add( Integer.valueOf( string ) );
		}
	}
	
	private void loadMaps() {
		File mapDir = new File( getDataFolder() + "/maps/" );
		if ( !mapDir.exists() ) {
			return;
		}
		for ( File file : mapDir.listFiles() ) {
			mapManager.constructNewMinimap( file.getName() );
		}
	}
	
	@EventHandler
	private void onMapInitializeEvent( MapInitializeEvent event ) {
		convert( event.getMap() );
	}
	
	@EventHandler
	private void onPlayerInteractEvent( PlayerInteractEvent event ) {
		if ( event.getHand() != EquipmentSlot.HAND ) {
			return;
		}
		ItemStack item = event.getPlayer().getEquipment().getItemInMainHand();
		if ( item != null && item.getType() == Material.FILLED_MAP ) {
			MapMeta meta = ( MapMeta ) item.getItemMeta();
			MapView view = Bukkit.getMap( ( short ) meta.getMapId() );
			for ( MapRenderer renderer : view.getRenderers() ) {
				if ( renderer instanceof CartographerRenderer ) {
					return;
				}
			}
			
			convert( view );
		}
	}
	
	@EventHandler
	private void onPlayerQuitEvent( PlayerQuitEvent event ) {
		protocol.removeChannel( event.getPlayer() );
	}
	
	private void convert( MapView view ) {
		if ( invalidIds.contains( view.getId() ) ) {
			return;
		}
		for ( MapRenderer render : view.getRenderers() ) {
			view.removeRenderer( render );
		}
		CartographerRenderer renderer = new CartographerRenderer();
		renderers.add( renderer );
		view.addRenderer( renderer );
		handler.registerMap( view.getId() );
	}
	
	public TinyProtocol getProtocol() {
		return protocol;
	}
	
	public PacketHandler getHandler() {
		return handler;
	}
	
	public MinimapPalette getPalette() {
		return palette;
	}

	public MinimapManager getMapManager() {
		return mapManager;
	}
	
	protected Set< CartographerRenderer > getRenderers() {
		return renderers;
	}
	
	public static Cartographer getInstance() {
		return INSTANCE;
	}
}

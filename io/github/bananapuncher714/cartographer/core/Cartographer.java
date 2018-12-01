package io.github.bananapuncher714.cartographer.core;

import java.awt.Color;
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
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapRenderer;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.bananapuncher714.cartographer.core.api.PacketHandler;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;
import io.github.bananapuncher714.cartographer.core.renderer.MapDataCache;
import io.github.bananapuncher714.cartographer.core.util.ReflectionUtil;
import io.github.bananapuncher714.cartographer.tinyprotocol.TinyProtocol;
import io.netty.channel.Channel;

public class Cartographer extends JavaPlugin implements Listener {
	private static Cartographer INSTANCE;
	
	private TinyProtocol protocol;
	private PacketHandler handler;
	
	private Minimap minimap;
	
	private MinimapPalette palette = new MinimapPalette( new Color( 0, 0, 0, 255 ) );
	
	private Set< CartographerRenderer > renderers = new HashSet< CartographerRenderer >();
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		
		handler = ReflectionUtil.getNewPacketHandlerInstance();
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
		
		FileConfiguration config = YamlConfiguration.loadConfiguration( new InputStreamReader( getResource( "data/colors-1.13.2.yml" ) ) );
		for ( String key : config.getConfigurationSection( "colors" ).getKeys( false ) ) {
			String[] data = config.getString( "colors." + key ).split( "\\D+" );
			Color color = new Color( Integer.parseInt( data[ 0 ] ), Integer.parseInt( data[ 1 ] ), Integer.parseInt( data[ 2 ] ) );
						
			palette.setColor( Material.valueOf( key.toUpperCase() ), color );
		}
		
		for ( String val : config.getStringList( "transparent-blocks" ) ) {
			palette.addTransparentMaterial( Material.valueOf( val.toUpperCase() ) );
		}
		
		Bukkit.getPluginManager().registerEvents( this, this );
	}
	
	@Override
	public void onDisable() {
		for ( CartographerRenderer renderer : renderers ) {
			renderer.terminate();
		}
	}
	
	@EventHandler
	private void onMapInitializeEvent( MapInitializeEvent event ) {
		for ( MapRenderer render : event.getMap().getRenderers() ) {
			event.getMap().removeRenderer( render );
		}
		CartographerRenderer renderer = new CartographerRenderer();
		renderers.add( renderer );
		event.getMap().addRenderer( renderer );
		handler.registerMap( event.getMap().getId() );
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
	
	public static Cartographer getInstance() {
		return INSTANCE;
	}
}

package io.github.bananapuncher714.cartographer.core;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapRenderer;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;

public class Cartographer extends JavaPlugin implements Listener {
	private Set< CartographerRenderer > renderers = new HashSet< CartographerRenderer >();
	
	@Override
	public void onEnable() {
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
	}
}

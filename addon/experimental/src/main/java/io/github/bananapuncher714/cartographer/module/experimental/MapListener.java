package io.github.bananapuncher714.cartographer.module.experimental;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.events.minimap.MinimapLoadEvent;

public class MapListener implements Listener {
	private ExperimentalModule module;
	
	public MapListener( ExperimentalModule module ) {
		this.module = module;
	}
	
	@EventHandler
	private void onEvent( MinimapLoadEvent event ) {
		event.getMinimap().register( new TextPixelProvider( module ) );
		event.getMinimap().register( new ChunkBorderShader( module::getLocations ) );
	}
	
	@EventHandler
	private void onEvent( PlayerMoveEvent event ) {
		module.addExplored( new ChunkLocation( event.getTo() ) );
	}
}

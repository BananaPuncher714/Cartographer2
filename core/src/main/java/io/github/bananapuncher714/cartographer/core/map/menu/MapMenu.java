package io.github.bananapuncher714.cartographer.core.map.menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;

import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

public class MapMenu {
	protected MenuCanvas canvas;
	protected List< MenuComponent > components = new ArrayList< MenuComponent >();
	protected Set< UUID > viewers = new HashSet< UUID >();
	
	public MapMenu() {
		// Set the height and width to 128 internally since this is for a map, which is always going to be 128 by 128
		canvas = new MenuCanvas( 128, 128 );
	}
	
	public boolean view( Player player, PlayerSetting setting ) {
		viewers.add( player.getUniqueId() );
		canvas.getCursors().clear();
		for ( MenuComponent component : components ) {
			if ( component.onView( canvas, player, setting.getCursorX() / 2.0 + 64, setting.getCursorY() / 2.0 + 64 ) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean interact( Player player, PlayerSetting setting ) {
		for ( MenuComponent component : components ) {
			if ( component.onInteract( player, setting.getCursorX() / 2.0 + 64, setting.getCursorY() / 2.0 + 64, setting.getInteraction() ) ) {
				return true;
			}
		}
		return false;
	}
	
	public void onClose( UUID uuid ) {
		viewers.remove( uuid );
	}
	
	public Set< UUID > getViewers() {
		return new HashSet< UUID >( viewers );
	}
	
	public MenuCanvas getCanvas() {
		return canvas;
	}
	
	public byte[] getDisplay() {
		return canvas.getDisplay();
	}
	
	public Collection< MapCursor > getCursors() {
		return canvas.getCursors();
	}
	
	public void removeComponent( MenuComponent component ) {
		components.remove( component );
	}
	
	public MapMenu addComponent( MenuComponent... components ) {
		for ( MenuComponent component : components ) {
			this.components.add( component );
		}
		return this;
	}
	
	public List< MenuComponent > getComponents() {
		return components;
	}
}

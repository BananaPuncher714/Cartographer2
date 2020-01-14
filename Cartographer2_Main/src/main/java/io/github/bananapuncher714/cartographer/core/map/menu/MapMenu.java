package io.github.bananapuncher714.cartographer.core.map.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;

public class MapMenu {
	protected int height;
	protected int width;
	protected int[] data;
	protected byte[] displayData;
	protected boolean dither = true;
	protected boolean dirty = false;
	
	protected List< MenuComponent > components = new ArrayList< MenuComponent >();
	
	public MapMenu() {
		// Set the height and width to 128 internally since this is for a map, which is always going to be 128 by 128
		this.height = 128;
		this.width = 128;
		
		data = new int[ width * height ];
		displayData = new byte[ 128 * 128 ];
	}
	
	public void setDither( boolean dither ) {
		if ( this.dither != dither ) {
			this.dither = dither;
			
			if ( dither ) {
				displayData = JetpImageUtil.dither( width, data );
			} else {
				displayData = JetpImageUtil.simplify( data );
			}
		}
	}
	
	public boolean isDither() {
		return dither;
	}
	
	public boolean view( Player player, PlayerSetting setting ) {
		for ( MenuComponent component : components ) {
			if ( component.onView( player, setting.getCursorX() + 127, setting.getCursorY() + 127 ) ) {
				dirty = component.isDirty() || dirty;
				return true;
			}
		}
		
		if ( dirty ) {
			update();
		}
		return false;
	}
	
	public boolean interact( Player player, PlayerSetting setting ) {
		for ( MenuComponent component : components ) {
			if ( component.onInteract( player, setting.getCursorX() + 127, setting.getCursorY() + 127, setting.getInteraction() ) ) {
				return true;
			}
		}
		return false;
	}
	
	public void onClose( UUID uuid ) {
	}
	
	public byte[] getDisplay() {
		return displayData;
	}
	
	public MapMenu addComponent( MenuComponent... components ) {
		for ( MenuComponent component : components ) {
			this.components.add( component );
		}
		update();
		return this;
	}
	
	public List< MenuComponent > getComponents() {
		return components;
	}
	
	private void update() {
		for ( MenuComponent component : components ) {
			Frame frame = component.getFrame();
			if ( frame != null ) {
				apply( frame );
			}
		}

		if ( dither ) {
			displayData = JetpImageUtil.dither( this.width, data );
		} else {
			displayData = JetpImageUtil.simplify( data );
		}
		
		dirty = false;
	}
	
	public void apply( Frame frame ) {
		int[] frameDisplay = frame.getDisplay();
		int frameHeight = frameDisplay.length / frame.width;
		
		int topX = Math.max( 0, frame.x );
		int topY = Math.max( 0, frame.y );
		
		int width = Math.min( this.width - frame.x, frame.width );
		int height = Math.min( this.height - frame.y, frameHeight );
		
		for ( int y = 0; y < height; y++ ) {
			int ly = ( y + topY ) * width;
			int fy = y * frame.width;
			for ( int x = 0; x < width; x++ ) {
				int lx = x + topX;
				data[ lx + ly ] = frameDisplay[ x + fy ];
			}
		}
	}
}

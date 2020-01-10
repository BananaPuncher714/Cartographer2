package io.github.bananapuncher714.cartographer.core.map.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;

public class MapMenu {
	protected int height;
	protected int width;
	protected int[] data;
	protected byte[] displayData;
	protected boolean dither;
	
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
	
	public void view( Player player, PlayerSetting setting ) {
		if ( setting.isInteracting() ) {
			for ( MenuComponent component : components ) {
				component.onInteract( player, setting.getCursorX() + 127, setting.getCursorY() + 127, setting.isInteractingMain() );
			}
		}
		
		for ( MenuComponent component : components ) {
			component.onView( player, setting.getCursorX() + 127, setting.getCursorY() + 127 );
		}
	}
	
	public byte[] getDisplay() {
		return displayData;
	}
	
	public MapMenu addComponent( MenuComponent component ) {
		components.add( component );
		return this;
	}
	
	public List< MenuComponent > getComponents() {
		return components;
	}
	
	public void apply( Frame frame ) {
		int[] frameDisplay = frame.getDisplay();
		int frameHeight = frameDisplay.length / frame.width;
		
		int topX = Math.max( 0, frame.x );
		int topY = Math.max( 0, frame.y );
		
		int width = Math.min( this.width - frame.x, frame.width );
		int height = Math.min( this.height - frame.y, frameHeight );
		
		for ( int x = 0; x < width; x++ ) {
			for ( int y = 0; y < height; y++ ) {
				data[ x + topX + ( y + topY ) * width ] = frameDisplay[ x + y * frame.width ];
			}
		}
		
		if ( dither ) {
			displayData = JetpImageUtil.dither( this.width, data );
		} else {
			displayData = JetpImageUtil.simplify( data );
		}
	}
}

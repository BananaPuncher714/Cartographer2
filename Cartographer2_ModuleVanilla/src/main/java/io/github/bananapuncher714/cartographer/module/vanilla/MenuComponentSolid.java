package io.github.bananapuncher714.cartographer.module.vanilla;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.map.menu.MapInteraction;
import io.github.bananapuncher714.cartographer.core.map.menu.MenuCanvas;
import io.github.bananapuncher714.cartographer.core.map.menu.MenuComponent;
import io.github.bananapuncher714.cartographer.core.map.text.CartographerFont;

public class MenuComponentSolid implements MenuComponent {
	protected int[] drawPad = new int[ 128 * 128 ];
	protected int color;
	
	public MenuComponentSolid( int color ) {
		this.color = color | 0xFF000000;
	}
	
	@Override
	public boolean onView( MenuCanvas canvas, Player player, double x, double y ) {
		canvas.fill( Color.WHITE );
		BufferedImage message = new CartographerFont( CartographerFont.getFontFromSystem( "DejaVu Sans ExtraLight" ) ).write( new SimpleDateFormat( "ss.SSS" ).format( new Date() ), new Color( 0, 255, 200 ), 22 );
		canvas.drawImage( message, 5, 5 );
		
		if ( player.isSneaking() ) {
			drawPad[ ( int ) x + ( ( int ) y ) * 128 ] = color;
		}
		
		canvas.drawImage( drawPad, 128, 0, 0 );
		return false;
	}

	@Override
	public boolean onInteract( Player player, double x, double y, MapInteraction interaction ) {
		if ( interaction == MapInteraction.Q ) {
			color = ( int ) ( Math.random() * 0xFFFFFF ) | 0xFF000000;
		} else if ( interaction == MapInteraction.RIGHT ) {
			color = 0;
		}
		return interaction == MapInteraction.CTRLQ;
	}
}

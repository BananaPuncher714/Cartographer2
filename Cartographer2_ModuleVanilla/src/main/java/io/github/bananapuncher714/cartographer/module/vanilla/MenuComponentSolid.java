package io.github.bananapuncher714.cartographer.module.vanilla;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.map.menu.Frame;
import io.github.bananapuncher714.cartographer.core.map.menu.MapInteraction;
import io.github.bananapuncher714.cartographer.core.map.menu.MenuCanvas;
import io.github.bananapuncher714.cartographer.core.map.menu.MenuComponent;
import io.github.bananapuncher714.cartographer.core.map.text.CartographerFont;

public class MenuComponentSolid implements MenuComponent {
	Frame frame;
	
	public MenuComponentSolid( int color ) {
		color |= 0xFF000000;
		int[] display = new int[ 128 * 128 ];
		Arrays.fill( display, color );
		frame = new Frame( display, 128 );
	}
	
	@Override
	public boolean onView( MenuCanvas canvas, Player player, double x, double y ) {
		canvas.fill( Color.WHITE );
		BufferedImage message = new CartographerFont( CartographerFont.getFontFromSystem( "DejaVu Sans ExtraLight" ) ).write( new SimpleDateFormat( "ss.SSS" ).format( new Date() ), new Color( 0, 255, 200 ), 22 );
		canvas.drawImage( message, 5, 5 );
		return false;
	}

	@Override
	public boolean onInteract( Player player, double x, double y, MapInteraction interaction ) {
		return interaction == MapInteraction.CTRLQ;
	}
}

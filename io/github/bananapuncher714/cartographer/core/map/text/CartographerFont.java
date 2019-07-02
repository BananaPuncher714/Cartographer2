package io.github.bananapuncher714.cartographer.core.map.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;

public class CartographerFont {
	protected Font font;
	
	public CartographerFont( Font font ) {
		this.font = font;
	}

	public BufferedImage write( String message, Color color, float size, FontStyle... styles ) {
		Font font = this.font.deriveFont( size );
		for ( FontStyle style : styles ) {
			font = font.deriveFont( style.style );
		}
		
		FontMetrics metrics = new JLabel().getFontMetrics( font );
		int height = metrics.getMaxAscent() + metrics.getMaxDescent();
		int width = metrics.stringWidth( message );
		BufferedImage image = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		Graphics2D graphics = image.createGraphics();
		graphics.setColor( new Color( 0, 0, 0, 0 ) );
		graphics.fillRect( 0, 0, width, height );
		graphics.setColor( color );
		graphics.setFont( font );
		graphics.setColor( color );
		graphics.drawString( message, 0, metrics.getMaxAscent() );
		graphics.dispose();
		return image;
	}
	
}

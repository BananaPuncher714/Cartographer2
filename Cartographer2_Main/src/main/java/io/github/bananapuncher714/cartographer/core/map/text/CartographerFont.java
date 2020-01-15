package io.github.bananapuncher714.cartographer.core.map.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;

import org.apache.commons.lang.Validate;

/**
 * Convert text to an image.
 * 
 * @author BananaPuncher714
 */
public class CartographerFont {
	protected Font font;
	
	/**
	 * Construct with a font.
	 * 
	 * @param font
	 * Cannot be null.
	 */
	public CartographerFont( Font font ) {
		Validate.notNull( font );
		this.font = font;
	}

	/**
	 * Create a new image from the message, color, size, and styles provided.
	 * 
	 * @param message
	 * The message to write. Supports special characters.
	 * @param color
	 * Cannot be null.
	 * @param size
	 * Size as defined by the java.
	 * @param styles
	 * Styles as defined by java.
	 * @return
	 * A transparent image with the text only.
	 */
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
		graphics.setColor( new Color( 0, true ) );
		graphics.fillRect( 0, 0, width, height );
		graphics.setColor( color );
		graphics.setFont( font );
		graphics.setColor( color );
		graphics.drawString( message, 0, metrics.getMaxAscent() );
		graphics.dispose();
		return image;
	}
	
	public static Font getFontFromSystem( String name ) {
		Font family = null;
		for ( Font font : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts() ) {
			if ( font.getFontName().equalsIgnoreCase( name ) ) {
				return font;
			} else if ( font.getFamily().equalsIgnoreCase( name ) ) {
				family = font;
			}
		}
		return family;
	}
}

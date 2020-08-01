package io.github.bananapuncher714.cartographer.core.map.palette;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.CartographerLogger;
import io.github.bananapuncher714.cartographer.core.map.Minimap;

/**
 * Manage the {@link MinimapPalette} for {@link Minimap}.
 * 
 * @author BananaPuncher714
 */
public class PaletteManager {
	protected Cartographer plugin;
	
	protected Map< String, MinimapPalette > palettes;
	
	protected CartographerLogger logger;
	
	/**
	 * Only one PaletteManager is recommended. To get the current one in use, call {@link Cartographer#getPaletteManager()}.
	 * 
	 * @param plugin
	 * Requires a Cartographer plugin.
	 */
	public PaletteManager( Cartographer plugin ) {
	}
	
	/**
	 * Register a new {@link MinimapPalette}. Will overwrite any existing one with the same id.
	 * 
	 * @param name
	 * Cannot be null.
	 * @param palette
	 * Cannot be null.
	 */
	public void register( String name, MinimapPalette palette ) {
	}
	
	/**
	 * Construct a new {@link MinimapPalette} from a list of palette ids.
	 * 
	 * @param palettes
	 * A non null list of ids.
	 * @return
	 * A compound MinimapPalette.
	 */
	public MinimapPalette construct( List< String > palettes ) {
		return null;
	}
	
	
	/**
	 * Create a {@link MinimapPalette} from the config provided.
	 * 
	 * @param config
	 * Cannot be null, and has to follow a {@link MinimapPalette} format.
	 * @return
	 * A {@link MinimapPalette}.
	 */
	public MinimapPalette load( FileConfiguration config ) {
		return null;
	}
	
	/**
	 * Save the given {@link MinimapPalette} to file with the {@link ColorType} indicated.
	 * 
	 * @param palette
	 * The {@link MinimapPalette} to save. Cannot be null.
	 * @param config
	 * The config to save to. Cannot be null.
	 * @param format
	 * The ColorType to use. Cannot be null.
	 */
	public void save( MinimapPalette palette, FileConfiguration config, ColorType format ) {
	}
	
	/**
	 * Convert an integer color to a string.
	 * 
	 * @param color
	 * The integer color.
	 * @param type
	 * The format to use. Cannot be null.
	 * @return
	 * A string that follows the {@link ColorType} pattern.
	 */
	public String toString( int color, ColorType type ) {
		return null;
	}
	
	/**
	 * Convert a color to a string.
	 * 
	 * @param color
	 * Cannot be null.
	 * @param type
	 * The format to use. Cannot be null.
	 * @return
	 * A string that follows the {@link ColorType} pattern.
	 */
	public String toString( Color color, ColorType type ) {
		return null;
	}
	
	public CartographerLogger getLogger() {
		return logger;
	}
	
	/**
	 * Contains different ways to write a color.
	 * 
	 * @author BananaPuncher714
	 */
	public enum ColorType {
		/**
		 * Hex form, accepts 6 digits. Ex: '#FF00FF'
		 */
		HEX( "^#?([A-Fa-f0-9]{1,6})$" ),
		/**
		 * RGB form, accepts 3 bytes separated by non-digit characters. Ex: '( 255, 0, 128 )'
		 */
		RGB( "^\\D*?(\\d{1,3})\\D+(\\d{1,3})\\D+(\\d{1,3})\\D*?$" ),
		/**
		 * RGBA form, accepts 4 bytes with the 4th being the alpha of the color. Ex: '( 255, 128, 0, 170 )'
		 */
		RGBA( "^\\D*?(\\d{1,3})\\D+(\\d{1,3})\\D+(\\d{1,3})\\D+(\\d{1,3})\\D*?$" ),
		/**
		 * Integer form, accepts an 8 digit number only. Ex: '219203'
		 */
		INT( "^([0-9]?){8}$" );
		
		private Pattern pattern;
		
		ColorType( String pattern ) {
			this.pattern = Pattern.compile( pattern );
		}
		
		/**
		 * Check if the input matches this pattern.
		 * 
		 * @param string
		 * Cannot be null.
		 * @return
		 * Whether the input matches the pattern of this ColorType.
		 */
		public boolean matches( String string ) {
			Validate.notNull( string );
			return pattern.matcher( string ).matches();
		}
		
		/**
		 * Get the pattern for this color type.
		 * 
		 * @return
		 * The pattern.
		 */
		public Pattern getPattern() {
			return pattern;
		}
	}
}

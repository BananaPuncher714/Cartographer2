package io.github.bananapuncher714.cartographer.core.map.palette;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.CartographerLogger;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;

/**
 * Manage the {@link MinimapPalette} for {@link Minimap}.
 * 
 * @author BananaPuncher714
 */
public class PaletteManager {
	protected Cartographer plugin;
	
	protected Map< String, MinimapPalette > palettes = new HashMap< String, MinimapPalette >();
	
	protected CartographerLogger logger = new CartographerLogger( "PaletteManager" );
	
	/**
	 * Only one PaletteManager is recommended. To get the current one in use, call {@link Cartographer#getPaletteManager()}.
	 * 
	 * @param plugin
	 * Requires a Cartographer plugin.
	 */
	public PaletteManager( Cartographer plugin ) {
		this.plugin = plugin;
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
		Validate.notNull( name );
		Validate.notNull( palette );
		palettes.put( name, palette );
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
		Validate.notNull( palettes );
		MinimapPalette palette = new MinimapPalette();
		for ( String id : palettes ) {
			MinimapPalette template = this.palettes.get( id );
			if ( template == null ) {
				logger.warning( id + " palette not found!" );
				continue;
			}
		
			palette.setDefaultColor( template.getDefaultRGB() );
			
			for ( CrossVersionMaterial material : template.getMaterials() ) {
				palette.setColor( material, template.getRGB( material ) );
				palette.getTransparentBlocks().remove( material );
			}
			
			for ( CrossVersionMaterial material : template.getTransparentBlocks() ) {
				palette.getTransparentBlocks().add( material );
			}
		}
		return palette;
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
		Validate.notNull( config );
		MinimapPalette palette = new MinimapPalette();
		String defColor = config.getString( "default-color", "TRANSPARENT" );
		if ( defColor.equalsIgnoreCase( "TRANSPARENT" ) ) {
			palette.setDefaultColor( new Color( 0, true ) );
		} else {
			Color color;
			if ( ColorType.HEX.matches( defColor ) ) {
				Matcher matcher = ColorType.HEX.pattern.matcher( defColor );
				matcher.find();
				String hexString = matcher.group( 1 );
				int val = Integer.parseInt( hexString, 16 );
				color = new Color( val );
			} else if ( ColorType.INT.matches( defColor ) ) {
				Matcher matcher = ColorType.INT.pattern.matcher( defColor );
				matcher.find();
				String intString = matcher.group( 1 );
				color = new Color( Integer.parseInt( intString ) );
			} else if ( ColorType.RGB.matches( defColor ) ) {
				Matcher matcher = ColorType.RGB.pattern.matcher( defColor );
				matcher.find();
				String r = matcher.group( 1 );
				String g = matcher.group( 2 );
				String b = matcher.group( 3 );
				color = new Color( Integer.parseInt( r ), Integer.parseInt( g ), Integer.parseInt( b ) );
			} else {
				logger.warning( "Cannot parse default color. Invalid value: " + defColor );
				color = new Color( 0 );
			}
			palette.setDefaultColor( color );
		}
		if ( config.contains( "colors" ) ) {
			for ( String key : config.getConfigurationSection( "colors" ).getKeys( false ) ) {
				String[] matVals = key.split( "," );
				Material material = Material.getMaterial( matVals[ 0 ].toUpperCase() );
				int durability = matVals.length > 1 ? Integer.parseInt( matVals[ 1 ] ): 0;
				if ( material == null ) {
					if ( plugin.isPaletteDebug() ) {
						logger.warning( key + " is an invalid material!" );
					}
					continue;
				}
				CrossVersionMaterial cvMaterial = new CrossVersionMaterial( material, durability );
				
				String data = config.getString( "colors." + key );
				
				Color color;
				if ( ColorType.HEX.matches( data ) ) {
					Matcher matcher = ColorType.HEX.pattern.matcher( data );
					matcher.find();
					String hexString = matcher.group( 1 );
					int val = Integer.parseInt( hexString, 16 );
					color = new Color( val );
				} else if ( ColorType.INT.matches( data ) ) {
					Matcher matcher = ColorType.INT.pattern.matcher( data );
					matcher.find();
					String intString = matcher.group( 1 );
					color = new Color( Integer.parseInt( intString ) );
				} else if ( ColorType.RGB.matches( data ) ) {
					Matcher matcher = ColorType.RGB.pattern.matcher( data );
					matcher.find();
					String r = matcher.group( 1 );
					String g = matcher.group( 2 );
					String b = matcher.group( 3 );
					color = new Color( Integer.parseInt( r ), Integer.parseInt( g ), Integer.parseInt( b ) );
				} else {
					logger.warning( "Cannot parse material " + cvMaterial.material + ". Invalid color: " + data );
					continue;
				}
				palette.setColor( cvMaterial, color );
			}
		}
		
		if ( config.contains( "transparent-blocks" ) ) {
			for ( String val : config.getStringList( "transparent-blocks" ) ) {
				String[] matVals = val.split( "," );
				Material material = Material.getMaterial( matVals[ 0 ].toUpperCase() );
				int durability = matVals.length > 1 ? Integer.parseInt( matVals[ 1 ] ): 0;
				if ( material == null ) {
					if ( plugin.isPaletteDebug() ) {
						logger.warning( val + " is an invalid material!" );
					}
					continue;
				}
				CrossVersionMaterial cvMaterial = new CrossVersionMaterial( material, durability );
				palette.addTransparentMaterial( cvMaterial );
			}
		}
		return palette;
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
		Validate.notNull( palette );
		Validate.notNull( config );
		Validate.notNull( format );
		if ( ( ( palette.getDefaultRGB() >>> 24 ) & 0xFF ) == 0 ) {
			config.set( "default", "TRANSPARENT" );
		} else {
			config.set( "default", toString( palette.getDefaultRGB(), format ) );
		}
		// Alphabetically sort the strings
		Set< String > transparent = new TreeSet< String >();
		for ( CrossVersionMaterial mat : palette.getTransparentBlocks() ) {
			String key = mat.material.name();
			if ( mat.durability != 0 ) {
				key += "," + mat.durability;
			}
			transparent.add( key );
		}
		config.set( "transparent-blocks", new ArrayList< String >( transparent ) );

		Map< String, CrossVersionMaterial > keys = new TreeMap< String, CrossVersionMaterial >();
		for ( CrossVersionMaterial mat : palette.getMaterials() ) {
			String key = mat.material.name();
			if ( mat.durability != 0 ) {
				key += "," + mat.durability;
			}
			keys.put( key, mat );
		}
		for ( Entry< String, CrossVersionMaterial > entry : keys.entrySet() ) {
			config.set( "colors." + entry.getKey(), toString( palette.getRGB( entry.getValue() ), format ) );
		}
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
		Validate.notNull( type );
		if ( type == ColorType.HEX ) {
			return "#" + Integer.toHexString( color );
		} else if ( type == ColorType.INT ) {
			return "" + ( color & 0xFFFFFF );
		} else if ( type == ColorType.RGB ) {
			return "( " + ( color >> 16 & 0xFF ) + ", " + ( color >> 8 & 0xFF ) + ", " + ( color & 0xFF ) + " )";
		} else {
			throw new IllegalArgumentException( "Unknown ColorType provided! " + type.name() );
		}
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
		Validate.notNull( color );
		return toString( color.getRGB(), type );
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

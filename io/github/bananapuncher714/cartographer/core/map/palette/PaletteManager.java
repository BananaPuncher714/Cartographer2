package io.github.bananapuncher714.cartographer.core.map.palette;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;

/**
 * @author BananaPuncher714
 *
 */
public class PaletteManager {
	protected Cartographer plugin;
	
	protected Map< String, MinimapPalette > palettes = new HashMap< String, MinimapPalette >();
	
	/**
	 * @param plugin
	 */
	public PaletteManager( Cartographer plugin ) {
		this.plugin = plugin;
	}
	
	/**
	 * @param name
	 * @param palette
	 */
	public void register( String name, MinimapPalette palette ) {
		palettes.put( name, palette );
	}
	
	/**
	 * @param palettes
	 * @return
	 */
	public MinimapPalette construct( List< String > palettes ) {
		MinimapPalette palette = new MinimapPalette();
		for ( String id : palettes ) {
			MinimapPalette template = this.palettes.get( id );
			if ( template == null ) {
				plugin.getLogger().warning( id + " palette not found!" );
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
	 * @param config
	 * @return
	 */
	public MinimapPalette load( FileConfiguration config ) {
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
				plugin.getLogger().warning( "Cannot parse default color. Invalid value: " + defColor );
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
						plugin.getLogger().warning( key + " is an invalid material!" );
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
					plugin.getLogger().warning( "Cannot parse material " + cvMaterial.material + ". Invalid color: " + data );
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
						plugin.getLogger().warning( val + " is an invalid material!" );
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
	 * @param palette
	 * @param config
	 * @param format
	 */
	public void save( MinimapPalette palette, FileConfiguration config, ColorType format ) {
		if ( ( ( palette.getDefaultRGB() >>> 24 ) & 0xFF ) == 0 ) {
			config.set( "default", "TRANSPARENT" );
		} else {
			config.set( "default", toString( palette.getDefaultRGB(), format ) );
		}
		List< String > transparent = new ArrayList< String >();
		for ( CrossVersionMaterial mat : palette.getTransparentBlocks() ) {
			String key = mat.material.name();
			if ( mat.durability != 0 ) {
				key += "," + mat.durability;
			}
			transparent.add( key );
		}
		config.set( "transparent-blocks", transparent );
		
		for ( CrossVersionMaterial mat : palette.getMaterials() ) {
			String key = mat.material.name();
			if ( mat.durability != 0 ) {
				key += "," + mat.durability;
			}
			config.set( "colors." + key, toString( palette.getRGB( mat ), format ) );
		}
	}
	
	/**
	 * @param color
	 * @param type
	 * @return
	 */
	public String toString( int color, ColorType type ) {
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
	 * @param color
	 * @param type
	 * @return
	 */
	public String toString( Color color, ColorType type ) {
		return toString( color.getRGB(), type );
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

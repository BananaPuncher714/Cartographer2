package io.github.bananapuncher714.cartographer.module.experimental;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.MapPixel;
import io.github.bananapuncher714.cartographer.core.api.command.CommandBase;
import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters;
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand;
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutableMessage;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorInt;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPlayer;
import io.github.bananapuncher714.cartographer.core.locale.Locale;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.module.Module;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.module.experimental.ChunkBorderShader.ChunkBorderData;
import io.github.bananapuncher714.cartographer.module.experimental.font.BananaFontParser;
import io.github.bananapuncher714.cartographer.module.experimental.font.BananaTypeFont;
import io.github.bananapuncher714.cartographer.module.experimental.font.PixelGlyph;
import io.github.bananapuncher714.cartographer.module.experimental.menu.OverviewMenu;

public class ExperimentalModule extends Module {
	private BananaTypeFont defaultFont;
	private BananaTypeFont asciiFont;
	private BananaTypeFont unicodeFont;
	
	private String testString;
	private Color color = new Color( 0 );
	
	private SubCommand experimental;
	
	private Set< ChunkLocation > mapped = new HashSet< ChunkLocation >();
	private Set< ChunkBorderData > faces = new HashSet< ChunkBorderData >();
	
	int tick = 0;
	
	@Override
	public void onEnable() {
		registerLocales();
		
		try {
			defaultFont = BananaFontParser.createFont( getResource( "data/minecraft-font.btf" ) );
			asciiFont = BananaFontParser.createFont( getResource( "data/minecraft-font-ascii.btf" ) );
			unicodeFont = BananaFontParser.createFont( getResource( "data/minecraft-font-unicode.btf" ) );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		
//		printGlyph( defaultFont.get( '!' ), false );
//		printGlyph( defaultFont.get( '#' ), false );
//		printGlyph( defaultFont.get( '^' ), false );
//		printGlyph( defaultFont.get( 'l' ), false );
//		printGlyph( defaultFont.get( 'w' ), false );
//		printGlyph( defaultFont.get( '死' ), true );
		
		experimental = new SubCommand( "experimental" )
				.add( new SubCommand( "setstring" )
						.defaultTo( this::setString ) )
				.add( new SubCommand( "setcolor" )
						.add( new SubCommand( new InputValidatorInt( 0, 0xFFFFFF ) )
								.defaultTo( this::setColor ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "You must provide a color!" ) ) )
				.add( new SubCommand( "menu" )
						.addSenderValidator( new SenderValidatorPlayer() )
						.defaultTo( this::menu ) )
				.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide an argument!" ) );
		
		registerCommand( new CommandBase( "experimental" )
				.setSubCommand( experimental )
				.setDescription( "Experimental command" )
				.build() );
		
		registerCommand( new CommandBase( "reset" )
				.setSubCommand( new SubCommand( "reset" )
						.defaultTo( ( sender, args, params ) -> { mapped.clear(); } ) )
				.setDescription( "Reset the explored area" )
				.build() );
		tick = 0;
		
		for ( Minimap minimap : getCartographer().getMapManager().getMinimaps().values() ) {
			minimap.registerProvider( new TextPixelProvider( this ) );
			minimap.register( new ChunkBorderShader( this::getLocations ) );
		}
		
		registerListener( new MapListener( this ) );

		runTaskTimer( this::tick, 20, 20 );
	}
	
	@Override
	public Collection< Locale > getLocales() {
		List< Locale > locales = new ArrayList< Locale >();
		locales.add( convertToDefaultLocale( loadLocale( getResource( "data/locale/test.yml" ) ) ) );
		return locales;
	}
	
	protected void addExplored( ChunkLocation location ) {
		mapped.add( location );
	}
	
	private void tick() {
		// Re-render the map
		faces.clear();
		Map< ChunkLocation, Set< BlockFace > > combined = ChunkBorderShader.getBorders( mapped );
		for ( Entry< ChunkLocation, Set< BlockFace > > entry : combined.entrySet() ) {
			ChunkBorderData data = new ChunkBorderData( entry.getKey(), Color.RED );
			data.getFaces().addAll( entry.getValue() );
			faces.add( data );
		}
	}
	
	private void setString( CommandSender sender, String[] args, CommandParameters params ) {
		StringBuilder builder = new StringBuilder();
		for ( String arg : args ) {
			builder.append( arg );
			builder.append( " " );
		}
		
		testString = builder.toString().trim();
		sender.sendMessage( "Test string set to " + testString );
		translateAndSend( sender, "test" );
	}
	
	private void setColor( CommandSender sender, String[] args, CommandParameters parameters ) {
		color = new Color( parameters.getLast( Integer.class ) );
		sender.sendMessage( String.format( "Color set to %x", color.getRGB() ) );
	}
	
	private void menu( CommandSender sender, String[] args, CommandParameters parameters ) {
		Player player = ( Player ) sender;
		ItemStack item = Cartographer.getUtil().getMainHandItem( player );
		if ( item == null || !getCartographer().getMapManager().isMinimapItem( item ) ) {
			sender.sendMessage( ChatColor.RED + "You must be holding a minimap!" );
			return;
		}
		CartographerRenderer renderer = getCartographer().getMapManager().getRendererFrom( Cartographer.getUtil().getMapViewFrom( item ) );
		renderer.setMapMenu( player.getUniqueId(), new OverviewMenu( getCartographer() ) );
	}
	
	public String getTestString() {
		return testString;
	}
	
	public Color getColor() {
		return color;
	}
	
	public BananaTypeFont getAsciiFont() {
		return asciiFont;
	}
	
	public BananaTypeFont getUnicodeFont() {
		return unicodeFont;
	}
	
	public Set< ChunkBorderData > getLocations( Player player, PlayerSetting setting ) {
		return faces;
	}
	
	public Collection< MapPixel > getFor( String message, int x, int y, Color color ) {
		Set< MapPixel > pixels = new HashSet< MapPixel >();
		int pos = x;
		int height = y;
		for ( char c : message.toCharArray() ) {
			if ( c == '\n' ) {
				height += unicodeFont.getMaxHeight() + 1;
				pos = x;
			} else if ( c == '\t' ) {
				pos += 24;
			} else {
				PixelGlyph glyph = unicodeFont.get( c );
				if ( glyph != null ) {
					for ( int ly = 0; ly < glyph.getHeight(); ly++ ) {
						int yIndex = ly * glyph.getWidth();
						for ( int lx = 0; lx < glyph.getWidth(); lx++ ) {
							if ( glyph.getData()[ yIndex + lx ] ) {
								pixels.add( new MapPixel( pos + lx, height - ly, color ) );
								
								if ( lx == glyph.getWidth() - 1 || ly == 0 || !glyph.getData()[ ( ly - 1 ) * glyph.getWidth() + lx + 1 ] ) {
									pixels.add( new MapPixel( pos + lx + 1, height - ly + 1, color.darker().darker().darker() ) );
								}
							}
						}
					}
					
					pos += glyph.getWidth();
				}
				pos += unicodeFont.getMaxWidth() >>> 3;
			}
		}
		return pixels;
	}
	
	public static void printGlyph( PixelGlyph glyph, boolean stretch ) {
		System.out.println( glyph.getChar() + "\t" + glyph.getWidth() + "x" + glyph.getHeight() );
		for ( int y = glyph.getHeight() - 1; y >= 0; y-- ) {
			int yIndex = y * glyph.getWidth();
			StringBuilder builder = new StringBuilder();
			for ( int x = 0; x < glyph.getWidth(); x++ ) {
				if ( stretch ) {
					builder.append( glyph.getData()[ x + yIndex ] ? "88" : "  " );
				} else {
					builder.append( glyph.getData()[ x + yIndex ] ? "8" : " " );
				}
			}
			builder.append( "|" );
			System.out.println( builder.toString() );
		}
	}
}
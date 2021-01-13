package io.github.bananapuncher714.cartographer.module.worldviewer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.CommandBase;
import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters;
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.core.locale.Locale;
import io.github.bananapuncher714.cartographer.core.module.Module;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;
import io.github.bananapuncher714.cartographer.core.util.FileUtil;
import io.github.bananapuncher714.cartographer.module.worldviewer.font.BananaFontParser;
import io.github.bananapuncher714.cartographer.module.worldviewer.font.BananaTypeFont;
import io.github.bananapuncher714.cartographer.module.worldviewer.menu.OverviewMenu;
import io.github.bananapuncher714.cartographer.module.worldviewer.menu.TextOverlayComponent;

public class WorldViewer extends Module {
	private static final String MESSAGE_MUST_BE_PLAYER = "must-be-player";
	private static final String MESSAGE_MUST_HOLD_MINIMAP = "must-hold-minimap";
	private static final String MAP_TEXT = "coord-info.%d";
	
	private BananaTypeFont defaultFont;
	private BananaTypeFont unicodeFont;
	
	private SettingStateDouble MOVEMENT_MODIFIER;
	private File CONFIG_FILE;
	private File LOCALE_DIR;
	
	private double defaultScale;
	private double[] scales;
	
	private boolean enabled = true;
	private int x;
	private int y;
	private boolean unicode;
	private boolean shadow;
	private int tabDistance;
	private int charDistance;
	private int spaceDistance;
	private int lineSpacing;
	
	@Override
	public void onEnable() {
		CONFIG_FILE = new File( getDataFolder(), "config.yml" );
		LOCALE_DIR = new File( getDataFolder() + "/locale/" );
		
		FileUtil.saveToFile( getResource( "README.md" ), new File( getDataFolder(), "README.md" ), false );
		FileUtil.saveToFile( getResource( "data/locale/en_us.yml" ), new File( LOCALE_DIR, "en_us.yml" ), false );
		FileUtil.saveToFile( getResource( "config.yml" ), CONFIG_FILE, false );
		
		registerLocales();

		try {
			defaultFont = BananaFontParser.createFont( getResource( "data/font/minecraft-font.btf" ) );
			unicodeFont = BananaFontParser.createFont( getResource( "data/font/minecraft-font-unicode.btf" ) );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		
		loadConfig();
		
		registerSettings();
		
		registerCommand( new CommandBase( "worldviewer" )
				.setPermission( "worldviewer.use" )
				.setDescription( "View the world" )
				.setSubCommand( new SubCommand( "worldviewer" )
						.defaultTo( this::menu ) )
				.build() );
	}
	
	private void loadConfig() {
		FileConfiguration config = YamlConfiguration.loadConfiguration( CONFIG_FILE );
		
		MOVEMENT_MODIFIER = new SettingStateDouble( "worldviewer_movement_modifier", false, config.getDouble( "map-movement-modifier" ), 1, 2, 3, 4 );
		
		defaultScale = config.getDouble( "default-zoom" );
		List< Double > zooms = config.getDoubleList( "allowed-zooms" );
		Collections.sort( zooms );
		
		scales = new double[ zooms.size() ];
		for ( int i = 0; i < zooms.size(); i++ ) {
			scales[ i ] = zooms.get( i );
		}
		
		enabled = config.getBoolean( "text.enabled", true );
		x = config.getInt( "text.x" );
		y = config.getInt( "text.y" );
		unicode = config.getBoolean( "text.unicode" );
		shadow = config.getBoolean( "text.shadow" );
		tabDistance = config.getInt( "text.tab-distance" );
		charDistance = config.getInt( "text.character-distance" );
		spaceDistance = config.getInt( "text.space" );
		lineSpacing = config.getInt( "text.line-space" );
	}
	
	@Override
	public Collection< Locale > getLocales() {
		Set< Locale > locales = new HashSet< Locale >();
		
		locales.add( convertToDefaultLocale( loadLocale( getResource( "data/locale/en_us.yml" ) ) ) );
		locales.addAll( loadLocale( LOCALE_DIR ) );
		
		return locales;
	}
	
	@Override
	public SettingState< ? >[] getSettingStates() {
		return new SettingState< ? >[] { MOVEMENT_MODIFIER };
	}
	
	public SettingStateDouble getSetting() {
		return MOVEMENT_MODIFIER;
	}
	
	public double getDefaultScale() {
		return defaultScale;
	}

	public double[] getScales() {
		return scales;
	}

	public BananaTypeFont getDefaultFont() {
		return defaultFont;
	}
	
	public BananaTypeFont getUnicodeFont() {
		return unicodeFont;
	}
	
	private void menu( CommandSender sender, String[] args, CommandParameters parameters ) {
		if ( sender instanceof Player ) {
			Player player = ( Player ) sender;
			ItemStack item = Cartographer.getUtil().getMainHandItem( player );
			if ( item == null || !getCartographer().getMapManager().isMinimapItem( item ) ) {
				translateAndSend( sender, MESSAGE_MUST_HOLD_MINIMAP );
				return;
			}
			CartographerRenderer renderer = getCartographer().getMapManager().getRendererFrom( Cartographer.getUtil().getMapViewFrom( item ) );
			renderer.resetCursorFor( player );
			
			OverviewMenu menu = new OverviewMenu( getCartographer(), this );
			if ( enabled ) {
				TextOverlayComponent overlay = new TextOverlayComponent( this, menu );
	
				overlay.setX( x );
				overlay.setY( y );
				overlay.setUnicode( unicode );
				overlay.setShadow( shadow );
				overlay.setSpaceDistance( spaceDistance );
				overlay.setTabDistance( tabDistance );
				overlay.setCharDistance( charDistance );
				overlay.setLineSpacing( lineSpacing );
				overlay.setKey( MAP_TEXT );
				
				menu.addComponent( overlay );
			}
			renderer.setMapMenu( player.getUniqueId(), menu );
		} else {
			translateAndSend( sender, MESSAGE_MUST_BE_PLAYER );
		}
	}
}

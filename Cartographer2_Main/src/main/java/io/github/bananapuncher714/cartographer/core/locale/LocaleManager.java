package io.github.bananapuncher714.cartographer.core.locale;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.CartographerLogger;
import io.github.bananapuncher714.cartographer.core.api.events.locale.DefaultLocaleChangeEvent;
import io.github.bananapuncher714.cartographer.core.api.events.locale.LocaleFinishReloadEvent;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;
import io.github.bananapuncher714.cartographer.core.module.Module;

public class LocaleManager {
	private static final String INTERNAL_DEFAULT_LOCALE_LOCATION = "data/locale/en_us.yml";
	
	private Cartographer plugin;
	private File dataFolder;
	private String defaultLocale = "default";
	private Map< String, Locale > locales = new HashMap< String, Locale >();
	
	protected CartographerLogger logger = new CartographerLogger( "LocaleManager" );
	
	public LocaleManager( Cartographer plugin, File dataFolder ) {
		this.plugin = plugin;
		this.dataFolder = dataFolder;
		dataFolder.mkdirs();
	}

	public String getDefaultLocale() {
		return defaultLocale;
	}

	public void setDefaultLocale( String defaultLocale ) {
		if ( !this.defaultLocale.equalsIgnoreCase( defaultLocale ) ) {
			DefaultLocaleChangeEvent event = new DefaultLocaleChangeEvent( this.defaultLocale, defaultLocale );
			event.callEvent();
			this.defaultLocale = event.getNewLocale();
			logger.infoTr( LocaleConstants.LOCALE_DEFAULT_LOCALE_CHANGED, this.defaultLocale );
		}
	}

	public File getDataFolder() {
		return dataFolder;
	}
	
	public String translateDefault( CommandSender sender, String key, Object... params ) {
		Locale locale = getLocale( defaultLocale );
		if ( locale == null ) {
			throw new IllegalArgumentException( translateDefault( Bukkit.getConsoleSender(), LocaleConstants.LOCALE_LOCALE_INVALID, defaultLocale ) );
		}
		
		return plugin.getDependencyManager().translateString( sender, locale.get( sender, key, params ) );
	}
	
	public String translate( String code, CommandSender sender, String key, Object... params ) {
		Locale locale = getLocale( code );
		if ( locale == null ) {
			return translateDefault( sender, key, params );
		}
		String message = locale.get( sender, key, params );

		// If the locale doesn't have anything, then try the default locale
		if ( message == null ) {
			return translateDefault( sender, key, params );
		}
		return plugin.getDependencyManager().translateString( sender, message );
	}
	
	public String translateFor( CommandSender sender, String key, Object... params ) {
		String locale = defaultLocale;
		if ( sender instanceof Player ) {
			Player player = ( Player ) sender;
			MapViewer viewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
			locale = viewer.getSetting( MapViewer.LOCALE );
		}
		
		return plugin.getLocaleManager().translate( locale, sender, key, params );
	}
	
	public String translateAndSend( String code, CommandSender sender, String key, Object... params ) {
		String message = translate( code, sender, key, params );
		if ( message != null && !message.isEmpty() ) {
			sender.sendMessage( message );
		}
		return message;
	}
	
	public String translateAndSend( CommandSender sender, String key, Object... params ) {
		String message = translateFor( sender, key, params );
		if ( message != null && !message.isEmpty() ) {
			sender.sendMessage( message );
		}
		return message;
	}
	
	public Locale getLocale( String code ) {
		return locales.get( code );
	}
	
	public Set< String > getLocaleCodes() {
		return locales.keySet();
	}
	
	public void register( Locale locale ) {
		if ( locales.containsKey( locale.getCode() ) ) {
			Locale l = locales.get( locale.getCode() );
			
			l.merge( locale );
			
		} else {
			locales.put( locale.getCode(), locale );
		}
	}
	
	public void add( String code, String key, LocaleMessage message ) {
		Locale locale = new Locale( "Unknown", "Unknown", "Unknown", key );
		locale.add( key, message );
		if ( locales.containsKey( code ) ) {
			Locale l = locales.get( code );
			
			l.merge( locale );
		} else {
			locales.put( locale.getCode(), locale );
		}
	}
	
	public void remove( String code ) {
		locales.remove( code );
	}
	
	public void reload() {
		locales.clear();
		logger.info( "Loading default locale" );
		Locale internal = load( plugin.getResource( INTERNAL_DEFAULT_LOCALE_LOCATION ) );
		locales.put( defaultLocale, internal );
		logger.infoTr( LocaleConstants.LOCALE_INTERNAL_LOCALE_LOADED, defaultLocale );
		for ( File file : dataFolder.listFiles() ) {
			if ( file.getName().endsWith( ".yml" ) ) {
				try {
					Locale loaded = load( file );
					register( loaded );
					logger.infoTr( LocaleConstants.LOCALE_LOCALE_LOADED, loaded.getName(), loaded.getLocation(), loaded.getCode() );
				} catch ( IllegalArgumentException e ) {
					logger.severeTr( LocaleConstants.LOCALE_FILE_LOAD_ERROR, file.getName() );
					e.printStackTrace();
				}
			}
		}
		
		// Now that we've loaded all the global locales, re-load the module locales since we cleared those out
		for ( Module module : plugin.getModuleManager().getModules() ) {
			for ( Locale locale : module.getLocales() ) {
				register( locale );
			}
		}
		
		new LocaleFinishReloadEvent().callEvent();
	}
	
	public Locale load( File file ) {
		FileConfiguration config = YamlConfiguration.loadConfiguration( file );
		return load( config );
	}
	
	public Locale load( InputStream stream ) {
		InputStreamReader reader = new InputStreamReader( stream, StandardCharsets.UTF_8 );
		return load( YamlConfiguration.loadConfiguration( reader ) );
	}
	
	public Locale load( FileConfiguration config ) {
		return load( config, null );
	}
	
	public Locale load( FileConfiguration config, String prefix ) {
		String name = config.getString( "name", "Unknown" );
		String lang = config.getString( "language", "Unknown" );
		String loc = config.getString( "location", "Unknown" );
		String code = config.getString( "locale-code" );
		if ( code == null || code.isEmpty() ) {
			throw new IllegalArgumentException( "Locale file must have a code!" );
		}
		
		Locale locale = new Locale( name, lang, loc, code );
		
		ConfigurationSection section = config.getConfigurationSection( "messages" );
		for ( String key : section.getKeys( true ) ) {
			LocaleMessage message = null;
			if ( section.isString( key ) ) {
				message = new LocaleMessageString( section.getString( key ) );
			} else if ( section.isList( key ) ){
				message = new LocaleMessageRandom( section.getStringList( key ) );
			}
			
			if ( message != null ) {
				if ( prefix == null || prefix.isEmpty() ) {
					locale.add( key, message );
				} else {
					locale.add( prefix + "." + key, message );
				}
			}
		}
		
		return locale;
	}
}

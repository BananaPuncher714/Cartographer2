package io.github.bananapuncher714.cartographer.core.locale;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.CartographerLogger;

public class LocaleManager {
	protected CartographerLogger logger = new CartographerLogger( "LocaleManager" );
	
	public LocaleManager( Cartographer plugin, File dataFolder ) {
	}

	public String getDefaultLocale() {
		return null;
	}

	public void setDefaultLocale( String defaultLocale ) {
	}

	public File getDataFolder() {
		return null;
	}
	
	public String translateDefault( CommandSender sender, String key, Object... params ) {
		return null;
	}
	
	public String translate( String code, CommandSender sender, String key, Object... params ) {
		return null;
	}
	
	public String translateFor( CommandSender sender, String key, Object... params ) {
		return null;
	}
	
	public String translateAndSend( String code, CommandSender sender, String key, Object... params ) {
		return null;
	}
	
	public String translateAndSend( CommandSender sender, String key, Object... params ) {
		return null;
	}
	
	public Locale getLocale( String code ) {
		return null;
	}
	
	public Set< String > getLocaleCodes() {
		return null;
	}
	
	public void register( Locale locale ) {
	}
	
	public void add( String code, String key, LocaleMessage message ) {
	}
	
	public void remove( String code ) {
	}
	
	public void reload() {
	}
	
	public Locale load( File file ) {
		return null;
	}
	
	public Locale load( InputStream stream ) {
		return null;
	}
	
	public Locale load( FileConfiguration config ) {
		return null;
	}
	
	public Locale load( FileConfiguration config, String prefix ) {
		return null;
	}
}

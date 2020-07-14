package io.github.bananapuncher714.cartographer.core;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

public class CartographerLogger extends Logger {
	private final String format;

	public CartographerLogger( String id ) {
		super( id, null );
		format = "[%s] [" + id + "] %s";
		setParent( Cartographer.getInstance().getLogger() );
		setLevel( Level.ALL );
	}

	@Override
	public void log( LogRecord record ) {
		if ( record.getMessage() != null && !record.getMessage().isEmpty() ) {
			record.setMessage( String.format( format, Cartographer.getInstance().getName(), record.getMessage() ) );
			super.log( record );
		}
	}
	
	public void infoTr( String key, Object... params ) {
		String message = Cartographer.getInstance().getLocaleManager().translateDefault( Bukkit.getConsoleSender(), key, params );
		if ( message != null && !message.isEmpty() ) {
			info( message );
		}
	}
	
	public void warningTr( String key, Object... params ) {
		String message = Cartographer.getInstance().getLocaleManager().translateDefault( Bukkit.getConsoleSender(), key, params );
		if ( message != null && !message.isEmpty() ) {
			warning( message );
		}
	}
	
	public void severeTr( String key, Object... params ) {
		String message = Cartographer.getInstance().getLocaleManager().translateDefault( Bukkit.getConsoleSender(), key, params );
		if ( message != null && !message.isEmpty() ) {
			severe( message );
		}
	}
}

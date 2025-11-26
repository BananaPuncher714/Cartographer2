package io.github.bananapuncher714.cartographer.core.module;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

import io.github.bananapuncher714.cartographer.core.Cartographer;

public class ModuleLogger extends Logger {
	private Module module;
	private String format = "[%s] [Module] [%s] %s";
	
	protected ModuleLogger( Module module ) {
		super( module.getName(), null );
		this.module = module;
		setParent( Cartographer.getInstance().getLogger() );
		setLevel( Level.ALL );
	}

	@Override
	public void log( LogRecord record ) {
		record.setMessage( String.format( format, Cartographer.getInstance().getName(), module.getName(), record.getMessage() ) );
		super.log( record );
	}
	
	public void infoTr( String key, Object... params ) {
		String message = module.translate( Bukkit.getConsoleSender(), key, params );
		if ( message != null && !message.isEmpty() ) {
			info( message );
		}
	}
	
	public void warningTr( String key, Object... params ) {
		String message = module.translate( Bukkit.getConsoleSender(), key, params );
		if ( message != null && !message.isEmpty() ) {
			warning( message );
		}
	}
	
	public void severeTr( String key, Object... params ) {
		String message = module.translate( Bukkit.getConsoleSender(), key, params );
		if ( message != null && !message.isEmpty() ) {
			severe( message );
		}
	}
}

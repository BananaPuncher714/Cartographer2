package io.github.bananapuncher714.cartographer.core;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

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
		record.setMessage( String.format( format, Cartographer.getInstance().getName(), record.getMessage() ) );
		super.log( record );
	}
}

package io.github.bananapuncher714.cartographer.core;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class CartographerLogger extends Logger {
	public CartographerLogger( String id ) {
		super( id, null );
	}

	@Override
	public void log( LogRecord record ) {
	}
	
	public void infoTr( String key, Object... params ) {
	}
	
	public void warningTr( String key, Object... params ) {
	}
	
	public void severeTr( String key, Object... params ) {
	}
}

package io.github.bananapuncher714.cartographer.core.module;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ModuleLogger extends Logger {
	protected ModuleLogger( Module module ) {
		super( module.getName(), null );
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

package io.github.bananapuncher714.cartographer.core.module;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import io.github.bananapuncher714.cartographer.core.Cartographer;

public class ModuleLogger extends Logger {
	private String format = "[%s] [Module] [%s] %s";
	private String moduleName;
	
	protected ModuleLogger( Module module ) {
		super( module.getName(), null );
		moduleName = module.getName();
		setParent( Cartographer.getInstance().getLogger() );
		setLevel( Level.ALL );
	}

	@Override
	public void log( LogRecord record ) {
		record.setMessage( String.format( format, Cartographer.getInstance().getName(), moduleName, record.getMessage() ) );
		super.log( record );
	}
}

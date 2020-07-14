package io.github.bananapuncher714.cartographer.core.locale;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;

public class Locale {
	private final String name;
	private final String language;
	private final String location;
	private final String code;
	
	private Map< String, LocaleMessage > messages = new HashMap< String, LocaleMessage >();

	public Locale( String name, String language, String location, String code ) {
		this.name = name;
		this.language = language;
		this.location = location;
		this.code = code;
	}

	public String getName() {
		return name;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public String getLocation() {
		return location;
	}
	
	public String getCode() {
		return code;
	}
	
	public Set< String > getKeys() {
		return messages.keySet();
	}
	
	protected Locale add( String key, LocaleMessage message ) {
		messages.put( key, message );
		return this;
	}
	
	protected Locale merge( Locale locale ) {
		messages.putAll( locale.messages );
		return this;
	}
	
	public String get( CommandSender sender, String key, Object... params ) {
		LocaleMessage message = messages.get( key );
		if ( message != null ) {
			return messages.get( key ).getMessageFor( sender, params );
		}
		return null;
	}
	
	public Locale copyOf() {
		return copyOf( name, language, location, code );
	}
	
	public Locale copyOf( String name, String language, String location, String code ) {
		if ( name == null ) {
			name = this.name;
		}
		
		if ( language == null ) {
			language = this.language;
		}
		
		if ( location == null ) {
			location = this.location;
		}
		
		if ( code == null || code.isEmpty() ) {
			code = this.code;
		}
		
		return new Locale( name, language, location, code ).merge( this );
	}
}
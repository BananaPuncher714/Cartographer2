package io.github.bananapuncher714.cartographer.core.locale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;

public class LocaleMessageRandom extends LocaleMessage {
	private Map< String, Integer > messages;
	
	public LocaleMessageRandom( String message, String... messages ) {
		this.messages = new HashMap< String, Integer >();
		this.messages.put( message, 1 );
		for ( String str : messages ) {
			this.messages.put( str, 1 );
		}
	}
	
	public LocaleMessageRandom( Map< String, Integer > messages ) {
		this.messages = messages;
	}
	
	public LocaleMessageRandom( List< String > messages ) {
		this.messages = new HashMap< String, Integer >();
		for ( String str : messages ) {
			this.messages.put( str, 1 );
		}
	}
	
	public Set< String > getMessages() {
		return messages.keySet();
	}
	
	@Override
	public String getMessageFor( CommandSender sender, Object... params ) {
		return String.format( getRandom( messages ), params );
	}

	private static < T > T getRandom( Map< T, Integer > objects ) {
		int sum = 0;
		for ( int i : objects.values() ) {
			sum = sum + i;
		}
		List< T > items = new ArrayList< T >( objects.keySet() );
		int randomIndex = -1;
		double random = Math.random() * sum;
		for ( int i = 0; i < items.size(); ++i ) {
		    random -= objects.get( items.get( i ) );
		    if ( random <= 0.0d )  {
		        randomIndex = i;
		        break;
		    }
		}
		return items.get( randomIndex );
	}
}

package io.github.bananapuncher714.cartographer.core.locale;

import org.bukkit.command.CommandSender;

public class LocaleMessageString extends LocaleMessage {
	protected String message;
	
	public LocaleMessageString( String message ) {
		this.message = message;
	}
	
	@Override
	public String getMessageFor( CommandSender sender, Object... params ) {
		return String.format( message, params );
	}
}

package io.github.bananapuncher714.cartographer.core.api.command.executor;

import org.bukkit.command.CommandSender;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters;
import io.github.bananapuncher714.cartographer.core.module.Module;

public class CommandExecutableMessageLocale implements CommandExecutable {
	protected Module module;
	protected String key;
	protected Object[] params;
	
	public CommandExecutableMessageLocale( Module module, String key, Object... params ) {
		this.module = module;
		this.key = key;
		this.params = params;
	}
	
	public CommandExecutableMessageLocale( String key, Object... params ) {
		this.key = key;
		this.params = params;
	}
	
	@Override
	public void execute( CommandSender sender, String[] args, CommandParameters params ) {
		if ( module == null ) {
			Cartographer.getInstance().getLocaleManager().translateAndSend( sender, key, params );
		} else {
			module.translateAndSend( sender, key, params );
		}
	}
}

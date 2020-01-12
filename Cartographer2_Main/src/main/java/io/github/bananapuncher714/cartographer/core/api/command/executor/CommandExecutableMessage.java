package io.github.bananapuncher714.cartographer.core.api.command.executor;

import org.bukkit.command.CommandSender;

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters;

public class CommandExecutableMessage implements CommandExecutable {
	protected String message;
	
	public CommandExecutableMessage( String message ) {
		this.message = message;
	}
	
	@Override
	public void execute( CommandSender sender, String[] args, CommandParameters params ) {
		sender.sendMessage( message );
	}
}

package io.github.bananapuncher714.cartographer.core.api.command.executor;

import org.bukkit.command.CommandSender;

import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters;

public interface CommandExecutable {
	void execute( CommandSender sender, String[] args, CommandParameters params );
}

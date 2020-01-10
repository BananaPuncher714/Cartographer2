package io.github.bananapuncher714.cartographer.core.api.command;

import org.bukkit.command.CommandSender;

public interface CommandExecutable {
	void execute( CommandSender sender, String[] args );
}

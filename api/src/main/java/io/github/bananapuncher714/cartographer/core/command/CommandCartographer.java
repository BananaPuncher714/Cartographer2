package io.github.bananapuncher714.cartographer.core.command;

import org.bukkit.command.PluginCommand;

import io.github.bananapuncher714.cartographer.core.Cartographer;

/**
 * Base Cartographer command.
 * 
 * @author BananaPuncher714
 */
public class CommandCartographer {
	public CommandCartographer( Cartographer plugin, PluginCommand command ) {
	}
	
	public CommandSettings getSettingsCommand() {
		return null;
	}
	
	public CommandModule getModuleCommand() {
		return null;
	}

	public void rebuildCommand() {
	}
}

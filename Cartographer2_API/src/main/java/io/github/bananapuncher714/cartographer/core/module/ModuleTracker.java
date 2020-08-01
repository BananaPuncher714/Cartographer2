package io.github.bananapuncher714.cartographer.core.module;

import java.util.Set;

import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;

public class ModuleTracker {
	public Set< Listener > getListeners() {
		return null;
	}
	
	public Set< PluginCommand > getCommands() {
		return null;
	}

	public Set< BukkitTask > getTasks() {
		return null;
	}
	
	public Set< SettingState< ? > > getSettings() {
		return null;
	}
}

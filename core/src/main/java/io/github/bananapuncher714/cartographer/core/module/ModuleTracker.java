package io.github.bananapuncher714.cartographer.core.module;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;

public class ModuleTracker {
	private Set< Listener > listeners = new HashSet< Listener >();
	private Set< PluginCommand > commands = new HashSet< PluginCommand >();
	private Set< BukkitTask > tasks = new HashSet< BukkitTask >();
	private Set< SettingState< ? > > states = new HashSet< SettingState< ? > >();
	
	public Set< Listener > getListeners() {
		return listeners;
	}
	
	public Set< PluginCommand > getCommands() {
		return commands;
	}

	public Set< BukkitTask > getTasks() {
		return tasks;
	}
	
	public Set< SettingState< ? > > getSettings() {
		return states;
	}
}

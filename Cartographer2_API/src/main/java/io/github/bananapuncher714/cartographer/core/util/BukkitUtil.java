package io.github.bananapuncher714.cartographer.core.util;

import org.bukkit.command.PluginCommand;
import org.bukkit.event.Event;

public class BukkitUtil {
	public static PluginCommand constructCommand( String id ) {
		return null;
	}
	
	/**
	 * Call an event synchronously, on the main thread
	 */
	public static void callEventSync( Event event ) {
	}
	
	/**
	 * Check if the plugin given is loaded
	 * 
	 * @param id
	 * case sensitive plugin id
	 * @return
	 * If the plugin is loaded and enabled
	 */
	public static boolean isPluginLoaded( String id ) {
		return false;
	}
	
	public static Class< ? > getClassByName( String name ) {
		return null;
	}
	
	public static void setClassToJavaPluginLoader( String name, Class< ? > clazz ) {
	}
	
	public static void removeClassFromJavaPluginLoader( String name ) {
	}
}

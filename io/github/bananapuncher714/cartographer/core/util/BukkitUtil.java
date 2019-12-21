package io.github.bananapuncher714.cartographer.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

import io.github.bananapuncher714.cartographer.core.Cartographer;

public class BukkitUtil {
	private static Constructor< PluginCommand > PLUGINCOMMAND_CONSTRUCTOR;
	
	static {
		try {
			PLUGINCOMMAND_CONSTRUCTOR = PluginCommand.class.getDeclaredConstructor( String.class, Plugin.class );
			PLUGINCOMMAND_CONSTRUCTOR.setAccessible( true );
		} catch ( NoSuchMethodException | SecurityException e ) {
			e.printStackTrace();
		}
	}
	
	private static PluginCommand constructCommand( String id ) {
		PluginCommand command = null;
		try {
			command = PLUGINCOMMAND_CONSTRUCTOR.newInstance( id, Cartographer.getInstance() );
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
		return command;
	}
	
	public static PluginCommand createPluginCommandFor( String fallbackPrefix, String id ) {
		PluginCommand command = constructCommand( id );
		
		Cartographer.getInstance().getHandler().registerCommand( Cartographer.getInstance().getName() + ":" + fallbackPrefix, command );
		
		return command;
	}
	
	public static PluginCommand createPluginCommandFor( String id ) {
		PluginCommand command = constructCommand( id );
		
		Cartographer.getInstance().getHandler().registerCommand( command );
		
		return command;
	}
	
	public static void callEventSync( Event event ) {
		if ( Cartographer.getInstance().getHandler().isCurrentThreadMain() ) {
			Bukkit.getPluginManager().callEvent( event );
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask( Cartographer.getInstance(), () -> { callEventSync( event ); } );
		}
	}
	
	public static boolean isPluginLoaded( String id ) {
		return Bukkit.getPluginManager().isPluginEnabled( id );
	}
}

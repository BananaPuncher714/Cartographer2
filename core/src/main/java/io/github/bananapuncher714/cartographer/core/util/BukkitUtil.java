package io.github.bananapuncher714.cartographer.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPluginLoader;

import io.github.bananapuncher714.cartographer.core.Cartographer;

public class BukkitUtil {
	private static Constructor< PluginCommand > PLUGINCOMMAND_CONSTRUCTOR;
	private static Field SIMPLEPLUGINMANAGER_FILEASSOCIATIONS;
	private static Method JAVAPLUGINLOADER_GETCLASS_1;
	private static Method JAVAPLUGINLOADER_GETCLASS_2;
	private static Method JAVAPLUGINLOADER_SETCLASS;
	private static Method JAVAPLUGINLOADER_REMOVECLASS_1;
	private static Method JAVAPLUGINLOADER_REMOVECLASS_2;
	
	static {
		try {
			PLUGINCOMMAND_CONSTRUCTOR = PluginCommand.class.getDeclaredConstructor( String.class, Plugin.class );
			PLUGINCOMMAND_CONSTRUCTOR.setAccessible( true );
			SIMPLEPLUGINMANAGER_FILEASSOCIATIONS = SimplePluginManager.class.getDeclaredField( "fileAssociations" );
			SIMPLEPLUGINMANAGER_FILEASSOCIATIONS.setAccessible( true );
			JAVAPLUGINLOADER_SETCLASS = JavaPluginLoader.class.getDeclaredMethod( "setClass", String.class, Class.class );
			JAVAPLUGINLOADER_SETCLASS.setAccessible( true );
		} catch ( NoSuchMethodException | SecurityException | NoSuchFieldException e ) {
			e.printStackTrace();
		}
		
		try {
			JAVAPLUGINLOADER_GETCLASS_1 = JavaPluginLoader.class.getDeclaredMethod( "getClassByName", String.class );
			JAVAPLUGINLOADER_GETCLASS_1.setAccessible( true );
		} catch ( NoSuchMethodException | SecurityException e ) {
			try {
				JAVAPLUGINLOADER_GETCLASS_2 = JavaPluginLoader.class.getDeclaredMethod( "getClassByName", String.class, boolean.class, PluginDescriptionFile.class );
				JAVAPLUGINLOADER_GETCLASS_2.setAccessible( true );
			} catch ( NoSuchMethodException | SecurityException e1 ) {
				e1.printStackTrace();
			}
		}
		
		try {
			JAVAPLUGINLOADER_REMOVECLASS_1 = JavaPluginLoader.class.getDeclaredMethod( "removeClass", String.class );
			JAVAPLUGINLOADER_REMOVECLASS_1.setAccessible( true );
		} catch ( NoSuchMethodException | SecurityException e ) {
			try {
				JAVAPLUGINLOADER_REMOVECLASS_2 = JavaPluginLoader.class.getDeclaredMethod( "removeClass", Class.class );
				JAVAPLUGINLOADER_REMOVECLASS_2.setAccessible( true );
			} catch ( NoSuchMethodException | SecurityException e1 ) {
				e1.printStackTrace();
			}
		}
	}
	
	public static PluginCommand constructCommand( String id ) {
		PluginCommand command = null;
		try {
			command = PLUGINCOMMAND_CONSTRUCTOR.newInstance( id, Cartographer.getInstance() );
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
		return command;
	}
	
	/**
	 * Call an event synchronously, on the main thread
	 */
	public static void callEventSync( Event event ) {
		if ( Bukkit.getServer().isPrimaryThread() ) {
			Bukkit.getPluginManager().callEvent( event );
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask( Cartographer.getInstance(), () -> { callEventSync( event ); } );
		}
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
		return Bukkit.getPluginManager().isPluginEnabled( id );
	}
	
	public static Class< ? > getClassByName( String name ) {
		PluginManager manager = Bukkit.getPluginManager();
		if ( manager instanceof SimplePluginManager ) {
			SimplePluginManager sManager = ( SimplePluginManager ) manager;
			try {
				Map< Pattern, PluginLoader > associations = ( Map< Pattern, PluginLoader > ) SIMPLEPLUGINMANAGER_FILEASSOCIATIONS.get( sManager );
				for ( PluginLoader loader : associations.values() ) {
					if ( loader instanceof JavaPluginLoader ) {
						if ( JAVAPLUGINLOADER_GETCLASS_1 != null ) {
							return ( Class< ? > ) JAVAPLUGINLOADER_GETCLASS_1.invoke( loader, name );
						} else {
							return ( Class< ? > ) JAVAPLUGINLOADER_GETCLASS_2.invoke( loader, name, true, Cartographer.getInstance().getDescription() );
						}
					}
				}
			} catch ( IllegalArgumentException | IllegalAccessException | InvocationTargetException e ) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static void setClassToJavaPluginLoader( String name, Class< ? > clazz ) {
		PluginManager manager = Bukkit.getPluginManager();
		if ( manager instanceof SimplePluginManager ) {
			SimplePluginManager sManager = ( SimplePluginManager ) manager;
			try {
				Map< Pattern, PluginLoader > associations = ( Map< Pattern, PluginLoader > ) SIMPLEPLUGINMANAGER_FILEASSOCIATIONS.get( sManager );
				for ( PluginLoader loader : associations.values() ) {
					if ( loader instanceof JavaPluginLoader ) {
						JAVAPLUGINLOADER_SETCLASS.invoke( loader, name, clazz );
					}
				}
			} catch ( IllegalArgumentException | IllegalAccessException | InvocationTargetException e ) {
				e.printStackTrace();
			}
		}
	}
	
	public static boolean removeClassFromJavaPluginLoader( Class clazz ) {
		if ( JAVAPLUGINLOADER_REMOVECLASS_2 != null ) {
			PluginManager manager = Bukkit.getPluginManager();
			if ( manager instanceof SimplePluginManager ) {
				SimplePluginManager sManager = ( SimplePluginManager ) manager;
				try {
					Map< Pattern, PluginLoader > associations = ( Map< Pattern, PluginLoader > ) SIMPLEPLUGINMANAGER_FILEASSOCIATIONS.get( sManager );
					for ( PluginLoader loader : associations.values() ) {
						if ( loader instanceof JavaPluginLoader ) {
							JAVAPLUGINLOADER_REMOVECLASS_2.invoke( loader, clazz );
						}
					}
				} catch ( IllegalArgumentException | IllegalAccessException | InvocationTargetException e ) {
					e.printStackTrace();
				}
			}
			return true;
		}
		return false;
	}
	
	public static boolean removeClassFromJavaPluginLoader( String name ) {
		if ( JAVAPLUGINLOADER_REMOVECLASS_1 != null ) {
			PluginManager manager = Bukkit.getPluginManager();
			if ( manager instanceof SimplePluginManager ) {
				SimplePluginManager sManager = ( SimplePluginManager ) manager;
				try {
					Map< Pattern, PluginLoader > associations = ( Map< Pattern, PluginLoader > ) SIMPLEPLUGINMANAGER_FILEASSOCIATIONS.get( sManager );
					for ( PluginLoader loader : associations.values() ) {
						if ( loader instanceof JavaPluginLoader ) {
							JAVAPLUGINLOADER_REMOVECLASS_1.invoke( loader, name );
						}
					}
				} catch ( IllegalArgumentException | IllegalAccessException | InvocationTargetException e ) {
					e.printStackTrace();
				}
			}
			return true;
		}
		return false;
	}
}

package io.github.bananapuncher714.cartographer.core.module;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.util.BukkitUtil;

public abstract class Module {
	private Cartographer plugin;
	private boolean isEnabled = false;
	private ModuleDescription description;
	private File dataFolder;

	/**
	 * There should always be an empty constructor for initialization by Cartographer2's ModuleLoader
	 */
	public Module() {
	}
	
	public final void load( Cartographer plugin, ModuleDescription description, File file ) {
		this.plugin = plugin;
		this.description = description;
		this.dataFolder = file;	
		setEnabled( true );
	}
	
	public final void unload() {
		setEnabled( false );
	}
	
	/**
	 * Starting point of any module
	 */
	public abstract void onEnable();
	
	/**
	 * Optional disable method
	 */
	public void onDisable() {
	}

	/**
	 * Gets a command with the given name
	 * 
	 * @param id
	 * The name of the command
	 * @return
	 * A new PluginCommand registered under Cartographer2, or an existing command
	 */
	protected final PluginCommand getCommand( String id ) {
		PluginCommand command = plugin.getCommand( id );
		if ( command == null ) {
			command = BukkitUtil.createPluginCommandFor( description.getName(), id );
		}
		
		return command;
	}
	
	/**
	 * Set to enable or disable
	 * @param enabled
	 * Enabled or not
	 * @return
	 * Whether or not it was successful; false indicates nothing changed
	 */
	public boolean setEnabled( boolean enabled ) {
		if ( isEnabled == enabled ) {
			return false;
		} else if ( enabled ) {
			onEnable();
		} else {
			onDisable();
		}
		isEnabled = enabled;
		return true;
	}
	
	/**
	 * Get a resource from the jar or zip of the module file
	 * @param mrl
	 * The path, starting at the base of the jar or zip
	 * @return
	 * An InputStream of the resource, or null
	 */
	public InputStream getResource( String mrl ) {
		if ( mrl == null ) {
			throw new IllegalArgumentException( "Filename cannot be null!" );
		}
		
		try {
			URL url = getClass().getClassLoader().getResource( mrl );
			
			if ( url == null ) {
				return null;
			}
			
			URLConnection connection = url.openConnection();
			connection.setUseCaches( false );
			return connection.getInputStream();
		} catch ( IOException e ) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Register any listener under Cartographer2
	 * 
	 * @param listener
	 * The listener to be registered
	 */
	public void registerListener( Listener listener ) {
		Bukkit.getPluginManager().registerEvents( listener, plugin );
	}
	
	/**
	 * Gets Cartographer instance
	 * 
	 * @return
	 * The current instance
	 */
	public final Cartographer getCartographer() {
		return plugin;
	}
	
	/**
	 * Enabled or not
	 * 
	 * @return
	 * Boolean indicating enabled state, not loaded state
	 */
	public final boolean isEnabled() {
		return isEnabled;
	}
	
	/**
	 * Gets a local data folder, much like a plugin's data folder
	 * 
	 * @return
	 * A directory
	 */
	public final File getDataFolder() {
		return dataFolder;
	}
	
	/**
	 * Get the description of the module
	 * 
	 * @return
	 * Should be unique per person
	 */
	public final ModuleDescription getDescription() {
		return description;
	}
	
	/**
	 * Quick get name method
	 * 
	 * @return
	 * String of the name
	 */
	public final String getName() {
		return description.getName();
	}
	
	/**
	 * Quick get version method
	 * 
	 * @return
	 * String of the version
	 */
	public final String getVersion() {
		return description.getVersion();
	}
}

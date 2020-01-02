package io.github.bananapuncher714.cartographer.core.module;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.util.BukkitUtil;

/**
 * An addon for Cartographer2. Allows for extreme customization.
 * 
 * @author BananaPuncher714
 */
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
	
	/**
	 * Developers should not call this. Purely for loading by Cartographer2.
	 * 
	 * @param plugin
	 * The Cartographer instance.
	 * @param description
	 * A {@link ModuleDescription} of this module.
	 * @param file
	 * The data folder.
	 */
	public final void load( Cartographer plugin, ModuleDescription description, File file ) {
		Validate.notNull( plugin );
		Validate.notNull( description );
		Validate.notNull( file );
		this.plugin = plugin;
		this.description = description;
		this.dataFolder = file;
	}
	
	public final void unload() {
	}
	
	/**
	 * Starting point of any module.
	 */
	public abstract void onEnable();
	
	/**
	 * Optional disable method.
	 */
	public void onDisable() {
	}

	/**
	 * Gets a command with the given name.
	 * 
	 * @param id
	 * The name of the command. Cannot be null.
	 * @return
	 * A new PluginCommand registered under Cartographer2, or an existing command.
	 */
	protected final PluginCommand getCommand( String id ) {
		Validate.notNull( id );
		PluginCommand command = plugin.getCommand( id );
		if ( command == null ) {
			command = BukkitUtil.createPluginCommandFor( description.getName(), id );
		}
		
		return command;
	}
	
	/**
	 * Set to enable or disable. You should use {@link ModuleManager#enableModule( Module )} or {@link ModuleManager#disableModule( Module )} instead.
	 * 
	 * @param enabled
	 * Enabled or not.
	 * @return
	 * Whether or not it was successful. false indicates nothing changed.
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
	 * Get a resource from the jar or zip of the module file.
	 * @param mrl
	 * The path, starting at the base of the jar or zip. Cannot be null.
	 * @return
	 * An InputStream of the resource, or null.
	 */
	public InputStream getResource( String mrl ) {
		Validate.notNull( mrl );
		
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
	 * Register any listener under Cartographer2.
	 * 
	 * @param listener
	 * The listener to be registered. Cannot be null.
	 */
	public void registerListener( Listener listener ) {
		Validate.notNull( listener );
		Bukkit.getPluginManager().registerEvents( listener, plugin );
	}
	
	/**
	 * Get the Cartographer instance.
	 * 
	 * @return
	 * The current instance.
	 */
	public final Cartographer getCartographer() {
		return plugin;
	}
	
	/**
	 * Enabled or not.
	 * 
	 * @return
	 * Boolean indicating enabled state, not loaded state.
	 */
	public final boolean isEnabled() {
		return isEnabled;
	}
	
	/**
	 * Get the local data folder, much like a plugin's data folder.
	 * 
	 * @return
	 * A directory.
	 */
	public final File getDataFolder() {
		return dataFolder;
	}
	
	/**
	 * Get the description of the module.
	 * 
	 * @return
	 * Should be unique per person.
	 */
	public final ModuleDescription getDescription() {
		return description;
	}
	
	/**
	 * Quick get name method.
	 * 
	 * @return
	 * String of the name.
	 */
	public final String getName() {
		return description.getName();
	}
	
	/**
	 * Quick get version method.
	 * 
	 * @return
	 * String of the version.
	 */
	public final String getVersion() {
		return description.getVersion();
	}
}

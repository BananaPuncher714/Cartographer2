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
	
	public final void load( Cartographer plugin, ModuleDescription description, File file ) {
		this.plugin = plugin;
		this.description = description;
		this.dataFolder = file;	
		setEnabled( true );
	}
	
	public final void unload() {
		setEnabled( false );
	}
	
	public abstract void onEnable();
	
	public void onDisable() {
	}

	protected final PluginCommand getCommand( String id ) {
		PluginCommand command = plugin.getCommand( id );
		if ( command == null ) {
			command = BukkitUtil.createPluginCommandFor( id );
		}
		
		return command;
	}
	
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
	
	public void registerListener( Listener listener ) {
		Bukkit.getPluginManager().registerEvents( listener, plugin );
	}
	
	public final Cartographer getCartographer() {
		return plugin;
	}
	
	public final boolean isEnabled() {
		return isEnabled;
	}
	
	public final File getDataFolder() {
		return dataFolder;
	}
	
	public final ModuleDescription getDescription() {
		return description;
	}
	
	public final String getName() {
		return description.getName();
	}
	public final String getVersion() {
		return description.getVersion();
	}
}

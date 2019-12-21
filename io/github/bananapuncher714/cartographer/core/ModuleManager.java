package io.github.bananapuncher714.cartographer.core;

import java.io.File;
import java.util.Map;

import io.github.bananapuncher714.cartographer.core.module.Module;
import io.github.bananapuncher714.cartographer.core.module.ModuleDescription;
import io.github.bananapuncher714.cartographer.core.module.ModuleLoader;
import io.github.bananapuncher714.cartographer.core.util.BukkitUtil;

public class ModuleManager {
	protected Cartographer plugin;
	protected Map< String, Module > modules;
	protected File moduleFolder;
	
	protected ModuleManager( Cartographer plugin ) {
		this.plugin = plugin;
		moduleFolder = new File( plugin.getDataFolder() + "/" + "modules" );
		moduleFolder.mkdirs();
	}
	
	protected void loadModules() {
		for ( File file : moduleFolder.listFiles() ) {
			if ( file.isDirectory() ) {
				continue;
			}
			
			Module module = loadModule( file );
			registerModule( module );
		}
	}
	
	public void registerModule( Module module ) {
		modules.put( module.getName(), module );
	}
	
	public void registerAndEnable( Module module ) {
		registerModule( module );
		module.setEnabled( true );
	}
	
	public Module loadModule( File file ) {
		ModuleDescription description = ModuleLoader.getDescriptionFor( file );
		Module module = ModuleLoader.load( file, description );
		
		File dataFolder = new File( moduleFolder + "/" + module.getName() );
		module.load( plugin, description, dataFolder );
		
		return module;
	}
	
	protected void enableModules() {
		// Load all modules, but check if their dependencies are loaded
		// This should be called after the server is done loading.
		// Essentially, all plugins *should* be loaded by this time
		for ( String id : modules.keySet() ) {
			Module module = modules.get( id );
			ModuleDescription description = module.getDescription();
			boolean allDependenciesLoaded = true;
			StringBuilder missingDeps = new StringBuilder();
			for ( String dependency : description.getDependencies() ) {
				if ( !BukkitUtil.isPluginLoaded( dependency ) ) { 
					allDependenciesLoaded = false;
					missingDeps.append( dependency );
					missingDeps.append( " " );
				}
			}
			if ( allDependenciesLoaded ) {
				plugin.getLogger().info( "[ModuleManager] Enabling " + description.getName() + " version " + description.getVersion() + " by " + description.getAuthor() );
				module.setEnabled( true );
			} else {
				plugin.getLogger().warning( "[ModuleManager] Unable to enable " + description.getName() + " due to the missing dependencies: " + missingDeps.toString().trim() );
			}
		}
	}
	
	protected void disableModules() {
		for ( String id : modules.keySet() ) {
			Module module = modules.get( id );
			module.unload();
			
			plugin.getLogger().info( "[ModuleManager] Unloading " + id );
		}
	}
}

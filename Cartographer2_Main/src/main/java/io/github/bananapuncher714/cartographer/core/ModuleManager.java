package io.github.bananapuncher714.cartographer.core;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import io.github.bananapuncher714.cartographer.core.api.events.module.ModuleDisableEvent;
import io.github.bananapuncher714.cartographer.core.api.events.module.ModuleEnableEvent;
import io.github.bananapuncher714.cartographer.core.api.events.module.ModuleLoadEvent;
import io.github.bananapuncher714.cartographer.core.locale.LocaleConstants;
import io.github.bananapuncher714.cartographer.core.module.Module;
import io.github.bananapuncher714.cartographer.core.module.ModuleDescription;
import io.github.bananapuncher714.cartographer.core.module.ModuleLoader;
import io.github.bananapuncher714.cartographer.core.util.BukkitUtil;

public class ModuleManager {
	protected Cartographer plugin;
	protected ModuleLoader loader;
	protected Map< String, Module > modules = new HashMap< String, Module >();
	protected File moduleFolder;
	
	protected CartographerLogger logger = new CartographerLogger( "ModuleManager" );
	
	protected ModuleManager( Cartographer plugin, File moduleFolder ) {
		this.plugin = plugin;
		this.moduleFolder = moduleFolder;
		loader = new ModuleLoader( this, plugin );
		moduleFolder.mkdirs();
	}
	
	protected void terminate() {
		disableModules();
		unloadModules( false );
	}
	
	public void registerModule( Module module ) {
		modules.put( module.getName(), module );
	}
	
	public void registerAndEnable( Module module ) {
		registerModule( module );
		enableModule( module );
	}
	
	public void reload() {
		logger.infoTr( LocaleConstants.MANAGER_MODULE_RELOAD_START );
		unloadModules( true );
		loadModules();
		enableModules();
		logger.infoTr( LocaleConstants.MANAGER_MODULE_RELOAD_FINISH );
	}
	
	public Module loadModule( File file ) {
		ModuleDescription description = loader.getDescriptionFor( file );
		logger.infoTr( LocaleConstants.MANAGER_MODULE_LOADING, description.getName(), description.getVersion(), description.getAuthor() );
		
		Module module = loader.load( description );
		
		if ( module == null ) {
			return null;
		}
		
		File dataFolder = new File( moduleFolder + "/" + description.getName() );
		module.load( plugin, description, dataFolder );
		
		new ModuleLoadEvent( module ).callEvent();
		
		return module;
	}
	
	protected void loadModules() {
		for ( File file : moduleFolder.listFiles() ) {
			if ( file.isDirectory() ) {
				continue;
			}
			if ( !file.getName().matches( ".*?\\.jar$" ) ) {
				continue;
			}
			
			Module module = loadModule( file );
			if ( module != null ) {
				registerModule( module );
			}
		}
	}
		
	public void loadModule( Module module, ModuleDescription description ) {
		File dataFolder = new File( moduleFolder + "/" + description.getName() );
		module.load( plugin, description, dataFolder );
		
		new ModuleLoadEvent( module ).callEvent();
	}
	
	protected void unloadModules( boolean reloadLocale ) {
		Set< String > keys = new HashSet< String >( modules.keySet() );
		for ( String id : keys ) {
			Module module = modules.get( id );
			
			unloadModule( module, false );
		}
		if ( reloadLocale ) {
			plugin.getLocaleManager().reload();
		}
	}
	
	public boolean unloadModule( Module module, boolean reloadLocale ) {
		module.unload();
		
		disableModule( module );
		
		logger.infoTr( LocaleConstants.MANAGER_MODULE_UNLOADING, module.getName() );
		if ( !loader.unload( module ) ) {
			return false;
		}
		
		modules.remove( module.getName() );

		if ( reloadLocale ) {
			// Reload the locale manager and clear out any changes
			// that it may have made or added
			plugin.getLocaleManager().reload();
		}
		
		return true;
	}
	
	public Module getModule( String name ) {
		return modules.get( name );
	}
	
	public Set< Module > getModules() {
		Set< Module > moduleSet = new HashSet< Module >();
		moduleSet.addAll( modules.values() );
		return moduleSet;
	}
	
	public boolean enableModule( Module module ) {
		Validate.notNull( module );
		if ( module.isEnabled() ) {
			return false;
		}
		// Check for their dependencies
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
			logger.infoTr( LocaleConstants.MANAGER_MODULE_ENABLING, description.getName(), description.getVersion(), description.getAuthor() );
			module.setEnabled( true );
			new ModuleEnableEvent( module ).callEvent();
		} else {
			logger.warningTr( LocaleConstants.MANAGER_MODULE_MISSING_DEPENDENCIES, description.getName(), missingDeps.toString().trim() );
			return false;
		}
		return true;
	}
	
	public void enableModules() {
		// Load all modules, but check if their dependencies are loaded
		// This should be called after the server is done loading.
		// Essentially, all plugins *should* be loaded by this time
		for ( Module module : modules.values() ) {
			enableModule( module );
		}
	}
	
	public boolean disableModule( Module module ) {
		Validate.notNull( module );
		if ( module.isEnabled() ) {
			ModuleDescription description = module.getDescription();
			new ModuleDisableEvent( module ).callEvent();
			logger.infoTr( LocaleConstants.MANAGER_MODULE_DISABLING, description.getName(), description.getVersion(), description.getAuthor() );
			module.setEnabled( false );
			loader.disable( module );
			plugin.getCommand().rebuildCommand();
			return true;
		}
		return false;
	}
	
	public void disableModules() {
		for ( Module module : modules.values() ) {
			disableModule( module );
		}
	}
	
	public ModuleLoader getLoader() {
		return loader;
	}
}

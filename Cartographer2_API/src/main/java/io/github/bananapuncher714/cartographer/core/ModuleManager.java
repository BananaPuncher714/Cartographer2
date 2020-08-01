package io.github.bananapuncher714.cartographer.core;

import java.io.File;
import java.util.Map;
import java.util.Set;

import io.github.bananapuncher714.cartographer.core.module.Module;
import io.github.bananapuncher714.cartographer.core.module.ModuleDescription;
import io.github.bananapuncher714.cartographer.core.module.ModuleLoader;

public class ModuleManager {
	protected Cartographer plugin;
	protected ModuleLoader loader;
	protected Map< String, Module > modules;
	protected File moduleFolder;
	
	protected CartographerLogger logger = new CartographerLogger( "ModuleManager" );
	
	protected ModuleManager( Cartographer plugin, File moduleFolder ) {
	}
	
	protected void terminate() {
	}
	
	public void registerModule( Module module ) {
	}
	
	public void registerAndEnable( Module module ) {
	}
	
	public void reload() {
	}
	
	public Module loadModule( File file ) {
		return null;
	}
	
	protected void loadModules() {
	}
		
	public void loadModule( Module module, ModuleDescription description ) {
	}
	
	protected void unloadModules( boolean reloadLocale ) {
	}
	
	public boolean unloadModule( Module module, boolean reloadLocale ) {
		return true;
	}
	
	public Module getModule( String name ) {
		return null;
	}
	
	public Set< Module > getModules() {
		return null;
	}
	
	public boolean enableModule( Module module ) {
		return true;
	}
	
	public void enableModules() {
	}
	
	public boolean disableModule( Module module ) {
		return false;
	}
	
	public void disableModules() {
	}
	
	public ModuleLoader getLoader() {
		return null;
	}
}

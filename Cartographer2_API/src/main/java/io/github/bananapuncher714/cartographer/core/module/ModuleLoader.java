package io.github.bananapuncher714.cartographer.core.module;

import java.io.File;
import java.io.InputStream;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.ModuleManager;

/**
 * Load modules and their descriptions, like a plugin.
 * 
 * @author BananaPuncher714
 */
public class ModuleLoader {
	public ModuleLoader( ModuleManager manager, Cartographer plugin ) {
	}
	
	/**
	 * Load a module with the given description.
	 * 
	 * @param description
	 * A {@link ModuleDescription} of the module being loaded. Cannot be null.
	 * @return
	 * A new module if successful.
	 */
	public Module load( ModuleDescription description ) {
		return null;
	}
	
	/**
	 * Unload the given module and all their classes, does not guarantee that the module's classes are not be in use.
	 * 
	 * @param module
	 * The module to unload. Cannot be null.
	 * @return
	 * Whether or not this module was unloaded.
	 */
	public boolean unload( Module module ) {
		return true;
	}
	
	/**
	 * Disable the module and remove whatever it may be trying to do.
	 * 
	 * @param module
	 */
	public void disable( Module module ) {
	}
	
	/**
	 * Get the {@link ModuleClassLoader} for the given module.
	 * 
	 * @param module
	 * Cannot be null.
	 * @return
	 * Null if the module isn't loaded by Cartographer2.
	 */
	public ModuleClassLoader getClassLoaderFor( Module module ) {
		return null;
	}
	
	/**
	 * Get a {@link ModuleDescription} from a module jar.
	 * 
	 * @param file
	 * The module jar. Cannot be null.
	 * @return
	 * A ModuleDescription if successful.
	 */
	public ModuleDescription getDescriptionFor( File file ) {
		return null;
	}
	
	/**
	 * Get a ModuleDescription from an input stream.
	 * 
	 * @param file
	 * The file of the module, cannot be null.
	 * @param stream
	 * The stream to read from. Cannot be null.
	 * @return
	 * A {@link ModuleDescription} generated from the stream.
	 */
	public ModuleDescription getDescriptionFor( File file, InputStream stream ) {
		return null;
	}
}

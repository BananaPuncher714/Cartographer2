package io.github.bananapuncher714.cartographer.core.module;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.NoSuchFileException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.MinimapManager;
import io.github.bananapuncher714.cartographer.core.ModuleManager;
import io.github.bananapuncher714.cartographer.core.api.map.MapCursorProvider;
import io.github.bananapuncher714.cartographer.core.api.map.MapPixelProvider;
import io.github.bananapuncher714.cartographer.core.api.map.WorldCursorProvider;
import io.github.bananapuncher714.cartographer.core.api.map.WorldPixelProvider;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.menu.MapMenu;
import io.github.bananapuncher714.cartographer.core.map.menu.MenuComponent;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkDataProvider;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkNotifier;
import io.github.bananapuncher714.cartographer.core.map.process.SimpleChunkProcessor;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;
import io.github.bananapuncher714.cartographer.core.util.BukkitUtil;

/**
 * Load modules and their descriptions, like a plugin.
 * 
 * @author BananaPuncher714
 */
public class ModuleLoader {
	private ModuleManager manager;
	private Cartographer plugin;
	private Map< Module, ModuleClassLoader > classLoaders = new ConcurrentHashMap< Module, ModuleClassLoader >();
	
	public ModuleLoader( ModuleManager manager, Cartographer plugin ) {
		this.manager = manager;
		this.plugin = plugin;
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
		Validate.notNull( description );
		File file = description.getFile();
		Validate.isTrue( file.exists(), file + " does not exist!" );
		Validate.isTrue( file.isFile(), file + " is not a file!" );
		
		try {
			// Cache this sometime
			ModuleClassLoader loader = new ModuleClassLoader( description, Bukkit.class.getClassLoader() );
			classLoaders.put( loader.getModule(), loader );
			
			return loader.getModule();
		} catch ( IllegalArgumentException | SecurityException | ClassNotFoundException | IOException e ) {
			e.printStackTrace();
		}
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
		Validate.notNull( module );

		disable( module );
		
		ModuleClassLoader loader = classLoaders.get( module );
		if ( loader == null ) {
			return false;
		}
		
		Set< String > classes = loader.getClassNames();
		
		// Remove from pool of common classes
		for ( String clazz : classes ) {
			BukkitUtil.removeClassFromJavaPluginLoader( clazz );
		}
		
		try {
			loader.close();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		
		// Remove from classloaders
		classLoaders.remove( module );
		
		return true;
	}
	
	/**
	 * Disable the module and remove whatever it may be trying to do.
	 * 
	 * @param module
	 */
	public void disable( Module module ) {
		ModuleClassLoader loader = classLoaders.get( module );
		if ( loader == null ) {
			return;
		}
		
		Set< String > classes = loader.getClassNames();
		
		for ( SettingState< ? > state : module.getTracker().getSettings() ) {
			MapViewer.removeSetting( state );
		}
		module.getTracker().getSettings().clear();
		
		// Remove integration from maps
		MinimapManager manager = plugin.getMapManager();
		for ( Minimap map : manager.getMinimaps().values() ) {
			for ( Iterator< WorldPixelProvider > iterator = map.getWorldPixelProviders().iterator(); iterator.hasNext(); ) {
				WorldPixelProvider provider = iterator.next();
				if ( classes.contains( provider.getClass().getName() ) ) {
					iterator.remove();
				}
			}
			for ( Iterator< WorldCursorProvider > iterator = map.getWorldCursorProviders().iterator(); iterator.hasNext(); ) {
				WorldCursorProvider provider = iterator.next();
				if ( classes.contains( provider.getClass().getName() ) ) {
					iterator.remove();
				}
			}
			for ( Iterator< MapPixelProvider > iterator = map.getMapPixelProviders().iterator(); iterator.hasNext(); ) {
				MapPixelProvider provider = iterator.next();
				if ( classes.contains( provider.getClass().getName() ) ) {
					iterator.remove();
				}
			}
			for ( Iterator< MapCursorProvider > iterator = map.getMapCursorProviders().iterator(); iterator.hasNext(); ) {
				MapCursorProvider provider = iterator.next();
				if ( classes.contains( provider.getClass().getName() ) ) {
					iterator.remove();
				}
			}

			ChunkDataProvider provider = map.getDataCache().getChunkDataProvider();
			if ( classes.contains( provider.getClass().getName() ) ) {
				map.getDataCache().setChunkDataProvider( new SimpleChunkProcessor( map.getDataCache(), map.getSettings().getPalette() ) );
			}
			ChunkNotifier notifier = map.getDataCache().getChunkNotifier();
			if ( notifier != null && classes.contains( notifier.getClass().getName() ) ) {
				map.getDataCache().setNotifier( null );
			}
		}

		ModuleTracker tracker = module.getTracker();
		
		// Remove listeners
		for ( Listener listener : tracker.getListeners() ) {
			HandlerList.unregisterAll( listener );
		}
		tracker.getListeners().clear();
		
		for ( CartographerRenderer renderer : plugin.getRenderers().values() ) {
			for ( UUID uuid : renderer.getActiveMapMenuViewers() ) {
				MapMenu menu = renderer.getMenu( uuid );
				if ( menu != null ) {
					boolean found = false;
					for ( MenuComponent component : menu.getComponents() ) {
						if ( classes.contains( component.getClass().getName() ) ) {
							found = true;
							break;
						}
					}
					if ( found ) {
						renderer.setMapMenu( uuid, null );
					}
				}
			}
		}
		
		// Remove commands
		for ( PluginCommand command : tracker.getCommands() ) {
			plugin.getHandler().unregisterCommand( command );
		}
		tracker.getCommands().clear();
		
		for ( BukkitTask task : tracker.getTasks() ) {
			task.cancel();
		}
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
		Validate.notNull( module );
		return classLoaders.get( module );
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
		if ( file == null ) {
			throw new IllegalArgumentException( "File cannot be null!" );
		}
		if ( !file.exists() ) {
			try {
				throw new FileNotFoundException( "File does not exist! " + file.getAbsolutePath() );
			} catch ( FileNotFoundException e ) {
				e.printStackTrace();
			}
			return null;
		}
		try ( JarFile jar = new JarFile( file ) ) {
			JarEntry entry = jar.getJarEntry( "module.json" );
			if ( entry == null ) {
				throw new NoSuchFileException( "module.json does not exist! " + file.getAbsolutePath() );
			}
			InputStream stream = jar.getInputStream( entry );
			
			return getDescriptionFor( file, stream );
		} catch ( IOException e ) {
			e.printStackTrace();
		} catch ( IllegalArgumentException e ) {
			e.printStackTrace();
		}
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
		Validate.notNull( file );
		Validate.notNull( stream );
		JsonReader reader = new JsonReader( new InputStreamReader( stream ) );
		
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse( reader );
		
		JsonObject object = element.getAsJsonObject();
		
		if ( !( object.has( "name" ) && object.has( "main" ) && object.has( "author" ) && object.has( "version" ) ) ) {
			throw new IllegalArgumentException( "Missing required information from module.json! (name/main/author/version)" );
		}
		
		String name = object.get( "name" ).getAsString();
		String main = object.get( "main" ).getAsString();
		String author = object.get( "author" ).getAsString();
		String version = object.get( "version" ).getAsString();

		ModuleDescription moduleDescription = new ModuleDescription( file, name, main, author, version );
		
		if ( object.has( "description" ) ) {
			moduleDescription.setDescription( object.get( "description" ).getAsString() );
		}
		
		if ( object.has( "website" ) ) {
			moduleDescription.setWebsite( object.get( "website" ).getAsString() );
		}
		
		if ( object.has( "depend" ) ) {
			JsonArray array = object.get( "depend" ).getAsJsonArray();
			for ( JsonElement dependElement : array ) {
				moduleDescription.getDependencies().add( dependElement.getAsString() );
			}
		}
		
		if ( object.has( "dependencies" ) ) {
			JsonArray array = object.get( "dependencies" ).getAsJsonArray();
			for ( JsonElement dependElement : array ) {
				moduleDescription.getDependencies().add( dependElement.getAsString() );
			}
		}
		
		return moduleDescription;
	}
}

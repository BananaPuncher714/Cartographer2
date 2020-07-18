package io.github.bananapuncher714.cartographer.core.module;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scheduler.BukkitTask;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.ModuleManager;
import io.github.bananapuncher714.cartographer.core.api.permission.PermissionBuilder;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.core.locale.Locale;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;

/**
 * An addon for Cartographer2. Allows for extreme customization.
 * 
 * @author BananaPuncher714
 */
public abstract class Module {
	private Cartographer plugin;
	private boolean isEnabled = false;
	private ModuleDescription description;
	private ModuleTracker tracker;
	private File dataFolder;
	private ModuleLogger logger;
	
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
		this.tracker = new ModuleTracker();
		this.description = description;
		this.dataFolder = file;
		this.logger = new ModuleLogger( this );
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
	
	protected final void registerSettings() {
		Permission user = Bukkit.getPluginManager().getPermission( "cartographer.settings.user" );
		Permission admin = Bukkit.getPluginManager().getPermission( "cartographer.settings.admin" );
		for ( SettingState< ? > state : getSettingStates() ) {
			MapViewer.addSetting( state );
			tracker.getSettings().add( state );
			
			if ( !state.isPrivate() ) {
				Permission get = new PermissionBuilder( "cartographer.settings.get." + state.getId() )
						.addChild( Bukkit.getPluginManager().getPermission( "cartographer.settings.get" ), true )
						.setDefault( PermissionDefault.FALSE ).register().build();
				Permission set = new PermissionBuilder( "cartographer.settings.set." + state.getId() )
						.addChild( Bukkit.getPluginManager().getPermission( "cartographer.settings.set" ), true )
						.addChild( get, true )
						.setDefault( PermissionDefault.FALSE ).register().build();
				Permission getOther = new PermissionBuilder( "cartographer.settings.getother." + state.getId() )
						.addChild( Bukkit.getPluginManager().getPermission( "cartographer.settings.getother" ), true )
						.addChild( get, true )
						.setDefault( PermissionDefault.FALSE ).register().build();
				Permission setOther = new PermissionBuilder( "cartographer.settings.setother." + state.getId() )
						.addChild( Bukkit.getPluginManager().getPermission( "cartographer.settings.setother" ), true )
						.addChild( set, true )
						.addChild( getOther, true )
						.setDefault( PermissionDefault.FALSE ).register().build();

				user.getChildren().put( get.getName(), true );
				user.getChildren().put( set.getName(), true );
				admin.getChildren().put( getOther.getName(), true );
				admin.getChildren().put( setOther.getName(), true );
			}
		}
		user.recalculatePermissibles();
		admin.recalculatePermissibles();
		
		plugin.getCommand().rebuildCommand();
	}
	
	public SettingState< ? >[] getSettingStates() {
		return new SettingState< ? >[ 0 ];
	}

	protected final ModuleTracker getTracker() {
		return tracker;
	}
	
	/**
	 * Gets a command with the given name.
	 * 
	 * @param id
	 * The name of the command. Cannot be null.
	 * @return
	 * A new PluginCommand registered under Cartographer2, or an existing command.
	 */
	protected final void registerCommand( PluginCommand command ) {
		Validate.notNull( command );
		plugin.getHandler().registerCommand( plugin.getName() + ":" + getName(), command );
		
		tracker.getCommands().add( command );
	}
	
	protected BukkitTask runTaskTimer( Runnable runnable, long delay, long interval ) {
		BukkitTask task = Bukkit.getScheduler().runTaskTimer( Cartographer.getInstance(), runnable, delay, interval );
		tracker.getTasks().add( task );
		return task;
	}
	
	protected BukkitTask runTask( Runnable runnable, long delay ) {
		BukkitTask task = Bukkit.getScheduler().runTaskLater( Cartographer.getInstance(), runnable, delay );
		tracker.getTasks().add( task );
		return task;
	}

	/**
	 * Get a list of supported locales for this module
	 * 
	 * @return
	 */
	public Collection< Locale > getLocales() {
		return new ArrayList< Locale >();
	}
	
	/**
	 * Call to register locales with the locale manager
	 * 
	 * Should be done in the onEnable method
	 */
	protected final void registerLocales() {
		for ( Locale locale : getLocales() ) {
			plugin.getLocaleManager().register( locale );
		}
	}
	
	protected final Locale convertToDefaultLocale( Locale locale ) {
		return locale.copyOf( null, null, null, plugin.getLocaleManager().getDefaultLocale() );
	}
	
	protected final Locale loadLocale( InputStream stream ) {
		InputStreamReader reader = new InputStreamReader( stream, StandardCharsets.UTF_8 );
		return plugin.getLocaleManager().load( YamlConfiguration.loadConfiguration( reader ), "modules." + getName() );
	}
	
	protected final Collection< Locale > loadLocale( File file ) {
		// Load the locales in the directory, if it's a folder
		List< Locale > locales = new ArrayList< Locale >();
		if ( file.exists() ) {
			if ( file.isDirectory() ) {
				for ( File localeFile : file.listFiles() ) {
					if ( localeFile.getName().endsWith( ".yml" ) ) {
						try {
							// Load it under the module prefix, since we don't want it to get mixed with global keys
							FileConfiguration config = YamlConfiguration.loadConfiguration( localeFile );
							locales.add( plugin.getLocaleManager().load( config, getLocalePrefix() ) );
						} catch ( IllegalArgumentException e ) {
							// TODO Translate this
							logger.severe( "Unable to load locale file " + file.getName() );
							e.printStackTrace();
						}
					}
				}
			} else {
				FileConfiguration config = YamlConfiguration.loadConfiguration( file );
				locales.add( plugin.getLocaleManager().load( config, getLocalePrefix() ) );
			}
		}
		return locales;
	}
	
	public String translate( String code, CommandSender sender, String key, Object... params ) {
		// First try to get the translated message for the module
		String message = plugin.getLocaleManager().translate( code, sender, getLocalePrefix() + "." + key, params );
		// If it doesn't exist, then check the global locales
		if ( message == null ) {
			message = plugin.getLocaleManager().translate( code, sender, key, params );
		}
		return message;
	}
	
	public String translate( CommandSender sender, String key, Object... params ) {
		String locale = plugin.getLocaleManager().getDefaultLocale();
		if ( sender instanceof Player ) {
			Player player = ( Player ) sender;
			MapViewer viewer = plugin.getPlayerManager().getViewerFor( player.getUniqueId() );
			locale = viewer.getSetting( MapViewer.LOCALE );
		}
		
		String message = plugin.getLocaleManager().translate( locale, sender, getLocalePrefix() + "." + key, params );
		if ( message == null ) {
			message = plugin.getLocaleManager().translate( locale, sender, key, params );
		}
		return message;
	}
	
	public String translateAndSend( String code, CommandSender sender, String key, Object... params ) {
		String message = translate( code, sender, key, params );
		
		if ( message != null && !message.isEmpty() ) {
			sender.sendMessage( message );
		}
		
		return message;
	}
	
	public String translateAndSend( CommandSender sender, String key, Object... params ) {
		String message = translate( sender, key, params );
		
		if ( message != null && !message.isEmpty() ) {
			sender.sendMessage( message );
		}
		
		return message;
	}
	
	private String getLocalePrefix() {
		return "modules." + getName();
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
		tracker.getListeners().add( listener );
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
	
	public final Logger getLogger() {
		return logger;
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
	 * Quick get file method.
	 * 
	 * @return
	 * The jar file of this module.
	 */
	public final File getFile() {
		return description.getFile();
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

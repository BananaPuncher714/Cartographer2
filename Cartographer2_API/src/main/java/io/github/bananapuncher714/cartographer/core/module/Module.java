package io.github.bananapuncher714.cartographer.core.module;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.ModuleManager;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.core.locale.Locale;

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
	}
	
	protected BukkitTask runTaskTimer( Runnable runnable, long delay, long interval ) {
		return null;
	}
	
	protected BukkitTask runTask( Runnable runnable, long delay ) {
		return null;
	}

	/**
	 * Get a list of supported locales for this module
	 * 
	 * @return
	 */
	public Collection< Locale > getLocales() {
		return null;
	}
	
	/**
	 * Call to register locales with the locale manager
	 * 
	 * Should be done in the onEnable method
	 */
	protected final void registerLocales() {
	}
	
	protected final Locale convertToDefaultLocale( Locale locale ) {
		return null;
	}
	
	protected final Locale loadLocale( InputStream stream ) {
		return null;
	}
	
	protected final Collection< Locale > loadLocale( File file ) {
		return null;
	}
	
	public String translate( String code, CommandSender sender, String key, Object... params ) {
		return null;
	}
	
	public String translate( CommandSender sender, String key, Object... params ) {
		return null;
	}
	
	public String translateAndSend( String code, CommandSender sender, String key, Object... params ) {
		return null;
	}
	
	public String translateAndSend( CommandSender sender, String key, Object... params ) {
		return null;
	}
	
	private String getLocalePrefix() {
		return null;
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
		return null;
	}
	
	/**
	 * Register any listener under Cartographer2.
	 * 
	 * @param listener
	 * The listener to be registered. Cannot be null.
	 */
	public void registerListener( Listener listener ) {
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

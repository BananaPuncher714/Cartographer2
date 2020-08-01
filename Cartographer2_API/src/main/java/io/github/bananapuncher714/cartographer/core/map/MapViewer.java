package io.github.bananapuncher714.cartographer.core.map;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;

import io.github.bananapuncher714.cartographer.core.api.SimpleImage;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingStateBoolean;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingStateBooleanOption;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingStateLocale;

/**
 * Individual per player settings that take priority over Cartographer's default settings but must conform to minimap settings.
 * The player represented may or may not be online.
 * 
 * @author BananaPuncher714
 */
public class MapViewer {
	public static final SettingStateBoolean SHOWNAME = null;
	public static final SettingStateBoolean CURSOR = null;
	public static final SettingStateBooleanOption ROTATE = null;
	public static final SettingStateLocale LOCALE = null;
	
	// This stuff gets saved
	protected UUID uuid;

	// This stuff does not get saved
	protected SimpleImage overlay;
	protected SimpleImage background;
	
	public MapViewer( UUID uuid ) {
		this.uuid = uuid;
	}
	
	public MapViewer( UUID uuid, FileConfiguration config ) {
	}

	public final UUID getUUID() {
		return uuid;
	}
	
	public SimpleImage getOverlay() {
		return overlay;
	}

	public void setOverlay( SimpleImage overlay ) {
		this.overlay = overlay;
	}

	public SimpleImage getBackground() {
		return background;
	}

	public void setBackground( SimpleImage background ) {
		this.background = background;
	}
	
	public < T extends Comparable< T > > T getSetting( SettingState< T > state ) {
		return null;
	}
	
	public < T extends Comparable< T > > void setSetting( SettingState< T > state, T val ) {
	}

	public Map< String, String > getSettings() {
		return null;
	}

	public void saveTo( FileConfiguration config ) {
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapViewer other = (MapViewer) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}
	
	/**
	 * Register a custom setting.
	 * 
	 * @param state
	 */
	public static void addSetting( SettingState< ? > state ) {
	}
	
	/**
	 * Unregister a custom setting.
	 * 
	 * @param state
	 */
	public static void removeSetting( SettingState< ? > state ) {
	}
	
	/**
	 * Get the state with the specified id.
	 * 
	 * @param id
	 * @return
	 */
	public static SettingState< ? > getState( String id ) {
		return null;
	}
	
	/**
	 * Get all the states available.
	 * 
	 * @return
	 */
	public static Collection< SettingState< ? > > getStates() {
		return null;
	}
}

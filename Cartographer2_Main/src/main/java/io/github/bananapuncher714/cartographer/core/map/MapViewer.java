package io.github.bananapuncher714.cartographer.core.map;

import java.awt.Image;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import io.github.bananapuncher714.cartographer.core.api.BooleanOption;
import io.github.bananapuncher714.cartographer.core.api.SimpleImage;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingStateBoolean;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingStateBooleanOption;

/**
 * Individual per player settings that take priority over Cartographer's default settings but must conform to minimap settings.
 * The player represented may or may not be online.
 * 
 * @author BananaPuncher714
 */
public class MapViewer {
	public static final SettingStateBoolean SHOWNAME = SettingStateBoolean.of( "showname", false, true );
	public static final SettingStateBoolean CURSOR = SettingStateBoolean.of( "cursor", false, false );
	public static final SettingStateBooleanOption ROTATE = SettingStateBooleanOption.of( "rotate", false, BooleanOption.TRUE );
	
	private static Map< String, SettingState< ? > > SETTING_STATES = new HashMap< String, SettingState< ? > >();
	
	static {
		addSetting( SHOWNAME );
		addSetting( CURSOR );
		addSetting( ROTATE );
	}
	
	// This stuff gets saved
	protected UUID uuid;
	private Map< String, String > settings = new HashMap< String, String >();
	
	// This stuff does not get saved
	protected SimpleImage overlay;
	protected SimpleImage background;
	
	public MapViewer( UUID uuid ) {
		this.uuid = uuid;
	}
	
	public MapViewer( UUID uuid, FileConfiguration config ) {
		ConfigurationSection section = config.getConfigurationSection( "settings" );
		if ( section != null ) {
			for ( String key : section.getKeys( false ) ) {
				settings.put( key, section.getString( key ) );
			}
		}
	}

	public final UUID getUUID() {
		return uuid;
	}
	
	public SimpleImage getOverlay() {
		return overlay;
	}

	public void setOverlay( SimpleImage overlay ) {
		if ( overlay != null ) {
			overlay = new SimpleImage( overlay, 128, 128, Image.SCALE_REPLICATE );
		}
		this.overlay = overlay;
	}

	public SimpleImage getBackground() {
		return background;
	}

	public void setBackground( SimpleImage background ) {
		if ( background != null ) {
			background = new SimpleImage( background, 128, 128, Image.SCALE_REPLICATE );
		}
		this.background = background;
	}
	
	public < T extends Comparable< T > > T getSetting( SettingState< T > state ) {
		String id = state.getId();
		if ( SETTING_STATES.containsKey( id ) ) {
			if ( settings.containsKey( id ) ) {
				Optional< T > optional = state.getFrom( settings.get( id ) );
				if ( optional.isPresent() ) {
					return optional.get();
				}
			}

			return state.getDefault();
		} else {
			throw new IllegalArgumentException( state.getId() + " is not a registered state!" );
		}
	}
	
	public < T extends Comparable< T > > void setSetting( SettingState< T > state, T val ) {
		if ( SETTING_STATES.containsKey( state.getId() ) ) {
			settings.put( state.getId(), state.convertToString( val ) );
		} else {
			throw new IllegalArgumentException( state.getId() + " is not a registered state!" );
		}
	}

	public Map< String, String > getSettings() {
		return settings;
	}

	public void saveTo( FileConfiguration config ) {
		ConfigurationSection section = config.createSection( "settings" );
		for ( Entry< String, String > entry : settings.entrySet() ) {
			section.set( entry.getKey(), entry.getValue() );
		}
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
	
	public static void addSetting( SettingState< ? > state ) {
		SETTING_STATES.put( state.getId(), state );
	}
	
	public static void removeSetting( SettingState< ? > state ) {
		SETTING_STATES.remove( state.getId() );
	}
	
	public static SettingState< ? > getState( String id ) {
		return SETTING_STATES.get( id );
	}
	
	public static Collection< SettingState< ? > > getStates() {
		return SETTING_STATES.values();
	}
}

package io.github.bananapuncher714.cartographer.module.guilds;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.map.MapCursor.Type;

import io.github.bananapuncher714.cartographer.core.api.events.minimap.MinimapLoadEvent;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingStateBoolean;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.module.Module;
import io.github.bananapuncher714.cartographer.core.util.FailSafe;
import io.github.bananapuncher714.cartographer.core.util.FileUtil;

public class GuildsModule extends Module implements Listener {
	public static final SettingStateBoolean GUILDS_SHOW_HOME = SettingStateBoolean.of( "guilds_show_home", false, true );
	public static final SettingStateBoolean GUILDS_SHOW_ALLIES = SettingStateBoolean.of( "guilds_show_allies", false, true );
	public static final SettingStateBoolean GUILDS_SHOW_MEMBERS = SettingStateBoolean.of( "guilds_show_members", false, true );
	public static final SettingStateBoolean GUILDS_SHOW_NEUTRAL = SettingStateBoolean.of( "guilds_show_neutral", false, true );
	
	protected CursorProperties homeProperties;
	protected Map< Integer, CursorProperties > roleProperties;
	protected CursorProperties allyProperties;
	protected CursorProperties neutralProperties;
	
	@Override
	public void onEnable() {
		registerSettings();

		FileUtil.saveToFile( getResource( "config.yml" ), new File( getDataFolder(), "/config.yml" ), false );
		FileUtil.saveToFile( getResource( "README.md" ), new File( getDataFolder(), "/README.md" ), false );

		roleProperties = new HashMap< Integer, CursorProperties >();
		loadConfig();

		for ( Minimap minimap : getCartographer().getMapManager().getMinimaps().values() ) {
			init( minimap );
		}

		registerListener( this );
	}
	
	@Override
	public SettingState< ? >[] getSettingStates() {
		SettingState< ? >[] states = new SettingState< ? >[] {
			GUILDS_SHOW_HOME,
			GUILDS_SHOW_ALLIES,
			GUILDS_SHOW_MEMBERS,
			GUILDS_SHOW_NEUTRAL
		};
		return states;
	}
	
	@EventHandler
	private void onEvent( MinimapLoadEvent event ) {
		init( event.getMinimap() );
	}

	private void loadConfig() {
		roleProperties.clear();
		
		FileConfiguration config = YamlConfiguration.loadConfiguration( new File( getDataFolder(), "config.yml" ) );
		ConfigurationSection section = config.getConfigurationSection( "icons" );
		
		if ( section == null ) {
			getLogger().severe( "No 'icons' found in the config!" );
		} else {
			neutralProperties = loadFrom( section.getConfigurationSection( "neutral" ), null );
			allyProperties = loadFrom( section.getConfigurationSection( "ally" ), null );
			String guildHomeName = section.getString( "guild-home.display-name" );
			homeProperties = loadFrom( section.getConfigurationSection( "guild-home" ), () -> { return guildHomeName; } );
			
			ConfigurationSection roles = section.getConfigurationSection( "roles" );
			for ( String key : roles.getKeys( false ) ) {
				int id = Integer.valueOf( key );
				roleProperties.put( id, loadFrom( roles.getConfigurationSection( key ), null ) );
			}
		}
	}
	
	private void init( Minimap minimap ) {
		minimap.register( new PlayerCursorProvider( this ) );
		minimap.register( new GuildHomeCursorProvider( this ) );
	}
	
	private CursorProperties loadFrom( ConfigurationSection section, Supplier< String > supplier ) {
		Type type = FailSafe.getEnum( Type.class, section.getString( "icon" ).split( "\\s+" ) );
		CursorVisibility visibility = FailSafe.getEnum( CursorVisibility.class, section.getString( "default-visibility" ) );
		double range = section.getDouble( "range" );
		boolean enabled = section.getBoolean( "enabled" );
		boolean showName = section.getBoolean( "show-name" );
		return new CursorProperties( type, supplier ).setEnabled( enabled ).setRadius( range ).setVisibility( visibility ).setShowName( showName );
	}

	public CursorProperties getHomeProperties() {
		return homeProperties;
	}

	public CursorProperties getAllyProperties() {
		return allyProperties;
	}

	public CursorProperties getNeutralProperties() {
		return neutralProperties;
	}
	
	public CursorProperties getRoleProperties( int id ) {
		return roleProperties.get( id );
	}
}

package io.github.bananapuncher714.cartographer.module.worldguard;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.github.bananapuncher714.cartographer.core.api.events.minimap.MinimapLoadEvent;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingStateBoolean;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.palette.PaletteManager;
import io.github.bananapuncher714.cartographer.core.module.Module;
import io.github.bananapuncher714.cartographer.core.util.FileUtil;
import io.github.bananapuncher714.cartographer.module.worldguard.api.WorldGuardWrapper;

public class WorldGuardModule extends Module implements Listener {
	public static final SettingStateBoolean WORLDGUARD_REGIONS = SettingStateBoolean.of( "worldguard_show_regions", false, true );
	
	protected Map< String, RegionColors > colors;
	protected RegionColors defColors;
	
	protected WorldGuardWrapper wrapper;
	
	@Override
	public void onEnable() {
		registerSettings();

		FileUtil.saveToFile( getResource( "config.yml" ), new File( getDataFolder(), "/config.yml" ), false );
		FileUtil.saveToFile( getResource( "README.md" ), new File( getDataFolder(), "/README.md" ), false );

		wrapper = getWrapperImpl();
		
		if ( wrapper == null ) {
			getLogger().severe( "No compatible WorldGuard version found! Disabling..." );
			getCartographer().getModuleManager().disableModule( this );
			return;
		}
		
		colors = new HashMap< String, RegionColors >();
		loadConfig();

		for ( Minimap minimap : getCartographer().getMapManager().getMinimaps().values() ) {
			init( minimap );
		}

		registerListener( this );
	}
	
	@Override
	public SettingState< ? >[] getSettingStates() {
		SettingState< ? >[] states = new SettingState< ? >[] {
			WORLDGUARD_REGIONS
		};
		return states;
	}
	
	private WorldGuardWrapper getWrapperImpl() {
		try {
            Class.forName( "com.sk89q.worldguard.WorldGuard" );
            return new io.github.bananapuncher714.cartographer.module.worldguard.implementation.v7.WorldGuardWrapperImpl();
        } catch (ClassNotFoundException e) {
            try {
                Class.forName( "com.sk89q.worldguard.protection.flags.registry.FlagRegistry" );
                return new io.github.bananapuncher714.cartographer.module.worldguard.implementation.v7.WorldGuardWrapperImpl();
            } catch (ClassNotFoundException e1) {
                getLogger().severe( "WorldGuard v6 and v7 not found!" );
            }
        }
		return null;
	}
	
	@EventHandler
	private void onEvent( MinimapLoadEvent event ) {
		init( event.getMinimap() );
	}

	private void init( Minimap minimap ) {
		minimap.register( new RegionBorderShader( this ) );
	}
	
	private void loadConfig() {
		colors.clear();
		
		FileConfiguration config = YamlConfiguration.loadConfiguration( new File( getDataFolder(), "config.yml" ) );
		ConfigurationSection section = config.getConfigurationSection( "colors" );
		
		if ( section == null ) {
			getLogger().warning( "No 'colors' section found!" );
		} else {
			defColors = loadFrom( section.getConfigurationSection( "default" ) );
			
			if ( section.contains( "regions" ) ) {
				ConfigurationSection regions = section.getConfigurationSection( "regions" );
				for ( String key : regions.getKeys( false ) ) {
					colors.put( key, loadFrom( regions.getConfigurationSection( key ) ) );
				}
			}
		}
	}
	
	private RegionColors loadFrom( ConfigurationSection section ) {
		Color nonmember = PaletteManager.fromString( section.getString( "nonmember" ) ).get();
		Color member = PaletteManager.fromString( section.getString( "member" ) ).get();
		Color owner = PaletteManager.fromString( section.getString( "owner" ) ).get();
		return new RegionColors( owner, member, nonmember );
	}
	
	protected RegionColors getFor( String name ) {
		return colors.getOrDefault( name, defColors );
	}
	
	protected WorldGuardWrapper getWrapper() {
		return wrapper;
	}
}

package io.github.bananapuncher714.cartographer.module.lands;

import java.awt.Color;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.map.MapCursor.Type;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.events.minimap.MinimapLoadEvent;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.palette.PaletteManager;
import io.github.bananapuncher714.cartographer.core.module.Module;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.core.util.FailSafe;
import io.github.bananapuncher714.cartographer.core.util.FileUtil;
import io.github.bananapuncher714.cartographer.module.lands.ChunkBorderShader.ChunkBorderData;
import io.github.bananapuncher714.cartographer.module.lands.settings.SettingStateLandVisibility;
import io.github.bananapuncher714.cartographer.module.lands.visibility.CursorVisibility;
import io.github.bananapuncher714.cartographer.module.lands.visibility.LandVisibility;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.ChunkCoordinate;
import me.angeschossen.lands.api.land.Land;

public class LandsModule extends Module implements Listener {
	public static final SettingStateLandVisibility LANDS_SHOW_LANDS = new SettingStateLandVisibility( "lands_show_lands", false, LandVisibility.ALL, LandVisibility.values() );
	public static final SettingStateLandVisibility LANDS_SHOW_SPAWN = new SettingStateLandVisibility( "lands_show_spawn", false, LandVisibility.OWN, LandVisibility.TRUSTED, LandVisibility.OWN, LandVisibility.NONE );
	public static final SettingStateLandVisibility LANDS_SHOW_PLAYERS = new SettingStateLandVisibility( "lands_show_players", false, LandVisibility.ALL, LandVisibility.ALL, LandVisibility.OWN, LandVisibility.NONE );
//	public static final SettingStateAreaVisibility LANDS_SHOW_AREAS = new SettingStateAreaVisibility( "lands_show_areas", false, AreaVisibility.LOCAL );
	
	private LandsIntegration integration;
	
	protected Map< Integer, Map< ChunkLocation, Set< BlockFace > > > data;

	protected Color landOwner;
	protected Color landTrusted;
	protected Color landUntrusted;
	
	protected CursorProperties owner;
	protected CursorProperties member;
	protected CursorProperties trusted;
	protected CursorProperties defCursor;
	protected CursorProperties landSpawn;
	
	@Override
	public void onEnable() {
		registerSettings();

		FileUtil.saveToFile( getResource( "config.yml" ), new File( getDataFolder(), "/config.yml" ), false );
		FileUtil.saveToFile( getResource( "README.md" ), new File( getDataFolder(), "/README.md" ), false );

		loadConfig();

		for ( Minimap minimap : getCartographer().getMapManager().getMinimaps().values() ) {
			init( minimap );
		}

		data = new HashMap< Integer, Map< ChunkLocation, Set< BlockFace > > >();
		
		integration = new LandsIntegration( getCartographer() );
		
		runTaskTimer( this::update, 20, 20 );
		
		registerListener( this );
	}
	
	@Override
	public SettingState< ? >[] getSettingStates() {
		SettingState< ? >[] states = new SettingState< ? >[] {
			LANDS_SHOW_LANDS,
			LANDS_SHOW_SPAWN,
			LANDS_SHOW_PLAYERS
//			LANDS_SHOW_AREAS
		};
		return states;
	}
	
	@EventHandler
	private void onEvent( MinimapLoadEvent event ) {
		init( event.getMinimap() );
	}

	private void init( Minimap minimap ) {
		minimap.register( new ChunkBorderShader( this::getData, 16 ) );
		minimap.register( new HomeCursorProvider( this ) );
		minimap.register( new PlayerCursorProvider( this ) );
	}
	
	private void loadConfig() {
		FileConfiguration config = YamlConfiguration.loadConfiguration( new File( getDataFolder(), "config.yml" ) );
		
		landOwner = PaletteManager.fromString( config.getString( "color.land.owner", "0x00FFFF" ) ).get();
		landTrusted = PaletteManager.fromString( config.getString( "color.land.trusted", "0x00FF00" ) ).get();
		landUntrusted = PaletteManager.fromString( config.getString( "color.land.untrusted", "0xFFFF00" ) ).get();
		
		owner = loadFrom( config.getConfigurationSection( "icons.owner" ), null );
		member = loadFrom( config.getConfigurationSection( "icons.member" ), null );
		trusted = loadFrom( config.getConfigurationSection( "icons.trusted" ), null );
		defCursor = loadFrom( config.getConfigurationSection( "icons.default" ), null );
		String spawnName = config.getString( "icons.spawn.name" );
		landSpawn = loadFrom( config.getConfigurationSection( "icons.spawn" ), () -> { return spawnName; } );
	}
	
	private void update() {
		data.clear();
		for ( Land land : integration.getLands() ) {
			Set< ChunkLocation > locations = new HashSet< ChunkLocation >();
			for ( World world : Bukkit.getWorlds() ) {
				Collection< ChunkCoordinate > coords = land.getChunks( world );
				if ( coords != null ) {
					for ( ChunkCoordinate coordinate : coords ) {
						locations.add( new ChunkLocation( world, coordinate.getX(), coordinate.getZ() ) );
					}
				}
			}
			
			if ( !locations.isEmpty() ) {
				data.put( land.getId(), ChunkBorderShader.getBorders( locations ) );
			}
		}
	}

	private Collection< ChunkBorderData > getData( Player player, PlayerSetting setting ) {
		Set< ChunkBorderData > chunks = new HashSet< ChunkBorderData >();
		
		MapViewer viewer = getCartographer().getPlayerManager().getViewerFor( setting.getUUID() );

		LandVisibility landVis = viewer.getSetting( LandsModule.LANDS_SHOW_LANDS );
		if ( landVis != LandVisibility.NONE ) {
			UUID uuid = setting.getUUID();
			Collection< Land > lands = integration.getLands();
			for ( Land land : lands ) {
				UUID owner = land.getOwnerUID();
				if ( uuid.equals( owner ) || landVis == LandVisibility.ALL || ( landVis == LandVisibility.TRUSTED && land.getTrustedPlayer( uuid ).isTrustedWholeLand() ) ) {
					Map< ChunkLocation, Set< BlockFace > > locations = data.get( land.getId() );
					
					if ( locations != null ) {
						Color color = landUntrusted;
						if ( uuid.equals( owner ) ) {
							color = landOwner;
						} else if ( land.getTrustedPlayer( uuid ).isTrustedWholeLand() ) {
							color = landTrusted;
						}
						
						for ( Entry< ChunkLocation, Set< BlockFace > > entry : locations.entrySet() ) {
							ChunkLocation chunkLoc = entry.getKey();
							if ( chunkLoc.getWorld() == setting.getLocation().getWorld() ) {
								ChunkBorderData chunkData = new ChunkBorderData( chunkLoc, color );
								chunkData.getFaces().addAll( entry.getValue() );
								chunks.add( chunkData );
							}
						}
					}
				}
			}
		}
		
		return chunks;
	}
	
	private CursorProperties loadFrom( ConfigurationSection section, Supplier< String > supplier ) {
		Type type = FailSafe.getEnum( Type.class, section.getString( "icon" ).split( "\\s+" ) );
		CursorVisibility visibility = FailSafe.getEnum( CursorVisibility.class, section.getString( "default-visibility" ) );
		double range = section.getDouble( "range" );
		boolean enabled = section.getBoolean( "enabled" );
		boolean showName = section.getBoolean( "show-name" );
		return new CursorProperties( type, supplier ).setEnabled( enabled ).setRadius( range ).setVisibility( visibility ).setShowName( showName );
	}
	
	protected LandsIntegration getIntegration() {
		return integration;
	}
	
	protected CursorProperties getLandOwnerProperties() {
		return owner;
	}
	
	protected CursorProperties getLandMemberProperties() {
		return member;
	}
	
	protected CursorProperties getLandTrustedProperties() {
		return trusted;
	}
	
	protected CursorProperties getLandUntrustedProperties() {
		return defCursor;
	}
	
	protected CursorProperties getLandSpawnProperties() {
		return landSpawn;
	}
}

package io.github.bananapuncher714.cartographer.module.towny;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursor.Type;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.configuration.YamlFileConfiguration;
import io.github.bananapuncher714.cartographer.core.api.events.minimap.MinimapLoadEvent;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingStateBoolean;
import io.github.bananapuncher714.cartographer.core.configuration.YamlMerger;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.palette.PaletteManager.ColorType;
import io.github.bananapuncher714.cartographer.core.module.Module;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;
import io.github.bananapuncher714.cartographer.core.util.FailSafe;
import io.github.bananapuncher714.cartographer.core.util.FileUtil;
import io.github.bananapuncher714.cartographer.module.towny.ChunkBorderShader.ChunkBorderData;

public class TownyModule extends Module implements Listener {
	public static final SettingStateBoolean TOWNY_CLAIMS = SettingStateBoolean.of( "towny_show_claims", false, true );
	public static final SettingStateBoolean TOWNY_PLAYERS = SettingStateBoolean.of( "towny_show_players", false, true );
	public static final SettingStateBoolean TOWNY_SPAWN = SettingStateBoolean.of( "towny_show_spawn", false, true );
	
	private Map< String, Map< ChunkLocation, Set< BlockFace > > > claims = new HashMap< String, Map< ChunkLocation, Set< BlockFace > > >();
	private Map< TownyRelation, Color > colors = new HashMap< TownyRelation, Color >();
	private Map< TownyRelation, CursorProperties > icons = new HashMap< TownyRelation, CursorProperties >();

	private boolean showName = true;

	private Type homeType;

	@Override
	public void onEnable() {
		registerSettings();

		FileUtil.saveToFile( getResource( "config.yml" ), new File( getDataFolder(), "/config.yml" ), false );
		FileUtil.saveToFile( getResource( "README.md" ), new File( getDataFolder(), "/README.md" ), false );

		loadConfig();

		for ( Minimap minimap : getCartographer().getMapManager().getMinimaps().values() ) {
			minimap.register( new ChunkBorderShader( this::getData, Coord.getCellSize() ) );
			minimap.register( new HomeWaypointProvider( homeType ) );
			minimap.register( new PlayerMarkerProvider( showName, this::getType ) );
		}

		runTaskTimer( this::tick, 20, 10 );
		
		registerListener( this );
	}

	@Override
	public SettingState< ? >[] getSettingStates() {
		SettingState< ? >[] states = new SettingState< ? >[] {
			TOWNY_CLAIMS,
			TOWNY_PLAYERS,
			TOWNY_SPAWN
		};
		return states;
	}

	private void loadConfig() {
		colors.clear();
		icons.clear();

		YamlFileConfiguration configuration = new YamlFileConfiguration( new File( getDataFolder(), "config.yml" ).toPath() );
		try {
			configuration.load();
			YamlMerger merger = new YamlMerger( configuration, getResource( "config.yml" ) );
			merger.updateHeader( false );
			merger.updateKeys();
			merger.trimKeys();
			merger.updateComments( false );
		} catch ( IOException | InvalidConfigurationException e ) {
			e.printStackTrace();
		}		
		FileConfiguration config = configuration.getConfiguration();
		
		for ( String key : config.getConfigurationSection( "town" ).getKeys( false ) ) {
			Matcher matcher = ColorType.RGB.getPattern().matcher( config.getString( "town." + key ) );
			matcher.find();
			String r = matcher.group( 1 );
			String g = matcher.group( 2 );
			String b = matcher.group( 3 );
			colors.put( FailSafe.getEnum( TownyRelation.class, key.toUpperCase() ), new Color( Integer.parseInt( r ), Integer.parseInt( g ), Integer.parseInt( b ) ) );
		}


		ConfigurationSection playerSection = config.getConfigurationSection( "player" );
		for ( String key : playerSection.getKeys( false ) ) {
			TownyRelation status = FailSafe.getEnum( TownyRelation.class, key.toUpperCase() );
			CursorProperties properties = loadFrom( playerSection.getConfigurationSection( key ) );

			icons.put( status, properties );
		}

		showName = config.getBoolean( "show-name" );

		homeType = getType( config.getString( "home-icon" ).split( "\\s+") );
		
		try {
			configuration.save();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}
	
	private CursorProperties loadFrom( ConfigurationSection section ) {
		CursorProperties properties = new CursorProperties();
		
		properties.setEnabled( section.getBoolean( "enabled" ) );
		properties.setType( getType( section.getString( "icon" ).split( "\\s+" ) ) );
		properties.setGlobalRange( section.getDouble( "global-range" ) );
		properties.setTownRange( section.getDouble( "town-range" ) );
		properties.setMaxZoom( section.getDouble( "max-zoom", 16 ) );
		
		return properties;
	}

	@EventHandler
	private void onEvent( MinimapLoadEvent event ) {
		Minimap minimap = event.getMinimap();
		minimap.register( new ChunkBorderShader( this::getData, Coord.getCellSize() ) );
		minimap.register( new HomeWaypointProvider( homeType ) );
		minimap.register( new PlayerMarkerProvider( showName, this::getType ) );
	}

	private void tick() {
		claims.clear();
		for ( Town town : TownyUniverse.getInstance().getTowns() ) {
			Set< ChunkLocation > chunks = new HashSet< ChunkLocation >();
			for ( TownBlock block : town.getTownBlocks() ) {
				Coord coord = block.getCoord();
				World world = block.getWorldCoord().getBukkitWorld();
				chunks.add( new ChunkLocation( world, coord.getX(), coord.getZ() ) );
			}
			claims.put( town.getName(), ChunkBorderShader.getBorders( chunks ) );
		}
	}

	private Optional< Type > getType( Player viewer, Player target, double scale ) {
		try {
			Resident resident = TownyUniverse.getInstance().getResident( viewer.getName() );
			Town resTown = null;
			if ( resident.hasTown() ) {
				resTown = resident.getTown();
			}
			Nation resNation = null;
			if ( resTown != null && resTown.hasNation() ) {
				resNation = resTown.getNation();
			}

			Resident targetRes = TownyUniverse.getInstance().getResident( target.getName() );
			Town town = null;
			if ( targetRes.hasTown() ) {
				town = targetRes.getTown();
			}
			Nation nation = null;
			if ( town != null && town.hasNation() ) {
				nation = town.getNation();
			}

			TownyRelation relation = TownyRelation.NEUTRAL;
			if ( resTown != null && town == resTown ) {
				relation = TownyRelation.MEMBER_TOWN;
			} else if ( nation != null && nation == resNation ) {
				relation = TownyRelation.MEMBER_NATION;
			} else if ( resTown != null && town != null && town.isAlliedWith( resTown ) ) {
				relation = TownyRelation.ALLIED_TOWN;
			} else if ( nation != null && resNation != null && nation.isAlliedWith( resNation ) ) {
				relation = TownyRelation.ALLIED_NATION;
			} else if ( nation != null && resNation != null && resNation.getEnemies().contains( nation ) ) {
				relation = TownyRelation.ENEMY_NATION;
			}

			CursorProperties properties = icons.get( relation );
			if ( properties != null && properties.isEnabled() && scale <= properties.getMaxZoom() ) {
				Location targetLoc = target.getLocation();
				UUID townUUID= TownyAPI.getInstance().getTownUUID( targetLoc );
				Town occupying = townUUID == null ? null : TownyUniverse.getInstance().getTown( townUUID );
				double range = properties.getGlobalRange();
				if ( occupying != null && occupying.getMayor().getUUID().equals( viewer.getUniqueId() ) ) {
					range = properties.getTownRange();
				}
				range *= range;

				Location loc = viewer.getLocation();
				if ( loc.distanceSquared( targetLoc ) <= range ) {
					return Optional.of( properties.getType() );
				}
			}
			return Optional.empty();
		} catch ( NotRegisteredException e ) {
			e.printStackTrace();
		}
		return Optional.of( Type.WHITE_POINTER );
	}
	
	private Collection< ChunkBorderData > getData( Player player, PlayerSetting setting ) {
		MapViewer viewer = getCartographer().getPlayerManager().getViewerFor( player.getUniqueId() );
		Set< ChunkBorderData > data = new HashSet< ChunkBorderData >();
		if ( viewer.getSetting( TOWNY_CLAIMS ) ) {
			try {
				Resident resident = TownyUniverse.getInstance().getResident( player.getName() );
				Town resTown = null;
				if ( resident.hasTown() ) {
					resTown = resident.getTown();
				}
				Nation resNation = null;
				if ( resTown != null && resTown.hasNation() ) {
					resNation = resTown.getNation();
				}

				for ( Entry< String, Map< ChunkLocation, Set< BlockFace > > > entry : claims.entrySet() ) {
					Town town = TownyUniverse.getInstance().getTown( entry.getKey() );
					if ( town == null ) {
						continue;
					}
					
					Nation nation = null;
					if ( town.hasNation() ) {
						nation = town.getNation();
					}

					Color color = colors.get( TownyRelation.NEUTRAL );
					if ( resTown != null && town == resTown ) {
						color = colors.get( TownyRelation.MEMBER_TOWN );
					} else if ( nation != null && nation == resNation ) {
						color = colors.get( TownyRelation.MEMBER_NATION );
					} else if ( resTown != null && town!= null && town.isAlliedWith( resTown ) ) {
						color = colors.get( TownyRelation.ALLIED_TOWN );
					} else if ( nation != null && resNation != null && nation.isAlliedWith( resNation ) ) {
						color = colors.get( TownyRelation.ALLIED_NATION );
					} else if ( nation != null && resNation != null && resNation.getEnemies().contains( nation ) ) {
						color = colors.get( TownyRelation.ENEMY_NATION );
					}

					for ( Entry< ChunkLocation, Set< BlockFace > > borderEntry : entry.getValue().entrySet() ) {
						ChunkBorderData borderData = new ChunkBorderData( borderEntry.getKey(), color );
						borderData.getFaces().addAll( borderEntry.getValue() );
						data.add( borderData );
					}
				}
			} catch ( NotRegisteredException e ) {
				e.printStackTrace();
			}
		}

		return data;
	}
	
   private static Type getType( String[] types ) {
        try {
            Method values = Type.class.getMethod( "values" );
            Method name = Type.class.getMethod( "name" );
            Type[] constants = ( Type[] ) values.invoke( Type.class );
            if ( types == null || types.length == 0 ) return constants[ 0 ];
            if ( types[ 0 ].equals( "RED_MARKER" ) ) types[ 0 ] = "TARGET_POINT";
            for ( Type t : constants ) {
                if ( name.invoke( t ).equals( types[ 0 ] ) ) {
                    return t;
                }
            }
            return getType( FailSafe.pop( types ) );
        } catch ( NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
            e.printStackTrace();
        }
        return null;
    }

	public enum TownyRelation {
		NEUTRAL, MEMBER_TOWN, ALLIED_TOWN, MEMBER_NATION, ALLIED_NATION, ENEMY_NATION;
	}
}

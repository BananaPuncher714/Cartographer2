package io.github.bananapuncher714.cartographer.module.towny;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.map.MapCursor.Type;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.events.minimap.MinimapLoadEvent;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingStateBoolean;
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

	private static Method TOWN_GETTOWNBLOCKS;
	
	static {
		try {
			TOWN_GETTOWNBLOCKS = Town.class.getMethod( "getTownBlocks" );
		} catch ( NoSuchMethodException | SecurityException e ) {
			e.printStackTrace();
		}
	}
	
	private Map< String, Map< ChunkLocation, Set< BlockFace > > > claims = new HashMap< String, Map< ChunkLocation, Set< BlockFace > > >();
	private Map< TownyRelation, Color > colors = new HashMap< TownyRelation, Color >();
	private Map< TownyRelation, Type > icons = new HashMap< TownyRelation, Type >();

	private int playerRange = 100;
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
			minimap.register( new PlayerMarkerProvider( playerRange, showName, this::getType ) );
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

		FileConfiguration config = YamlConfiguration.loadConfiguration( new File( getDataFolder(), "config.yml" ) );
		for ( String key : config.getConfigurationSection( "town" ).getKeys( false ) ) {
			Matcher matcher = ColorType.RGB.getPattern().matcher( config.getString( "town." + key ) );
			matcher.find();
			String r = matcher.group( 1 );
			String g = matcher.group( 2 );
			String b = matcher.group( 3 );
			colors.put( FailSafe.getEnum( TownyRelation.class, key.toUpperCase() ), new Color( Integer.parseInt( r ), Integer.parseInt( g ), Integer.parseInt( b ) ) );
		}


		for ( String key : config.getConfigurationSection( "player" ).getKeys( false ) ) {
			TownyRelation status = FailSafe.getEnum( TownyRelation.class, key.toUpperCase() );
			Type type = FailSafe.getEnum( Type.class, config.getString( "player." + key ) );

			icons.put( status, type );
		}

		playerRange = config.getInt( "player-range" );
		showName = config.getBoolean( "show-name" );

		homeType = FailSafe.getEnum( Type.class, config.getString( "home-icon" ) );
	}

	@EventHandler
	private void onEvent( MinimapLoadEvent event ) {
		Minimap minimap = event.getMinimap();
		minimap.register( new ChunkBorderShader( this::getData, Coord.getCellSize() ) );
		minimap.register( new HomeWaypointProvider( homeType ) );
		minimap.register( new PlayerMarkerProvider( playerRange, showName, this::getType ) );
	}

	private void tick() {
		claims.clear();
		for ( Town town : TownyAPI.getInstance().getDataSource().getTowns() ) {
			Set< ChunkLocation > chunks = new HashSet< ChunkLocation >();
			for ( TownBlock block : getBlocks( town ) ) {
				Coord coord = block.getCoord();
				World world = block.getWorldCoord().getBukkitWorld();
				chunks.add( new ChunkLocation( world, coord.getX(), coord.getZ() ) );
			}
			claims.put( town.getName(), ChunkBorderShader.getBorders( chunks ) );
		}
	}
	
	private Collection< TownBlock > getBlocks( Town town ) {
		Object collection = new ArrayList< TownBlock >();
		try {
			collection = TOWN_GETTOWNBLOCKS.invoke( town );
		} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			e.printStackTrace();
		}
		return ( Collection< TownBlock > ) collection;
	}

	private Type getType( Player viewer, Player target ) {
		try {
			Resident resident = TownyAPI.getInstance().getDataSource().getResident( viewer.getName() );
			Town resTown = null;
			if ( resident.hasTown() ) {
				resTown = resident.getTown();
			}
			Nation resNation = null;
			if ( resTown != null && resTown.hasNation() ) {
				resNation = resTown.getNation();
			}

			Resident targetRes = TownyAPI.getInstance().getDataSource().getResident( target.getName() );
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

			return icons.get( relation );
		} catch ( NotRegisteredException e ) {
			e.printStackTrace();
		}
		return Type.WHITE_POINTER;
	}
	
	private Collection< ChunkBorderData > getData( Player player, PlayerSetting setting ) {
		MapViewer viewer = getCartographer().getPlayerManager().getViewerFor( player.getUniqueId() );
		Set< ChunkBorderData > data = new HashSet< ChunkBorderData >();
		if ( viewer.getSetting( TOWNY_CLAIMS ) ) {
			try {
				Resident resident = TownyAPI.getInstance().getDataSource().getResident( player.getName() );
				Town resTown = null;
				if ( resident.hasTown() ) {
					resTown = resident.getTown();
				}
				Nation resNation = null;
				if ( resTown != null && resTown.hasNation() ) {
					resNation = resTown.getNation();
				}

				for ( Entry< String, Map< ChunkLocation, Set< BlockFace > > > entry : claims.entrySet() ) {
					Town town = null;
					try {
						town = TownyAPI.getInstance().getDataSource().getTown( entry.getKey() );
					} catch ( NotRegisteredException e ) {
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

	public enum TownyRelation {
		NEUTRAL, MEMBER_TOWN, ALLIED_TOWN, MEMBER_NATION, ALLIED_NATION, ENEMY_NATION;
	}
}

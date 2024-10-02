package io.github.bananapuncher714.cartographer.module.factionsuuid;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.map.MapCursor.Type;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.perms.Relation;

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
import io.github.bananapuncher714.cartographer.module.factionsuuid.ChunkBorderShader.ChunkBorderData;

public class FactionsUUIDModule extends Module implements Listener {
	public static final SettingStateBoolean FACTION_CLAIMS = SettingStateBoolean.of( "factions_show_claims", false, true );
	public static final SettingStateBoolean FACTION_PLAYERS = SettingStateBoolean.of( "factions_show_players", false, true );
	public static final SettingStateBoolean FACTION_HOME = SettingStateBoolean.of( "factions_show_fhome", false, true );

	private Map< String, Map< ChunkLocation, Set< BlockFace > > > claims = new HashMap< String, Map< ChunkLocation, Set< BlockFace > > >();
	private Map< FactionStatus, Color > colors = new HashMap< FactionStatus, Color >();
	private Map< Relation, Type > icons = new HashMap< Relation, Type >();

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
			minimap.register( new ChunkBorderShader( this::getData ) );
			minimap.register( new HomeWaypointProvider( homeType ) );
			minimap.register( new PlayerMarkerProvider( playerRange, showName, this::getType ) );
		}

		runTaskTimer( this::tick, 20, 10 );
		
		registerListener( this );
	}

	@Override
	public SettingState< ? >[] getSettingStates() {
		SettingState< ? >[] states = new SettingState< ? >[] {
			FACTION_CLAIMS,
			FACTION_PLAYERS,
			FACTION_HOME
		};
		return states;
	}

	private void loadConfig() {
		colors.clear();
		icons.clear();

		FileConfiguration config = YamlConfiguration.loadConfiguration( new File( getDataFolder(), "config.yml" ) );
		for ( String key : config.getConfigurationSection( "faction" ).getKeys( false ) ) {
			Matcher matcher = ColorType.RGB.getPattern().matcher( config.getString( "faction." + key ) );
			matcher.find();
			String r = matcher.group( 1 );
			String g = matcher.group( 2 );
			String b = matcher.group( 3 );
			colors.put( FailSafe.getEnum( FactionStatus.class, key.toUpperCase() ), new Color( Integer.parseInt( r ), Integer.parseInt( g ), Integer.parseInt( b ) ) );
		}


		for ( String key : config.getConfigurationSection( "player" ).getKeys( false ) ) {
			Relation status = FailSafe.getEnum( Relation.class, key.toLowerCase(), key.toUpperCase() );
			Type type = getType( config.getString( "player." + key ).split( "\\s+" ) );

			icons.put( status, type );
		}

		playerRange = config.getInt( "player-range" );
		showName = config.getBoolean( "show-name" );

		homeType = getType( config.getString( "home-icon" ).split( "\\s+" ) );
	}

	@EventHandler
	private void onEvent( MinimapLoadEvent event ) {
		Minimap minimap = event.getMinimap();
		minimap.register( new ChunkBorderShader( this::getData ) );
		minimap.register( new HomeWaypointProvider( homeType ) );
		minimap.register( new PlayerMarkerProvider( playerRange, showName, this::getType ) );
	}

	private void tick() {
		claims.clear();
		for ( Faction faction : Factions.getInstance().getAllFactions() ) {
			Set< ChunkLocation > chunks = faction
					.getAllClaims()
					.stream()
					.map( loc -> { return new ChunkLocation( loc.getWorld(), ( int ) loc.getX(), ( int ) loc.getZ() ); } )
					.collect( Collectors.toSet() );
			claims.put( faction.getId(), ChunkBorderShader.getBorders( chunks ) );
		}
	}

	private Type getType( Player viewer, Player target ) {
		FPlayer fViewer = FPlayers.getInstance().getByPlayer( viewer );
		FPlayer fTarget = FPlayers.getInstance().getByPlayer( target );
		Relation relation = fViewer.getRelationTo( fTarget );
		return icons.get( relation );
	}

	private Collection< ChunkBorderData > getData( Player player, PlayerSetting setting ) {
		MapViewer viewer = getCartographer().getPlayerManager().getViewerFor( player.getUniqueId() );
		Set< ChunkBorderData > data = new HashSet< ChunkBorderData >();
		if ( viewer.getSetting( FACTION_CLAIMS ) ) {
			FPlayer fPlayer = FPlayers.getInstance().getByPlayer( player );
			Faction playerFaction = fPlayer.getFaction();
			for ( Entry< String, Map< ChunkLocation, Set< BlockFace > > > entry : claims.entrySet() ) {
				Faction faction = Factions.getInstance().getFactionById( entry.getKey() );
				Color color = colors.get( FactionStatus.NEUTRAL );
				Relation relation = faction.getRelationTo( fPlayer );
				if ( playerFaction == faction ) {
					color = colors.get( FactionStatus.SELF );
				} else if ( faction.isSafeZone() ) {
					color = colors.get( FactionStatus.SAFEZONE );
				} else if ( faction.isWarZone() ) {
					color = colors.get( FactionStatus.WARZONE );
				} else if ( relation == Relation.ALLY ) {
					color = colors.get( FactionStatus.ALLY );
				} else if ( relation == Relation.ENEMY ) {
					color = colors.get( FactionStatus.ENEMY );
				} else if ( relation == Relation.TRUCE ) {
					color = colors.get( FactionStatus.TRUCE );
				} else if ( relation == Relation.NEUTRAL ) {
					color = colors.get( FactionStatus.NEUTRAL );
				}

				for ( Entry< ChunkLocation, Set< BlockFace > > borderEntry : entry.getValue().entrySet() ) {
					ChunkBorderData borderData = new ChunkBorderData( borderEntry.getKey(), color );
					borderData.getFaces().addAll( borderEntry.getValue() );
					data.add( borderData );
				}
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

	public enum FactionStatus {
		WARZONE, SAFEZONE, SELF, ALLY, ENEMY, NEUTRAL, TRUCE;
	}
}

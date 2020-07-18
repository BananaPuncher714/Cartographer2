package io.github.bananapuncher714.cartographer.module.vanilla;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.map.MapCursor.Type;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import io.github.bananapuncher714.cartographer.core.api.permission.PermissionBuilder;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingStateBoolean;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.module.Module;
import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;
import io.github.bananapuncher714.cartographer.core.util.FailSafe;
import io.github.bananapuncher714.cartographer.core.util.FileUtil;
import io.github.bananapuncher714.cartographer.module.vanilla.providers.CursorConverter;
import io.github.bananapuncher714.cartographer.module.vanilla.providers.CursorConverterEntity;
import io.github.bananapuncher714.cartographer.module.vanilla.providers.CursorConverterNamedLocation;
import io.github.bananapuncher714.cartographer.module.vanilla.providers.CursorConverterPlayer;
import io.github.bananapuncher714.cartographer.module.vanilla.providers.CursorProviderDeathLocation;
import io.github.bananapuncher714.cartographer.module.vanilla.providers.CursorProviderEntity;
import io.github.bananapuncher714.cartographer.module.vanilla.providers.CursorProviderPlayer;
import io.github.bananapuncher714.cartographer.module.vanilla.providers.CursorProviderSpawnLocation;
import io.github.bananapuncher714.cartographer.module.vanilla.providers.ObjectProvider;

public class VanillaPlus extends Module {
	protected static final SettingStateBoolean SETTING_SHOW_DEATH = SettingStateBoolean.of( "vp_show_death_location", false, true );
	protected static final SettingStateBoolean SETTING_SHOW_SPAWN = SettingStateBoolean.of( "vp_show_spawn_location", false, true );
	protected static final SettingStateBoolean SETTING_SHOW_PLAYERS = SettingStateBoolean.of( "vp_show_players", false, true );
	protected static final SettingStateBoolean SETTING_SHOW_ENTITIES = SettingStateBoolean.of( "vp_show_mobs", false, true );
	
	private Map< UUID, Location > deaths = new HashMap< UUID, Location >();
	
	private boolean isBlacklist;
	private Set< String > blacklistedWorlds = new HashSet< String >();
	
	private Set< CursorConverter > defaultConverters = new HashSet< CursorConverter >();

	private Map< UUID, PlayerViewer > viewers = new HashMap< UUID, PlayerViewer >();
	private Map< EntityType, CrossVersionMaterial > entityMaterials = new HashMap< EntityType, CrossVersionMaterial >();
	
	private VanillaWorldCursorProvider cursorProvider;
	
	private boolean deathLocEnabled = true;
	private boolean spawnLocEnabled = true;
	private boolean playerEnabled = true;
	private boolean hasEntities = false;
	
	@Override
	public void onEnable() {
		registerListener( new VanillaListener( this ) );
		
		cursorProvider = new VanillaWorldCursorProvider( this );
		
		for ( Minimap minimap : getCartographer().getMapManager().getMinimaps().values() ) {
			minimap.registerProvider( cursorProvider );
		}
		
		FileUtil.saveToFile( getResource( "README.md" ), new File( getDataFolder() + "/README.md" ), true );
		FileUtil.saveToFile( getResource( "config.yml" ), new File( getDataFolder() + "/config.yml" ), false );
		
		loadConfig();
		
		registerSettings();
		
		Permission death = new PermissionBuilder( "vanillaplus.cursor.location.death" ).setDefault( PermissionDefault.TRUE ).register().build();
		Permission spawn = new PermissionBuilder( "vanillaplus.cursor.location.spawn" ).setDefault( PermissionDefault.TRUE ).register().build();
		Permission players = new PermissionBuilder( "vanillaplus.cursor.players" ).setDefault( PermissionDefault.TRUE ).register().build();
		PermissionBuilder admin = new PermissionBuilder( "vanillaplus.admin" ).setDefault( PermissionDefault.OP )
				.addChild( death, true )
				.addChild( spawn, true )
				.addChild( players, true );

		Set< Permission > entityPermissions = new HashSet< Permission >();
		for ( EntityType type : EntityType.values() ) {
			Permission permission = new PermissionBuilder( "vanillaplus.cursor.entity." + type.name().toLowerCase() ).setDefault( PermissionDefault.TRUE ).register().build();
			entityPermissions.add( permission );
			admin.addChild( permission, true );
		}
		Permission invisible = new PermissionBuilder( "vanillaplus.invisible" ).setDefault( PermissionDefault.FALSE ).register().build();
		admin.addChild( invisible, true );
		
		admin.register();
	}
	
	@Override
	public SettingState< ? >[] getSettingStates() {
		List< SettingState< ? > > states = new ArrayList< SettingState< ? > >();
		
		if ( deathLocEnabled ) {
			states.add( SETTING_SHOW_DEATH );
		}
		
		if ( spawnLocEnabled ) {
			states.add( SETTING_SHOW_SPAWN );
		}
		
		if ( playerEnabled ) {
			states.add( SETTING_SHOW_PLAYERS );
		}
		
		if ( hasEntities ) {
			states.add( SETTING_SHOW_ENTITIES );
		}
		
		return states.toArray( new SettingState[ states.size() ] );
	}

	@Override
	public void onDisable() {
		blacklistedWorlds.clear();
		viewers.clear();
		entityMaterials.clear();
		defaultConverters.clear();
	}
	
	private void loadConfig() {
		FileConfiguration config = YamlConfiguration.loadConfiguration( new File( getDataFolder() + "/" + "config.yml" ) );
		isBlacklist = config.getBoolean( "blacklist", true );
		blacklistedWorlds.addAll( config.getStringList( "blacklisted-worlds" ) );
		
		if ( config.contains( "cursors" ) ) {
			ConfigurationSection cursorSection = config.getConfigurationSection( "cursors" );
			for ( String key : cursorSection.getKeys( false ) ) {
				
				ObjectProvider< NamedLocation > provider = null;
				if ( key.equalsIgnoreCase( "spawn" ) ) {
					provider = new CursorProviderSpawnLocation();
					spawnLocEnabled = cursorSection.getBoolean( key + ".enabled" );
				} else if ( key.equalsIgnoreCase( "death" ) ) {
					provider = new CursorProviderDeathLocation( this );
					deathLocEnabled = cursorSection.getBoolean( key + ".enabled" );
				}
				
				if ( provider != null ) {
					String iconTypes = cursorSection.getString( key + ".icon" );
					Type type = FailSafe.getEnum( Type.class, iconTypes.split( "\\s+" ) );
					CursorVisibility visibility = FailSafe.getEnum( CursorVisibility.class, cursorSection.getString( key + ".default-visibility" ) );
					boolean showName = cursorSection.getBoolean( key + ".show-name" );
					String displayName = ChatColor.translateAlternateColorCodes( '&', cursorSection.getString( key + ".display-name" ) );
					
					CursorConverterNamedLocation converter = new CursorConverterNamedLocation( this, key );
					converter.setType( type );
					converter.setShowName( showName );
					converter.setDisplayName( displayName );
					converter.setVisibility( visibility );
					defaultConverters.add( converter );
				}
			}
		}
		
		if ( config.contains( "players" ) ) {
			playerEnabled = config.getBoolean( "players.enabled" );
			String iconTypes = config.getString( "players.icon" );
			Type type = FailSafe.getEnum( Type.class, iconTypes.split( "\\s+" ) );
			CursorVisibility visibility = FailSafe.getEnum( CursorVisibility.class, config.getString( "players.default-visibility" ) );
			boolean showName = config.getBoolean( "players.show-name" );
			double range = config.getDouble( "players.range" );
			
			CursorConverterPlayer converter = new CursorConverterPlayer( null );
			converter.setShowName( showName );
			converter.setIcon( type );
			converter.setVisibility( visibility );
			defaultConverters.add( converter );
			
			CursorProviderPlayer provider = new CursorProviderPlayer( range );
			
			cursorProvider.setPlayerProvider( provider );
		}
		
		if ( config.contains( "entity" ) ) {
			ConfigurationSection section = config.getConfigurationSection( "entity" );
			for ( String key : section.getKeys( false ) ) {
				hasEntities = true;
				EntityType type = EntityType.valueOf( key.toUpperCase() );
				String iconTypes = section.getString( key + ".icon" );
				Type icon = FailSafe.getEnum( Type.class, iconTypes.split( "\\s+" ) );
				double range = section.getDouble( key + ".range" );
				CursorVisibility visibility = FailSafe.getEnum( CursorVisibility.class, section.getString( key + ".default-visibility" ) );
				boolean showName = section.getBoolean( key + ".show-name" );
				String materialSer = section.getString( key + ".item" );
				
				String[] matVals = materialSer.split( ":" );
				Material material = Material.getMaterial( matVals[ 0 ].toUpperCase() );
				int durability = matVals.length > 1 ? Integer.parseInt( matVals[ 1 ] ): 0;
				if ( material == null ) {
					material = Material.DIAMOND;
				}
				CrossVersionMaterial cvMaterial = new CrossVersionMaterial( material, durability );
				entityMaterials.put( type, cvMaterial );

				CursorConverterEntity converter = new CursorConverterEntity( type );
				converter.setIcon( icon );
				converter.setVisibility( visibility );
				converter.setShowName( showName );
				defaultConverters.add( converter );
				
				CursorProviderEntity provider = new CursorProviderEntity( type, range );
				cursorProvider.addEntityProvider( provider );
			}
		}
	}
	
	public Location getDeathOf( UUID uuid ) {
		return deaths.get( uuid );
	}
	
	public void setDeathOf( UUID uuid, Location loc ) {
		if ( loc == null ) {
			deaths.remove( uuid );
		} else {
			deaths.put( uuid, loc.clone() );
		}
	}
	
	public boolean isWhitelisted( World world ) {
		return isBlacklist ^ blacklistedWorlds.contains( world.getName() );
	}
	
	public CursorConverter getConverterFor( Object object ) {
		for ( CursorConverter converter : defaultConverters ) {
			if ( converter.convertable( object ) ) {
				return converter;
			}
		}
		return null;
	}
	
	public PlayerViewer getViewerFor( UUID uuid ) {
		PlayerViewer viewer = viewers.get( uuid );
		if ( viewer == null ) {
			viewer = new PlayerViewer( this );
			viewers.put( uuid, viewer );
		}
		return viewer;
	}
	
	public VanillaWorldCursorProvider getCursorProvider() {
		return cursorProvider;
	}

	public boolean isDeathLocEnabled() {
		return deathLocEnabled;
	}

	public void setDeathLocEnabled( boolean deathLocEnabled ) {
		this.deathLocEnabled = deathLocEnabled;
	}

	public boolean isSpawnLocEnabled() {
		return spawnLocEnabled;
	}

	public void setSpawnLocEnabled( boolean spawnLocEnabled ) {
		this.spawnLocEnabled = spawnLocEnabled;
	}

	public boolean isPlayerEnabled() {
		return playerEnabled;
	}

	public void setPlayerEnabled( boolean playerEnabled ) {
		this.playerEnabled = playerEnabled;
	}
	
	public boolean hasEntityCursors() {
		return hasEntities;
	}
}

package io.github.bananapuncher714.cartographer.module.residence;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import io.github.bananapuncher714.cartographer.core.api.events.minimap.MinimapLoadEvent;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;
import io.github.bananapuncher714.cartographer.core.api.setting.SettingStateBoolean;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.palette.PaletteManager;
import io.github.bananapuncher714.cartographer.core.module.Module;
import io.github.bananapuncher714.cartographer.core.util.FileUtil;

public class ResidenceModule extends Module implements Listener {
	public static final SettingStateBoolean RESIDENCE_CLAIMS = SettingStateBoolean.of( "residence_show_claims", false, true );
	
	protected List< ClaimProperty > properties;
	protected Optional< Color > owner;
	
	@Override
	public void onEnable() {
		registerSettings();

		FileUtil.saveToFile( getResource( "config.yml" ), new File( getDataFolder(), "/config.yml" ), false );
		FileUtil.saveToFile( getResource( "README.md" ), new File( getDataFolder(), "/README.md" ), false );

		properties = new ArrayList< ClaimProperty >();
		loadConfig();

		for ( Minimap minimap : getCartographer().getMapManager().getMinimaps().values() ) {
			init( minimap );
		}

		registerListener( this );
	}
	
	@Override
	public SettingState< ? >[] getSettingStates() {
		SettingState< ? >[] states = new SettingState< ? >[] {
			RESIDENCE_CLAIMS
		};
		return states;
	}
	
	@EventHandler
	private void onEvent( MinimapLoadEvent event ) {
		init( event.getMinimap() );
	}

	private void init( Minimap minimap ) {
		minimap.register( new ClaimBorderShader( this ) );
	}
	
	private void loadConfig() {
		properties.clear();
		owner = Optional.empty();
		
		FileConfiguration config = YamlConfiguration.loadConfiguration( new File( getDataFolder(), "config.yml" ) );
		ConfigurationSection section = config.getConfigurationSection( "colors" );
		
		if ( section == null ) {
			getLogger().warning( "No 'colors' section found!" );
		} else {
			owner = PaletteManager.fromString( section.getString( "owner", "" ) );
			
			if ( section.contains( "claims" ) ) {
				// Not very safe, but oh well
				List< Map< String, Object > > sections = ( List< Map< String, Object > > ) section.getList( "claims", new ArrayList< Map< String, Object > >() );
				for ( Map< String, Object > claim : sections ) {
					String colorString = ( String ) claim.getOrDefault( "color", "" );
					Color color = PaletteManager.fromString( colorString ).orElse( Color.YELLOW );
					ClaimProperty property = new ClaimProperty( color );
					
					if ( claim.containsKey( "include" ) ) {
						List< String > includes = ( List< String > ) claim.get( "include" );
						for ( String include : includes ) {
							Flags flag = Flags.getFlag( include );
							if ( flag == null ) {
								getLogger().warning( "Invalid flag " + include );
							} else {
								property.getFlags().put( flag, true );
							}
						}
					}
					
					if ( claim.containsKey( "exclude" ) ) {
						List< String > excludes = ( List< String > ) claim.get( "exclude" );
						for ( String exclude : excludes ) {
							Flags flag = Flags.getFlag( exclude );
							if ( flag == null ) {
								getLogger().warning( "Invalid flag " + exclude );
							} else {
								property.getFlags().put( flag, false );
							}
						}
					}
					
					properties.add( property );
				}
			}
		}
	}
	
	public Optional< ClaimProperty > getMatching( Player player, ClaimedResidence residence ) {
		for ( ClaimProperty property : properties ) {
			if ( property.matches( player, residence ) ) {
				return Optional.of( property );
			}
		}
		return Optional.empty();
	}

	public Optional< Color > getOwnerColor() {
		return owner;
	}
}

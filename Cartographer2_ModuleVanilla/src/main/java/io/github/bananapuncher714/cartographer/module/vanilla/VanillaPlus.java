package io.github.bananapuncher714.cartographer.module.vanilla;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.command.CommandBase;
import io.github.bananapuncher714.cartographer.core.api.command.CommandParameters;
import io.github.bananapuncher714.cartographer.core.api.command.SubCommand;
import io.github.bananapuncher714.cartographer.core.api.command.executor.CommandExecutableMessage;
import io.github.bananapuncher714.cartographer.core.api.command.validator.InputValidatorInt;
import io.github.bananapuncher714.cartographer.core.api.command.validator.sender.SenderValidatorPlayer;
import io.github.bananapuncher714.cartographer.core.api.permission.PermissionBuilder;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.menu.MapMenu;
import io.github.bananapuncher714.cartographer.core.module.Module;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;

public class VanillaPlus extends Module {
	private Map< UUID, Location > deaths = new HashMap< UUID, Location >();
	
	@Override
	public void onEnable() {
		registerListener( new VanillaListener( this ) );
		
		for ( Minimap minimap : getCartographer().getMapManager().getMinimaps().values() ) {
			minimap.registerProvider( new VanillaWorldCursorProvider( this ) );
		}
		
		registerCommand( new CommandBase( "test" )
				.setSubCommand( new SubCommand( "test" )
						.add( new SubCommand( new InputValidatorInt( 0, 0xFFFFFF ) )
								.addSenderValidator( new SenderValidatorPlayer() )
								.defaultTo( this::showMenu ) )
						.whenUnknown( new CommandExecutableMessage( ChatColor.RED + "You must provide a color!" ) )
						.defaultTo( new CommandExecutableMessage( ChatColor.RED + "You must provide an argument!" ) ) )
				.setDescription( "Test command" )
				.setPermission( new PermissionBuilder( "test" ).setDefault( PermissionDefault.OP ).register().build() )
				.build() );
	}
	
	private void showMenu( CommandSender sender, String[] args, CommandParameters parameters ) {
		Player player = ( Player ) sender;
		int color = parameters.getLast( int.class );

		ItemStack item = Cartographer.getUtil().getMainHandItem( player );
		if ( item == null || !Cartographer.getInstance().getMapManager().isMinimapItem( item ) ) {
			sender.sendMessage( ChatColor.RED + "You must be holding a minimap!" );
			return;
		}
		CartographerRenderer renderer = Cartographer.getInstance().getMapManager().getRendererFrom( Cartographer.getUtil().getMapViewFrom( item ) );
		
		MapMenu menu = new MapMenu();
		menu.addComponent( new MenuComponentSolid( color ) );
		
		renderer.setMapMenu( player.getUniqueId(), menu );
	}
	
	@Override
	public void onDisable() {
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
}

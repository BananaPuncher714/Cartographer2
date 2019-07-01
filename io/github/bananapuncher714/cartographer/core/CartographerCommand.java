package io.github.bananapuncher714.cartographer.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.bukkit.util.StringUtil;

import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.util.NBTEditor;

public class CartographerCommand implements CommandExecutor, TabCompleter {

	@Override
	public List< String > onTabComplete( CommandSender sender, Command command, String label, String[] args ) {
		List< String > aos = new ArrayList< String >();
		if ( !sender.hasPermission( "cartographer.admin" ) ) {
			return aos;
		}

		if ( args.length == 1 ) {
			aos.add( "create" );
			aos.add( "get" );
		} else if ( args.length == 2 ) {
			if ( args[ 0 ].equalsIgnoreCase( "get" ) ) {
				
			}
		}
			
		List< String > completions = new ArrayList< String >();
		StringUtil.copyPartialMatches( args[ args.length - 1 ], aos, completions );
		Collections.sort( completions );
		return completions;
	}

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
		try {
			if ( args.length == 0 ) {
				sender.sendMessage( ChatColor.RED + "Usage: /cartographer create <id>" );
			} else if ( args.length > 0 ) {
				String option = args[ 0 ];
				args = pop( args );
				if ( option.equalsIgnoreCase( "create" ) ) {
					create( sender, args );
				} else if ( option.equalsIgnoreCase( "get" ) ) {
					get( sender, args );
				} else if ( option.equalsIgnoreCase( "shulker" ) ) {
					if ( sender instanceof Player ) {
						Player player = ( Player ) sender;
						Shulker shulker = player.getWorld().spawn( player.getLocation(), Shulker.class );
						player.addPassenger( shulker );
					}
				} else {
					sender.sendMessage( ChatColor.RED + "Usage: /cartographer <create|get> ..." );
				}
			}
		} catch ( IllegalArgumentException exception ) {
			sender.sendMessage( exception.getMessage() );
		}
		return false;
	}
	
	private void create( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "cartographer.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( args.length == 1, ChatColor.RED + "Usage: /cartographer create <id>" );
		
		Minimap map = Cartographer.getInstance().getMapManager().constructNewMinimap( args[ 0 ] );
		sender.sendMessage( ChatColor.GREEN + "Created and registered a new minimap with id " + args[ 0 ] );
	}
	
	private void get( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "cartographer.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( sender instanceof Player, ChatColor.RED + "You must be a player to run this command!" );
		
		Player player = ( Player ) sender;
		
		MapView view = Bukkit.createMap( player.getWorld() );
		while ( Cartographer.getInstance().getInvalidIds().contains( view.getId() ) ) {
			view = Bukkit.createMap( player.getWorld() );
		}
		
		Cartographer.getInstance().getMapManager().convert( view, Cartographer.getInstance().getMapManager().defaultMinimap );
		ItemStack map = Cartographer.getInstance().getHandler().getMapItem( view.getId() );
		
		map = NBTEditor.set( map, ( byte ) 1, "io.github.bananapuncher714", "cartographer", "core", "item", "minimap" );
		
		player.getInventory().addItem( map );
		
	}

	private String[] pop( String[] array ) {
		String[] array2 = new String[ Math.max( 0, array.length - 1 ) ];
		for ( int i = 1; i < array.length; i++ ) {
			array2[ i - 1 ] = array[ i ];
		}
		return array2;
	}
}

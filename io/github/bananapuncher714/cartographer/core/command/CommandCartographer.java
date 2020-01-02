package io.github.bananapuncher714.cartographer.core.command;

import java.io.File;
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
import org.bukkit.util.StringUtil;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.map.Minimap;

/**
 * Base Cartographer command.
 * 
 * @author BananaPuncher714
 */
public class CommandCartographer implements CommandExecutor, TabCompleter {
	private Cartographer plugin;
	private CommandModule moduleCommand;
	
	public CommandCartographer( Cartographer plugin ) {
		this.plugin = plugin;
		
		moduleCommand = new CommandModule( plugin );
	}
	
	@Override
	public List< String > onTabComplete( CommandSender sender, Command command, String label, String[] args ) {
		List< String > aos = new ArrayList< String >();
		if ( !sender.hasPermission( "cartographer.admin" ) ) {
			return aos;
		}
		
		if ( args.length > 1 && args[ 0 ].equalsIgnoreCase( "module" ) ) {
			// Module sub command
			String[] subArgs = pop( args );
			aos.addAll( moduleCommand.onTabComplete( sender, command, label, subArgs ) );
		} else if ( args.length == 1 ) {
			aos.add( "create" );
			aos.add( "get" );
			aos.add( "delete" );
			aos.add( "reload" );
			aos.add( "unload" );
			aos.add( "load" );
			aos.add( "module" );
		} else if ( args.length == 2 ) {
			if ( args[ 0 ].equalsIgnoreCase( "get" ) ||
					args[ 0 ].equalsIgnoreCase( "delete" ) ||
					args[ 0 ].equalsIgnoreCase( "unload" ) || 
					args[ 0 ].equalsIgnoreCase( "reload" ) ) {
				aos.addAll( plugin.getMapManager().getMinimaps().keySet() );
			} else if ( args[ 0 ].equalsIgnoreCase( "load" ) ) {
				for ( File file : Cartographer.getMapSaveDir().listFiles() ) {
					if ( !file.isDirectory() ) {
						continue;
					}
					
					String name = file.getName();
					if ( !plugin.getMapManager().getMinimaps().containsKey( name ) ) {
						aos.add( name );
					}
				}
			}
		} else if ( args.length == 3 ) {
			if ( args[ 0 ].equalsIgnoreCase( "get" ) ) {
				for ( Player player : Bukkit.getOnlinePlayers() ) {
					aos.add( player.getName() );
				}
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
				} else if ( option.equalsIgnoreCase( "delete" ) ) {
					delete( sender, args );
				} else if ( option.equalsIgnoreCase( "reload" ) ) {
					reload( sender, args );
				} else if ( option.equalsIgnoreCase( "unload" ) ) {
					unload( sender, args );
				} else if ( option.equalsIgnoreCase( "load" ) ) {
					load( sender, args );
				} else if ( option.equalsIgnoreCase( "module" ) ) {
					return moduleCommand.onCommand( sender, command, label, args );
				} else {
					sender.sendMessage( ChatColor.RED + "Usage: /cartographer <create|get|delete> ..." );
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
		
		Minimap map = plugin.getMapManager().constructNewMinimap( args[ 0 ] );
		sender.sendMessage( ChatColor.GREEN + "Created and registered a new minimap with id '" + ChatColor.YELLOW + args[ 0 ] + ChatColor.GREEN + "'" );
	}
	
	private void get( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "cartographer.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		int slot = -1;
		if ( args.length == 1 ) {
			Validate.isTrue( sender instanceof Player, ChatColor.RED + "You must be a player to run this command!" );
		} else if ( args.length > 1 ) {
			Validate.isTrue( Bukkit.getPlayer( args[ 1 ] ) != null, ChatColor.RED + "'" + args[ 1 ] + "' is not online!" );
			if ( args.length > 2 ) {
				try {
					slot = Integer.parseInt( args[ 2 ] );
				} catch ( Exception exception ) {
					throw new IllegalArgumentException( ChatColor.RED +"'" + args[ 2 ] + "' is not a valid integer!" );
				}
			}
		} else {
			throw new IllegalArgumentException( ChatColor.RED + "Usage: /cartographer get <id> [player] [slot]" );
		}
		Minimap map = plugin.getMapManager().getMinimaps().get( args[ 0 ] );
		Validate.isTrue( map != null, ChatColor.RED + "That map does not exist!" );
		
		Player player;
		if ( args.length > 1 ) {
			player = Bukkit.getPlayer( args[ 1 ] );
		} else {
			player = ( Player ) sender;
		}
		
		if ( slot == -1 ) {
			player.getInventory().addItem( plugin.getMapManager().getItemFor( map ) );
		} else {
			player.getInventory().setItem( slot, plugin.getMapManager().getItemFor( map ) );
		}
	}
	
	private void delete( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "cartographer.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( args.length == 1, ChatColor.RED + "Usage: /cartographer delete <id>" );
		Minimap map = plugin.getMapManager().getMinimaps().get( args[ 0 ] );
		Validate.isTrue( map != null, ChatColor.RED + "'" + args[ 0 ] + "' does not exist!" );
		plugin.getMapManager().remove( map );
		sender.sendMessage( ChatColor.BLUE + "Deleted minimap '" + ChatColor.YELLOW + map.getId() + ChatColor.BLUE + "'" );
	}
	
	private void reload( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "cartographer.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		if ( args.length == 0 ) {
			// Reload the Cartographer config and whatnot
			plugin.reload();
			sender.sendMessage( ChatColor.BLUE + "Reloaded Cartographer2 settings");
		} else {
			// Reload the minimap
			// Essentially unloading it then loading it again
			Minimap map = plugin.getMapManager().getMinimaps().get( args[ 0 ] );
			Validate.isTrue( map != null, ChatColor.RED + "'" + args[ 0 ] + "' does not exist!" );
			File saveDir = map.getDataFolder();
			
			plugin.getMapManager().unload( map );
			plugin.getMapManager().load( saveDir );
			
			sender.sendMessage( ChatColor.BLUE + "Reloaded minimap '" + ChatColor.YELLOW + map.getId() + ChatColor.BLUE + "'" );
		}
	}
	
	private void unload( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "cartographer.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( args.length == 1, ChatColor.RED + "Usage: /cartographer unload <id>" );
		Minimap map = plugin.getMapManager().getMinimaps().get( args[ 0 ] );
		Validate.isTrue( map != null, ChatColor.RED + "'" + args[ 0 ] + "' does not exist!" );
		plugin.getMapManager().unload( map );
		sender.sendMessage( ChatColor.BLUE + "Unloaded minimap '" + ChatColor.YELLOW + map.getId() + ChatColor.BLUE + "'" );
	}
	
	private void load( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "cartographer.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( args.length == 1, ChatColor.RED + "Usage: /cartographer load <id>" );
		Validate.isTrue( !plugin.getMapManager().getMinimaps().containsKey( args[ 0 ] ), ChatColor.RED + args[ 0 ] + " is already loaded!" );
		
		File mapDir = plugin.getMapDirFor( args[ 0 ] );
		Validate.isTrue( mapDir.exists(), ChatColor.RED + "'" + args[ 0 ] + "' does not exist!" );
		
		Minimap map = plugin.getMapManager().load( mapDir );
		
		sender.sendMessage( ChatColor.BLUE + "Loaded minimap '" + ChatColor.YELLOW + map.getId() + ChatColor.BLUE + "'" );
	}

	protected static String[] pop( String[] array ) {
		String[] array2 = new String[ Math.max( 0, array.length - 1 ) ];
		for ( int i = 1; i < array.length; i++ ) {
			array2[ i - 1 ] = array[ i ];
		}
		return array2;
	}
}

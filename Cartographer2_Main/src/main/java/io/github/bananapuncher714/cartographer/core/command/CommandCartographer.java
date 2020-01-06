package io.github.bananapuncher714.cartographer.core.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
		
		if ( args.length > 1 && args[ 0 ].equalsIgnoreCase( "module" ) ) {
			// Module sub command
			String[] subArgs = pop( args );
			aos.addAll( moduleCommand.onTabComplete( sender, command, label, subArgs ) );
		} else if ( args.length == 1 ) {
			if ( sender.hasPermission( "cartographer.reload" ) || sender.hasPermission( "cartographer.map.reload" ) ) {
				aos.add( "reload" );
			}
			if ( sender.hasPermission( "cartographer.map.get" ) || sender.hasPermission( "cartographer.map.give" ) ) {
				aos.add( "get" );
			}
			if ( sender.hasPermission( "cartographer.map.create" ) ) {
				aos.add( "create" );
			}
			if ( sender.hasPermission( "cartographer.map.delete" ) ) {
				aos.add( "delete" );
			}
			if ( sender.hasPermission( "cartographer.map.unload" ) ) {
				aos.add( "unload" );
			}
			if ( sender.hasPermission( "cartographer.map.load" ) ) {
				aos.add( "load" );
			}
			if ( sender.hasPermission( "cartographer.map.list" ) ) {
				aos.add( "list" );
			}
			if ( sender.hasPermission( "cartographer.module" ) ) {
				aos.add( "module" );
			}
		} else if ( args.length == 2 ) {
			if ( ( args[ 0 ].equalsIgnoreCase( "get" ) && ( sender.hasPermission( "cartographer.map.get" ) || sender.hasPermission( "cartographer.map.give" ) ) ) ||
					( args[ 0 ].equalsIgnoreCase( "delete" ) && sender.hasPermission( "cartographer.map.delete" ) ) ||
					( args[ 0 ].equalsIgnoreCase( "unload" ) && sender.hasPermission( "cartographer.map.unload" ) ) || 
					( args[ 0 ].equalsIgnoreCase( "reload" ) && ( sender.hasPermission( "cartographer.reload" ) || sender.hasPermission( "cartographer.map.reload" ) ) ) ) {
				aos.addAll( plugin.getMapManager().getMinimaps().keySet() );
			} else if ( args[ 0 ].equalsIgnoreCase( "load" ) && sender.hasPermission( "cartographer.map.load" ) ) {
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
			if ( args[ 0 ].equalsIgnoreCase( "get" ) && sender.hasPermission( "cartographer.map.give" ) ) {
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
				sender.sendMessage( ChatColor.RED + "You must provide an argument!" );
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
				} else if ( option.equalsIgnoreCase( "list" ) ) {
					list( sender, args );
				} else if ( option.equalsIgnoreCase( "module" ) ) {
					return moduleCommand.onCommand( sender, command, label, args );
				} else {
					sender.sendMessage( ChatColor.RED + "Invalid argument!" );
				}
			}
		} catch ( IllegalArgumentException exception ) {
			sender.sendMessage( exception.getMessage() );
		}
		return false;
	}
	
	private void list( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "cartographer.map.list" ), ChatColor.RED + "You do not have permission to run this command!" );
		
		Set< String > minimaps = plugin.getMapManager().getMinimaps().keySet();
		if ( minimaps.isEmpty() ) {
			sender.sendMessage( ChatColor.GOLD + "There are currently no modules loaded!" );
		} else {
			StringBuilder builder = new StringBuilder();
			builder.append( ChatColor.AQUA );
			builder.append( "Cartographer2 Minimaps (" );
			builder.append( minimaps.size() );
			builder.append( "): " );
			for ( Iterator< String > iterator = minimaps.iterator(); iterator.hasNext(); ) {
				String minimap = iterator.next();
				builder.append( ChatColor.YELLOW );
				builder.append( minimap );
				
				if ( iterator.hasNext() ) {
					builder.append( ChatColor.AQUA );
					builder.append( ", " );
				}
			}
			sender.sendMessage( builder.toString() );
		}
	}
	
	private void create( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "cartographer.map.create" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( args.length == 1, ChatColor.RED + "Usage: /cartographer create <id>" );
		
		Minimap map = plugin.getMapManager().constructNewMinimap( args[ 0 ] );
		sender.sendMessage( ChatColor.GREEN + "Created and registered a new minimap with id '" + ChatColor.YELLOW + args[ 0 ] + ChatColor.GREEN + "'" );
	}
	
	private void get( CommandSender sender, String[] args ) {
		if ( args.length > 1 ) {
			Validate.isTrue( sender.hasPermission( "cartographer.map.give" ), ChatColor.RED + "You do not have permission to run this command!" );
		} else {
			Validate.isTrue( sender.hasPermission( "cartographer.map.get" ), ChatColor.RED + "You do not have permission to run this command!" );
		}
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
			if ( sender.hasPermission( "cartographer.map.give" ) ) {
				throw new IllegalArgumentException( ChatColor.RED + "Usage: /cartographer get <id> [player] [slot]" );
			} else {
				throw new IllegalArgumentException( ChatColor.RED + "Usage: /cartographer get <id>" );
			}
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
		Validate.isTrue( sender.hasPermission( "cartographer.map.delete" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( args.length == 1, ChatColor.RED + "Usage: /cartographer delete <id>" );
		Minimap map = plugin.getMapManager().getMinimaps().get( args[ 0 ] );
		Validate.isTrue( map != null, ChatColor.RED + "'" + args[ 0 ] + "' does not exist!" );
		plugin.getMapManager().remove( map );
		sender.sendMessage( ChatColor.AQUA + "Deleted minimap '" + ChatColor.YELLOW + map.getId() + ChatColor.AQUA + "'" );
	}
	
	private void reload( CommandSender sender, String[] args ) {
		if ( args.length == 0 ) {
			Validate.isTrue( sender.hasPermission( "cartographer.reload" ), ChatColor.RED + "You do not have permission to run this command!" );
			// Reload the Cartographer config and whatnot
			plugin.reload();
			sender.sendMessage( ChatColor.AQUA + "Reloaded Cartographer2 settings");
		} else {
			Validate.isTrue( sender.hasPermission( "cartographer.map.reload" ), ChatColor.RED + "You do not have permission to run this command!" );
			// Reload the minimap
			// Essentially unloading it then loading it again
			Minimap map = plugin.getMapManager().getMinimaps().get( args[ 0 ] );
			Validate.isTrue( map != null, ChatColor.RED + "'" + args[ 0 ] + "' does not exist!" );
			File saveDir = map.getDataFolder();
			
			plugin.getMapManager().unload( map );
			plugin.getMapManager().load( saveDir );
			
			sender.sendMessage( ChatColor.AQUA + "Reloaded minimap '" + ChatColor.YELLOW + map.getId() + ChatColor.AQUA + "'" );
		}
	}
	
	private void unload( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "cartographer.map.unload" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( args.length == 1, ChatColor.RED + "Usage: /cartographer unload <id>" );
		Minimap map = plugin.getMapManager().getMinimaps().get( args[ 0 ] );
		Validate.isTrue( map != null, ChatColor.RED + "'" + args[ 0 ] + "' does not exist!" );
		plugin.getMapManager().unload( map );
		sender.sendMessage( ChatColor.AQUA + "Unloaded minimap '" + ChatColor.YELLOW + map.getId() + ChatColor.AQUA + "'" );
	}
	
	private void load( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "cartographer.map.load" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( args.length == 1, ChatColor.RED + "Usage: /cartographer load <id>" );
		Validate.isTrue( !plugin.getMapManager().getMinimaps().containsKey( args[ 0 ] ), ChatColor.RED + args[ 0 ] + " is already loaded!" );
		
		File mapDir = plugin.getMapDirFor( args[ 0 ] );
		Validate.isTrue( mapDir.exists(), ChatColor.RED + "'" + args[ 0 ] + "' does not exist!" );
		
		Minimap map = plugin.getMapManager().load( mapDir );
		
		sender.sendMessage( ChatColor.AQUA + "Loaded minimap '" + ChatColor.YELLOW + map.getId() + ChatColor.AQUA + "'" );
	}

	protected static String[] pop( String[] array ) {
		String[] array2 = new String[ Math.max( 0, array.length - 1 ) ];
		for ( int i = 1; i < array.length; i++ ) {
			array2[ i - 1 ] = array[ i ];
		}
		return array2;
	}
}

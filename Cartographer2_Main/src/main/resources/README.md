# Cartographer 2
*The best minimap plugin for Bukkit*
![Falcon's Rock](https://i.imgur.com/RCkxvQ4.png)

## What is this?
Cartographer 2 aims to be a better version of the original Cartographer. It is unfortunately not a lightweight plugin by any means. However, it has been optimized to be as fast as possible and provide the best results. It saves map data asynchronously to minimize the impact of file IO. The processing of each chunk is also done on separate threads so that loading chunks is done fast and efficiently. Rendering the map on the items, which requires the most amount of processing power, is multi-threaded to distribute the load and finish as fast as possible. Color conversion and mixing is done by Jetp250's image util, which dithers and matches colors extremely fast for almost no delay. In addition to speed, Cartographer2 features a module system and an (almost) fully featured API to customize everything easily.

## How to install
- Place in the `plugins/` folder and restart server
- Delete this file to regenerate any missing files/folders, such as the built in palettes, or images.

## Commands
- `/cartographer  <create|get|delete|reload|unload|load|list|module> ...`
  - `/cartographer create <id>` - Create a minimap with the given id. Minimaps can span worlds, so normally one is enough.
  - `/cartographer delete <id>` - Delete the minimap with the given id. Removes it from the server and from disk.
  - `/cartographer get <id> [player] [slot]` - Get a map item for the minimap with the given id. A player and optional slot can be provided to be ran from console or command block.
  - `/cartographer reload [id]` -  Reload Cartographer2's config, or reload a minimap if an id is provided.
  - `/cartographer unload <id>` - Unload a minimap with the given id.
  - `/cartographer load <id>` - Load an unloaded minimap with the given id.
  - `/cartographer module <list|reload|unload|load|enable|disable> ...`
    - `/cartographer module list` - List all the modules, regardless if they are disabled.
    - `/cartographer module reload` - Reload all the modules.
    - `/cartographer module enable <id>` - Enable the specified module.
    - `/cartographer module disable <id>` - Disable the specified module.
    - `/cartographer module load <file>` - Load the specified file.
    - `/cartographer module unload <id>` - Unload the specified module.

## Permissions
- `cartographer.admin` - Master permission.
- `cartographer.reload` - Reload Cartographer2's settings and images.
- `cartographer.map.admin` - Master permission for maps.
- `cartographer.map.list` - List all minimaps.
- `cartographer.map.reload` - Reload a minimap.
- `cartographer.map.unload` - Unload a minimap.
- `cartographer.map.load` - Load a minimap.
- `cartographer.map.give` - Give a minimap to someone.
- `cartographer.map.get` - Get a minimap for yourself.
- `cartographer.map.create` - Create a new minimap.
- `cartographer.map.delete` - Delete a minimap.
- `cartographer.module` - Module command.
- `cartographer.module.admin` - Master permission for modules.
- `cartographer.module.list` - List all modules.
- `cartographer.module.reload` - Reload all modules.
- `cartographer.module.load` - Load a given module.
- `cartographer.module.unload` - Unload a given module.
- `cartographer.module.disable` - Disable a given module.
- `cartographer.module.enable` - Enable a given module.


## Module ideas
- Show land claims
- Draw animated beacons
- Located nearby entities and mark players
- Add a sepia filter to the worldmap
- Show a treasure hunt
- Display pathfinding points
- Show locations and statistics
- Cool looking HUD
- RPG style waypoints

## TODO
- Add vanilla module integration
- Fix the wiki
- Add factionsuuid and Residence integration
- Lands integration - Put on hold unfortunately

## More information
- [Github](https://github.com/BananaPuncher714/Cartographer2)
- [Wiki](https://github.com/BananaPuncher714/Cartographer2/wiki)
- [Spigot](https://www.spigotmc.org/resources/46922/)

## Credits
- `BananaPuncher714` - Creator, Developer, Maintainer
- `Jetp250` - Developer
- `REjoin` - Supporter
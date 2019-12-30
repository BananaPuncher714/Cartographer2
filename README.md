# Cartographer 2
*The best minimap plugin for Bukkit*
![Falcon's Rock](https://i.imgur.com/RCkxvQ4.png)

## What is this?
Cartographer 2 aims to be a better version of the original Cartographer. It is unfortunately not a lightweight plugin by any means. However, it has been optimized to be as fast as possible and provide the best results. It saves map data asynchronously to minimize the impact of file IO. The processing of each chunk is also done on separate threads so that loading chunks is done fast and efficiently. Rendering the map on the items, which requires the most amount of processing power, is multi-threaded to distribute the load and finish as fast as possible. Color conversion and mixing is done by Jetp250's image util, which dithers and matches colors extremely fast for almost no delay. In addition to speed, Cartographer2 features a module system and an (almost) fully featured API to customize everything easily.

## How to install
- Place in the `plugins/` folder and restart server
- Delete this file to regenerate any missing files/folders, such as the built in palettes, or images.

## Commands
- `/cartographer  <create|get|delete|reload|unload|load> ...`
  - `/cartographer create <id>` - Create a minimap with the given id. Minimaps can span worlds, so normally one is enough.
  - `/cartographer delete <id>` - Delete the minimap with the given id. Removes it from the server and from disk
  - `/cartographer get <id> [player] [slot]` - Get a map item for the minimap with the given id. A player and optional slot can be provided to be ran from console or command block.
  - `/cartographer reload [id]` -  Reload Cartographer2's config, or reload a minimap if an id is provided
  - `/cartographer unload <id>` - 
  - `/cartographer load <id>` - 

## Permissions
- `cartographer.admin` - Master permission to run the commands

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
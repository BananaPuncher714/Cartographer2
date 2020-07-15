# Cartographer 2
*The best minimap plugin for Bukkit*
![Falcon's Rock](https://i.imgur.com/RCkxvQ4.png)

## What is this?
Cartographer 2 aims to be a better version of the original Cartographer. It is unfortunately not a lightweight plugin by any means. However, it has been optimized to be as fast as possible and provide the best results. It saves map data asynchronously to minimize the impact of file IO. The processing of each chunk is also done on separate threads so that loading chunks is done fast and efficiently. Rendering the map on the items, which requires the most amount of processing power, is multi-threaded to distribute the load and finish as fast as possible. Color conversion and mixing is done by Jetp250's image util, which dithers and matches colors extremely fast for almost no delay. In addition to speed, Cartographer2 features a module system and an (almost) fully featured API to customize everything easily.

## More information
- [Github](https://github.com/BananaPuncher714/Cartographer2)
- [Wiki](https://github.com/BananaPuncher714/Cartographer2/wiki)
- [Spigot](https://www.spigotmc.org/resources/46922/)
- [Javadocs](https://bananapuncher714.github.io/Cartographer2/)

## Credits
- `BananaPuncher714` - Creator, Developer, Maintainer
- `Jetp250` - Developer
- `REjoin` - Supporter
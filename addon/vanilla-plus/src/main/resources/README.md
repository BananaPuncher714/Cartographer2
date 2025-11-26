# VanillaPlus
Cartographer2 module with vanilla integration. Requires Cartographer 2.14.0 or newer.

### Features
- Track entities
- Track players
- Track spawn
- Track last death

### Settings
VanillaPlus adds in settings configurable by the user through Cartographer's setting system. They may be enabled/disabled in the config.
- `vp_show_death_location` - Show the location of the player's last death.
- `vp_show_spawn_location` - Show the player's spawn location.
- `vp_show_players` - Show other players on the minimap.
- `vp_show_mobs` - Show the mobs listed in the config.

### Permissions
- `vanillaplus.admin` - Grants all permissions for VanillaPlus
- `vanillaplus.cursor.players` - Allows tracking of all players, except those with the invisibility permission, invisiblity, or sneaking
- `vanillaplus.cursor.location.death` - Allows tracking of the player's last death location
- `vanillaplus.cursor.location.spawn` - Allows tracking of the player's spawn
- `vanillaplus.cursor.entity.<type>` - Allows tracking of specified entity type, if enabled in the config
- `vanillaplus.invisible` - Prevents a player from showing up on the minimap

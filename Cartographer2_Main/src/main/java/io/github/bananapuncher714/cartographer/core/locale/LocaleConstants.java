package io.github.bananapuncher714.cartographer.core.locale;

public final class LocaleConstants {
	// Locale related messages
	public static final String LOCALE_DEFAULT_LOCALE_LOADED = "locale.loaded-internal-locale";
	public static final String LOCALE_INTERNAL_LOCALE_LOADED = "locale.loaded-internal-locale";
	public static final String LOCALE_LOCALE_LOADED = "locale.loaded-locale";
	public static final String LOCALE_DEFAULT_LOCALE_CHANGED = "locale.default-locale-changed";
	public static final String LOCALE_LOCALE_INVALID = "locale.invalid-locale";
	public static final String LOCALE_FILE_LOAD_ERROR = "locale.error-loading-file";
	
	// Cartographer2 core related messages
	public static final String CORE_UNSUPPORTED_VERSION = "core.unsupported-version";
	public static final String CORE_PLUGIN_DISABLE = "core.disabling-plugin";
	
	// Cartographer2 enable messages
	public static final String CORE_ENABLE_MODULES_ENABLE = "core.enable.enabling-modules";
	public static final String CORE_ENABLE_LOAD_CONFIG = "core.enable.loading-config";
	public static final String CORE_ENABLE_LOAD_IMAGES = "core.enable.loading-images";
	public static final String CORE_ENABLE_LOAD_PALETTES = "core.enable.loading-palettes";
	public static final String CORE_ENABLE_LOAD_DATA = "core.enable.loading-data";
	
	// Core config
	public static final String CORE_ENABLE_CONFIG_INVENTORY_ADDED = "core.enable.config.added-blacklisted-inventory";
	public static final String CORE_ENABLE_CONFIG_INVENTORY_UNKNOWN = "core.enable.config.unknown-inventory-type";
	
	// Core palette
	public static final String CORE_ENABLE_PALETTE_VANILLA_CREATE = "core.enable.palette.creating-vanilla-palette";
	public static final String CORE_ENABLE_PALETTE_VANILLA_MISSING = "core.enable.palette.vanilla-palette-missing";
	public static final String CORE_ENABLE_PALETTE_VANILLA_MAPPED = "core.enable.palette.found-materials";
	public static final String CORE_ENABLE_PALETTE_LOADING = "core.enable.palette.loading-palettes";
	public static final String CORE_ENABLE_PALETTE_LOADING_DONE = "core.enable.palette.successfully-loaded-palette";
	public static final String CORE_ENABLE_PALETTE_FOLDER_MISSING = "core.enable.palette.palette-folder-missing";
	
	// Images
	public static final String CORE_ENABLE_IMAGE_OVERLAY_FOUND = "core.enable.image.overlay-found";
	public static final String CORE_ENABLE_IMAGE_OVERLAY_MISSING = "core.enable.image.overlay-missing";
	public static final String CORE_ENABLE_IMAGE_BACKGROUND_FOUND = "core.enable.image.background-found";
	public static final String CORE_ENABLE_IMAGE_BACKGROUND_MISSING = "core.enable.image.background-missing";
	public static final String CORE_ENABLE_IMAGE_MISSING_FOUND = "core.enable.image.missing-map-found";
	public static final String CORE_ENABLE_IMAGE_MISSING_MISSING = "core.enable.image.missing-map-missing";
	public static final String CORE_ENABLE_IMAGE_DISABLED_FOUND = "core.enable.image.disabled-map-found";
	public static final String CORE_ENABLE_IMAGE_DISABLED_MISSING = "core.enable.image.disabled-map-missing";
	
	// Disable messages
	public static final String CORE_DISABLE_MODULES_DISABLE = "core.disable.disabling-modules";
	public static final String CORE_DISABLE_SAVING_MAP_START = "core.disable.saving-map-data-start";
	public static final String CORE_DISABLE_SAVING_MAP_FINISH = "core.disable.saving-map-data-finish";
	public static final String CORE_DISABLE_SAVING_PLAYER_START= "core.disable.saving-player-data-start";
	public static final String CORE_DISABLE_SAVING_PLAYER_FINISH = "core.disable.saving-player-data-finish";
	
	// Commands
	public static final String COMMAND_CARTOGRAPHER_RELOAD_USAGE = "core.command.cartographer.reload.usage";
	public static final String COMMAND_CARTOGRAPHER_RELOAD_PLUGIN = "core.command.cartographer.reload.reloaded-plugin";
	public static final String COMMAND_CARTOGRAPHER_RELOAD_MINIMAP = "core.command.cartographer.reload.reloaded-minimap";
	public static final String COMMAND_CARTOGRAPHER_LIST_USAGE = "core.command.cartographer.list.usage";
	public static final String COMMAND_CARTOGRAPHER_LIST_EMPTY = "core.command.cartographer.list.no-minimaps";
	public static final String COMMAND_CARTOGRAPHER_LIST_FORMAT = "core.command.cartographer.list.list";
	public static final String COMMAND_CARTOGRAPHER_GET_USAGE = "core.command.cartographer.get.usage";
	public static final String COMMAND_CARTOGRAPHER_GET_SELF = "core.command.cartographer.get.success-self";
	public static final String COMMAND_CARTOGRAPHER_GET_OTHER = "core.command.cartographer.get.success-other";
	public static final String COMMAND_CARTOGRAPHER_CREATE_USAGE = "core.command.cartographer.create.usage";
	public static final String COMMAND_CARTOGRAPHER_CREATE_SUCCESS = "core.command.cartographer.create.success";
	public static final String COMMAND_CARTOGRAPHER_DELETE_USAGE = "core.command.cartographer.delete.usage";
	public static final String COMMAND_CARTOGRAPHER_DELETE_SUCCESS = "core.command.cartographer.delete.success";
	public static final String COMMAND_CARTOGRAPHER_LOAD_USAGE = "core.command.cartographer.load.usage";
	public static final String COMMAND_CARTOGRAPHER_LOAD_SUCCESS = "core.command.cartographer.load.success";
	public static final String COMMAND_CARTOGRAPHER_UNLOAD_USAGE = "core.command.cartographer.unload.usage";
	public static final String COMMAND_CARTOGRAPHER_UNLOAD_SUCCESS = "core.command.cartographer.unload.success";
	public static final String COMMAND_CARTOGRAPHER_HELP_USAGE = "core.command.cartographer.help.usage";
	public static final String COMMAND_CARTOGRAPHER_HELP_FORMAT = "core.command.cartographer.help.help-%d";
	
	public static final String COMMAND_MODULE_LIST_EMPTY = "core.command.module.list.no-modules";
	public static final String COMMAND_MODULE_LIST_FORMAT = "core.command.module.list.list";
	public static final String COMMAND_MODULE_RELOAD_SUCESS = "core.command.module.reload.success";
	public static final String COMMAND_MODULE_ENABLE_USAGE = "core.command.module.enable.usage";
	public static final String COMMAND_MODULE_ENABLE_SUCCESS = "core.command.module.enable.success";
	public static final String COMMAND_MODULE_ENABLE_ENABLE_ERROR = "core.command.module.enable.already-enabled";
	public static final String COMMAND_MODULE_ENABLE_LOAD_ERROR = "core.command.module.enable.error-loading";
	public static final String COMMAND_MODULE_DISABLE_USAGE = "core.command.module.disable.usage";
	public static final String COMMAND_MODULE_DISABLE_ERROR = "core.command.module.disable.already-disabled";
	public static final String COMMAND_MODULE_DISABLE_SUCCESS = "core.command.module.disable.success";
	public static final String COMMAND_MODULE_LOAD_USAGE = "core.command.module.load.usage";
	public static final String COMMAND_MODULE_LOAD_SUCCESS = "core.command.module.load.success";
	public static final String COMMAND_MODULE_LOAD_LOAD_ERROR = "core.command.module.load.error-loading";
	public static final String COMMAND_MODULE_LOAD_ENABLE_ERROR = "core.command.module.load.error-enabling";
	public static final String COMMAND_MODULE_UNLOAD_USAGE = "core.command.module.unload.usage";
	public static final String COMMAND_MODULE_UNLOAD_SUCCESS = "core.command.module.unload.success";
	public static final String COMMAND_MODULE_UNLOAD_ERROR = "core.command.module.unload.error-unloading";
	public static final String COMMAND_MODULE_HELP_USAGE = "core.command.module.help.usage";
	public static final String COMMAND_MODULE_HELP_FORMAT = "core.command.module.help.help-%d";
	public static final String COMMAND_MODULE_MESSAGE_INVALID_FILE = "core.command.module.file-already-loaded-or-not-real";
	public static final String COMMAND_MODULE_MESSAGE_ALREADY_ENABLED = "core.command.module.module-already-enabled-or-not-real";
	public static final String COMMAND_MODULE_MESSAGE_ALREADY_DISABLED = "core.command.module.module-already-disabled-or-not-real";
	public static final String COMMAND_MODULE_MESSAGE_NOT_REAL = "core.command.module.module-not-real";
	
	public static final String COMMAND_SETTING_SET_CONSOLE = "core.command.settings.set.console-usage";
	public static final String COMMAND_SETTING_SET_SUCCESS = "core.command.settings.set.success";
	public static final String COMMAND_SETTING_GET_CONSOLE = "core.command.settings.get.console-usage";
	public static final String COMMAND_SETTING_GET_SUCCESS = "core.command.settings.get.success";
	public static final String COMMAND_SETTING_SETOTHER_USAGE = "core.command.settings.setother.usage";
	public static final String COMMAND_SETTING_SETOTHER_SUCCESS = "core.command.settings.setother.success";
	public static final String COMMAND_SETTING_GETOTHER_USAGE = "core.command.settings.getother.usage";
	public static final String COMMAND_SETTING_GETOTHER_SUCCESS = "core.command.settings.getother.success";
	public static final String COMMAND_SETTING_HELP_USAGE = "core.command.settings.help.usage";
	public static final String COMMAND_SETTING_HELP_FORMAT = "core.command.settings.help.help-%d";
	public static final String COMMAND_SETTING_MESSAGE_INVALID = "core.command.settings.invalid-value";
	public static final String COMMAND_SETTING_MESSAGE_PROVIDE = "core.command.settings.must-provide-values";
	public static final String COMMAND_SETTING_MESSAGE_UNIMPLEMENTED = "core.command.settings.not-implemented";
	
	public static final String COMMAND_MESSAGE_PLAYER_MISSING = "core.command.player-not-found";
	public static final String COMMAND_MESSAGE_MINIMAP_EXISTS = "core.command.minimap-already-exists";
	public static final String COMMAND_MESSAGE_INVALID_MINIMAP = "core.command.invalid-minimap";
	public static final String COMMAND_MESSAGE_INVALID_SLOT = "core.command.invalid-slot";
	public static final String COMMAND_MESSAGE_INVALID_ARGUMENT = "core.command.invalid-argument";
	public static final String COMMAND_MESSAGE_INVALID_SETTING = "core.command.invalid-setting";
	public static final String COMMAND_MESSAGE_PROVIDE_MINIMAP = "core.command.must-provide-minimap";
	public static final String COMMAND_MESSAGE_PROVIDE_PLAYER = "core.command.must-provide-player";
	public static final String COMMAND_MESSAGE_PROVIDE_ARGUMENT = "core.command.must-provide-argument";
	public static final String COMMAND_MESSAGE_PROVIDE_SETTING = "core.command.must-provide-setting";
	
	// Managers
	public static final String MANAGER_MINIMAP_LOADING = "core.manager.minimap.loading-minimap";
	public static final String MANAGER_MINIMAP_UNLOADING = "core.manager.minimap.unloading-minimap";
	public static final String MANAGER_MINIMAP_DELETING = "core.manager.minimap.deleting-minimap";
	public static final String MANAGER_MODULE_RELOAD_START = "core.manager.module.reloading-modules-start";
	public static final String MANAGER_MODULE_RELOAD_FINISH = "core.manager.module.reloading-modules-finish";
	public static final String MANAGER_MODULE_LOADING = "core.manager.module.loading-module";
	public static final String MANAGER_MODULE_UNLOADING = "core.manager.module.unloading-module";
	public static final String MANAGER_MODULE_ENABLING = "core.manager.module.enabling-module";
	public static final String MANAGER_MODULE_DISABLING = "core.manager.module.disabling-module";
	public static final String MANAGER_MODULE_MISSING_DEPENDENCIES = "core.manager.module.missing-dependencies";
	
	// Minimap
	public static final String MINIMAP_DEFAULT_ROTATION = "core.minimap.default-rotation";
	public static final String MINIMAP_AUTO_UPDATE = "core.minimap.auto-update";
	public static final String MINIMAP_ZOOM_CIRCULAR = "core.minimap.circular-zoom";
	public static final String MINIMAP_RENDER = "core.minimap.render-out-of-border";
	public static final String MINIMAP_ZOOM_DEFAULT = "core.minimap.default-zoom";
	public static final String MINIMAP_ZOOM_ALLOWED = "core.minimap.allowed-zooms";
	public static final String MINIMAP_WORLD_WHITELIST = "core.minimap.whitelisted-worlds";
	public static final String MINIMAP_WORLD_BLACKLIST = "core.minimap.blacklisted-worlds";
	public static final String MINIMAP_LOADED_OVERLAY = "core.minimap.loaded-overlay";
	public static final String MINIMAP_LOADED_BACKGROUND = "core.minimap.loaded-background";
	public static final String MINIMAP_LOADED_DISABLED = "core.minimap.loaded-disabled";
}

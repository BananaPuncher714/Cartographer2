package io.github.bananapuncher714.cartographer.module.worldviewer.menu;

import java.util.UUID;

import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;
import io.github.bananapuncher714.cartographer.core.renderer.PlayerSetting;

public class CustomPlayerSetting extends PlayerSetting {
	protected CustomPlayerSetting( CartographerRenderer renderer, UUID uuid, String map, Location location ) {
		super( renderer, uuid, map, location );
	}
	
	public void setRotation( boolean rotation ) {
		this.rotating = rotation;
	}
}

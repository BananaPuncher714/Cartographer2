package io.github.bananapuncher714.cartographer.core.api;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.util.CrossVersionMaterial;

/**
 * Different Bukkit methods that change across various Minecraft versions.
 * 
 * @author BananaPuncher714
 */
public interface GeneralUtil {
	/**
	 * Get the a MapView from a map item.
	 * 
	 * @param item
	 * A map item, may be null.
	 * @return
	 * Null if the item is null, or not a map.
	 */
	MapView getMapViewFrom( ItemStack item );
	
	/**
	 * Get a MapView for the id provided.
	 * 
	 * @param id
	 * The id of the map, may not work for values above 32767 depending on the version of Minecraft.
	 * @return
	 * A Bukkit MapView.
	 */
	MapView getMap( int id );
	
	/**
	 * Get the id of the MapView as an integer.
	 * 
	 * @param view
	 * Cannot be null.
	 * @return
	 * An integer representing the id.
	 */
	int getId( MapView view );
	
	/**
	 * Get a filled map item with the mapview of the given id.
	 * 
	 * @param id
	 * An integer, may have to be below 32767 depending on the version of Minecraft.
	 * @return
	 * An ItemStack of the filled map material.
	 */
	ItemStack getMapItem( int id );
	
	/**
	 * Get the material for a filled map.
	 * 
	 * @return
	 * Actual enum may differ depending on the version of Minecraft.
	 */
	Material getMapMaterial();
	
	/**
	 * Check if the event is called with the player's main hand.
	 * 
	 * @param event
	 * Cannot be null.
	 * @return
	 * If the event's hand is equal to the player's main hand, or true by default in earlier versions of Minecraft.
	 */
	boolean isValidHand( PlayerInteractEvent event );
	
	/**
	 * Get the item in the player's main hand
	 * 
	 * @param player
	 * The player, cannot be null.
	 * @return
	 * The item being held in the main hand
	 */
	ItemStack getMainHandItem( Player player );
	
	/**
	 * Get the item in the player's off hand
	 * 
	 * @param player
	 * The player, cannot null.
	 * @return
	 * Null if no item is being held.
	 */
	ItemStack getOffHandItem( Player player );
	
	/**
	 * Check if a block at the given position is water.
	 * 
	 * @param snapshot
	 * The snapshot to check
	 * @param x
	 * X coordinate from 0 to 15.
	 * @param y
	 * Y coordinate from 0 to 255
	 * @param z
	 * Z coordinate from 0 to 15.
	 * @return
	 * If the block is some form of water, included waterlogged and kelp.
	 */
	boolean isWater( ChunkSnapshot snapshot, int x, int y, int z );
	
	/**
	 * Check if a block is water.
	 * 
	 * @param block
	 * The block to check, cannot be null.
	 * @return
	 * If the block is some form of water, included waterlogged and kelp.
	 */
	boolean isWater( Block block );
	
	/**
	 * Get the material of the block in the chunk snapshot at the specified coordinates.
	 * 
	 * @param snapshot
	 * Cannot be null.
	 * @param x
	 * Local x value from 0 to 15.
	 * @param y
	 * Local y value from 0 to 255.
	 * @param z
	 * Local z value from 0 to 15.
	 * @return
	 * A {@link CrossVersionMaterial} representing the type.
	 */
	CrossVersionMaterial getBlockType( ChunkSnapshot snapshot, int x, int y, int z );
	
	/**
	 * Get the material type of the item.
	 * 
	 * @param item
	 * Cannot be null.
	 * @return
	 * A {@link CrossVersionMaterial} representing the type.
	 */
	CrossVersionMaterial getItemType( ItemStack item );
	
	/**
	 * Get the material type of the block.
	 * 
	 * @param block
	 * Cannot be null.
	 * @return
	 * A {@link CrossVersionMaterial} representing the type.
	 */
	CrossVersionMaterial getBlockType( Block block );
	
	/**
	 * Check if this event is significant enough to require updating.
	 * 
	 * @param event
	 * Cannot be null.
	 * @return
	 * If the block being updated is the source block.
	 */
	boolean updateEvent( BlockPhysicsEvent event );
}

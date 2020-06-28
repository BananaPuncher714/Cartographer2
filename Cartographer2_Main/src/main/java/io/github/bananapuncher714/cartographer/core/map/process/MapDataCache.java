package io.github.bananapuncher714.cartographer.core.map.process;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.events.chunk.ChunkPreProcessEvent;
import io.github.bananapuncher714.cartographer.core.file.BigChunkQueue;
import io.github.bananapuncher714.cartographer.core.map.MapSettings;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;
import io.github.bananapuncher714.cartographer.core.util.BlockUtil;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;

/**
 * A thread safe cache with chunk data.
 * 
 * @author BananaPuncher714
 */
public class MapDataCache {
	protected final ExecutorService service = Executors.newFixedThreadPool( 2 );
	protected final Map< ChunkLocation, Future< ChunkData > > renderers = new HashMap< ChunkLocation, Future< ChunkData > >();
	protected final Map< ChunkLocation, ChunkData > data;
	protected final Map< ChunkLocation, ChunkSnapshot > chunks;
	
	protected final Set< ChunkLocation > forcedLoading = new HashSet< ChunkLocation >();
	protected final Set< ChunkLocation > loaded = new HashSet< ChunkLocation >();
	
	protected ChunkDataProvider provider;
	
	protected ChunkNotifier notifier;
	
	protected MapSettings setting;
	
	public MapDataCache( ChunkDataProvider provider, MapSettings setting ) {
		this( setting );
		this.provider = provider;
	}
	
	public MapDataCache( MapSettings setting ) {
		this.setting = setting;
		data = new ConcurrentHashMap< ChunkLocation, ChunkData >();
		chunks = new ConcurrentHashMap< ChunkLocation, ChunkSnapshot >();
	}

	public MapDataCache setNotifier( ChunkNotifier notifier ) {
		this.notifier = notifier;
		return this;
	}
	
	public ChunkNotifier getChunkNotifier() {
		return notifier;
	}
	
	public void update() {
		for ( Iterator< Entry< ChunkLocation, Future< ChunkData > > > iterator = renderers.entrySet().iterator(); iterator.hasNext(); ) {
			Entry< ChunkLocation, Future< ChunkData > > entry = iterator.next();
			
			if ( entry.getValue().isDone() ) {
				ChunkData chunkData = null;
				try {
					// Shouldn't throw any exceptions at this point
					chunkData = entry.getValue().get();
				} catch ( InterruptedException | ExecutionException e ) {
					e.printStackTrace();
				}
				
				// Get the northern and southern chunk
				ChunkLocation location = entry.getKey();
				ChunkLocation north = new ChunkLocation( location ).subtract( 0, 1 );
				ChunkLocation south = new ChunkLocation( location ).add( 0, 1 );
				// Check to see if the chunk was processed properly
				if ( chunkData != null ) {
					// Check to see if the chunk loaded was forced
					// If so, then we can remove unused chunk snapshots
					// by determining if we already have the ChunkData for the southern chunk
					if ( forcedLoading.contains( location ) ) {
						// This may be completely unnecessary?
//						if ( data.containsKey( north ) ) {
//							chunks.remove( north );
//							forcedLoading.remove( north );
//						}
						if ( data.containsKey( south ) ) {
							chunks.remove( location );
							forcedLoading.remove( location );
						}
					} else {
						// If not, then we simply mark the chunk as loaded
						loaded.add( location );
					}
					
					ChunkData newData = notifier != null ? notifier.onChunkProcessed( location, chunkData ) : chunkData;
					data.put( location, newData );
				} else {
					if ( forcedLoading.contains( location ) ) {
						addToProcessQueue( north );
					}
				}
				
				// Remove from loading
				iterator.remove();
			}
		}
		
		// Trim redundant ChunkSnapshots and load required ones
		Set< ChunkLocation > requiresLoading = new HashSet< ChunkLocation >();
		Set< ChunkLocation > removeNatural = new HashSet< ChunkLocation >();
		for ( Iterator< Entry< ChunkLocation, ChunkSnapshot > > iterator = chunks.entrySet().iterator(); iterator.hasNext(); ) {
			Entry< ChunkLocation, ChunkSnapshot > entry = iterator.next();
			
			ChunkLocation loc = entry.getKey();
			ChunkLocation south = new ChunkLocation( loc ).add( 0, 1 );
			ChunkLocation north = new ChunkLocation( loc ).subtract( 0, 1 );
			// We want to skip any chunks that are loaded in naturally, since they will get removed by themselves
			if ( !forcedLoading.contains( loc ) ) {
				// If the north, center, and south chunks have been loaded, then we know this is no longer needed
				if ( loaded.contains( south ) && loaded.contains( loc ) && loaded.contains( north ) ) {
					removeNatural.add( loc );
				} else if ( !loaded.contains( loc ) && chunks.containsKey( north ) ) {
					// Only if our map is set to update or we don't have the location to begin with
					if ( !data.containsKey( loc ) || setting.isAutoUpdate() ) {
						// Only if our minimap is willing to render outside of worldborders and such
						if ( withinVisiblePlayerRange( loc ) ) {
							if ( setting.isRenderOutOfBorder() || Cartographer.getInstance().getDependencyManager().shouldChunkBeLoaded( loc ) ) {
								process( loc );
							}
						}
					}
				}
				continue;
			}
			
			boolean hasLoc = data.containsKey( loc );
			boolean hasSouth = data.containsKey( south );
			
			// So first we need to see if the chunk location is insignificant and if our data already has the chunk
			// We also check if the southern chunk exists, for loading.
			// If any of those conditions are met, then we remove it from memory
			if ( ( hasLoc && !BlockUtil.needsRender( south ) ) || ( hasSouth && !BlockUtil.needsRender( north ) ) ) {
				iterator.remove();
				continue;
			}
			
			// Remove if redundant
			// Now we check if the south and current location both exist
			// If so, then we know that there is no need to actually keep the chunks loaded
			if ( hasSouth && hasLoc ) {
				forcedLoading.remove( loc );
				iterator.remove();
				continue;
			}
			if ( !hasLoc ) {
				if ( chunks.containsKey( north ) ) {
					process( loc );
				} else {
					requiresLoading.add( north );
				}
			}
			if ( !hasSouth && !chunks.containsKey( south ) ) {
				requiresLoading.add( south );
			}
		}
		for ( ChunkLocation location : requiresLoading ) {
			if ( !renderers.containsKey( location ) ) {
				addToProcessQueue( location );
			}
		}
		for ( ChunkLocation location : removeNatural ) {
			// This can probably go in the main update for loop but oh well
			if ( !forcedLoading.contains( location ) ) {
				chunks.remove( location );
			}
		}
	}
	
	public void setChunkDataProvider( ChunkDataProvider provider ) {
		this.provider = provider;
	}
	
	public ChunkDataProvider getChunkDataProvider() {
		return provider;
	}
	
	public Map< ChunkLocation, ChunkData > getData() {
		return data;
	}
	
	public void registerSnapshot( ChunkLocation location ) {
		// Ensure that the chunk actually needs to be rendered, and that the snapshot isn't already cached.
		if ( !BlockUtil.needsRender( location ) || chunks.containsKey( location ) ) {
			return;
		}
		ChunkLocation south = new ChunkLocation( location ).add( 0, 1 );
		if ( !forcedLoading.contains( location ) || !data.containsKey( location ) || !data.containsKey( south ) ) {
			if ( setting.isRenderOutOfBorder() || Cartographer.getInstance().getDependencyManager().shouldChunkBeLoaded( location ) || Cartographer.getInstance().getDependencyManager().shouldChunkBeLoaded( south ) ) {
				if ( withinVisiblePlayerRange( location ) ) {
					// TODO Getting the chunksnapshot here is pretty laggy. It computes lighting and unnecessary data when all we're looking for is the block types and potentially biome data.
					chunks.put( location, location.getChunk().getChunkSnapshot() );
				}
			}
		}
	}
	
	public void unregisterSnapshot( ChunkLocation location ) {
		if ( !forcedLoading.contains( location ) ) {
			chunks.remove( location );
			loaded.remove( location );
		}
	}
	
	public boolean hasSnapshot( ChunkLocation location ) {
		return chunks.containsKey( location );
	}
	
	public ChunkData getDataAt( ChunkLocation location ) {
		return data.get( location );
	}
	
	public boolean containsDataAt( ChunkLocation location ) {
		return data.containsKey( location );
	}
	
	public void loadData( ChunkLocation location, ChunkData data ) {
		ChunkData newData = notifier != null ? notifier.onChunkLoad( location, data ) : null;
		if ( newData != null ) {
			this.data.put( location, newData );
		} else {
			this.data.put( location, data );
		}
	}
	
	public ChunkSnapshot getChunkSnapshotAt( ChunkLocation location ) {
		return chunks.get( location );
	}
	
	public void releaseSnapshot( ChunkLocation location ) {
		chunks.remove( location );
	}
	
	/**
	 * Used by other processes to force load a chunk
	 * 
	 * @param location
	 * A location that requires loading
	 */
	public void addToProcessQueue( ChunkLocation location ) {
		forcedLoading.add( location );
		ChunkLoadListener.queueChunk( location );
	}
	
	public void process( ChunkLocation location ) {
		ChunkLocation north = new ChunkLocation( location ).subtract( 0, 1 );
		if ( !hasSnapshot( north ) || !hasSnapshot( location ) ) {
			return;
		}
		if ( !renderers.containsKey( location ) ) {
			ChunkProcessor processor = new ChunkProcessor( getChunkSnapshotAt( location ), provider );
			
			ChunkPreProcessEvent event = new ChunkPreProcessEvent( location, processor );
			event.callEvent();
			processor = event.getDataProcessor();
			
			renderers.put( location, service.submit( processor ) );
		}
	}
	
	public boolean isProcessing( ChunkLocation location ) {
		return renderers.containsKey( location );
	}
	
	/**
	 * Check if it is stored anywhere
	 * 
	 * @param location
	 * @return
	 */
	public boolean absent( ChunkLocation location ) {
		// Check if the location is already awaiting processing, if the data contains the key, or if the processors are doing something
		return !( renderers.containsKey( location ) || data.containsKey( location ) || chunks.containsKey( location ) );
	}
	
	public boolean withinVisiblePlayerRange( ChunkLocation location ) {
		int cx = location.getX() >> 4 << 8;
		int cz = location.getZ() >> 4 << 8;
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			Location playerLoc = player.getLocation();
			if ( playerLoc.getWorld() != location.getWorld() ) {
				continue;
			}
			int x = playerLoc.getBlockX();
			int z = playerLoc.getBlockZ();
			// Distance of the corner from the farthest zoom
			double farthest = setting.getFarthestZoom();
			double dist = 91 * farthest + farthest * 2;
			
			if ( BlockUtil.distance( cx, cz, x, z ) < dist ) {
				return true;
			}
		}
		return false;
	}
	
	public static ChunkState getStateOf( Minimap map, ChunkLocation location ) {
		MapDataCache cache = map.getDataCache();
		BigChunkQueue queue = map.getQueue();
		
		// Check if it's being loaded somehow
		if ( ChunkLoadListener.isLoading( location ) ) {
			return ChunkState.LOADING;
		} else if ( ChunkLoadListener.isQueued( location ) ) {
			return ChunkState.QUEUED;
		} else if ( queue.isLoading( location ) ) {
			return ChunkState.FILE_LOADING;
		} else if ( queue.isSaving( location ) ) {
			return ChunkState.FILE_SAVING;
		} else if ( cache.containsDataAt( location ) ) {
			return ChunkState.CACHED;
		} else if ( cache.isProcessing( location ) ) {
			return ChunkState.PROCESSING;
		} else if ( cache.hasSnapshot( location ) ) {
			return ChunkState.WAITING_FOR_PROCESSING;
		} else {
			return ChunkState.NONE;
		}
	}
	
	public void updateLocation( Location location, MinimapPalette palette ) {
		if ( !setting.isAutoUpdate() ) {
			return;
		}
		
		if ( !( setting.isRenderOutOfBorder() || Cartographer.getInstance().getDependencyManager().shouldLocationBeLoaded( location ) ) ) {
			return;
		}
		
		Location south = location.clone().add( 0, 0, 1 );
		for ( int i = -1; i < 2; i++ ) {
			ChunkLocation chunkLoc = new ChunkLocation( south );
			ChunkData cData = data.get( chunkLoc );
			if ( cData != null ) {
				int index = ( south.getBlockX() - ( chunkLoc.getX() << 4 ) ) + ( south.getBlockZ() - ( chunkLoc.getZ() << 4 ) ) * 16;
				cData.getData()[ index ] = JetpImageUtil.getBestColorIncludingTransparent( provider.process( south, palette ) );
			} else {
				addToProcessQueue( chunkLoc );
			}
			south.subtract( 0, 0, 1 );
		}
	}
	
	public void terminate() {
		service.shutdown();
	}
	
	public static enum ChunkState {
		NONE,
		QUEUED,
		LOADING,
		WAITING_FOR_PROCESSING,
		PROCESSING,
		FILE_LOADING,
		FILE_SAVING,
		CACHED;
	}
}

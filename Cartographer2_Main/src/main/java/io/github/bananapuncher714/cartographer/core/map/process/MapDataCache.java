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
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.events.chunk.ChunkPreProcessEvent;
import io.github.bananapuncher714.cartographer.core.file.BigChunk;
import io.github.bananapuncher714.cartographer.core.file.BigChunkLocation;
import io.github.bananapuncher714.cartographer.core.file.BigChunkQueue;
import io.github.bananapuncher714.cartographer.core.map.MapSettings;
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

	protected final Set< BigChunkLocation > scanned = new HashSet< BigChunkLocation >();

	protected ChunkDataProvider provider;
	protected ChunkNotifier notifier;

	// Minimap specific objects
	protected MapSettings setting;
	protected BigChunkQueue queue;

	protected final ReentrantLock lock = new ReentrantLock();

	public MapDataCache( ChunkDataProvider provider, MapSettings setting ) {
		this( setting );
		this.provider = provider;
	}

	public MapDataCache( MapSettings setting ) {
		this.setting = setting;
		data = new ConcurrentHashMap< ChunkLocation, ChunkData >();
		chunks = new ConcurrentHashMap< ChunkLocation, ChunkSnapshot >();
	}

	public void setFileQueue( BigChunkQueue queue ) {
		this.queue = queue;
	}

	public MapDataCache setNotifier( ChunkNotifier notifier ) {
		this.notifier = notifier;
		return this;
	}

	public ChunkNotifier getChunkNotifier() {
		return notifier;
	}

	public void update() {
		// So, first iterate through the currently rendering futures and save them if they're done
		for ( Iterator< Entry< ChunkLocation, Future< ChunkData > > > iterator = renderers.entrySet().iterator(); iterator.hasNext(); ) {
			Entry< ChunkLocation, Future< ChunkData > > entry = iterator.next();

			ChunkLocation location = entry.getKey();
			if ( entry.getValue().isDone() ) {
				ChunkData chunkData = null;
				try {
					// Shouldn't throw any exceptions at this point
					chunkData = entry.getValue().get();
				} catch ( InterruptedException | ExecutionException e ) {
					e.printStackTrace();
				}

				if ( chunkData != null ) {
					// Add it to our data
					updateDataAt( location, chunkData, true );
				}

				// Remove from loading
				iterator.remove();
			}
		}

		// Now, scan the rest of the chunks and remove whatever is fully loaded
		lock.lock();
		for ( Iterator< Entry< ChunkLocation, ChunkSnapshot > > iterator = chunks.entrySet().iterator(); iterator.hasNext(); ) {
			Entry< ChunkLocation, ChunkSnapshot > entry = iterator.next();
			ChunkLocation location = entry.getKey();
			ChunkLocation south = new ChunkLocation( location.getWorld(), location.getX(), location.getZ() + 1 );
			boolean updating = data.containsKey( location ) || renderers.containsKey( location ) || renderers.containsKey( south );
			if ( !updating ) {
				iterator.remove();
			}
		}
		lock.unlock();
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
		// We've just received a chunk location of a loaded chunk
		// Register the snapshot, but only if it's required

		// Absolutely do not render chunks that are not in the main world
		if ( setting.isBlacklisted( location.getWorld().getName() ) ) {
			return;
		}

		// Now, check if we can render the location or the one south of it
		ChunkLocation south = new ChunkLocation( location ).add( 0, 1 );
		
		// Check if we want to reload chunks that are already present
		// If not, then it means we want to rely only on map updates
		if ( !setting.isReloadChunks() ) {
			// Is this chunk snapshot really necessary?
			if ( data.containsKey( location ) && data.containsKey( south ) ) {
				return;
			}
		}
		
		// Ignore the visible player range for now
		lock.lock();
		chunks.put( location, location.getChunk().getChunkSnapshot() );
		lock.unlock();

		process( location, true );
		process( south, true );
	}

	public void unregisterSnapshot( ChunkLocation location ) {
		// Remove the snapshot only if it's not getting loaded
		ChunkLocation south = new ChunkLocation( location.getWorld(), location.getX(), location.getZ() + 1 );
		boolean inUse = renderers.containsKey( location ) || renderers.containsKey( south ) || ChunkLoadListener.INSTANCE.isForceLoad();
		if ( !inUse ) {
			lock.lock();
			chunks.remove( location );
			lock.unlock();
		}
	}

	public boolean hasSnapshot( ChunkLocation location ) {
		lock.lock();
		boolean contains = chunks.containsKey( location );
		lock.unlock();
		return contains;
	}

	public ChunkData getDataAt( ChunkLocation location ) {
		return data.get( location );
	}

	public boolean containsDataAt( ChunkLocation location ) {
		return data.containsKey( location );
	}

	public ChunkSnapshot getChunkSnapshotAt( ChunkLocation location ) {
		lock.lock();
		ChunkSnapshot snapshot = chunks.get( location );
		lock.unlock();
		return snapshot;
	}

	/**
	 * Used by other processes to force load a chunk
	 * 
	 * @param location
	 * A location that requires loading
	 */
	public void addToChunkLoader( ChunkLocation location ) {
		ChunkLoadListener.queueChunk( location );
	}

	public void process( ChunkLocation location, boolean force ) {
		ChunkLocation north = new ChunkLocation( location ).subtract( 0, 1 );
		if ( !hasSnapshot( north ) || !hasSnapshot( location ) ) {
			return;
		}
		if ( !renderers.containsKey( location ) || force ) {
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

	public void removeScannedLocation( BigChunkLocation location ) {
		scanned.remove( location );
	}

	public void removeChunkDataAt( ChunkLocation location ) {
		data.remove( location );
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
				addToChunkLoader( chunkLoc );
			}
			south.subtract( 0, 0, 1 );
		}
	}

	public void updateDataAt( ChunkLocation location, ChunkData data, boolean force ) {
		// Provide the data at the given location
		if ( data != null ) {
			// Force replace?
			if ( force || !this.data.containsKey( location ) ) {
				ChunkData newData = notifier != null ? notifier.onChunkLoad( location, data ) : null;
				this.data.put( location, newData == null ? data : newData );
			}
		} else {
			throw new IllegalArgumentException( "ChunkData cannot be null!" );
		}
	}

	public void updateDataAt( BigChunkLocation location, BigChunk chunk, boolean force ) {
		int cx = location.getX() << 4;
		int cz = location.getZ() << 4;
		for ( int z = 0; z < 16; z++ ) {
			int zIndex = z << 4;
			for ( int x = 0; x < 16; x++ ) {
				ChunkLocation chunkLocation = new ChunkLocation( location.getWorld(), cx + x, cz + z );

				ChunkData data = chunk.getData()[ x + zIndex ];
				if ( data != null ) {
					updateDataAt( chunkLocation, data, force );
				} else {
					requestLoadFor( chunkLocation, force );
				}
			}
		}
	}

	public void requestLoadFor( ChunkLocation location, boolean force ) {
		// This request does not try to load it from file
		// If forced, it will forcefully reload the location

		// First, check if the location is within the worldborder
		boolean withinBorders = setting.isRenderOutOfBorder() || Cartographer.getInstance().getDependencyManager().shouldChunkBeLoaded( location );
		if ( withinBorders ) {
			// Next, check if the location is already processed, or being processed
			boolean required = !( data.containsKey( location ) || renderers.containsKey( location ) );
			if ( required || force ) {
				// We need to render the chunk
				// For that, we need the northern chunk snapshot too
				ChunkLocation north = new ChunkLocation( location.getWorld(), location.getX(), location.getZ() - 1 );

				requestSnapshotFor( location, force );
				requestSnapshotFor( north, force );
			}
		}
	}

	public void requestSnapshotFor( ChunkLocation location, boolean force ) {
		// This request attempts to fetch the snapshot for a given location
		// Check if it is already loaded first
		lock.lock();
		boolean required = !chunks.containsKey( location );
		lock.unlock();
		if ( required || force ) {
			// We do not have the snapshot
			// Ask the chunk loader to fetch it
			ChunkLoadListener.queueChunk( location );
		}
	}

	public void requestLoadFor( BigChunkLocation location ) {
		// This request tries to load it from file
		// Something is requesting that the location gets loaded
		// Don't bother fetching the same file if we've already done it
		int cx = location.getX() << 4;
		int cz = location.getZ() << 4;
		if ( !scanned.contains( location ) ) {
			boolean attemptToLoad = false;
			// Scan through each ChunkLocation to see if we're currently processing it
			for ( int z = 0; z < 16 && !attemptToLoad; z++ ) {
				for ( int x = 0; x < 16 && !attemptToLoad; x++ ) {
					ChunkLocation chunkLocation = new ChunkLocation( location.getWorld(), cx + x, cz + z );

					// Right now, we only care if it is being rendered, or if data contains it
					// or if we have the snapshot or if it's queued in the chunk load listener already
					// Try to load from file as few times as possible
					// This also means that while we're loading a chunk, it's not going to appear on the map,
					// especially if all of the locations are loading or something.
					// But, they should load in relatively quickly, and not stay as blank spots
					boolean isQueued = ChunkLoadListener.isLoading( chunkLocation ) || ChunkLoadListener.isQueued( chunkLocation ) || hasSnapshot( chunkLocation );
					boolean required = !( data.containsKey( chunkLocation ) || renderers.containsKey( chunkLocation ) || isQueued );
					if ( required ) {
						// We don't have it anywhere, so send a request to the queue to try and load it from file
						if ( queue != null ) {
							// We have a file queue that we can try to load from
							attemptToLoad = true;
							if ( queue.load( location ) ) {
								// Only include this if we've successfully queued a load
								scanned.add( location );
							}
						} else {
							requestLoadFor( chunkLocation, false );
						}
					}
				}
			}
		} else {
			for ( int x = 0; x < 16; x++ ) {
				for ( int z = 0; z < 16; z++ ) {
					ChunkLocation chunkLocation = new ChunkLocation( location.getWorld(), cx + x, cz + z );
					requestLoadFor( chunkLocation, false );
				}
			}
		}
	}

	public void terminate() {
		service.shutdown();
	}
}

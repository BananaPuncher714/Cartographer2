package io.github.bananapuncher714.cartographer.core.map.process;

import java.util.Arrays;
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
import java.util.concurrent.ThreadLocalRandom;
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
public class MapDataCache implements DataCache {
	protected final ExecutorService service = Executors.newFixedThreadPool( 2 );
	protected final Map< ChunkLocation, Future< ChunkData > > renderers = new HashMap< ChunkLocation, Future< ChunkData > >();
	protected ChunkDataStorage storage;
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
		chunks = new HashMap< ChunkLocation, ChunkSnapshot >();
		
		storage = new SimpleChunkDataStorage();
	}

	@Override
	public void setFileQueue( BigChunkQueue queue ) {
		this.queue = queue;
	}

	@Override
	public MapDataCache setNotifier( ChunkNotifier notifier ) {
		this.notifier = notifier;
		return this;
	}

	@Override
	public ChunkNotifier getChunkNotifier() {
		return notifier;
	}

	@Override
	public void update() {
		lock.lock();
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
		
		for ( Iterator< Entry< ChunkLocation, ChunkSnapshot > > iterator = chunks.entrySet().iterator(); iterator.hasNext(); ) {
			Entry< ChunkLocation, ChunkSnapshot > entry = iterator.next();
			ChunkLocation location = entry.getKey();
			ChunkLocation south = new ChunkLocation( location.getWorld(), location.getX(), location.getZ() + 1 );
			// Fix this if possible
			boolean updating = renderers.containsKey( location ) ||
					renderers.containsKey( south ) ||
					location.isLoaded() ||
					ChunkLoadListener.INSTANCE.isForceLoad();
			if ( !updating ) {
				iterator.remove();
			}
		}
		lock.unlock();
	}
	

	@Override
	public void setChunkDataProvider( ChunkDataProvider provider ) {
		this.provider = provider;
	}

	@Override
	public ChunkDataProvider getChunkDataProvider() {
		return provider;
	}

	@Override
	public void setChunkDataStorage( ChunkDataStorage storage ) {
		this.storage = storage;
	}
	
	@Override
	public ChunkDataStorage getStorage() {
		return storage;
	}
	
	@Override
	public void registerSnapshot( ChunkLocation location ) {
		byte[] arr = new byte[ 256 ];
		Arrays.fill( arr, ( byte ) ( ThreadLocalRandom.current().nextInt( 128 ) + 5 ) );
		
		if ( setting.isBlacklisted( location.getWorld().getName() ) ) {
			return;
		}

		ChunkLocation south = new ChunkLocation( location ).add( 0, 1 );
		
		lock.lock();
		if ( !setting.isReloadChunks() ) {
			// Is this chunk snapshot really necessary?
			if ( storage.contains( location ) && storage.contains( south ) ) {
				lock.unlock();
				return;
			}
		}
		
		// Ignore the visible player range for now
		chunks.put( location, location.getChunk().getChunkSnapshot() );

		process( location, true );
		process( south, true );
		
		lock.unlock();
	}

	@Override
	public void unregisterSnapshot( ChunkLocation location ) {
		ChunkLocation south = new ChunkLocation( location.getWorld(), location.getX(), location.getZ() + 1 );
		lock.lock();
		if ( !( renderers.containsKey( location ) || renderers.containsKey( south ) ) ) {
			chunks.remove( location );
		}
		lock.unlock();
	}

	@Override
	public boolean hasSnapshot( ChunkLocation location ) {
		lock.lock();
		boolean contains = chunks.containsKey( location );
		lock.unlock();
		return contains;
	}

	@Override
	public ChunkData getDataAt( ChunkLocation location ) {
		// No lock
		return storage.get( location );
	}

	@Override
	public boolean containsDataAt( ChunkLocation location ) {
		// No lock
		return storage.contains( location );
	}

	@Override
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
	@Override
	public void addToChunkLoader( ChunkLocation location ) {
		ChunkLoadListener.queueChunk( location );
	}

	@Override
	public void process( ChunkLocation location, boolean force ) {
		ChunkLocation north = new ChunkLocation( location ).subtract( 0, 1 );
		lock.lock();
		if ( hasSnapshot( north ) && hasSnapshot( location ) ) {
			if ( !renderers.containsKey( location ) || force ) {
				ChunkProcessor processor = new ChunkProcessor( getChunkSnapshotAt( location ), provider );
	
				ChunkPreProcessEvent event = new ChunkPreProcessEvent( location, processor );
				event.callEvent();
				processor = event.getDataProcessor();
	
				Future< ChunkData > fut = renderers.put( location, service.submit( processor ) );
				if ( fut != null && !fut.isDone() ) {
					fut.cancel( true );
				}
			}
		}
		lock.unlock();
	}

	@Override
	public boolean isProcessing( ChunkLocation location ) {
		lock.lock();
		boolean rendering = renderers.containsKey( location );
		lock.unlock();
		return rendering;
	}

	@Override
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

	@Override
	public void removeScannedLocation( BigChunkLocation location ) {
		lock.lock();
		scanned.remove( location );
		lock.unlock();
	}

	@Override
	public void removeChunkDataAt( ChunkLocation location ) {
		lock.lock();
		storage.remove( location );
		lock.unlock();
	}

	@Override
	public void updateLocation( Location location, MinimapPalette palette ) {
		if ( !setting.isAutoUpdate() ) {
			return;
		}

		if ( !( setting.isRenderOutOfBorder() || Cartographer.getInstance().getDependencyManager().shouldLocationBeLoaded( location ) ) ) {
			return;
		}

		Set< ChunkLocation > needsUpdate = new HashSet< ChunkLocation >();
		
		Location south = location.clone().add( 0, 0, 1 );
		for ( int i = -1; i < 2; i++ ) {
			ChunkLocation chunkLoc = new ChunkLocation( south );
			ChunkData cData = storage.get( chunkLoc );
			if ( cData != null ) {
				int index = ( south.getBlockX() - ( chunkLoc.getX() << 4 ) ) + ( south.getBlockZ() - ( chunkLoc.getZ() << 4 ) ) * 16;
				cData.getData()[ index ] = JetpImageUtil.getBestColorIncludingTransparent( provider.process( south, palette ) );
			} else {
				needsUpdate.add( chunkLoc );
			}
			south.subtract( 0, 0, 1 );
		}
		
		for ( ChunkLocation loc : needsUpdate ) {
			addToChunkLoader( loc );
		}
	}

	@Override
	public void updateDataAt( ChunkLocation location, ChunkData data, boolean force ) {
		if ( data != null ) {
			// Check if it's within the worldborder
			boolean withinBorders = setting.isRenderOutOfBorder() || Cartographer.getInstance().getDependencyManager().shouldChunkBeLoaded( location );
			if ( withinBorders ) {
				lock.lock();
				if ( force || !storage.contains( location ) ) {
					ChunkData newData = notifier != null ? notifier.onChunkLoad( location, data ) : null;
					newData = newData == null ? data : newData;
					
					storage.store( location, newData );
				}
				lock.unlock();
			}
		} else {
			throw new IllegalArgumentException( "ChunkData cannot be null!" );
		}
	}

	@Override
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

	@Override
	public void requestLoadFor( ChunkLocation location, boolean force ) {
		boolean withinBorders = setting.isRenderOutOfBorder() || Cartographer.getInstance().getDependencyManager().shouldChunkBeLoaded( location );
		if ( withinBorders ) {
			lock.lock();
			boolean required = !( storage.contains( location ) || renderers.containsKey( location ) );
			lock.unlock();
			if ( required || force ) {
				// We need to render the chunk
				// For that, we need the northern chunk snapshot too
				ChunkLocation north = new ChunkLocation( location.getWorld(), location.getX(), location.getZ() - 1 );

				requestSnapshotFor( location, force );
				requestSnapshotFor( north, force );
			}
		}
	}

	@Override
	public void requestSnapshotFor( ChunkLocation location, boolean force ) {
		lock.lock();
		boolean required = !chunks.containsKey( location );
		lock.unlock();
		if ( required || force ) {
			ChunkLoadListener.queueChunk( location );
		}
	}

	@Override
	public void requestLoadFor( BigChunkLocation location ) {
		int cx = location.getX() << 4;
		int cz = location.getZ() << 4;
		lock.lock();
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
					boolean required = !( storage.contains( chunkLocation ) || renderers.containsKey( chunkLocation ) || isQueued );
					if ( required ) {
						// We don't have it anywhere, so send a request to the queue to try and load it from file
						if ( queue != null ) {
							// We have a file queue that we can try to load from
							attemptToLoad = true;
						} else {
							requestLoadFor( chunkLocation, false );
						}
					}
				}
			}
			if ( attemptToLoad ) {
				if ( queue.load( location ) ) {
					// Only include this if we've successfully queued a load
					scanned.add( location );
				}
			}
		}
		lock.unlock();
	}
	
	@Override
	public void terminate() {
		service.shutdown();
	}
}

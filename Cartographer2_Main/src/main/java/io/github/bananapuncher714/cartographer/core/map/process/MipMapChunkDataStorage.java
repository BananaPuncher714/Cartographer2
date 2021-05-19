package io.github.bananapuncher714.cartographer.core.map.process;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;

public class MipMapChunkDataStorage implements ChunkDataStorage {
	protected static final int BLOCK_POWER = 6;
	protected static final int BLOCK_WIDTH = 64;
	protected static final int SECTION_POWER = 4;
	protected static final int SECTION_WIDTH = 16;
	
	protected int levels;
	protected List< Map< ChunkLocation, byte[] > > maps;
	protected final Map< ChunkLocation, ChunkData > data;
	protected final List< Map< ChunkLocation, int[][] > > locations = new ArrayList< Map< ChunkLocation, int[][] > >();
	protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock( true );
	
	protected final int chunkDataWidth = ChunkData.CHUNK_WIDTH - 1;
	protected final int sectionWidth = SECTION_WIDTH - 1;
	protected final int sectionsPerBlock = ( BLOCK_WIDTH >> SECTION_POWER ) - 1;
	protected final int blockSize = BLOCK_WIDTH * BLOCK_WIDTH;
	
	public MipMapChunkDataStorage( int levels ) {
		this.levels = Math.min( 22, Math.max( 0, levels ) );
		data = new HashMap< ChunkLocation, ChunkData >();
		maps = new ArrayList< Map< ChunkLocation, byte[] > >( levels );
		for ( int i = 0; i <= levels; i++ ) {
			maps.add( new HashMap< ChunkLocation, byte[] >() );
		}
		for ( int i = 0; i < levels; i++ ) {
			locations.add( new HashMap< ChunkLocation, int[][] >() );
		}
	}
	
	@Override
	public void store( ChunkLocation location, ChunkData data ) {
		lock.writeLock().lock();
		
		this.data.put( location, data );
		
		addMipMapLocation( location );
		
		lock.writeLock().unlock();
	}

	@Override
	public void remove( ChunkLocation location ) {
		lock.writeLock().lock();
		
		this.data.remove( location );
		
		
		lock.writeLock().unlock();
	}

	@Override
	public ChunkData get( ChunkLocation location ) {
		ChunkData data;
		lock.readLock().lock();
		
		data = this.data.get( location );
		
		lock.readLock().unlock();
		return data;
	}

	@Override
	public Collection< ChunkLocation > getLocations() {
		Set< ChunkLocation > chunks;
		lock.readLock().lock();
		
		chunks = new HashSet< ChunkLocation >( data.keySet() );
		
		lock.readLock().unlock();
		return chunks;
	}

	@Override
	public boolean contains( ChunkLocation location ) {
		boolean contains;
		lock.readLock().lock();
		
		contains = data.containsKey( location );
		
		lock.readLock().unlock();
		return contains;
	}

	@Override
	public byte getColorAt( Location location ) {
		return getColorAt( location, 1 );
	}

	@Override
	public byte getColorAt( Location location, double scale ) {
		byte color = -1;
		int level = 0;
		int power = 1;
		
		while ( power < scale && level < levels ) {
			power <<= 1;
			level++;
		}
		
		int locX = location.getBlockX() >> level;
		int locZ = location.getBlockZ() >> level;
		
		lock.readLock().lock();

		Map< ChunkLocation, byte[] > map = maps.get( level );
		ChunkLocation mipLoc = new ChunkLocation( location.getWorld(), locX >> BLOCK_POWER, locZ >> BLOCK_POWER );
		byte[] data = map.get( mipLoc );
		if ( data != null ) {
			int index = ( ( ( ( ( locX >> SECTION_POWER ) & sectionsPerBlock ) << SECTION_POWER ) << SECTION_POWER ) | ( locX & sectionWidth ) ) |
					( ( ( ( ( locZ >> SECTION_POWER ) & sectionsPerBlock ) << BLOCK_POWER ) << SECTION_POWER ) | ( ( locZ & sectionWidth ) << SECTION_POWER ) );
			color = data[ index ];
		}
		
		lock.readLock().unlock();
		return color;
	}

	private void addMipMapLocation( ChunkLocation location ) {
		Map< ChunkLocation, byte[] > baseMap = maps.get( 0 );
		ChunkData chunk = data.get( location );
		
		// Counter for the entire block
		int[][][] mipCounts = new int[ levels ][][];
		// The entire block
		byte[][] mipmaps = new byte[ levels ][];
		int chunkSize = ( 1 << ChunkData.CHUNK_POWER ) << ChunkData.CHUNK_POWER;
		for ( int i = 0; i < levels; i++ ) {
			// Get the block that the location is part of
			ChunkLocation mipLoc = new ChunkLocation( location.getWorld(), location.getX() >> ( ( BLOCK_POWER - 4 ) + i + 1 ), location.getZ() >> ( ( BLOCK_POWER - 4 ) + i + 1 ) );
			// Don't want to wipe out the previous block...
			// But the current location should also be cleared
			mipCounts[ i ] = locations.get( i ).get( mipLoc );
			if ( mipCounts[ i ] == null ) {
				mipCounts[ i ] = new int[ blockSize ][ JetpImageUtil.getLargestColorVal() ];
				locations.get( i ).put( mipLoc, mipCounts[ i ] );
			}
			
			mipmaps[ i ] = maps.get( i + 1 ).get( mipLoc );
			if ( mipmaps[ i ] == null ) {
				mipmaps[ i ] = new byte[ blockSize ];
				maps.get( i + 1 ).put( mipLoc, mipmaps[ i ] );
			}
			
			// Clear this chunk's location
			for ( int j = 0; j < chunkSize; j++ ) {
				int locX = ( location.getX() << 4 | ( j & chunkDataWidth ) ) >> ( i + 1 ); 
				int locZ = ( location.getZ() << 4 | ( ( j >> ChunkData.CHUNK_POWER ) & chunkDataWidth ) ) >> ( i + 1 ); 
				int index = ( ( ( ( ( locX >> SECTION_POWER ) & sectionsPerBlock ) << SECTION_POWER ) << SECTION_POWER ) | ( locX & sectionWidth ) ) |
						( ( ( ( ( locZ >> SECTION_POWER ) & sectionsPerBlock ) << BLOCK_POWER ) << SECTION_POWER ) | ( ( locZ & sectionWidth ) << SECTION_POWER ) );
				mipCounts[ i ][ index ] = new int[ JetpImageUtil.getLargestColorVal() ];
				mipmaps[ i ][ index ] = -1;
			}
			
		}
		
		if ( chunk != null ) {
			ChunkLocation mipmapLocation = new ChunkLocation( location.getWorld(), location.getX() >> ( BLOCK_POWER - 4 ), location.getZ() >> ( BLOCK_POWER - 4 ) );
			byte[] data = baseMap.get( mipmapLocation );
			if ( data == null ) {
				data = new byte[ blockSize ];
				baseMap.put( mipmapLocation, data );
			}
			
			byte[] chunkData = chunk.getData();
			for ( int arrIndex = 0; arrIndex < chunkData.length; arrIndex++ ) {
				byte b = chunkData[ arrIndex ];
			
				// Find the absolute X and Z
				int locX = ( location.getX() << ChunkData.CHUNK_POWER ) | ( arrIndex & chunkDataWidth ); 
				int locZ = ( location.getZ() << ChunkData.CHUNK_POWER ) | ( ( arrIndex >> ChunkData.CHUNK_POWER ) & chunkDataWidth ); 
				
				// Convert it to a local index
				int index = ( ( ( ( ( locX >> SECTION_POWER ) & sectionsPerBlock ) << SECTION_POWER ) << SECTION_POWER ) | ( locX & sectionWidth ) ) |
						( ( ( ( ( locZ >> SECTION_POWER ) & sectionsPerBlock ) << BLOCK_POWER ) << SECTION_POWER ) | ( ( locZ & sectionWidth ) << SECTION_POWER ) );
				data[ index ] = b;
				
				// Count the number of each color
				for ( int i = 0; i < levels; i++ ) {
					locX >>= 1;
					locZ >>= 1;

					index = ( ( ( ( ( locX >> SECTION_POWER ) & sectionsPerBlock ) << SECTION_POWER ) << SECTION_POWER ) | ( locX & sectionWidth ) ) |
							( ( ( ( ( locZ >> SECTION_POWER ) & sectionsPerBlock ) << BLOCK_POWER ) << SECTION_POWER ) | ( ( locZ & sectionWidth ) << SECTION_POWER ) );
					mipCounts[ i ][ index ][ b & 0xFF ]++;
				}
			}
		}
		
		IntStream.range( 0, levels ).parallel().forEach( i -> {
			int[][] mipCount = mipCounts[ i ];
			byte[] mipmap = mipmaps[ i ];
			
			IntStream.range( 0, blockSize ).parallel().forEach( j -> {
				int best = 0;
				int highest = 0;
				for ( int k = 0; k < JetpImageUtil.getLargestColorVal(); k++ ) {
					int num = mipCount[ j ][ k ];
					if ( num > highest ) {
						best = k;
						highest = num;
					} else if ( num == highest ) {
						int bestColor = JetpImageUtil.getColorFromMinecraftPalette( ( byte ) best );
						int color = JetpImageUtil.getColorFromMinecraftPalette( ( byte ) k );
						float bestBright = Color.RGBtoHSB( ( bestColor >> 16 ) & 0xFF, ( bestColor >> 8 ) & 0xFF, bestColor & 0xFF, null )[ 2 ];
						float colorBright = Color.RGBtoHSB( ( color >> 16 ) & 0xFF, ( color >> 8 ) & 0xFF, color & 0xFF, null )[ 2 ];
						if ( colorBright > bestBright ) {
							best = k;
						}
					}
				}
				if ( highest > 0 ) {
					mipmap[ j ] = ( byte ) best;
				}
			} );
		} );
	}
}

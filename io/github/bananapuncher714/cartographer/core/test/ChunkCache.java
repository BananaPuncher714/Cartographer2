package io.github.bananapuncher714.cartographer.core.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ChunkCache {
	protected ExecutorService generator = Executors.newFixedThreadPool( 2 );
	protected Map< Coord, Future< int[] > > generatorMap = new HashMap< Coord, Future< int[] > >();
	
	protected final Map< Coord, int[] > data;
	protected PaintPanel panel;
	
	public ChunkCache( PaintPanel panel ) {
		data = new ConcurrentHashMap< Coord, int[] >();
		this.panel = panel;
	}
	
	public void update() {
		for ( Iterator< Entry< Coord, Future< int[] > > > iterator = generatorMap.entrySet().iterator(); iterator.hasNext(); ) {
			Entry< Coord, Future< int[] > > entry = iterator.next();
			
			if ( entry.getValue().isDone() ) {
				iterator.remove();
				try {
					data.put( entry.getKey(), entry.getValue().get() );
				} catch ( InterruptedException | ExecutionException e ) {
					e.printStackTrace();
				}
			}
		}
		if ( generatorMap.size() > 0 ) {
			System.out.println( "Generation remaining: " + generatorMap.size() );
		}
	}
	
	public void generateCoord( Coord coord ) {
		generatorMap.put( coord, generator.submit( new GenerateTask() ) );
	}
	
	public boolean isGenerating( Coord coord ) {
		return generatorMap.containsKey( coord );
	}
	
	public static class Coord {
		public final int x;
		public final int y;
		
		public Coord( int x, int y ) {
			this.x = x;
			this.y = y;
		}
		
		public double distance( Coord coord ) {
			int dx = Math.abs( x - coord.x );
			int dy = Math.abs( y - coord.y );
			return Math.sqrt( dx * dx + dy * dy );
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Coord other = (Coord) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}
	}
}

package io.github.bananapuncher714.cartographer.core.test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.bukkit.map.MapCursor;

import com.google.common.base.Enums;

import io.github.bananapuncher714.cartographer.core.test.ChunkCache.Coord;
import net.minecraft.server.v1_13_R2.MapIcon;

public class CartographerTest {
	private JFrame frame;
	
	private final int windowWidth = 800;
	private final int windowHeight = 800;
	
	private File save;
	
	private static File base = new File( System.getProperty( "user.dir" ) );
	
	public static void main( String[] args ) {
		newMain();
//		SwingUtilities.invokeLater( new Runnable() {
//			@Override
//			public void run() {
//				new CartographerTest( base );
//			}
//		} );
	}
	
	public static void newMain() {
		for ( MapCursor.Type type : MapCursor.Type.values() ) {
			System.out.println( "CURSOR_TYPES.put( MapCursor.Type." + type.name() + ", MapIcon.Type." + getEnum( MapIcon.Type.class, type.name() ).name() + " );" );
		}
	}
	
	public static < T > T getEnum( Class< T > clazz, String value ) {
		if ( !clazz.isEnum() ) return null;
		if ( value == null ) return clazz.getEnumConstants()[ 0 ];
		for ( Object object : clazz.getEnumConstants() ) {
			if ( object.toString().equals( value ) ) {
				return ( T ) object;
			}
		}
		return clazz.getEnumConstants()[ 0 ];
	}
	
	protected Coord coord = new Coord( 0, 0 );
	protected ChunkCache cache;
	protected LoadSave queue;
	
	public CartographerTest( File saveDir ) {
		save = new File( saveDir + "/" + "cache" );
		save.mkdirs();
		
		frame = new JFrame( "CartographerTest" );
		
		PaintPanel panel = new CartographerPanel( this, 400, 400 );
		
		panel.setCenterX( 0 );
		panel.setCenterY( 0 );
		panel.setScale( 4 );
		
		frame.add( panel );
		
		frame.setSize( windowWidth, windowHeight );
		frame.setVisible( true );
		frame.setResizable( true );
		
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		
		cache = new ChunkCache( panel );
		queue = new LoadSave( cache, save );
		
		new Thread() {
			@Override
			public void run() {
				long tick = 0;
				while ( true ) {
					if ( tick % 5 == 0 ) {
						queue.update();
						frame.repaint();
					}
					if ( tick++ % 1000 == 0 ) {
						System.out.println( "Did stuff" );
						Set< Coord > save = new HashSet< Coord >();
						for ( Coord tcoord : cache.data.keySet() ) {
							Coord newCoord = new Coord( tcoord.x << 2, tcoord.y << 2 );
							if ( newCoord.distance( coord ) > 100 ) {
								save.add( tcoord );
							}
						}
						for ( Coord coord : save ) {
							if ( queue.saveFuture( coord, cache.data.get( coord ) ) ) {
								cache.data.remove( coord );
							}
						}
					}
					if ( tick % 10 == 0 ) {
						cache.update();
					}
					try {
						Thread.sleep( 5 );
					} catch ( InterruptedException e ) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	public Coord getCoord() {
		return coord;
	}
	
	public void setCoord( Coord coord ) {
		this.coord = coord;
	}
}

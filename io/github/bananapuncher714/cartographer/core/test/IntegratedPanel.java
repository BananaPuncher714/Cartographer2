package io.github.bananapuncher714.cartographer.core.test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.bukkit.map.MapPalette;

import io.github.bananapuncher714.cartographer.core.ChunkLoadListener;
import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.ChunkData;
import io.github.bananapuncher714.cartographer.core.map.MapDataCache;
import io.github.bananapuncher714.cartographer.core.test.ChunkCache.Coord;

public class IntegratedPanel extends PaintPanel implements MouseMotionListener, MouseListener {
	private static IntegratedPanel instance;
	
	int originX;
	int originY;
	
	int middleX;
	int middleY;
	
	MapDataCache cache;
	
	public static void start( MapDataCache cache ) {
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				instance = new IntegratedPanel( cache );
			}
		} );
	}
	
	public static void redraw() {
		instance.repaint();
	}
	
	public IntegratedPanel( MapDataCache cache ) {
		JFrame frame = new JFrame( "CartographerTest" );
		
		setCenterX( 0 );
		setCenterY( 0 );
		setScale( 4 );
		
		frame.add( this );
		
		frame.setSize( 800, 800 );
		frame.setVisible( true );
		frame.setResizable( true );
		this.cache = cache;
		
		addMouseListener( this );
		addMouseMotionListener( this );
	}
	
	@Override
	public void paintComponent( Graphics g ) {
		super.paintComponent( g );
		
		for ( ChunkLocation location : cache.getData().keySet() ) {
			ChunkData data = cache.getData().get( location );
			Color color = MapPalette.getColor( data.getData()[ 0 ] );
			if ( cache.hasSnapshot( location ) ) {
				color = Color.BLACK;
			}
			g.setColor( color );
			this.drawPoint( location.getX() + middleX, location.getZ() + middleY );
		}
		for ( ChunkLocation location : ChunkLoadListener.INSTANCE.getChunks() ) {
			Color color = Color.CYAN;
			if ( cache.containsDataAt( location ) ) {
				color = Color.RED;
			}
			g.setColor( color );
			this.drawPoint( location.getX() + middleX, location.getZ() + middleY );
		}
		
		
		g.drawString( "Pos " + middleX + ", " + middleY, 10, 10 );
	}
	
	@Override
	public void mouseDragged( MouseEvent e ) {
		middleX = originX - e.getX();
		middleY = originY - e.getY();
		repaint();
		System.out.println( "Set the middle to " + middleX + ", " + middleY );
	}

	@Override
	public void mouseMoved( MouseEvent arg0 ) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if ( e.getButton() == MouseEvent.BUTTON1 ) {
			originX = e.getX() + middleX;
			originY = e.getY() + middleY;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
}

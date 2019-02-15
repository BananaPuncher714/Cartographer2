package io.github.bananapuncher714.cartographer.core.test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import io.github.bananapuncher714.cartographer.core.test.ChunkCache.Coord;

public class CartographerPanel extends PaintPanel implements MouseListener {
	private int width;
	private int height;
	private CartographerTest cache;
	
	public CartographerPanel( CartographerTest cache, int width, int height ) {
		this.width = width;
		this.height = height;
		this.cache = cache;
		
		setCenterX( width / 2 );
		setCenterY( height / 2 );
		
		addMouseListener( this );
	}
	
	@Override
	public void paintComponent( Graphics g ) {
		super.paintComponent( g );
		for ( Coord coord : cache.cache.data.keySet()  ) {
			int[] data = cache.cache.data.get( coord );
			if ( data == null ) {
				System.out.println( "Sync data error!" );
				continue;
			}
			for ( int x = 0; x < 4; x++ ) {
				for ( int y = 0; y < 4; y++ ) {
					g.setColor( new Color( data[ x + y * 4 ] ) );
					drawPoint( coord.x << 2 | x, coord.y << 2 | y );
				}
			}
		}
		
		Coord center = cache.getCoord();
		int x = center.x;
		int y = center.y;
		for ( int i = -19; i < 20; i++ ) {
			for ( int j = -19; j < 20; j++ ) {
				int nx = x + i;
				int ny = y + j;
				Coord relChunk = new Coord( nx >> 2, ny >> 2 );
				int[] data = cache.cache.data.get( relChunk );
				if ( data != null ) {
					int rx = ( nx % 4 + 4 ) % 4;
					int ry = ( ny % 4 + 4 ) % 4;
					g.setColor( new Color( data[ rx + ry * 4 ] ) );
					drawPoint( nx, ny );
				} else {
					cache.queue.loadFuture( relChunk );
				}
			}
		}
	}
	
	@Override
	public void mouseClicked( MouseEvent e ) {
		if ( e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3 ) {
			int x = e.getX() / scale + centerX;
			int y = e.getY() / scale + centerY;
			
			System.out.println( "Set new coords to " + x + ", " + y );
			
			cache.setCoord( new Coord( x, y ) );
			
			repaint();
		}
	}

	@Override
	public void mousePressed( MouseEvent e ) {
	}

	@Override
	public void mouseReleased( MouseEvent e ) {
	}

	@Override
	public void mouseEntered( MouseEvent e ) {
	}

	@Override
	public void mouseExited( MouseEvent e ) {
	}
}
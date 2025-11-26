package io.github.bananapuncher714.cartographer.module.worldviewer.menu;

import java.awt.Color;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.cartographer.core.map.menu.MapInteraction;
import io.github.bananapuncher714.cartographer.core.map.menu.MenuCanvas;
import io.github.bananapuncher714.cartographer.core.map.menu.MenuComponent;
import io.github.bananapuncher714.cartographer.core.util.JetpImageUtil;
import io.github.bananapuncher714.cartographer.module.worldviewer.WorldViewer;
import io.github.bananapuncher714.cartographer.module.worldviewer.font.BananaTypeFont;
import io.github.bananapuncher714.cartographer.module.worldviewer.font.PixelGlyph;

public class TextOverlayComponent implements MenuComponent {
	private WorldViewer module;
	private OverviewMenu menu;
	
	private int x;
	private int y;
	private String key;
	private boolean unicode;
	private boolean shadow;
	private int tabDistance;
	private int charDistance;
	private int spaceDistance;
	private int lineSpacing;
	
	public TextOverlayComponent( WorldViewer module, OverviewMenu menu ) {
		this.module = module;
		this.menu = menu;
	}
	
	@Override
	public boolean onView( MenuCanvas canvas, Player player, double x, double y ) {
		Location center = menu.getCenter();
		if ( center == null ) {
			return false;
		}
		int xCoord = ( int ) ( center.getX() + menu.getScale() * ( x - 64 ) );
		int yCoord = ( int ) ( center.getZ() + menu.getScale() * ( y - 64 ) );
		
		drawText( canvas, player, xCoord, yCoord );
		
		return false;
	}

	@Override
	public boolean onInteract( Player player, double x, double y, MapInteraction interaction ) {
		return false;
	}
	
	private void drawText( MenuCanvas canvas, Player player, int x, int y ) {
		BananaTypeFont font = unicode ? module.getUnicodeFont(): module.getDefaultFont();
		int pos = this.x;
		int height = this.y;
		int index = 1;
		
		String message = module.translate( player, String.format( key, index++ ), x, y );
		while ( message != null && !message.isEmpty() ) {
			Color color = new Color( 171, 171, 171 );
			Color darker = color.darker().darker().darker();
			char[] chars = message.toCharArray();
			for ( int i = 0; i < chars.length; i++ ) {
				char c = chars[ i ];
				if ( c == '\n' ) {
					height += lineSpacing;
					pos = this.x;
				} else if ( c == '\t' ) {
					pos += tabDistance;
				} else if ( c == ' ' ) {
					pos += spaceDistance;
				} else if ( message.substring( i ).matches( "^§[0-9]{1,3};.*" ) ) {
					int newIndex = message.substring( i ).indexOf( ';' );
					int v = Integer.valueOf( message.substring( i + 1, i + newIndex ) );
					i += newIndex;
					color = new Color( JetpImageUtil.getColorFromMinecraftPalette( ( byte ) v ) );
					darker = color.darker().darker().darker();
				} else {
					PixelGlyph glyph = font.get( c );
					if ( glyph != null ) {
						for ( int ly = 0; ly < glyph.getHeight(); ly++ ) {
							int yIndex = ly * glyph.getWidth();
							for ( int lx = 0; lx < glyph.getWidth(); lx++ ) {
								if ( glyph.getData()[ yIndex + lx ] ) {
									int relX = pos + lx;
									int relY = height - ly;
									if ( between( relX, 0, 128 ) && between( relY, 0, 128 ) ) {
										canvas.setPixel( relX, relY, color );
									}
									
									if ( shadow && ( lx == glyph.getWidth() - 1 || ly == 0 || !glyph.getData()[ ( ly - 1 ) * glyph.getWidth() + lx + 1 ] ) ) {
										int shadowX = relX + 1;
										int shadowY = relY + 1;
										if ( between( shadowX, 0, 128 ) && between( shadowY, 0, 128 ) ) {
											canvas.setPixel( shadowX, shadowY, darker );
										}
									}
								}
							}
						}
						
						pos += glyph.getWidth();
					}
					pos += charDistance;
				}
			}
			
			message = module.translate( player, String.format( key, index++ ), x, y );
			height += lineSpacing;
			pos = this.x;
		}
	}
	
	private boolean between( int value, int lower, int higher ) {
		return value >= lower && value < higher;
	}
	
	public int getX() {
		return x;
	}

	public void setX( int x ) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY( int y ) {
		this.y = y;
	}

	public String getKey() {
		return key;
	}

	public void setKey( String key ) {
		this.key = key;
	}

	public boolean isUnicode() {
		return unicode;
	}

	public void setUnicode( boolean unicode ) {
		this.unicode = unicode;
	}

	public boolean isShadow() {
		return shadow;
	}

	public void setShadow( boolean shadow ) {
		this.shadow = shadow;
	}

	public int getTabDistance() {
		return tabDistance;
	}

	public void setTabDistance( int tabDistance ) {
		this.tabDistance = tabDistance;
	}

	public int getSpaceDistance() {
		return spaceDistance;
	}

	public void setSpaceDistance( int spaceDistance ) {
		this.spaceDistance = spaceDistance;
	}

	public int getLineSpacing() {
		return lineSpacing;
	}

	public void setLineSpacing( int lineSpacing ) {
		this.lineSpacing = lineSpacing;
	}

	public int getCharDistance() {
		return charDistance;
	}

	public void setCharDistance( int charDistance ) {
		this.charDistance = charDistance;
	}
}

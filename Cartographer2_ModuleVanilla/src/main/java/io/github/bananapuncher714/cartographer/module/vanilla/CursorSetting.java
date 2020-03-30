package io.github.bananapuncher714.cartographer.module.vanilla;

import org.bukkit.map.MapCursor.Type;

public class CursorSetting {
	private boolean showName;
	private boolean range;
	private CursorVisibility cursorVisibility;
	private Type cursorType;
	
	public boolean isShowName() {
		return showName;
	}
	
	public void setShowName( boolean showName ) {
		this.showName = showName;
	}
	
	public boolean isRange() {
		return range;
	}
	
	public void setRange( boolean range ) {
		this.range = range;
	}
	
	public void setCursorVisibility( CursorVisibility cursorVisibility ) {
		this.cursorVisibility = cursorVisibility;
	}

	public CursorVisibility getCursorVisibility() {
		return cursorVisibility;
	}

	public Type getCursorType() {
		return cursorType;
	}
	
	public void setCursorType( Type cursorType ) {
		this.cursorType = cursorType;
	}
}

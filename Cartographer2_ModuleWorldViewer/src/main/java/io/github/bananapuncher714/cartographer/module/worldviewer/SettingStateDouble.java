package io.github.bananapuncher714.cartographer.module.worldviewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.github.bananapuncher714.cartographer.core.api.setting.SettingState;

public class SettingStateDouble extends SettingState< Double > {
	private double def;
	private double[] suggestions;
	
	public SettingStateDouble( String id, boolean isPrivate, double def, double... suggestions ) {
		super( id, isPrivate, Double.class );
		this.def = def;
		this.suggestions = suggestions;
	}

	@Override
	public String convertToString( Double value ) {
		return value.toString();
	}

	@Override
	public Optional< Double > getFrom( String value ) {
		if ( value.matches( "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?" ) ) {
			return Optional.of( Double.valueOf( value ) );
		}
		return Optional.empty();
	}

	@Override
	public Collection< String > getValues() {
		List< String > strs = new ArrayList< String >();
		for ( double d : suggestions ) {
			strs.add( String.valueOf( d ) );
		}
		return strs;
	}

	@Override
	public Double getDefault() {
		return def;
	}
}

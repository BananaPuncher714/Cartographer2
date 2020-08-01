package io.github.bananapuncher714.cartographer.core.api.setting;

import java.util.Collection;
import java.util.Optional;

import io.github.bananapuncher714.cartographer.core.Cartographer;

public class SettingStateLocale extends SettingState< String > {
	public SettingStateLocale( String id, boolean isPrivate ) {
		super( id, isPrivate, String.class );
	}

	@Override
	public String convertToString( String value ) {
		return value;
	}

	@Override
	public Optional< String > getFrom( String value ) {
		return Optional.of( value.toLowerCase() );
	}

	@Override
	public Collection< String > getValues() {
		return Cartographer.getInstance().getLocaleManager().getLocaleCodes();
	}

	@Override
	public String getDefault() {
		return Cartographer.getInstance().getLocaleManager().getDefaultLocale();
	}
}

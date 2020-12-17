package io.github.bananapuncher714.cartographer.core.api.configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class YamlComment {
	protected List< String > comments = new ArrayList< String >();
	protected boolean divide = false;
	
	public YamlComment( List< String > comments ) {
		this.comments.addAll( comments );
	}
	
	public YamlComment( String... keys ) {
		for ( String s : keys ) {
			comments.add( s );
		}
	}

	public boolean isDivide() {
		return divide;
	}

	public YamlComment setDivide( boolean divide ) {
		this.divide = divide;
		return this;
	}

	public List< String > getComments() {
		return comments;
	}
	
	public boolean isEmpty() {
		return comments.isEmpty();
	}
	
	public String toIndentedString( int amount ) {
		StringBuilder builder = new StringBuilder();
		if ( !comments.isEmpty() ) {
			String indent = new String( new char[ amount ] ).replace( "\0", " " );
			if ( divide ) {
				builder.append( "\n" );
			}
			for ( Iterator< String > it = comments.iterator(); it.hasNext(); ) {
				builder.append( indent );
				builder.append( "#" );
				builder.append( it.next() );
				if ( it.hasNext() ) {
					builder.append( "\n" );
				}
			}
		}
		return builder.toString();
	}
	
	public YamlComment copyOf() {
		return new YamlComment( comments ).setDivide( divide );
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if ( divide ) {
			builder.append( "\n" );
		}
		for ( Iterator< String > it = comments.iterator(); it.hasNext(); ) {
			builder.append( "#" );
			builder.append( it.next() );
			if ( it.hasNext() ) {
				builder.append( "\n" );
			}
		}
		return builder.toString();
	}
}

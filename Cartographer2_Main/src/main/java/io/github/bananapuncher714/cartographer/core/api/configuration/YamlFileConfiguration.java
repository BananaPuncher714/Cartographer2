package io.github.bananapuncher714.cartographer.core.api.configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.events.AliasEvent;
import org.yaml.snakeyaml.events.DocumentEndEvent;
import org.yaml.snakeyaml.events.DocumentStartEvent;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.MappingEndEvent;
import org.yaml.snakeyaml.events.MappingStartEvent;
import org.yaml.snakeyaml.events.ScalarEvent;
import org.yaml.snakeyaml.events.SequenceEndEvent;
import org.yaml.snakeyaml.events.SequenceStartEvent;
import org.yaml.snakeyaml.events.StreamEndEvent;
import org.yaml.snakeyaml.events.StreamStartEvent;

public class YamlFileConfiguration {
	private static final String HEADER = "(?s)((?:^##.*?\r?\n)+)";
	
	protected Path file;
	protected YamlConfiguration config;
	protected Map< List< YamlKey >, YamlComment > comments;
	protected List< String > header;
	
	public YamlFileConfiguration( Path file ) {
		this.file = file;
		config = new YamlConfiguration();
	}
	
	public void load() throws FileNotFoundException, IOException, InvalidConfigurationException {
		load( Files.newInputStream( file ) );
	}
	
	public void load( InputStream stream ) throws IOException, InvalidConfigurationException {
		BufferedReader reader = new BufferedReader( new InputStreamReader( stream, StandardCharsets.UTF_8 ) );
		String content = reader.lines().collect( Collectors.joining( "\n" ) );
		reader.close();
		loadFromString( content );
	}
	
	public void loadFromString( String contents ) throws InvalidConfigurationException {
		header = new ArrayList< String >();
		Pattern pattern = Pattern.compile( HEADER, Pattern.MULTILINE );
		Matcher matcher = pattern.matcher( contents );
		if ( matcher.find() ) {
			String fullHeader = matcher.group( 1 );
			for ( String str : fullHeader.split( "\r?\n" ) ) {
				if ( str.startsWith( "##" ) ) {
					header.add( str.substring( 2 ) );
				}
			}
		}
		
		comments = fetchComments( contents );
		
		config.loadFromString( contents.replaceAll( "[\t ]*?#.*?\r?\n", "" ) );
		config.options().copyHeader( false );
		config.options().header( null );
	}
	
	public Path getFile() {
		return file;
	}
	
	public YamlConfiguration getConfiguration() {
		return config;
	}
	
	public List< String > getHeader() {
		return header;
	}

	public void setHeader( List< String > lines ) {
		header = new ArrayList< String >( lines );
	}

	public void setHeader( String... lines ) {
		header = Arrays.asList( lines );
	}
	
	public Collection< List< YamlKey > > getKeys() {
		return comments.keySet().stream().map( list -> { return list.stream().map( YamlKey::copyOf ).collect( Collectors.toList() ); } ).collect( Collectors.toSet() );
	}
	
	public YamlComment getComment( List< YamlKey > keys ) {
		return comments.get( keys );
	}
	
	public YamlComment getCommentExplicit( Object... keys ) {
		return getComment( convertToKeys( keys ) );
	}
	
	public YamlComment getOrCreateComment( List< YamlKey > keys ) {
		YamlComment comment = getComment( keys );
		if ( comment == null ) {
			comment = new YamlComment();
			setComment( keys, comment );
		}
		return comment;
	}
	
	public YamlComment getOrCreateCommentExplicit( Object... keys ) {
		return getOrCreateComment( convertToKeys( keys ) );
	}
	
	public YamlComment removeComment( List< YamlKey > keys ) {
		return comments.remove( keys );
	}
	
	public YamlComment removeCommentExplicit( Object... keys ) {
		return comments.remove( convertToKeys( keys ) );
	}
	
	public YamlComment setComment( List< YamlKey > keys, YamlComment comment ) {
		List< YamlKey > newKeys = new ArrayList< YamlKey >();
		for ( YamlKey key : keys ) {
			newKeys.add( key.copyOf() );
		}
		return comments.put( newKeys, comment );
	}
	
	public YamlComment setComment( YamlComment comment, Object... keys ) {
		return setComment( convertToKeys( keys ), comment );
	}
	
	public boolean hasComment( List< YamlKey > keys ) {
		return comments.containsKey( keys );
	}
	
	public boolean hasCommentExplicit( Object... keys ) {
		return hasComment( convertToKeys( keys ) );
	}
	
	public Map< List< YamlKey >, YamlComment > getComments() {
		return comments;
	}
	
	protected List< YamlKey > convertToKeys( Object[] keys ) {
		List< YamlKey > newKeys = new LinkedList< YamlKey >();
		for ( Object object : keys ) {
			YamlKey key = YamlKeyFactory.construct( object );
			newKeys.add( key );
		}
		return newKeys;
	}
	
	public void save( Path file ) throws IOException {
		Files.createDirectories( file.getParent() );
		BufferedWriter writer = Files.newBufferedWriter( file );
		writer.write( saveToString() );
		writer.close();
	}
	
	public void save() throws IOException {
		save( file );
	}
	
	public String saveToString() {
		config.options().header( null );
		String yaml = config.saveToString();
		yaml = insertComments( yaml, comments );
		
		StringBuilder builder = new StringBuilder();
		for ( String str : header ) {
			builder.append( "##" );
			builder.append( str );
			builder.append( "\n" );
		}
		builder.append( yaml );
		return builder.toString();
	}
	
	protected Map< List< YamlKey >, YamlComment > fetchComments( String contents ) {
		Map< List< YamlKey >, YamlComment > map = new HashMap< List< YamlKey >, YamlComment >();
		List< Event > parsed = toList( new Yaml().parse( new StringReader( contents ) ) );
		String[] separated = contents.split( "\r?\n" );
		int curX = 0;
		int curY = 0;
		
		Deque< YamlKey > keyQueue = new ArrayDeque< YamlKey >();
		YamlKey currentKey = null;
		for ( int i = 0; i < parsed.size(); i++ ) {
			Event event = parsed.get( i );
			
			if ( event instanceof StreamStartEvent ||
					event instanceof DocumentStartEvent ||
					event instanceof DocumentEndEvent ||
					event instanceof StreamEndEvent ) {
				continue;
			}
			
			if ( event instanceof MappingStartEvent ) {
				if ( currentKey != null ) {
					keyQueue.add( currentKey );
				}
				currentKey = null;
			} else if ( event instanceof SequenceStartEvent ) {
				if ( currentKey != null ) {
					keyQueue.add( currentKey );
				}
				currentKey = new YamlKeyInteger( 0 );
			} else if ( event instanceof MappingEndEvent || event instanceof SequenceEndEvent ) {
				YamlKey lastKey = keyQueue.pollLast();
				currentKey = null;
				if ( lastKey instanceof YamlKeyInteger ) {
					currentKey = new YamlKeyInteger( ( ( YamlKeyInteger ) lastKey ).index + 1 );
				}
			} else if ( event instanceof ScalarEvent ) {
				ScalarEvent scalar = ( ScalarEvent ) event;
				Mark start = event.getStartMark();
				if ( currentKey == null ) {
					currentKey = new YamlKeyString( scalar.getValue() );
					
					YamlComment comments = getComments( separated, curX, curY, start.getColumn(), start.getLine() );
					if ( !comments.isEmpty() || comments.isDivide() ) {
						List< YamlKey > keys = new ArrayList< YamlKey >( keyQueue );
						keys.add( currentKey );
						map.put( keys, comments );
					}
				} else if ( currentKey instanceof YamlKeyString ) {
					currentKey = null;
				} else if ( currentKey instanceof YamlKeyInteger ) {
					YamlKeyInteger currentKeyInt = ( YamlKeyInteger ) currentKey;
					
					YamlComment comments = getComments( separated, curX, curY, start.getColumn(), start.getLine() );
					if ( !comments.isEmpty() || comments.isDivide() ) {
						List< YamlKey > keys = new ArrayList< YamlKey >( keyQueue );
						keys.add( currentKey );
						map.put( keys, comments );
					}
					
					currentKey = new YamlKeyInteger( currentKeyInt.index + 1 );
				}
				curX = event.getEndMark().getColumn();
				curY = event.getEndMark().getLine();
			} else if ( event instanceof AliasEvent ) {
				if ( currentKey instanceof YamlKeyString ) {
					currentKey = null;
				} else if ( currentKey instanceof YamlKeyInteger ) {
					Mark start = event.getStartMark();
					YamlKeyInteger currentKeyInt = ( YamlKeyInteger ) currentKey;
					
					YamlComment comments = getComments( separated, curX, curY, start.getColumn(), start.getLine() );
					if ( !comments.isEmpty() || comments.isDivide() ) {
						List< YamlKey > keys = new ArrayList< YamlKey >( keyQueue );
						keys.add( currentKey );
						map.put( keys, comments );
					}
					
					currentKey = new YamlKeyInteger( currentKeyInt.index + 1 );
				}
				
				curX = event.getEndMark().getColumn();
				curY = event.getEndMark().getLine();
			}
		}
		
		return map;
	}
	
	protected String insertComments( String yaml, Map< List< YamlKey >, YamlComment > comments ) {
		List< Event > parsed = toList( new Yaml().parse( new StringReader( yaml ) ) );
		String[] separated = yaml.split( "\r?\n" );
		List< String > values = new LinkedList< String >();
		for ( String str : separated ) {
			values.add( str );
		}
		
		int offset = 0;
		Deque< YamlKey > keyQueue = new ArrayDeque< YamlKey >();
		YamlKey currentKey = null;
		for ( int i = 0; i < parsed.size(); i++ ) {
			Event event = parsed.get( i );
			
			if ( event instanceof StreamStartEvent ||
					event instanceof DocumentStartEvent ||
					event instanceof DocumentEndEvent ||
					event instanceof StreamEndEvent ) {
				continue;
			}
			
			if ( event instanceof MappingStartEvent ) {
				if ( currentKey != null ) {
					keyQueue.add( currentKey );
				}
				currentKey = null;
			} else if ( event instanceof SequenceStartEvent ) {
				if ( currentKey != null ) {
					keyQueue.add( currentKey );
				}
				currentKey = new YamlKeyInteger( 0 );
			} else if ( event instanceof MappingEndEvent || event instanceof SequenceEndEvent ) {
				YamlKey lastKey = keyQueue.pollLast();
				currentKey = null;
				if ( lastKey instanceof YamlKeyInteger ) {
					currentKey = new YamlKeyInteger( ( ( YamlKeyInteger ) lastKey ).index + 1 );
				}
			} else if ( event instanceof ScalarEvent ) {
				ScalarEvent scalar = ( ScalarEvent ) event;
				Mark start = event.getStartMark();
				if ( currentKey == null ) {
					currentKey = new YamlKeyString( scalar.getValue() );
					
					List< YamlKey > keys = new ArrayList< YamlKey >( keyQueue );
					keys.add( currentKey );
					YamlComment comment = comments.get( keys );
					if ( comment != null ) {
						values.add( start.getLine() + offset++, comment.toIndentedString( start.getColumn() ) );
					}
				} else if ( currentKey instanceof YamlKeyString ) {
					currentKey = null;
				} else if ( currentKey instanceof YamlKeyInteger ) {
					YamlKeyInteger currentKeyInt = ( YamlKeyInteger ) currentKey;
					
					List< YamlKey > keys = new ArrayList< YamlKey >( keyQueue );
					keys.add( currentKey );
					YamlComment comment = comments.get( keys );
					if ( comment != null ) {
						values.add( start.getLine() + offset++, comment.toIndentedString( start.getColumn() ) );
					}
					
					currentKey = new YamlKeyInteger( currentKeyInt.index + 1 );
				}
			} else if ( event instanceof AliasEvent ) {
				if ( currentKey instanceof YamlKeyString ) {
					currentKey = null;
				} else if ( currentKey instanceof YamlKeyInteger ) {
					Mark start = event.getStartMark();
					YamlKeyInteger currentKeyInt = ( YamlKeyInteger ) currentKey;
					
					List< YamlKey > keys = new ArrayList< YamlKey >( keyQueue );
					keys.add( currentKey );
					YamlComment comment = comments.get( keys );
					if ( comment != null ) {
						values.add( start.getLine() + offset++, comment.toIndentedString( start.getColumn() ) );
					}
					
					currentKey = new YamlKeyInteger( currentKeyInt.index + 1 );
				}
			}
		}
		
		return values.stream().collect( Collectors.joining( "\n" ) );
	}
	
	protected YamlComment getComments( String[] contents, int x, int y, int endx, int endy ) {
		YamlComment comment = new YamlComment();
		for ( int i = y; i < endy; i++ ) {
			String line = contents[ i ];
			int sub = line.indexOf( "#" );
			if ( sub > -1 ) {
				String value = line.substring( sub + 1 );
				if ( !value.startsWith( "#" ) ) {
					comment.getComments().add( value );
				}
			} else if ( line.matches( "\\s*" ) ) {
				comment.setDivide( true );
			}
		}
		return comment;
	}
	
	protected static < T > List< T > toList( Iterable< T > iter ) {
		List< T > list = new ArrayList< T >();
		
		for ( T t : iter ) {
			list.add( t );
		}
		
		return list;
	}
}


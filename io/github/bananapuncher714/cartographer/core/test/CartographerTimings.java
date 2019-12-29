package io.github.bananapuncher714.cartographer.core.test;

import java.util.HashMap;
import java.util.Map;

public class CartographerTimings {
	private Map< String, TimingSection > sections = new HashMap< String, TimingSection >();
	
	public TimingSection getSection( String name ) {
		TimingSection section = sections.get( name );
		if ( section == null ) {
			section = new TimingSection();
			sections.put( name, section );
		}
		return section;
	}
	
	public void printSections() {
		System.out.println( "TIMINGS START" );
		for ( String name : sections.keySet() ) {
			TimingSection section = sections.get( name );
			long min = section.min();
			long max = section.max();
			double avg = section.average();
			long size = section.samples();
			
			System.out.println( "Section " + name + ": min:" + min + " max:" + max + " size:" + size + " avg:" + avg );
		}
		System.out.println( "TIMINGS END" );
	}
	
	public void reset() {
		sections.clear();
	}
}

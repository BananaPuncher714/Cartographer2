package io.github.bananapuncher714.cartographer.core.test;

public class TimingSection {
	private long total;
	private long min = Long.MAX_VALUE;
	private long max;
	private long start;
	private long count;
	
    public void start() {
		start = System.currentTimeMillis();
	}
	
	public void stop() {
		long time = System.currentTimeMillis() - start;
		total += time;
		count++;
		min = Math.min( min, time );
		max = Math.max( max, time );
	}
	
	public double average() {
		return total / ( double ) count;
	}
	
	public long min() {
		return min;
	}
	
	public long max() {
		return max;
	}
	
	public long samples() {
		return count;
	}
}

package io.github.bananapuncher714.cartographer.core.util;

/**
 * Riven's fast sin and cos with lookup tables.
 * Find the original at http://www.java-gaming.org/topics/fast-math-sin-cos-lookup-tables/24191/view.html
 */
public class RivenMath {
	   public static final float sin(float rad)
	   {
	      return sin[(int) (rad * radToIndex) & SIN_MASK];
	   }

	   public static final float cos(float rad)
	   {
	      return cos[(int) (rad * radToIndex) & SIN_MASK];
	   }

	   public static final float sinDeg(float deg)
	   {
	      return sin[(int) (deg * degToIndex) & SIN_MASK];
	   }

	   public static final float cosDeg(float deg)
	   {
	      return cos[(int) (deg * degToIndex) & SIN_MASK];
	   }

	   private static final float   RAD,DEG;
	   private static final int     SIN_BITS,SIN_MASK,SIN_COUNT;
	   private static final float   radFull,radToIndex;
	   private static final float   degFull,degToIndex;
	   private static final float[] sin, cos;

	   static
	   {
	      RAD = (float) Math.PI / 180.0f;
	      DEG = 180.0f / (float) Math.PI;

	      SIN_BITS  = 12;
	      SIN_MASK  = ~(-1 << SIN_BITS);
	      SIN_COUNT = SIN_MASK + 1;

	      radFull    = (float) (Math.PI * 2.0);
	      degFull    = (float) (360.0);
	      radToIndex = SIN_COUNT / radFull;
	      degToIndex = SIN_COUNT / degFull;

	      sin = new float[SIN_COUNT];
	      cos = new float[SIN_COUNT];

	      for (int i = 0; i < SIN_COUNT; i++)
	      {
	         sin[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * radFull);
	         cos[i] = (float) Math.cos((i + 0.5f) / SIN_COUNT * radFull);
	      }

	      // Four cardinal directions (credits: Nate)
	      for (int i = 0; i < 360; i += 90)
	      {
	         sin[(int)(i * degToIndex) & SIN_MASK] = (float)Math.sin(i * Math.PI / 180.0);
	         cos[(int)(i * degToIndex) & SIN_MASK] = (float)Math.cos(i * Math.PI / 180.0);
	      }
	   }
}

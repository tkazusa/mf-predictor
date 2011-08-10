package jp.ndca.recommend.common.util;

public class PrimitiveArrayHandler {

	public static double dot( double[] a, double[] b ){
		if(a.length != b.length )
			throw new IllegalArgumentException("array size different !");
		double value = 0.0d;
		for( int i = 0 ; i < b.length ; i++ )
			value += a[i] * b[i];
		return value;
	}
	       
	public static void normalize( double[] a ){
		double total = 0.0d;
		for( int i = 0 ; i < a.length ; i++ )
			total += a[i];
		for( int i = 0 ; i < a.length ; i++ )
			a[i] /= total;
	}
	
	public static double square( double[] a ){
		double value = 0.0d;
		for( int i = 0 ; i < a.length ; i++ )
			value += a[i] * a[i];
		return value;
	}

	public static void init( int[] a, int value ){
		for( int i = 0 ; i < a.length ; i++ )
			a[i] = value;
	}
	
	public static void init( double[] a, double value ){
		for( int i = 0 ; i < a.length ; i++ )
			a[i] = value;
	}

	public static void init( int[][] a, int value ){
		for( int i = 0 ; i < a.length ; i++ )
			for( int j = 0 ; j < a[0].length ; j++ )
				a[i][j] = value;
	}

	public static void init( double[][] a, double value ){
		for( int i = 0 ; i < a.length ; i++ )
			for( int j = 0 ; j < a[0].length ; j++ )
				a[i][j] = value;
	}
}

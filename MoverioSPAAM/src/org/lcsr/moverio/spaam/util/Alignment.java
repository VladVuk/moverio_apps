/*
 *  Alignment.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.spaam.util;

import java.util.List;

import android.util.Log;
import Jama.Matrix;

public class Alignment {
	private static String TAG = "Alignment";
	
	// 3*1 homogeneous representation of screen point
	public Matrix screenPoint = null;
	
	// 4*1 homogeneous representation of space point
	public Matrix spacePoint = null;
	
	// 3*1 homogeneous representation of screen point recovered
	public Matrix screenPointAligned = null;
		
	public enum PointType { Screen, Space};
	
	public Alignment(int X, int Y, Matrix sp){
		if ( sp != null ) {
			screenPoint = new Matrix(3, 1);
			screenPoint.set(0, 0, Double.valueOf(X));
			screenPoint.set(1, 0, Double.valueOf(Y));
			screenPoint.set(2, 0, 1.0);
			spacePoint = sp.copy();
		}
	}
	
	public Alignment (Matrix sc, Matrix sp) {
		if ( sc != null && sp != null ) {
			if ( sc.getColumnDimension() == 1 && sc.getRowDimension() == 3 &&
					sp.getColumnDimension() == 1 && sp.getRowDimension() == 4 )
				screenPoint = sc.copy();
				spacePoint = sp.copy();
		}
	}
	
	private void printAlignmentInfo() {
		Log.i(TAG, screenPoint.get(0, 0) + " - " + screenPointAligned.get(0, 0)
				+ " | " + screenPoint.get(1, 0) + " - " + screenPointAligned.get(1, 0)
				+ " | " + screenPoint.get(2, 0) + " - " + screenPointAligned.get(2, 0));
	}
	
	public void setPointAligned( Matrix sc ) {
		if ( sc != null ) {
			if ( sc.getColumnDimension() == 1 && sc.getRowDimension() == 3 ) {
				screenPointAligned = sc.copy();
			}
			printAlignmentInfo();
		}
	}
	
	public void setPointAligned( int X, int Y ) {
		Matrix m = new Matrix (3, 1);
		m.set(0, 0, Double.valueOf(X));
		m.set(1, 0, Double.valueOf(Y));
		m.set(2, 0, 1.0);
		setPointAligned( m );
	}
	
	public static double avg( List<Alignment> alignPoints, int index, PointType type ) {
		double num = alignPoints.size();
		if ( num == 0.0 )
			return 0.0;
		else
			return sum(alignPoints, index, type) / num;
	}
	
	public static double sum( List<Alignment> alignPoints, int index, PointType type ) {
		double total = 0.0;
		if ( alignPoints.size() == 0 )
			return 0.0;
		if ( type == PointType.Screen ) {
			if ( index < 0 || index >= 2 )
				return 0.0;
			else {
				for ( Alignment a : alignPoints )
					total += a.screenPoint.get(index, 0);	
				return total;			
			}
		}
		else { // Space
			if ( index < 0 || index >= 3 )
				return 0.0;
			else {
				for ( Alignment a : alignPoints )
					total += a.spacePoint.get(index, 0);
				return total;			
			}
		}
	}
	
	public static double norm( List<Alignment> alignPoints, int index, PointType type ) {
		double total = 0.0;
		if ( alignPoints.size() == 0 )
			return 0.0;
		if ( type == PointType.Screen ) {
			if ( index < 0 || index >= 2 )
				return 0.0;
			else {
				double temp;
				for ( Alignment a : alignPoints ) {
					temp = a.screenPoint.get(index, 0);
					total += temp * temp;
				}
				return total;			
			}
		}
		else { // Space
			if ( index < 0 || index >= 3 )
				return 0.0;
			else {
				double temp;
				for ( Alignment a : alignPoints ) {
					temp = a.spacePoint.get(index, 0);	
					total += temp * temp;
				}
				return total;
			}
		}
	}
	
}

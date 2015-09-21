/*
 *  VisualTracker.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.spaam.util;

import org.artoolkit.ar.base.ARToolKit;
import Jama.Matrix;
import android.util.Log;

public class VisualTracker {

	private static final String TAG = "VisualTracker";
	private int markerID = -1;
	
	private static VisualTracker vInstance;
	
	public static VisualTracker getInstance() {
		return vInstance;
	}
	
	public VisualTracker() {
		Log.i(TAG, "Dummy VisualTracker object created");
		vInstance = this;
	}
	
	public void setMarker(String param) {
		markerID = ARToolKit.getInstance().addMarker(param);
		if (markerID >= 0) {
			Log.i(TAG, "Marker added");
		}
	}

	public Matrix getMarkerTransformation() {
    	if (markerID == -1)
    		return null;
    	
    	if (ARToolKit.getInstance().queryMarkerVisible(markerID)) {
    		float[] floatArray = ARToolKit.getInstance().queryMarkerTransformation(markerID);
			double[] toDouble = new double[16];
			for (int i = 0; i < 16; i++ )
				toDouble[i] = Double.valueOf(floatArray[i]);
			return new Matrix(toDouble, 4);
    	}
    	else
    		return null;
    }
	
	public float[] getMarkerTransformationGL() {
    	if (markerID == -1)
    		return null;
    	
    	if (ARToolKit.getInstance().queryMarkerVisible(markerID)) {
    		return ARToolKit.getInstance().queryMarkerTransformation(markerID);
    	}
    	else
    		return null;
    }
	
	public boolean getMarkerVisibility() {
		return ARToolKit.getInstance().queryMarkerVisible(markerID);
	}
}

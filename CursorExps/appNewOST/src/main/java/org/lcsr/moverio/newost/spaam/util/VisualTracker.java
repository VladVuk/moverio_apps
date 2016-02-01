/*
 *  VisualTracker.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.newost.spaam.util;

import org.artoolkit.ar.base.ARToolKit;
import Jama.Matrix;
import android.util.Log;

public class VisualTracker {

	private static final String TAG = "VisualTracker";
	private int markerID = -1;

    // visibility is for temporary storing, safety not guaranteed
    // can be used right after getTransformation function is called
	public boolean visibility = false;

	public VisualTracker() {
		Log.i(TAG, "Object constructed");
	}
	
	public void setMarker(String param) {
		markerID = ARToolKit.getInstance().addMarker(param);
		if (markerID >= 0) {
			Log.i(TAG, "Marker added");
		}
	}

	public Matrix getMarkerTransformation() {
    	if (markerID == -1) {
    		visibility = false;
    		return null;
    	}
    	
    	if (ARToolKit.getInstance().queryMarkerVisible(markerID)) {
    		float[] floatArray = ARToolKit.getInstance().queryMarkerTransformation(markerID);
			double[] toDouble = new double[16];
			for (int i = 0; i < 16; i++ )
				toDouble[i] = Double.valueOf(floatArray[i]);
    		visibility = true;
			return new Matrix(toDouble, 4);
    	}
    	else {
    		visibility = false;
    		return null;
    	}
	}
	
	public float[] getMarkerTransformationGL() {
    	if (markerID == -1) {
			visibility = false;
			return null;
		}
    	
    	if (ARToolKit.getInstance().queryMarkerVisible(markerID)) {
			visibility = true;
    		return ARToolKit.getInstance().queryMarkerTransformation(markerID);
    	}
    	else {
			visibility = false;
			return null;
		}
    }
	
	public boolean getMarkerVisibility() {
		visibility = ARToolKit.getInstance().queryMarkerVisible(markerID);
	    return visibility;
    }
}

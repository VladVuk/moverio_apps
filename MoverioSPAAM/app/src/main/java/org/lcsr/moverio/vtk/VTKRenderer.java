package org.lcsr.moverio.vtk;

import javax.microedition.khronos.opengles.GL10;

import org.medcare.igtl.util.PolyData;

import android.util.Log;

public class VTKRenderer {
	private static String TAG = "VTKRenderer";
	
	PolyData polydata;
	
	public void setPolyData(PolyData pd) {
		polydata = pd;
		prepareRenderer();
	}
	
	public void prepareRenderer() {
		if ( polydata == null ) {
			Log.i(TAG, "PolyData not set");
			return;
		}
		
	}
	
	public void draw(GL10 gl) {
		
	}
}

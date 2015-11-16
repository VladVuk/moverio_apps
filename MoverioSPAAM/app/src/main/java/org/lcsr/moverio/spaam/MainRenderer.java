/*
 *  MainRenderer.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.spaam;

import javax.microedition.khronos.opengles.GL10;
//import android.opengl.GLES20;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.lcsr.moverio.spaam.util.OpenGLRenderer;
import org.lcsr.moverio.spaam.util.VisualTracker;
import org.lcsr.moverio.vtk.VTKRenderer;

import android.util.Log;


public class MainRenderer extends ARRenderer {

	private static String TAG = "MainRenderer";
	
	private OpenGLRenderer glRenderer = null;
	private VTKRenderer vtkRenderer = null;
	private VisualTracker visualTracker = null;
	
    @Override
    public boolean configureARScene() {
    	visualTracker = VisualTracker.getInstance();
        visualTracker.setMarker("single;Data/patt.hiro;80");
        glRenderer = new OpenGLRenderer(visualTracker);
        vtkRenderer = new VTKRenderer();
		Log.i(TAG, "ARScene configured");
		return true;
    }
    
    public void draw(GL10 gl) {
    	gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
    	if ( visualTracker.visibility ) {
	    	glRenderer.draw(gl);
//	    	vtkRenderer.draw(gl);
    	}
//    	else
//    		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
    }
}

package org.lcsr.moverio.spaam;

import javax.microedition.khronos.opengles.GL10;
import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.artoolkit.ar.base.rendering.Cube;
import org.lcsr.moverio.spaam.util.VisualTracker;
import android.util.Log;


/**
 * A simple Renderer that adds a marker and draws a spinning cube on it. The spinning is toggled 
 * in the {@link click} method, which is called from the activity when the user taps the screen.
 */
public class MainRenderer extends ARRenderer {

	private static String TAG = "MainRenderer";
	
	private Cube cube = new Cube(40.0f, 0.0f, 0.0f, 20.0f);
	
	private VisualTracker visualTracker = null;
	

    /**
     * By overriding {@link configureARScene}, the markers and other settings can be configured
     * after the native library is initialised, but prior to the rendering actually starting.
     */
    @Override
    public boolean configureARScene() {
    	visualTracker = VisualTracker.getInstance();
        visualTracker.setMarker("single;Data/patt.hiro;80");
		Log.i(TAG, "ARScene configured");
		return true;
    }
        	
    public void draw(GL10 gl) {
    	
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadMatrixf(ARToolKit.getInstance().getProjectionMatrix(), 0);
        
		gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);        
    	gl.glFrontFace(GL10.GL_CW);
		
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        
        float[] mat = VisualTracker.getInstance().getMarkerTransformationGL();
        if ( mat != null ) {        
        	gl.glLoadMatrixf(mat, 0);
        	cube.draw(gl);
        }
    }
}

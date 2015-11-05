/*
 *  OpenGLRenderer.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.spaam.util;

import javax.microedition.khronos.opengles.GL10;
import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.rendering.Cube;

public class OpenGLRenderer {
	
	private Cube cube = new Cube(40.0f, 0.0f, 0.0f, 20.0f);
	
	private VisualTracker visualTracker;
	
	public OpenGLRenderer(VisualTracker vt) {
		visualTracker = vt;
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
        
        float[] mat = visualTracker.getMarkerTransformationGL();
        if ( mat != null ) {
        	gl.glLoadMatrixf(mat, 0);
        	cube.draw(gl);
        }
    }
}

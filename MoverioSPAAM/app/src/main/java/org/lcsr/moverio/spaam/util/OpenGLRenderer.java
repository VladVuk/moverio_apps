/*
 *  OpenGLRenderer.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.spaam.util;


import android.util.Log;

import javax.microedition.khronos.opengles.GL10;
import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.rendering.Cube;

import Jama.Matrix;

public class OpenGLRenderer {
	
	private Cube cube = new Cube(0.04f, 0.0f, 0.0f, 0.02f);
    private static String TAG = "OpenGLRenderer";
	
	private VisualTracker visualTracker;
	
	public OpenGLRenderer(VisualTracker vt) {
		visualTracker = vt;
	}

//	public void draw(GL10 gl) {
//
//        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
//
//        gl.glMatrixMode(GL10.GL_PROJECTION);
//        gl.glLoadMatrixf(ARToolKit.getInstance().getProjectionMatrix(), 0);
//
//		gl.glEnable(GL10.GL_CULL_FACE);
//        gl.glShadeModel(GL10.GL_SMOOTH);
//        gl.glEnable(GL10.GL_DEPTH_TEST);
//    	gl.glFrontFace(GL10.GL_CW);
//
//        gl.glMatrixMode(GL10.GL_MODELVIEW);
//
//        float[] mat = visualTracker.getMarkerTransformationGL();
//
//        if ( mat != null ) {
//        	gl.glLoadMatrixf(mat, 0);
//        	cube.draw(gl);
//        }
//    }

	public void draw(GL10 gl) {

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glViewport(0, 0, 640, 480);

        float[] proj = {8.29109954834f, 0.0f, 0.0f, 0.0f, 0.0868728935719f, -10.1726918538f, 0.0f, 0.0f, -0.395378828049f, -2.92362594604f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f};
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadMatrixf(proj, 0);

		gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glDisable(GL10.GL_DEPTH_TEST);
    	gl.glFrontFace(GL10.GL_CW);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        float[] MOV = {0.981557f, 0.0390133f, -0.187148f, 0.0f, -0.014927f, -0.960326f, -0.278481f, 0.0f, -0.190587f, 0.276138f, -0.942032f, 0.0f, 0.0443321f, 0.0109995f, 0.0000315559f, 1.0f};
        gl.glLoadMatrixf(MOV, 0);

        float[] mat = visualTracker.getMarkerTransformationGL();

        if ( mat != null ) {
//            Log.i(TAG, "trans1: " + mat[0] + ", " + mat[4] + ", " + mat[8] + ", " + mat[12]);
//            Log.i(TAG, "trans2: " + mat[1] + ", " + mat[5] + ", " + mat[9] + ", " + mat[13]);
//            Log.i(TAG, "trans3: " + mat[2] + ", " + mat[6] + ", " + mat[10] + ", " + mat[14]);
//            Log.i(TAG, "trans4: " + mat[3] + ", " + mat[7] + ", " + mat[11] + ", " + mat[15]);
            mat[12] = mat[12]/1000.0f;
            mat[13] = mat[13]/1000.0f;
            mat[14] = mat[14]/1000.0f;
            gl.glMultMatrixf(mat, 0);
        	cube.draw(gl);
        }
    }
}

/*
 *  MainRenderer.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.spaam;

import javax.microedition.khronos.opengles.GL10;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.artoolkit.ar.base.rendering.Cube;
import org.lcsr.moverio.spaam.util.SPAAM;
import org.lcsr.moverio.spaam.util.VisualTracker;
import Jama.*;

import android.util.Log;


public class MainRenderer extends ARRenderer {

	private static String TAG = "MainRenderer";
    private VisualTracker visualTracker;

    // unit for object is millimeter
    private Cube cube = new Cube(40f, 0.0f, 0.0f, 20f);
    // unit for width and height is pixel
    private int width = 640;
    private int height = 480;
    private float widthf = 640f;
    private float heightf = 480f;
    // unit for clipping plane distance is in millimeter
    private float zNear = 0.1f;
    private float zFar = 1000.0f;

    private float[] glProj =
                            {2.25287802167f, -0.0710149988668f, 0.164145578453f, -0.000164112753899f,
                            -0.105387765071f, -2.28596066826f, 0.244253093084f, -0.000244204249253f,
                            -0.596516092764f, -0.186700628568f, 0.82624725921f, -0.00082608203272f,
                            103.416115863f, 23.5753742276f, 199.972322633f, 0.0000276718324112f};

    private Matrix Util = null;


    public MainRenderer(VisualTracker vs) {
        super();
        visualTracker = vs;
        updateUtilMat();
        Log.i(TAG, "Object constructed");
    }


    public void setWindowGeometry(int w, int h) {
        width = w;
        height = h;
        widthf = (float)w;
        heightf = (float)h;
        Log.i(TAG, "Window geometry set to (" + w + ", " + h + ")");
    }

    public void setClippingPlane(float n, float f) {
        zNear = n;
        zFar = f;
        updateUtilMat();
        Log.i(TAG, "Clipping plane set to " + zNear + "and" + zFar);
    }

    public void updateUtilMat() {
        Util = new Matrix(4,3);
        Util.set(0, 0, 1.0);
        Util.set(1, 1, 1.0);
        Util.set(3, 2, 1.0);
        Util.set(2, 2, -zNear - zFar);
    }

    public void updateCalibMat( Matrix M ) {
        if ( M == null) {
            glProj = null;
        }
        else {
//            Log.i(TAG, "M1: " + M.get(0, 0) + ", " + M.get(0, 1) + ", " + M.get(0, 2) + ", " + M.get(0, 3));
//            Log.i(TAG, "M2: " + M.get(1, 0) + ", " + M.get(1, 1) + ", " + M.get(1, 2) + ", " + M.get(1, 3));
//            Log.i(TAG, "M3: " + M.get(2, 0) + ", " + M.get(2,1) + ", " + M.get(2,2) + ", " + M.get(2, 3));
            Matrix P = Util.times(M);
            if (M.get(0,3) < 0 && M.get(1,3) < 0)
                P = P.times(-1.0);
            P.set(2, 3, P.get(2, 3) + zFar * zNear);
            Log.i(TAG, "P1: " + P.get(0,0) + ", " + P.get(0,1) + ", " + P.get(0,2) + ", " + P.get(0,3));
            Log.i(TAG, "P2: " + P.get(1,0) + ", " + P.get(1,1) + ", " + P.get(1,2) + ", " + P.get(1,3));
            Log.i(TAG, "P3: " + P.get(2,0) + ", " + P.get(2,1) + ", " + P.get(2,2) + ", " + P.get(2,3));
            Log.i(TAG, "P4: " + P.get(3,0) + ", " + P.get(3,1) + ", " + P.get(3,2) + ", " + P.get(3,3));
            glProj = Matrix2GLArray(P);
        }
    }

    public float[] Matrix2GLArray(Matrix M) {
        int col = M.getColumnDimension();
        int row =  M.getRowDimension();
        float[] fa = new float[col*row];
        for ( int i = 0; i < col; i++ ) {
            for ( int j = 0; j < row; j++ ) {
                fa[i*row+j] = (float)(M.get(j,i));
            }
        }
        return fa;
    }

	
    @Override
    public boolean configureARScene() {
        visualTracker.setMarker("single;Data/patt.hiro;80");
		Log.i(TAG, "ARScene configured");
		return true;
    }
    
    public void draw(GL10 gl) {
    	gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        float[] trans = visualTracker.getMarkerTransformationGL();
    	if ( trans != null && glProj != null ) {
//            Log.i(TAG, "glProj1: " + glProj[0] + ", " + glProj[1] + ", " + glProj[2] + ", " + glProj[3]);
//            Log.i(TAG, "glProj2: " + glProj[4] + ", " + glProj[5] + ", " + glProj[6] + ", " + glProj[7]);
//            Log.i(TAG, "glProj3: " + glProj[8] + ", " + glProj[9] + ", " + glProj[10] + ", " + glProj[11]);
//            Log.i(TAG, "glProj4: " + glProj[12] + ", " + glProj[13] + ", " + glProj[14] + ", " + glProj[15]);
            gl.glViewport(0, 0, width, height);

            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glOrthof(0.0f, widthf, heightf, 0.0f, zNear, zFar);
            gl.glMultMatrixf(glProj, 0);

            gl.glEnable(GL10.GL_CULL_FACE);
            gl.glShadeModel(GL10.GL_SMOOTH);
            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glFrontFace(GL10.GL_CW);

            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadMatrixf(trans, 0);
            cube.draw(gl);
        }
    }
}

package org.lcsr.moverio.newvst.spaam;

import android.util.Log;

import javax.microedition.khronos.opengles.GL10;

import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.artoolkit.ar.base.rendering.Cube;
import org.lcsr.moverio.newvst.spaam.util.VisualTracker;

import Jama.Matrix;

/**
 * A very simple Renderer that adds a marker and draws a cube on it.
 */
public class SimpleRenderer extends ARRenderer {

    private int markerID = -1;
    private Cube cube = new Cube(40.0f, 0.0f, 0.0f, 20.0f);
    private final static String TAG = "SimpleRenderer";
    private VisualTracker visualTracker;
    private Matrix mProj = null;
    private float[] glProj = null;

    public SimpleRenderer(VisualTracker vt){
        visualTracker = vt;
    }

    /**
     * Markers can be configured here.
     */
    @Override
    public boolean configureARScene() {
        markerID = visualTracker.setMarker("single;Data/patt.hiro;80");
        Log.i(TAG, "ARScene configured");
        return true;
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

    public Matrix GLArray2Matrix(float[] fa, int row, int col) {
        Matrix m = new Matrix(row, col);
        for ( int i = 0; i < col; i++ ) {
            for ( int j = 0; j < row; j++ ) {
                m.set(j, i, fa[i*row+j]);
            }
        }
        return m;
    }

    /**
     * Override the draw function from ARRenderer.
     */
    @Override
    public void draw(GL10 gl) {

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        // Apply the ARToolKit projection matrix
        gl.glMatrixMode(GL10.GL_PROJECTION);


        if ( mProj == null ) {
            glProj = ARToolKit.getInstance().getProjectionMatrix();
            mProj = GLArray2Matrix(glProj, 4, 4);
        }

        gl.glLoadMatrixf(glProj, 0);

        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glFrontFace(GL10.GL_CW);

        // If the marker is visible, apply its transformation, and draw a cube
        if (ARToolKit.getInstance().queryMarkerVisible(markerID)) {
            gl.glMatrixMode(GL10.GL_MODELVIEW);

            gl.glLoadMatrixf(ARToolKit.getInstance().queryMarkerTransformation(markerID), 0);

//            if (mProj != null ) {
//                mModelv = GLArray2Matrix(modelv, 4, 4);
//
//                ndc = mProj.times(mModelv.times(point));
//                ndc = ndc.times(1.0 / ndc.get(3, 0));
//
//                x = (int)(640.0 * (ndc.get(0,0) + 1.0) / 2.0);
//                y = (int)(480.0 * (-ndc.get(1,0) + 1.0) / 2.0);
//
//                Log.i(TAG, "Pixel x: " + x + ", y: " + y);
//            }
//            cube.draw(gl);
        }

//        Log.i(TAG, "glProj1: " + glProj[0] + ", " + glProj[1] + ", " + glProj[2] + ", " + glProj[3]);
//        Log.i(TAG, "glProj2: " + glProj[4] + ", " + glProj[5] + ", " + glProj[6] + ", " + glProj[7]);
//        Log.i(TAG, "glProj3: " + glProj[8] + ", " + glProj[9] + ", " + glProj[10] + ", " + glProj[11]);
//        Log.i(TAG, "glProj4: " + glProj[12] + ", " + glProj[13] + ", " + glProj[14] + ", " + glProj[15]);

    }

    public Matrix getmProj(){
        return mProj;
    }

}
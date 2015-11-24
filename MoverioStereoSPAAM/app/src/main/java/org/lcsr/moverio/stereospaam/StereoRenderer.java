package org.lcsr.moverio.stereospaam;

import android.content.Context;
import android.opengl.GLES10;
import android.util.Log;
import android.view.MotionEvent;

import org.artoolkit.ar.base.rendering.ARRenderer;
import org.lcsr.moverio.stereospaam.stereo.*;
import org.lcsr.moverio.stereospaam.util.SPAAM;
import org.lcsr.moverio.stereospaam.util.SPAAMConsole;
import org.lcsr.moverio.stereospaam.util.VisualTracker;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

import Jama.Matrix;

/**
 * Created by qian on 11/20/15.
 */
public class StereoRenderer extends ARRenderer {
    private static String TAG = "StereoRenderer";
    private Matrix T;
    private float[] glTrans;
    private SPAAMConsole spaam;
    private VisualTracker visualTracker;

    Cube cube;
    int surfaceWidth, surfaceHeight;

    private FrameBuffer left, right;
    private Context context;
    private float zNear;
    private float zFar;
    private Matrix Util = null;
    private float[] glProjLeft, glProjRight;

    private boolean semaphore = true;



    public StereoRenderer(Context ct, VisualTracker vt, SPAAMConsole spaamConsle){
        super();
        context = ct;
        cube = new Cube(40f, 0.0f, 0.0f, 20.0f);
        spaam = spaamConsle;
        visualTracker = vt;
        Log.i(TAG, "constructed");
    }

    public float[] Matrix2GLArray(Matrix M) {
        if (M == null)
            return null;
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


    public void setClippingPlane(float n, float f) {
        zNear = n;
        zFar = f;
        left.setClippingPlane(n, f);
        right.setClippingPlane(n, f);
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

    @Override
    public boolean configureARScene() {
        visualTracker.setMarker("single;Data/patt.hiro;80");
        Log.i(TAG, "ARScene configured");
        return true;
    }

    public void renderScene(GL10 gl) {
//        gl.glMatrixMode(GL10.GL_MODELVIEW);
//        gl.glLoadIdentity();
//        gl.glTranslatef(480f, 246f, 0f);
//        cube.draw(gl);
//        gl.glMatrixMode(GL10.GL_MODELVIEW);
//        gl.glLoadIdentity();
//        gl.glTranslatef(-480f, 246f, 0f);
//        cube.draw(gl);
//        gl.glMatrixMode(GL10.GL_MODELVIEW);
//        gl.glLoadIdentity();
//        gl.glTranslatef(480f, -246f, 0f);
//        cube.draw(gl);
//        gl.glMatrixMode(GL10.GL_MODELVIEW);
//        gl.glLoadIdentity();
//        gl.glTranslatef(-480f, -246f, 0f);
//        cube.draw(gl);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glLoadMatrixf(glTrans, 0);
//        gl.glTranslatef(param, 0.0f, 0.0f);
//        gl.glScalef(10.0f, 10.0f, 10.0f);
//        gl.glRotatef(angle, 0.0f, 0.0f, 1.0f);
        cube.draw(gl);
    }

    @Override
    public void draw(GL10 gl)
    {
        semaphore = false;
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        if (glTrans == null || spaam.getStatus() == SPAAM.SPAAMStatus.CALIB_RAW) {
            semaphore = true;
            gl.glClearColor(0.3f, 0.3f, 0.0f, 0.5f);
            return;
        }

        updateCalibMat();
        GL11ExtensionPack gl11 = (GL11ExtensionPack)gl;

        // Render Left Screen Texture
        left.renderToTexturePrepare(gl, glProjLeft);
        renderScene(gl);
        gl11.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);

        // Render Right Screen Texture
        right.renderToTexturePrepare(gl, glProjRight);
        renderScene(gl);
        gl11.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);


        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glViewport(0, 0, surfaceWidth, surfaceHeight);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthof(-surfaceWidth / 2, surfaceWidth / 2, -surfaceHeight / 2, surfaceHeight / 2, zNear, zFar);

        // Render Left Screen
        left.textureToScreen(gl);
        // Render Right Screen
        right.textureToScreen(gl);

        gl.glDisable(GL10.GL_TEXTURE_2D);
        semaphore = true;
    }

    public boolean setupFrameBuffer(GL10 gl){
        left = new FrameBuffer(FrameBuffer.FBO_TYPE.LEFT, surfaceWidth, surfaceHeight);
        right = new FrameBuffer(FrameBuffer.FBO_TYPE.RIGHT, surfaceWidth, surfaceHeight);
        setClippingPlane(0.01f, 1000.0f);
        boolean retLeft = left.setupFrameBuffer(gl);
        boolean retRight = right.setupFrameBuffer(gl);
        return retLeft && retRight;
    }

    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        surfaceWidth = width;
        surfaceHeight = height;
        Log.i(TAG, "surface changed: " + surfaceWidth + ", " + surfaceHeight);

        if (setupFrameBuffer(gl))
            Log.i(TAG, "Framebuffers setup complete");
        else
            Log.i(TAG, "Framebuffers setup incomplete");
        cube.loadTexture(gl, context.getResources(), R.drawable.box);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glDisable(GL10.GL_CULL_FACE);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

    }


    public void updateTransformation(Matrix t) {
        if (semaphore) {
            T = t;
            glTrans = Matrix2GLArray(T);
        }
    }

    public void updateCalibMat() {
        if ( spaam.getUpdated() ) {
            Matrix leftMat = spaam.getCalibMatLeft();
            Matrix rightMat = spaam.getCalibMatRight();
            if (leftMat != null && rightMat != null) {
                Matrix P = Util.times(leftMat);
                if (leftMat.get(0, 3) < 0 && leftMat.get(1, 3) < 0)
                    P = P.times(-1.0);
                P.set(2, 3, P.get(2, 3) + zFar * zNear);
                Log.i(TAG, "P1: " + P.get(0, 0) + ", " + P.get(0, 1) + ", " + P.get(0, 2) + ", " + P.get(0, 3));
                Log.i(TAG, "P2: " + P.get(1,0) + ", " + P.get(1,1) + ", " + P.get(1,2) + ", " + P.get(1,3));
                Log.i(TAG, "P3: " + P.get(2,0) + ", " + P.get(2,1) + ", " + P.get(2,2) + ", " + P.get(2,3));
                Log.i(TAG, "P4: " + P.get(3,0) + ", " + P.get(3,1) + ", " + P.get(3,2) + ", " + P.get(3,3));
                glProjLeft = Matrix2GLArray(P);
//                glProjLeft = new float[] {2.25287802167f, -0.0710149988668f, 0.164145578453f, -0.000164112753899f,
//                                -0.105387765071f, -2.28596066826f, 0.244253093084f, -0.000244204249253f,
//                                -0.596516092764f, -0.186700628568f, 0.82624725921f, -0.00082608203272f,
//                                103.416115863f, 23.5753742276f, 199.972322633f, 0.0000276718324112f};
                P = Util.times(rightMat);
                if (rightMat.get(0, 3) < 0 && rightMat.get(1, 3) < 0)
                    P = P.times(-1.0);
                P.set(2, 3, P.get(2, 3) + zFar * zNear);
                glProjRight = Matrix2GLArray(P);
                Log.i(TAG, "calibration matrix updated");
            }
            else{
                glProjRight = null;
                glProjLeft = null;
            }
            spaam.setUpdated(false);
        }
    }
}

//            Log.i(TAG, "P1: " + P.get(0,0) + ", " + P.get(0,1) + ", " + P.get(0,2) + ", " + P.get(0,3));
//            Log.i(TAG, "P2: " + P.get(1,0) + ", " + P.get(1,1) + ", " + P.get(1,2) + ", " + P.get(1,3));
//            Log.i(TAG, "P3: " + P.get(2,0) + ", " + P.get(2,1) + ", " + P.get(2,2) + ", " + P.get(2,3));
//            Log.i(TAG, "P4: " + P.get(3,0) + ", " + P.get(3,1) + ", " + P.get(3,2) + ", " + P.get(3,3));
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
    private float[] glTransRead, glTrans;
    private float[] glTransDefault = new float[] {0.95015115f, 0.19861476f, -0.24059308f, 0.0f, -0.31156227f, 0.62816185f, -0.7128754f, 0.0f, 0.009452152f, 0.75230217f, 0.65875655f, 0.0f, -163.45354f, 44.031586f, -468.28262f, 1.0f};

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



    public StereoRenderer(Context ct, VisualTracker vt, SPAAMConsole spaamConsle){
        super();
        context = ct;
        // Large TAG
        // cube = new Cube(160f, 160f, 160f, 0.0f, 0.0f, 80f);
        // Small TAG
        cube = new Cube(40f, 40f, 40f, 0.0f, 0.0f, 20f);

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
        // Large TAG
//        visualTracker.setMarker("single;Data/patt.hiro;320");
        // Small TAG
        visualTracker.setMarker("single;Data/patt.hiro;80");
        Log.i(TAG, "ARScene configured");
        return true;
    }

    public void renderScene(GL10 gl) {
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glLoadMatrixf(glTrans, 0);
        gl.glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);
        cube.draw(gl);
    }

    @Override
    public void draw(GL10 gl)
    {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        if ( glTransRead == null )
            glTrans = glTransDefault;
        else
            glTrans = glTransRead.clone();

        if ( spaam.getStatus() == SPAAM.SPAAMStatus.CALIB_RAW ) {
            glProjLeft = new float[]{1.16524563f, -0.01455041f, -0.0120612057056f, 1.206e-05f, 0.00278049f, -1.12576543f, 0.0451345118982f, -4.513e-05f, -0.31905683f, -0.2071979f, 0.454765460898f, -0.00045472f, 131.49488093f, 12.96216352f, 102.236653588f, -0.00223643f};
            glProjRight = new float[]{1.19551985f, -0.01836658f, 0.0582958275769f, -5.829e-05f, 0.01441908f, -1.12986884f, -0.0396739660315f, 3.967e-05f, -0.31620645f, -0.20908707f, 0.437273712325f, -0.00043723f, 55.14054087f, 17.42113775f, 98.5020902606f, 0.00149776f};
        }
        else{
            updateCalibMat();
        }

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
    }

    public boolean setupFrameBuffer(GL10 gl){
        left = new FrameBuffer(FrameBuffer.FBO_TYPE.LEFT, surfaceWidth, surfaceHeight);
        right = new FrameBuffer(FrameBuffer.FBO_TYPE.RIGHT, surfaceWidth, surfaceHeight);
        setClippingPlane(0.1f, 1000.0f);
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
        glTransRead = Matrix2GLArray(t);
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
                glProjLeft = Matrix2GLArray(P);
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
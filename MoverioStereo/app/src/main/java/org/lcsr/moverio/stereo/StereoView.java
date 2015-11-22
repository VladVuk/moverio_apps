package org.lcsr.moverio.stereo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import org.lcsr.moverio.stereo.FrameBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

/**
 * Created by qian on 11/20/15.
 */
public class StereoView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private static String TAG = "StereoView";
    Cube cube;
    int surfaceWidth, surfaceHeight;
    float angle = 0.0f;
    float param = 0.0f;

    private FrameBuffer left, right;


    public StereoView(Context context){
        super(context);
        cube = new Cube(20f);
        setRenderer(this);
        Log.i(TAG, "constructed");
    }

    public void renderScene(GL10 gl, int c) {
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
        gl.glTranslatef(param, 0.0f, 0.0f);
        gl.glScalef(10.0f, 10.0f, 10.0f);
        gl.glRotatef(angle, 0.0f, 0.0f, 1.0f);
        cube.draw(gl);
    }

    public void onDrawFrame(GL10 gl)
    {
        GL11ExtensionPack gl11 = (GL11ExtensionPack)gl;
        angle += 0.5f;

        // Render Left Screen Texture
        left.renderToTexturePrepare(gl);
        renderScene(gl, -1);
        gl11.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);

        // Render Right Screen Texture
        right.renderToTexturePrepare(gl);
        renderScene(gl, 1);
        gl11.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);


        gl.glClearColor(1.0f, 0.0f, 0.0f, 0.5f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glViewport(0, 0, surfaceWidth, surfaceHeight);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthof(-surfaceWidth / 2, surfaceWidth / 2, -surfaceHeight / 2, surfaceHeight / 2, -1000f, 1000f);

        // Render Left Screen
        left.textureToScreen(gl);
        // Render Right Screen
        right.textureToScreen(gl);

        gl.glDisable(GL10.GL_TEXTURE_2D);

    }

    public boolean setupFrameBuffer(GL10 gl){
        left = new FrameBuffer(FrameBuffer.FBO_TYPE.LEFT, surfaceWidth, surfaceHeight);
        right = new FrameBuffer(FrameBuffer.FBO_TYPE.RIGHT, surfaceWidth, surfaceHeight);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            param += 10.0f;
            Log.i(TAG, "Parameter set to " + param);
        }
        return true;
    }


}

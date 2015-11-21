package org.lcsr.moverio.stereo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

/**
 * Created by qian on 11/20/15.
 */
public class StereoRenderer extends GLSurfaceView implements GLSurfaceView.Renderer {
    private static String TAG = "StereoView";
    Cube_bak cube;
    Plain plane;
    int w, h;

    private int mTargetTexture;
    private int mFramebuffer;
    private int mFramebufferWidth = 256;
    private int mFramebufferHeight = 256;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    public StereoRenderer(Context context){
        super(context);
        cube = new Cube_bak(0.1f);
        plane = new Plain(0.2f);
        setRenderer(this);
        Log.i(TAG, "constructed");
    }


    public void onDrawFrame(GL10 gl)
    {
        GL11ExtensionPack gl11 = (GL11ExtensionPack)gl;
//
        gl11.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, mTargetTexture);
//
        gl.glViewport(0, 0, mFramebufferWidth, mFramebufferHeight);
        gl11.glBindTexture(GL10.GL_TEXTURE_2D, 0);
        gl.glClearColor(0.0f, 0.0f, 1.0f, 0.5f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(0.2f, 0.0f, 0.0f);
        cube.draw(gl);


        gl11.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glViewport(0, 0, w, h);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTargetTexture);
//        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
//        gl.glActiveTexture(GL10.GL_TEXTURE0);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        GLU.gluLookAt(gl, -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 0.5f, 0.5f, 1.0f);

        plane.draw(gl);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
        gl.glDisable(GL10.GL_TEXTURE_2D);
    }

    public void setupFrameBuffer(GL10 gl){
        GL11ExtensionPack gl11 = (GL11ExtensionPack)gl;
        gl.glEnable(GL10.GL_TEXTURE_2D);

        int[] texture = new int[1];
        gl.glGenTextures(1, texture, 0);
        mTargetTexture = texture[0];
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTargetTexture);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, mFramebufferWidth, mFramebufferHeight,
                0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, null);

        int[] framebuffers = new int[1];

        gl11.glGenFramebuffersOES(1, framebuffers, 0);
        mFramebuffer = framebuffers[0];

        gl11.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, mFramebuffer);
        gl11.glFramebufferTexture2DOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, GL11ExtensionPack.GL_COLOR_ATTACHMENT0_OES,
                GL10.GL_TEXTURE_2D, mTargetTexture, 0);

        int[] renderbuffers = new int[1];
        gl11.glGenRenderbuffersOES(1, renderbuffers, 0);
        int depthbufferID = renderbuffers[0];

        gl11.glBindRenderbufferOES(GL11ExtensionPack.GL_RENDERBUFFER_OES, depthbufferID);
        gl11.glRenderbufferStorageOES(GL11ExtensionPack.GL_RENDERBUFFER_OES,
                GL11ExtensionPack.GL_DEPTH_COMPONENT16, mFramebufferWidth, mFramebufferHeight);
        gl11.glFramebufferRenderbufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, GL11ExtensionPack.GL_DEPTH_ATTACHMENT_OES,
                GL11ExtensionPack.GL_RENDERBUFFER_OES, depthbufferID);
        gl11.glBindRenderbufferOES(GL11ExtensionPack.GL_RENDERBUFFER_OES, 0);

        gl.glDisable(GL10.GL_TEXTURE_2D);

        int status = gl11.glCheckFramebufferStatusOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES);
        if (status == GL11ExtensionPack.GL_FRAMEBUFFER_COMPLETE_OES) {
            Log.i(TAG, "Everything good.");
        }
        else
            Log.i(TAG, "Something wrong there." + status);
        gl11.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);

    }

    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        w = width;
        h = height;
        gl.glViewport(0, 0, w, h);
        Log.i(TAG, "viewport: " + w + ", " + h);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glDisable(GL10.GL_CULL_FACE);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        setupFrameBuffer(gl);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    }


}

package org.lcsr.moverio.stereo;

import android.opengl.GLU;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

/**
 * Created by qian on 11/21/15.
 */
public class FrameBuffer {
    public enum FBO_TYPE {LEFT, RIGHT}

    private String TAG = "FrameBuffer";
    private FBO_TYPE type;

    private int textureID = 0;
    private int frameBufferID = 0;
    private Plain holder;

    // The surface dimension is 1024 * 512
    // In OpenGL, the framebuffer dimension must be POT (power of 2)
    private int frameBufferWidth = 1024;
    private int frameBufferHeight = 512;

    private int surfaceWidth, surfaceHeight;
    private float[] planeTranslatef = {0.0f, 0.0f, -0.5f};
    private int glViewportX = 0;
    private int glViewportY = 0;
    private float[] eyef = {0.0f, -100.0f, 20.0f};
    private float[] upf = {0.0f, 0.0f, 1.0f};
    private float[] centerf = {0.0f, 0.0f, 0.0f};


    public FrameBuffer(FBO_TYPE t, int width, int height){
        type = t;
        surfaceWidth = width;
        surfaceHeight = height;
        holder = new Plain(512);

        planeTranslatef[1] = (frameBufferHeight-surfaceHeight)/2;
        glViewportY = 0;
        if (type == FBO_TYPE.LEFT) {
            TAG = TAG + "_LEFT";
            planeTranslatef[0] = -256.0f;
            glViewportX = frameBufferWidth - surfaceWidth;
            eyef[0] = -10.0f;
            centerf[0] = -10.0f;
        }
        else {
            TAG = TAG + "_RIGHT";
            planeTranslatef[0] = 256.0f;
            glViewportX = 0;
            eyef[0] = 10.0f;
            centerf[0] = 10.0f;
        }
        Log.i(TAG, "constructed");
    }

    public void renderToTexturePrepare(GL10 gl) {
        GL11ExtensionPack gl11 = (GL11ExtensionPack)gl;
        gl11.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, textureID);
        gl.glViewport(glViewportX, glViewportY, surfaceWidth, surfaceHeight);
        gl11.glBindTexture(GL10.GL_TEXTURE_2D, 0);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthof(-surfaceWidth / 2, surfaceWidth / 2, -surfaceHeight / 2, surfaceHeight / 2, -1000f, 1000f);
        GLU.gluLookAt(gl, eyef[0], eyef[1], eyef[2], centerf[0], centerf[1], centerf[2], upf[0], upf[1], upf[2]);
    }

    public void textureToScreen(GL10 gl) {
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(planeTranslatef[0], planeTranslatef[1], planeTranslatef[2]);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID);
        holder.draw(gl);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
    }

    public boolean setupFrameBuffer(GL10 gl) {

        GL11ExtensionPack gl11 = (GL11ExtensionPack) gl;
        gl.glEnable(GL10.GL_TEXTURE_2D);

        int[] texture = new int[1];
        gl.glGenTextures(1, texture, 0);
        textureID = texture[0];
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, frameBufferWidth, frameBufferHeight,
                0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, null);

        int[] framebuffers = new int[1];

        gl11.glGenFramebuffersOES(1, framebuffers, 0);
        frameBufferID = framebuffers[0];

        gl11.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, frameBufferID);
        gl11.glFramebufferTexture2DOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, GL11ExtensionPack.GL_COLOR_ATTACHMENT0_OES,
                GL10.GL_TEXTURE_2D, textureID, 0);

        int[] renderbuffers = new int[1];
        gl11.glGenRenderbuffersOES(1, renderbuffers, 0);
        int depthbufferID = renderbuffers[0];

        gl11.glBindRenderbufferOES(GL11ExtensionPack.GL_RENDERBUFFER_OES, depthbufferID);
        gl11.glRenderbufferStorageOES(GL11ExtensionPack.GL_RENDERBUFFER_OES,
                GL11ExtensionPack.GL_DEPTH_COMPONENT16, frameBufferWidth, frameBufferHeight);
        gl11.glFramebufferRenderbufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, GL11ExtensionPack.GL_DEPTH_ATTACHMENT_OES,
                GL11ExtensionPack.GL_RENDERBUFFER_OES, depthbufferID);
        gl11.glBindRenderbufferOES(GL11ExtensionPack.GL_RENDERBUFFER_OES, 0);

        gl.glDisable(GL10.GL_TEXTURE_2D);

        int status = gl11.glCheckFramebufferStatusOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES);
        if (status == GL11ExtensionPack.GL_FRAMEBUFFER_COMPLETE_OES) {
            Log.i(TAG, "setup complete.");
            gl11.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);
            return true;
        }
        else {
            Log.i(TAG, "setup incomplete:" + status);
            gl11.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);
            return false;
        }
    }

}

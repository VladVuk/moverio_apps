package org.lcsr.moverio.stereo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.SystemClock;


class Triangle
{
	public Triangle()
	{

		ByteBuffer vbb = ByteBuffer.allocateDirect(VERTS * 3 * 4);
		vbb.order(ByteOrder.nativeOrder());
		mFVertexBuffer = vbb.asFloatBuffer();

		ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
		tbb.order(ByteOrder.nativeOrder());
		mTexBuffer = tbb.asFloatBuffer();

		ByteBuffer ibb = ByteBuffer.allocateDirect(VERTS * 2);
		ibb.order(ByteOrder.nativeOrder());
		mIndexBuffer = ibb.asShortBuffer();

		float[] coords =
		{ -0.5f, -0.25f, 0, 0.5f, -0.25f, 0, 0.0f, 0.559016994f, 0 };

		for (int i = 0; i < VERTS; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				mFVertexBuffer.put(coords[i * 3 + j] * 2.0f);
			}
		}

		for (int i = 0; i < VERTS; i++)
		{
			for (int j = 0; j < 2; j++)
			{
				mTexBuffer.put(coords[i * 3 + j] * 2.0f + 0.5f);
			}
		}

		for (int i = 0; i < VERTS; i++)
		{
			mIndexBuffer.put((short) i);
		}

		mFVertexBuffer.position(0);
		mTexBuffer.position(0);
		mIndexBuffer.position(0);
	}

	public void draw(GL10 gl)
	{
		gl.glFrontFace(GL10.GL_CCW);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
		gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, VERTS,
				GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
	}

	private final static int VERTS = 3;

	private FloatBuffer mFVertexBuffer;
	private FloatBuffer mTexBuffer;
	private ShortBuffer mIndexBuffer;
}

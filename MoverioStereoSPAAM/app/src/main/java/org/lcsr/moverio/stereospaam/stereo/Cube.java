package org.lcsr.moverio.stereospaam.stereo;

/**
 * Created by qian on 11/20/15.
 */

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;


/**
 * Simple class to render a coloured cube.
 */
public class Cube {

    private FloatBuffer	vertexBuffer, textureBuffer;
    float[] vertices, texture;
    private ByteBuffer	indexBuffer;
    byte[] indices;
    private int textureID;
    private Bitmap bmp;

    public Cube(float xlen, float ylen, float zlen, float x, float y, float z) {
        textureID = 0;
        vertices = new float[]
                {
                        // Vertices according to faces
                        x - xlen/2, y - ylen/2, z + zlen/2, //v0
                        x + xlen/2, y - ylen/2, z + zlen/2,     //v1
                        x - xlen/2, y + ylen/2, z + zlen/2,     //v2
                        x + xlen/2, y + ylen/2, z + zlen/2,     //v3

                        x + xlen/2, y - ylen/2, z + zlen/2,     //...
                        x + xlen/2, y - ylen/2, z - zlen/2,
                        x + xlen/2, y + ylen/2, z + zlen/2,
                        x + xlen/2, y + ylen/2, z - zlen/2,

                        x + xlen/2, y - ylen/2, z - zlen/2,
                        x - xlen/2, y - ylen/2, z - zlen/2,
                        x + xlen/2, y + ylen/2, z - zlen/2,
                        x - xlen/2, y + ylen/2, z - zlen/2,

                        x - xlen/2, y - ylen/2, z - zlen/2,
                        x - xlen/2, y - ylen/2, z + zlen/2,
                        x - xlen/2, y + ylen/2, z - zlen/2,
                        x - xlen/2, y + ylen/2, z + zlen/2,

                        x - xlen/2, y - ylen/2, z - zlen/2,
                        x + xlen/2, y - ylen/2, z - zlen/2,
                        x - xlen/2, y - ylen/2, z + zlen/2,
                        x + xlen/2, y - ylen/2, z + zlen/2,

                        x - xlen/2, y + ylen/2, z + zlen/2,
                        x + xlen/2, y + ylen/2, z + zlen/2,
                        x - xlen/2, y + ylen/2, z - zlen/2,
                        x + xlen/2, y + ylen/2, z - zlen/2
                };


        texture = new float[]
                {
                        //Mapping coordinates for the vertices
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        1.0f, 1.0f,

                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        1.0f, 1.0f,

                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        1.0f, 1.0f,

                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        1.0f, 1.0f,

                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        1.0f, 1.0f,

                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        1.0f, 1.0f
                };

        indices = new byte[]
                {
                        // Faces definition
                        0, 1, 3, 0, 3, 2,         // Face front
                        4, 5, 7, 4, 7, 6,         // Face right
                        8, 9, 11, 8, 11, 10,     // ...
                        12, 13, 15, 12, 15, 14,
                        16, 17, 19, 16, 19, 18,
                        20, 21, 23, 20, 23, 22
                };


        //
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuf.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        //
        byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuf.asFloatBuffer();
        textureBuffer.put(texture);
        textureBuffer.position(0);


        //
        indexBuffer = ByteBuffer.allocateDirect(indices.length);
        indexBuffer.put(indices);
        indexBuffer.position(0);

    }

    public boolean loadTexture(GL10 gl, Resources res, int resID){
        int[] temp_tex = new int[1];
        gl.glGenTextures(1, temp_tex, 0);
        textureID = temp_tex[0];
        bmp = BitmapFactory.decodeResource(res, resID);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
        return true;
    }

    public void draw(GL10 gl) {
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID);

        //Enable the vertex, texture and normal state
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        //Point to our buffers
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

        //Draw the vertices as triangles, based on the Index Buffer information
        gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE, indexBuffer);

        //Disable the client state before leaving
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
        gl.glDisable(GL10.GL_TEXTURE_2D);

    }

}
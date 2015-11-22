package org.lcsr.moverio.stereo;

/**
 * Created by qian on 11/20/15.
 */

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES10;

/**
 * Simple class to render a coloured cube.
 */
public class Plain {

    private float SIZE = 1.0f;

    private FloatBuffer	vertexBuffer, textureBuffer;
    float[] vertices, texture;
    private ByteBuffer	indexBuffer;
    byte[] indices;

    public Plain(float size) {
        SIZE = size/2;
        vertices = new float[]
                {
                        // Vertices according to faces
                        -SIZE, -SIZE, 0.0f, //v0
                        SIZE, -SIZE, 0.0f,     //v1
                        -SIZE, SIZE, 0.0f,     //v2
                        SIZE, SIZE, 0.0f     //v3

                };


        texture = new float[]
                {
                        //Mapping coordinates for the vertices
                        0.0f, 0.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f
                };

        indices = new byte[]
                {
                        // Faces definition
                        0, 1, 3, 0, 3, 2
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

    public void draw(GL10 gl) {

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

    }

}
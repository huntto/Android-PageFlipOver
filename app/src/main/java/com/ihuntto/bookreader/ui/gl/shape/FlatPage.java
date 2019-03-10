package com.ihuntto.bookreader.ui.gl.shape;

import com.ihuntto.bookreader.ui.gl.program.FlatPageShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform2f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.Matrix.multiplyMM;

public class FlatPage extends Page {
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int BYTES_PER_FLOAT = 4;

    private FloatBuffer mVertexData;

    private int mWidth;
    private int mHeight;

    private FlatPageShaderProgram mProgram;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mTemp = new float[16];


    public FlatPage(FlatPageShaderProgram program, int width, int height, int maxFoldHeight) {
        super(width, height, maxFoldHeight);

        mProgram = program;
        mWidth = width;
        mHeight = height;

        final float[] vertices = {
                0, mHeight, 0,        // left bottom
                0, 0, 0,              // left top
                mWidth, mHeight, 0,   // right bottom
                mWidth, 0, 0          // right top
        };


        mVertexData = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexData.put(vertices);
    }


    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void draw(float[] viewMatrix, float[] projectionMatrix) {
        mProgram.use();

        multiplyMM(mTemp, 0, projectionMatrix, 0, viewMatrix, 0);
        multiplyMM(mMVPMatrix, 0, mTemp, 0, mModelMatrix, 0);
        glUniformMatrix4fv(mProgram.getMatrixLocation(), 1, false, mMVPMatrix, 0);

        glUniform2f(mProgram.getPageSizeLocation(), mWidth, mHeight);

        mVertexData.position(0);
        glVertexAttribPointer(mProgram.getPositionLocation(), POSITION_COMPONENT_COUNT, GL_FLOAT,
                false, 0, mVertexData);
        glEnableVertexAttribArray(mProgram.getPositionLocation());

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mTextureId);
        glUniform1i(mProgram.getTextureUnitLocation(), 0);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, mVertexData.limit() / POSITION_COMPONENT_COUNT);
    }
}

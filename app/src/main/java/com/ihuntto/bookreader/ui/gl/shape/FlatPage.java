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


    public FlatPage(FlatPageShaderProgram program, int width, int height, int flatHeight, int maxFoldHeight) {
        super(width, height, maxFoldHeight);

        mProgram = program;
        mWidth = width;
        mHeight = height;

        final float[] vertices = {
                0, mHeight, flatHeight,        // left bottom
                0, 0, flatHeight,              // left top
                mWidth, mHeight, flatHeight,   // right bottom
                mWidth, 0, flatHeight          // right top
        };


        mVertexData = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexData.put(vertices);
    }

    public void draw(FlatPageShaderProgram program, float[] viewMatrix, float[] projectionMatrix) {
        program.use();

        multiplyMM(mTemp, 0, projectionMatrix, 0, viewMatrix, 0);
        multiplyMM(mMVPMatrix, 0, mTemp, 0, mModelMatrix, 0);
        glUniformMatrix4fv(program.getMatrixLocation(), 1, false, mMVPMatrix, 0);

        glUniform2f(program.getPageSizeLocation(), mWidth, mHeight);

        mVertexData.position(0);
        glVertexAttribPointer(program.getPositionLocation(), POSITION_COMPONENT_COUNT, GL_FLOAT,
                false, 0, mVertexData);
        glEnableVertexAttribArray(program.getPositionLocation());

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mTextureId);
        glUniform1i(program.getTextureUnitLocation(), 0);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, mVertexData.limit() / POSITION_COMPONENT_COUNT);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void draw(float[] viewMatrix, float[] projectionMatrix) {
        draw(mProgram, viewMatrix, projectionMatrix);
    }
}

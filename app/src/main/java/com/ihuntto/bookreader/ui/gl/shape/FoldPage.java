package com.ihuntto.bookreader.ui.gl.shape;

import android.graphics.PointF;

import com.ihuntto.bookreader.ui.gl.program.FoldPageShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform2f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;

public class FoldPage extends Page {
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int BYTES_PER_FLOAT = 4;

    private int mWidth;
    private int mHeight;
    private int mBaseFoldHeight;
    private int mMaxFoldHeight;
    private PointF mDragPoint = new PointF();
    private PointF mOriginPoint = new PointF();

    private FloatBuffer mVertexData;

    private FoldPageShaderProgram mProgram;

    public FoldPage(FoldPageShaderProgram program, int width, int height, int baseFoldHeight, int maxFoldHeight) {
        super(width, height, maxFoldHeight);
        mProgram = program;
        mWidth = width;
        mHeight = height;
        mBaseFoldHeight = baseFoldHeight;
        mMaxFoldHeight = maxFoldHeight;

        final int step = 5;
        final int wCount = width / step;
        final int hCount = height / step;

        final float[] vertices = new float[wCount
                * hCount
                * 6
                * POSITION_COMPONENT_COUNT];

        int count = 0;
        int x, y;
        for (int w = 0; w < wCount; w++) {
            x = w * step;
            for (int h = 0; h < hCount; h++) {
                y = h * step;

                vertices[count++] = x;
                vertices[count++] = y;

                vertices[count++] = x + step;
                vertices[count++] = y;

                vertices[count++] = x + step;
                vertices[count++] = y + step;

                vertices[count++] = x;
                vertices[count++] = y;

                vertices[count++] = x + step;
                vertices[count++] = y + step;

                vertices[count++] = x;
                vertices[count++] = y + step;
            }
        }

        mVertexData = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexData.put(vertices);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void draw(float[] viewMatrix, float[] projectionMatrix) {
        mProgram.use();

        glUniformMatrix4fv(mProgram.getModelMatrixLocation(), 1, false, mModelMatrix, 0);
        glUniformMatrix4fv(mProgram.getViewMatrixLocation(), 1, false, viewMatrix, 0);
        glUniformMatrix4fv(mProgram.getProjectionMatrixLocation(), 1, false, projectionMatrix, 0);

        glUniform2f(mProgram.getPageSizeLocation(), mWidth, mHeight);
        glUniform2f(mProgram.getDragLocation(), mDragPoint.x, mDragPoint.y);
        glUniform2f(mProgram.getOriginLocation(), mOriginPoint.x, mOriginPoint.y);
        glUniform1f(mProgram.getMaxFoldHeightLocation(), mMaxFoldHeight);
        glUniform1f(mProgram.getBaseFoldHeightLocation(), mBaseFoldHeight);

        mVertexData.position(0);
        glVertexAttribPointer(mProgram.getPositionLocation(), POSITION_COMPONENT_COUNT, GL_FLOAT,
                false, 0, mVertexData);
        glEnableVertexAttribArray(mProgram.getPositionLocation());

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mTextureId);
        glUniform1i(mProgram.getTextureUnitLocation(), 0);

        glDrawArrays(GL_TRIANGLES, 0, mVertexData.limit() / POSITION_COMPONENT_COUNT);
    }

    public void fold(float originX, float originY, float dragX, float dragY) {
        mOriginPoint.set(originX, originY);
        mDragPoint.set(dragX, dragY);
    }
}

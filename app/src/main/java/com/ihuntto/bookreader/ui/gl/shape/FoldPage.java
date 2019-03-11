package com.ihuntto.bookreader.ui.gl.shape;

import android.graphics.PointF;

import com.ihuntto.bookreader.ui.gl.program.FoldPageShaderProgram;
import com.ihuntto.bookreader.ui.gl.program.FoldPageShadowShaderProgram;

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
import static android.opengl.GLES20.glUniform3fv;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.transposeM;

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

    private final float[] mMVPMatrix = new float[16];
    private final float[] mTemp = new float[32];

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
    public void draw(final float[] eyePos, float[] viewMatrix, float[] projectionMatrix) {
        mProgram.use();

        multiplyMM(mTemp, 0, projectionMatrix, 0, viewMatrix, 0);
        multiplyMM(mMVPMatrix, 0, mTemp, 0, mModelMatrix, 0);
        glUniformMatrix4fv(mProgram.getMatrixLocation(), 1, false, mMVPMatrix, 0);

        glUniform2f(mProgram.getPageSizeLocation(), mWidth, mHeight);
        glUniform2f(mProgram.getDragLocation(), mDragPoint.x, mDragPoint.y);
        glUniform2f(mProgram.getOriginLocation(), mOriginPoint.x, mOriginPoint.y);
        glUniform1f(mProgram.getMaxFoldHeightLocation(), mMaxFoldHeight);
        glUniform1f(mProgram.getBaseFoldHeightLocation(), mBaseFoldHeight);
        glUniform3fv(mProgram.getLightDirectionLocation(), 1, mLightDirection, 0);
        glUniform3fv(mProgram.getLightAmbientLocation(), 1, mLightAmbient, 0);
        glUniform3fv(mProgram.getLightDiffuseLocation(), 1, mLightDiffuse, 0);
        glUniform3fv(mProgram.getLightSpecularLocation(), 1, mLightSpecular, 0);
        glUniform3fv(mProgram.getLightColorLocation(), 1, mLightColor, 0);

        invertM(mTemp, 0, mMVPMatrix, 0);
        transposeM(mTemp, 16, mTemp, 0);
        mTemp[4] = eyePos[0];
        mTemp[5] = eyePos[1];
        mTemp[6] = eyePos[2];
        mTemp[7] = 0;
        multiplyMV(mTemp, 0, mTemp, 16, mTemp, 4);
        glUniform3fv(mProgram.getViewPosLocation(), 1, mTemp, 0);

        mVertexData.position(0);
        glVertexAttribPointer(mProgram.getPositionLocation(), POSITION_COMPONENT_COUNT, GL_FLOAT,
                false, 0, mVertexData);
        glEnableVertexAttribArray(mProgram.getPositionLocation());

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mTextureId);
        glUniform1i(mProgram.getTextureUnitLocation(), 0);

        glDrawArrays(GL_TRIANGLES, 0, mVertexData.limit() / POSITION_COMPONENT_COUNT);
    }

    public void drawShadow(FoldPageShadowShaderProgram shadowProgram, final float[] eyePos, float[] viewMatrix, float[] projectionMatrix) {
        shadowProgram.use();

        multiplyMM(mTemp, 0, projectionMatrix, 0, viewMatrix, 0);
        multiplyMM(mMVPMatrix, 0, mTemp, 0, mModelMatrix, 0);
        glUniformMatrix4fv(shadowProgram.getMatrixLocation(), 1, false, mMVPMatrix, 0);

        glUniform2f(shadowProgram.getPageSizeLocation(), mWidth, mHeight);
        glUniform2f(shadowProgram.getDragLocation(), mDragPoint.x, mDragPoint.y);
        glUniform2f(shadowProgram.getOriginLocation(), mOriginPoint.x, mOriginPoint.y);
        glUniform1f(shadowProgram.getMaxFoldHeightLocation(), mMaxFoldHeight);
        glUniform1f(shadowProgram.getBaseFoldHeightLocation(), mBaseFoldHeight);
        glUniform3fv(shadowProgram.getLightPosLocation(), 1, eyePos, 0);

        mVertexData.position(0);
        glVertexAttribPointer(shadowProgram.getPositionLocation(), POSITION_COMPONENT_COUNT, GL_FLOAT,
                false, 0, mVertexData);
        glEnableVertexAttribArray(shadowProgram.getPositionLocation());

        glDrawArrays(GL_TRIANGLES, 0, mVertexData.limit() / POSITION_COMPONENT_COUNT);
    }

    public void fold(float originX, float originY, float dragX, float dragY) {
        mOriginPoint.set(originX, originY);
        mDragPoint.set(dragX, dragY);
    }
}

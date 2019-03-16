package com.ihuntto.bookreader.ui.gl.shape;

import android.graphics.PointF;

import com.ihuntto.bookreader.ui.gl.light.Light;
import com.ihuntto.bookreader.ui.gl.program.FoldPageShaderProgram;
import com.ihuntto.bookreader.ui.gl.program.FoldPageShadowForFlatShaderProgram;
import com.ihuntto.bookreader.ui.gl.program.FoldPageShadowForSelfShaderProgram;

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
    public void draw(final float[] eyePos, final Light light, float[] viewProjectionMatrix) {
        mProgram.use();

        multiplyMM(mMVPMatrix, 0, viewProjectionMatrix, 0, mModelMatrix, 0);
        glUniformMatrix4fv(mProgram.getMVPMatrixLocation(), 1, false, mMVPMatrix, 0);

        glUniform2f(mProgram.getPageSizeLocation(), mWidth, mHeight);
        glUniform2f(mProgram.getDragLocation(), mDragPoint.x, mDragPoint.y);
        glUniform2f(mProgram.getOriginLocation(), mOriginPoint.x, mOriginPoint.y);
        glUniform1f(mProgram.getMaxFoldHeightLocation(), mMaxFoldHeight);
        glUniform1f(mProgram.getBaseFoldHeightLocation(), mBaseFoldHeight);
        glUniform3fv(mProgram.getLightDirectionLocation(), 1, light.getDirection(), 0);
        glUniform3fv(mProgram.getLightAmbientLocation(), 1, light.getAmbient(), 0);
        glUniform3fv(mProgram.getLightDiffuseLocation(), 1, light.getDiffuse(), 0);
        glUniform3fv(mProgram.getLightSpecularLocation(), 1, light.getSpecular(), 0);
        glUniform3fv(mProgram.getLightColorLocation(), 1, light.getColor(), 0);

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

    private FoldPageShadowForFlatShaderProgram mShadowForFlatShaderProgram;

    public void setShadowForFlatProgram(FoldPageShadowForFlatShaderProgram shadowForFlatProgram) {
        mShadowForFlatShaderProgram = shadowForFlatProgram;
    }

    public void drawShadowForFlat(float[] viewProjectionMatrix) {
        mShadowForFlatShaderProgram.use();

        multiplyMM(mMVPMatrix, 0, viewProjectionMatrix, 0, mModelMatrix, 0);
        glUniformMatrix4fv(mShadowForFlatShaderProgram.getMVPMatrixLocation(), 1, false, mMVPMatrix, 0);

        glUniform2f(mShadowForFlatShaderProgram.getPageSizeLocation(), mWidth, mHeight);
        glUniform2f(mShadowForFlatShaderProgram.getDragLocation(), mDragPoint.x, mDragPoint.y);
        glUniform2f(mShadowForFlatShaderProgram.getOriginLocation(), mOriginPoint.x, mOriginPoint.y);
        glUniform1f(mShadowForFlatShaderProgram.getMaxFoldHeightLocation(), mMaxFoldHeight);
        glUniform1f(mShadowForFlatShaderProgram.getBaseFoldHeightLocation(), mBaseFoldHeight);

        mVertexData.position(0);
        glVertexAttribPointer(mShadowForFlatShaderProgram.getPositionLocation(), POSITION_COMPONENT_COUNT, GL_FLOAT,
                false, 0, mVertexData);
        glEnableVertexAttribArray(mShadowForFlatShaderProgram.getPositionLocation());

        glDrawArrays(GL_TRIANGLES, 0, mVertexData.limit() / POSITION_COMPONENT_COUNT);
    }

    private FoldPageShadowForSelfShaderProgram mShadowForSelfShaderProgram;

    public void setShadowForSelfProgram(FoldPageShadowForSelfShaderProgram shadowForSelfProgram) {
        mShadowForSelfShaderProgram = shadowForSelfProgram;
    }

    public void drawShadowForSelf(float[] viewProjectionMatrix) {
        mShadowForSelfShaderProgram.use();

        multiplyMM(mMVPMatrix, 0, viewProjectionMatrix, 0, mModelMatrix, 0);
        glUniformMatrix4fv(mShadowForSelfShaderProgram.getMVPMatrixLocation(), 1, false, mMVPMatrix, 0);

        glUniform2f(mShadowForSelfShaderProgram.getPageSizeLocation(), mWidth, mHeight);
        glUniform2f(mShadowForSelfShaderProgram.getDragLocation(), mDragPoint.x, mDragPoint.y);
        glUniform2f(mShadowForSelfShaderProgram.getOriginLocation(), mOriginPoint.x, mOriginPoint.y);
        glUniform1f(mShadowForSelfShaderProgram.getMaxFoldHeightLocation(), mMaxFoldHeight);
        glUniform1f(mShadowForSelfShaderProgram.getBaseFoldHeightLocation(), mBaseFoldHeight);

        mVertexData.position(0);
        glVertexAttribPointer(mShadowForSelfShaderProgram.getPositionLocation(), POSITION_COMPONENT_COUNT, GL_FLOAT,
                false, 0, mVertexData);
        glEnableVertexAttribArray(mShadowForSelfShaderProgram.getPositionLocation());

        glDrawArrays(GL_TRIANGLES, 0, mVertexData.limit() / POSITION_COMPONENT_COUNT);
    }

    public void fold(float originX, float originY, float dragX, float dragY) {
        mOriginPoint.set(originX, originY);
        mDragPoint.set(dragX, dragY);
    }
}

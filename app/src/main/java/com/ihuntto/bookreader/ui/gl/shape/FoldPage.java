package com.ihuntto.bookreader.ui.gl.shape;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;

import com.ihuntto.bookreader.R;
import com.ihuntto.bookreader.ui.gl.light.Light;
import com.ihuntto.bookreader.ui.gl.program.ShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;
import static android.opengl.Matrix.transposeM;

public class FoldPage extends Page {
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int BYTES_PER_FLOAT = 4;

    private static final String U_MVP_MATRIX = "uMVPMatrix";
    private static final String U_TEXTURE_UNIT = "uTextureUnit";
    private static final String U_ORIGIN_POINT = "uOriginPoint";
    private static final String U_DRAG_POINT = "uDragPoint";
    private static final String U_PAGE_SIZE = "uPageSize";
    private static final String U_FOLD_HEIGHT = "uFoldHeight";
    // light
    private static final String U_LIGHT_DIRECTION = "uLight.direction";
    private static final String U_LIGHT_AMBIENT = "uLight.ambient";
    private static final String U_LIGHT_DIFFUSE = "uLight.diffuse";
    private static final String U_LIGHT_SPECULAR = "uLight.specular";
    private static final String U_LIGHT_COLOR = "uLight.color";
    private static final String U_VIEW_POS = "uViewPos";

    private static final String A_POSITION = "aPosition";

    private static ShaderProgram sFoldProgram;
    private static ShaderProgram sShadowOnFlatProgram;
    private static ShaderProgram sShadowOnFoldProgram;

    public static void initProgram(Context context) {
        sFoldProgram = new ShaderProgram(context, R.raw.fold_page_vertex_shader, R.raw.fold_page_fragment_shader);
        sFoldProgram.compile();

        sShadowOnFlatProgram = new ShaderProgram(context, R.raw.fold_page_shadow_for_flat_vertex_shader, R.raw.fold_page_shadow_for_flat_fragment_shader);
        sShadowOnFlatProgram.compile();

        sShadowOnFoldProgram = new ShaderProgram(context, R.raw.fold_page_shadow_for_self_vertex_shader, R.raw.fold_page_shadow_for_self_fragment_shader);
        sShadowOnFoldProgram.compile();
    }


    private int mWidth;
    private int mHeight;
    private int mFoldHeight;
    private PointF mDragPoint = new PointF();
    private PointF mOriginPoint = new PointF();

    private FloatBuffer mVertexData;


    private final float[] mMVPMatrix = new float[16];
    private final float[] mTemp = new float[32];
    private final float[] mFoldModelMatrix = new float[16];
    private final float[] mShadowForFlatModelMatrix = new float[16];

    public FoldPage(int width, int height, int foldHeight) {
        mWidth = width;
        mHeight = height;
        mFoldHeight = foldHeight;

        final float[] translateMatrix = new float[16];
        final float[] scaleMatrix = new float[16];

        setIdentityM(scaleMatrix, 0);

        scaleM(scaleMatrix, 0, 2.0f / width, -2.0f / height, 1.0f / foldHeight);
        // 调整xy
        setIdentityM(translateMatrix, 0);
        translateM(translateMatrix, 0, -width / 2f, -height / 2f, foldHeight / 2.0f + 10.0f);
        setIdentityM(mFoldModelMatrix, 0);
        multiplyMM(mFoldModelMatrix, 0, scaleMatrix, 0, translateMatrix, 0);

        setIdentityM(translateMatrix, 0);
        translateM(translateMatrix, 0, -width / 2f, -height / 2f, 5.0f);
        setIdentityM(mShadowForFlatModelMatrix, 0);
        multiplyMM(mShadowForFlatModelMatrix, 0, scaleMatrix, 0, translateMatrix, 0);

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
        sFoldProgram.use();

        multiplyMM(mMVPMatrix, 0, viewProjectionMatrix, 0, mFoldModelMatrix, 0);
        sFoldProgram.setUniformMatrix4fv(U_MVP_MATRIX, mMVPMatrix);
        sFoldProgram.setUniform2f(U_PAGE_SIZE, mWidth, mHeight);
        sFoldProgram.setUniform2f(U_DRAG_POINT, mDragPoint.x, mDragPoint.y);
        sFoldProgram.setUniform2f(U_ORIGIN_POINT, mOriginPoint.x, mOriginPoint.y);
        sFoldProgram.setUniform3fv(U_LIGHT_DIRECTION, light.getDirection());
        sFoldProgram.setUniform3fv(U_LIGHT_AMBIENT, light.getAmbient());
        sFoldProgram.setUniform3fv(U_LIGHT_DIFFUSE, light.getDiffuse());
        sFoldProgram.setUniform3fv(U_LIGHT_SPECULAR, light.getSpecular());
        sFoldProgram.setUniform3fv(U_LIGHT_COLOR, light.getColor());
        sFoldProgram.setUniform1f(U_FOLD_HEIGHT, mFoldHeight);

        invertM(mTemp, 0, mMVPMatrix, 0);
        transposeM(mTemp, 16, mTemp, 0);
        mTemp[4] = eyePos[0];
        mTemp[5] = eyePos[1];
        mTemp[6] = eyePos[2];
        mTemp[7] = 0;
        multiplyMV(mTemp, 0, mTemp, 16, mTemp, 4);
        sFoldProgram.setUniform3fv(U_VIEW_POS, mTemp);

        mVertexData.position(0);
        sFoldProgram.setVertexAttribPointer(A_POSITION, POSITION_COMPONENT_COUNT, mVertexData);

        GLES20.glActiveTexture(GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_2D, mTextureId);
        sFoldProgram.setUniform1i(U_TEXTURE_UNIT, 0);

        GLES20.glDrawArrays(GL_TRIANGLES, 0, mVertexData.limit() / POSITION_COMPONENT_COUNT);
    }

    public void drawShadowForFlat(float[] viewProjectionMatrix) {
        sShadowOnFlatProgram.use();

        multiplyMM(mMVPMatrix, 0, viewProjectionMatrix, 0, mShadowForFlatModelMatrix, 0);
        sShadowOnFlatProgram.setUniformMatrix4fv(U_MVP_MATRIX, mMVPMatrix);
        sShadowOnFlatProgram.setUniform2f(U_PAGE_SIZE, mWidth, mHeight);
        sShadowOnFlatProgram.setUniform2f(U_DRAG_POINT, mDragPoint.x, mDragPoint.y);
        sShadowOnFlatProgram.setUniform2f(U_ORIGIN_POINT, mOriginPoint.x, mOriginPoint.y);
        sShadowOnFlatProgram.setUniform1f(U_FOLD_HEIGHT, mFoldHeight);

        mVertexData.position(0);
        sShadowOnFlatProgram.setVertexAttribPointer(A_POSITION, POSITION_COMPONENT_COUNT, mVertexData);

        GLES20.glDrawArrays(GL_TRIANGLES, 0, mVertexData.limit() / POSITION_COMPONENT_COUNT);
    }

    public void drawShadowForSelf(float[] viewProjectionMatrix) {
        sShadowOnFoldProgram.use();

        multiplyMM(mMVPMatrix, 0, viewProjectionMatrix, 0, mFoldModelMatrix, 0);
        sShadowOnFoldProgram.setUniformMatrix4fv(U_MVP_MATRIX, mMVPMatrix);
        sShadowOnFoldProgram.setUniform2f(U_PAGE_SIZE, mWidth, mHeight);
        sShadowOnFoldProgram.setUniform2f(U_DRAG_POINT, mDragPoint.x, mDragPoint.y);
        sShadowOnFoldProgram.setUniform2f(U_ORIGIN_POINT, mOriginPoint.x, mOriginPoint.y);
        sShadowOnFoldProgram.setUniform1f(U_FOLD_HEIGHT, mFoldHeight);

        mVertexData.position(0);
        sShadowOnFoldProgram.setVertexAttribPointer(A_POSITION, POSITION_COMPONENT_COUNT, mVertexData);

        GLES20.glDrawArrays(GL_TRIANGLES, 0, mVertexData.limit() / POSITION_COMPONENT_COUNT);
    }

    public void fold(float originX, float originY, float dragX, float dragY) {
        mOriginPoint.set(originX, originY);
        mDragPoint.set(dragX, dragY);
    }
}

/*
 *    Copyright 2019 Huntto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.ihuntto.bookreader.ui.gl.shape;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;

import com.ihuntto.bookreader.ui.gl.light.Light;
import com.ihuntto.bookreader.ui.gl.program.ShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

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
    private static ShaderProgram sShadowRightProgram;
    private static ShaderProgram sShadowLeftProgram;

    public static void initProgram(Context context) {
        sFoldProgram = new ShaderProgram(context,
                "fold_page.vert",
                "fold_page.frag");
        sFoldProgram.compile();

        sShadowRightProgram = new ShaderProgram(context,
                "fold_page_shadow_right.vert",
                "fold_page_shadow_right.frag");
        sShadowRightProgram.compile();

        sShadowLeftProgram = new ShaderProgram(context,
                "fold_page_shadow_left.vert",
                "fold_page_shadow_left.frag");
        sShadowLeftProgram.compile();
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
    private final float[] mShadowRightModelMatrix = new float[16];

    public FoldPage(int width, int height) {
        mWidth = width;
        mHeight = height;
        mFoldHeight = height;

        final float[] translateMatrix = new float[16];
        final float[] scaleMatrix = new float[16];

        setIdentityM(scaleMatrix, 0);

        scaleM(scaleMatrix, 0, 2.0f / width, -2.0f / height, 1.0f / height);
        // 调整xy
        setIdentityM(translateMatrix, 0);
        translateM(translateMatrix, 0, -width / 2f, -height / 2f, height / 2.0f + 2.0f);
        setIdentityM(mFoldModelMatrix, 0);
        multiplyMM(mFoldModelMatrix, 0, scaleMatrix, 0, translateMatrix, 0);

        setIdentityM(translateMatrix, 0);
        translateM(translateMatrix, 0, -width / 2f, -height / 2f, 1.0f);
        setIdentityM(mShadowRightModelMatrix, 0);
        multiplyMM(mShadowRightModelMatrix, 0, scaleMatrix, 0, translateMatrix, 0);

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
        sFoldProgram.setUniform3fv(U_VIEW_POS, eyePos);

        mVertexData.position(0);
        sFoldProgram.setVertexAttribPointer(A_POSITION, POSITION_COMPONENT_COUNT, mVertexData);

        GLES20.glActiveTexture(GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_2D, mTextureId);
        sFoldProgram.setUniform1i(U_TEXTURE_UNIT, 0);

        GLES20.glDrawArrays(GL_TRIANGLES, 0, mVertexData.limit() / POSITION_COMPONENT_COUNT);
    }

    @Override
    public void drawShadow(Light light, float[] viewProjectionMatrix) {
        drawShadowLeft(viewProjectionMatrix);
        drawShadowRight(viewProjectionMatrix);
    }

    private void drawShadowRight(float[] viewProjectionMatrix) {
        sShadowRightProgram.use();

        multiplyMM(mMVPMatrix, 0, viewProjectionMatrix, 0, mShadowRightModelMatrix, 0);
        sShadowRightProgram.setUniformMatrix4fv(U_MVP_MATRIX, mMVPMatrix);
        sShadowRightProgram.setUniform2f(U_PAGE_SIZE, mWidth, mHeight);
        sShadowRightProgram.setUniform2f(U_DRAG_POINT, mDragPoint.x, mDragPoint.y);
        sShadowRightProgram.setUniform2f(U_ORIGIN_POINT, mOriginPoint.x, mOriginPoint.y);
        sShadowRightProgram.setUniform1f(U_FOLD_HEIGHT, mFoldHeight);

        mVertexData.position(0);
        sShadowRightProgram.setVertexAttribPointer(A_POSITION, POSITION_COMPONENT_COUNT, mVertexData);

        GLES20.glDrawArrays(GL_TRIANGLES, 0, mVertexData.limit() / POSITION_COMPONENT_COUNT);
    }

    private void drawShadowLeft(float[] viewProjectionMatrix) {
        sShadowLeftProgram.use();

        multiplyMM(mMVPMatrix, 0, viewProjectionMatrix, 0, mFoldModelMatrix, 0);
        sShadowLeftProgram.setUniformMatrix4fv(U_MVP_MATRIX, mMVPMatrix);
        sShadowLeftProgram.setUniform2f(U_PAGE_SIZE, mWidth, mHeight);
        sShadowLeftProgram.setUniform2f(U_DRAG_POINT, mDragPoint.x, mDragPoint.y);
        sShadowLeftProgram.setUniform2f(U_ORIGIN_POINT, mOriginPoint.x, mOriginPoint.y);
        sShadowLeftProgram.setUniform1f(U_FOLD_HEIGHT, mFoldHeight);

        mVertexData.position(0);
        sShadowLeftProgram.setVertexAttribPointer(A_POSITION, POSITION_COMPONENT_COUNT, mVertexData);

        GLES20.glDrawArrays(GL_TRIANGLES, 0, mVertexData.limit() / POSITION_COMPONENT_COUNT);
    }

    public void fold(float originX, float originY, float dragX, float dragY) {
        mOriginPoint.set(originX, originY);
        mDragPoint.set(dragX, dragY);

        float cx = (originX + dragX) / 2;
        float cy = (originY + dragY) / 2;

        int candidateX = (int) (mWidth - cx);
        int candidateY = (int) (mHeight - cy);
        mFoldHeight = candidateX > candidateY ? candidateY : candidateX;
    }
}

package com.ihuntto.bookreader.ui.gl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.Log;

import com.ihuntto.bookreader.BuildConfig;
import com.ihuntto.bookreader.R;
import com.ihuntto.bookreader.ui.gl.util.ShaderHelper;
import com.ihuntto.bookreader.ui.gl.util.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform2f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLUtils.texImage2D;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

public final class PageMesh {
    private static final boolean D = BuildConfig.DEBUG;
    private static final String TAG = PageMesh.class.getSimpleName();

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int BYTES_PER_FLOAT = 4;

    private static final String U_VIEW_MATRIX = "uViewMatrix";
    private static final String U_MODEL_MATRIX = "uModelMatrix";
    private static final String U_PROJECTION_MATRIX = "uProjectionMatrix";
    private static final String U_TEXTURE_UNIT = "uTextureUnit";
    private static final String U_IS_FLAT = "uIsFlat";
    private static final String U_ORIGIN_POINT = "uOriginPoint";
    private static final String U_DRAG_POINT = "uDragPoint";
    private static final String U_SIZE = "uSize";
    private static final String U_MAX_FOLD_HEIGHT = "uMaxFoldHeight";
    private static final String U_BASE_FOLD_HEIGHT = "uBaseFoldHeight";
    private static final String U_FLAT_HEIGHT = "uFlatHeight";

    private static final String A_POSITION = "aPosition";

    private static FloatBuffer sVertexData;
    private static int sProgram;

    private static int uViewMatrixLocation;
    private static int uModelMatrixLocation;
    private static int uProjectionMatrixLocation;
    private static int uTextureUnitLocation;
    private static int uIsFlatLocation;
    private static int uOriginLocation;
    private static int uDragLocation;
    private static int uSizeLocation;
    private static int uMaxFoldHeightLocation;
    private static int uBaseFoldHeightLocation;
    private static int uFlatHeightLocation;

    private static int aPositionLocation;
    private static int sWidth;
    private static int sHeight;
    public static float sMaxFoldHeight;
    private static float sBaseFoldHeight;
    private static float sFlatHeight;

    public static void initProgram(Context context) {
        String vertexShaderSource = TextResourceReader.readTextFromResource(context, R.raw.texture_vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFromResource(context, R.raw.texture_fragment_shader);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);
        sProgram = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        if (D) {
            ShaderHelper.validateProgram(sProgram);
        }
        glUseProgram(sProgram);

        uViewMatrixLocation = glGetUniformLocation(sProgram, U_VIEW_MATRIX);
        uModelMatrixLocation = glGetUniformLocation(sProgram, U_MODEL_MATRIX);
        uProjectionMatrixLocation = glGetUniformLocation(sProgram, U_PROJECTION_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(sProgram, U_TEXTURE_UNIT);
        uIsFlatLocation = glGetUniformLocation(sProgram, U_IS_FLAT);
        uOriginLocation = glGetUniformLocation(sProgram, U_ORIGIN_POINT);
        uDragLocation = glGetUniformLocation(sProgram, U_DRAG_POINT);
        uSizeLocation = glGetUniformLocation(sProgram, U_SIZE);
        uMaxFoldHeightLocation = glGetUniformLocation(sProgram, U_MAX_FOLD_HEIGHT);
        uBaseFoldHeightLocation = glGetUniformLocation(sProgram, U_BASE_FOLD_HEIGHT);
        uFlatHeightLocation = glGetUniformLocation(sProgram, U_FLAT_HEIGHT);

        aPositionLocation = glGetAttribLocation(sProgram, A_POSITION);
    }

    public static void updateMesh(int width, int height) {
        sWidth = width;
        sHeight = height;
        sBaseFoldHeight = 1.0f;
        sFlatHeight = 0.0f;
        sMaxFoldHeight = width / 5.0f + sBaseFoldHeight;

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

        sVertexData = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        sVertexData.put(vertices);
    }

    private long mBitmapHashCode = 0;
    private int mTextureId;
    private boolean mIsFlat;
    private final float[] mModelMatrix = new float[16];
    private final PointF mOriginPoint = new PointF();
    private final PointF mDragPoint = new PointF();

    public PageMesh() {
        final float[] translateMatrix = new float[16];
        final float[] scaleMatrix = new float[16];

        setIdentityM(translateMatrix, 0);
        setIdentityM(scaleMatrix, 0);

        // 调整xy
        setIdentityM(mModelMatrix, 0);
        translateM(translateMatrix, 0, -sWidth / 2f, -sHeight / 2f, 0f);
        scaleM(scaleMatrix, 0, 2.0f / sHeight, -2.0f / sHeight, 1.0f);
        multiplyMM(mModelMatrix, 0, scaleMatrix, 0, translateMatrix, 0);

        final float[] temp1 = new float[16];
        setIdentityM(scaleMatrix, 0);
        scaleM(scaleMatrix, 0, -1.0f, 1.0f, 1.0f);
        multiplyMM(temp1, 0, scaleMatrix, 0, mModelMatrix, 0);
        System.arraycopy(temp1, 0, mModelMatrix, 0, 16);

        // 调整z
        final float[] temp2 = new float[16];
        setIdentityM(scaleMatrix, 0);
        scaleM(scaleMatrix, 0, 1.0f, 1.0f, -1.0f / sMaxFoldHeight / 10.0f);
        setIdentityM(translateMatrix, 0);
        translateM(translateMatrix, 0, 0.0f, 0.0f, 1.0f);
        multiplyMM(temp1, 0, translateMatrix, 0, scaleMatrix, 0);
        multiplyMM(temp2, 0, mModelMatrix, 0, temp1, 0);
        System.arraycopy(temp2, 0, mModelMatrix, 0, 16);
    }

    public void updateTexture(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }

        if (mBitmapHashCode != bitmap.hashCode()) {
            final int[] textureObjectIds = new int[1];
            if (mTextureId != 0) {
                textureObjectIds[0] = mTextureId;
                glDeleteTextures(1, textureObjectIds, 0);
                textureObjectIds[0] = 0;
            }
            glGenTextures(1, textureObjectIds, 0);

            if (textureObjectIds[0] == 0) {
                if (D) {
                    Log.w(TAG, "Cloud not generate a new OpenGL texture object.");
                }
                return;
            }

            glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

            glGenerateMipmap(GL_TEXTURE_2D);

            glBindTexture(GL_TEXTURE_2D, 0);

            mTextureId = textureObjectIds[0];
            mBitmapHashCode = bitmap.hashCode();
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public void draw(float[] viewMatrix, float[] projectionMatrix) {
        glUseProgram(sProgram);

        glUniformMatrix4fv(uModelMatrixLocation, 1, false, mModelMatrix, 0);
        glUniformMatrix4fv(uViewMatrixLocation, 1, false, viewMatrix, 0);
        glUniformMatrix4fv(uProjectionMatrixLocation, 1, false, projectionMatrix, 0);

        glUniform1f(uIsFlatLocation, mIsFlat ? 1 : 0);
        glUniform2f(uSizeLocation, sWidth, sHeight);
        glUniform2f(uDragLocation, mDragPoint.x, mDragPoint.y);
        glUniform2f(uOriginLocation, mOriginPoint.x, mOriginPoint.y);
        glUniform1f(uMaxFoldHeightLocation, sMaxFoldHeight);
        glUniform1f(uBaseFoldHeightLocation, sBaseFoldHeight);
        glUniform1f(uFlatHeightLocation, sFlatHeight);

        sVertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT,
                false, 0, sVertexData);
        glEnableVertexAttribArray(aPositionLocation);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mTextureId);
        glUniform1i(uTextureUnitLocation, 0);

        glDrawArrays(GL_TRIANGLES, 0, sVertexData.limit() / POSITION_COMPONENT_COUNT);
    }

    public void flat() {
        mIsFlat = true;
    }

    public void fold(float pullOriginX, float pullOriginY, float pullTerminalX, float pullTerminalY) {
        mOriginPoint.set(pullOriginX, pullOriginY);
        mDragPoint.set(pullTerminalX, pullTerminalY);
        mIsFlat = false;
    }
}

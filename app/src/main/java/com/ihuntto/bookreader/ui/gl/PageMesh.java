package com.ihuntto.bookreader.ui.gl;

import android.content.Context;
import android.graphics.Bitmap;
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
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
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
    private static final int COORDINATES_COMPONENT_COUNT = 2;
    private static final int BYTES_PER_FLOAT = 4;

    private static final String U_MATRIX = "uMatrix";
    private static final String U_TEXTURE_UNIT = "uTextureUnit";
    private static final String U_FLAT = "uFlat";
    private static final String U_FOLD_LINE_POINT1 = "uFoldLinePoint1";
    private static final String U_FOLD_LINE_POINT2 = "uFoldLinePoint2";

    private static final String A_POSITION = "aPosition";
    private static final String A_TEXTURE_COORDINATES = "aTextureCoordinates";

    private static FloatBuffer sVertexData;
    private static FloatBuffer sTextureCoordinatesData;
    private static int sProgram;

    private static int uMatrixLocation;
    private static int uTextureUnitLocation;
    private static int uFlatLocation;
    private static int uFoldLinePoint1Location;
    private static int uFoldLinePoint2Location;

    private static int aPositionLocation;
    private static int aTextureCoordinatesLocation;
    private static int sWidth;
    private static int sHeight;

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

        uMatrixLocation = glGetUniformLocation(sProgram, U_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(sProgram, U_TEXTURE_UNIT);
        uFlatLocation = glGetUniformLocation(sProgram, U_FLAT);
        uFoldLinePoint1Location = glGetUniformLocation(sProgram, U_FOLD_LINE_POINT1);
        uFoldLinePoint2Location = glGetUniformLocation(sProgram, U_FOLD_LINE_POINT2);

        aPositionLocation = glGetAttribLocation(sProgram, A_POSITION);
        aTextureCoordinatesLocation = glGetAttribLocation(sProgram, A_TEXTURE_COORDINATES);
    }

    public static void updateMesh(int width, int height) {
        sWidth = width;
        sHeight = height;

        final int step = 5;

        for (int w = 0; w < sWidth; w += step) {
            for (int h = 0; h < sHeight; h += step) {

            }
        }

        final float[] vertices = {
                0, sHeight,         // left bottom
                0, 0,               // left top
                sWidth, sHeight,    // right bottom
                sWidth, 0           // right top
        };

        final float[] textureCoordinates = {
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };
        sVertexData = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        sVertexData.put(vertices);

        sTextureCoordinatesData = ByteBuffer.allocateDirect(textureCoordinates.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        sTextureCoordinatesData.put(textureCoordinates);
    }

    private long mBitmapHashCode = 0;
    private int mTextureId;
    private boolean mIsFlat;
    private final float[] mModelMatrix = new float[16];
    private final float[] mTranslateMatrix = new float[16];
    private final float[] mScaleMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];

    public PageMesh() {
        setIdentityM(mModelMatrix, 0);
        setIdentityM(mTranslateMatrix, 0);
        setIdentityM(mScaleMatrix, 0);
        translateM(mTranslateMatrix, 0, sWidth / 2f, -sHeight / 2f, 0f);
        scaleM(mScaleMatrix, 0, 2.0f / sHeight, -2.0f / sHeight, 1.0f);
        multiplyMM(mModelMatrix, 0, mScaleMatrix, 0, mTranslateMatrix, 0);
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

    public void draw(float[] viewProjectionMatrix) {
        glUseProgram(sProgram);

        multiplyMM(mMVPMatrix, 0, viewProjectionMatrix, 0, mModelMatrix, 0);
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0);

        glUniform1f(uFlatLocation, mIsFlat ? 1 : 0);

        sVertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT,
                false, 0, sVertexData);
        glEnableVertexAttribArray(aPositionLocation);

        sTextureCoordinatesData.position(0);
        glVertexAttribPointer(aTextureCoordinatesLocation, COORDINATES_COMPONENT_COUNT, GL_FLOAT,
                false, 0, sTextureCoordinatesData);
        glEnableVertexAttribArray(aTextureCoordinatesLocation);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mTextureId);
        glUniform1i(uTextureUnitLocation, 0);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    }

    public void flat() {
        mIsFlat = true;
    }

    public void fold(float pullOriginX, float pullOriginY, float pullTerminalX, float pullTerminalY) {
        mIsFlat = false;
    }
}

package com.ihuntto.bookreader.ui.gl.shape;

import android.content.Context;

import com.ihuntto.bookreader.ui.gl.light.Light;
import com.ihuntto.bookreader.ui.gl.program.ShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

public class FlatPage extends Page {
    private static final String U_MVP_MATRIX = "uMVPMatrix";
    private static final String U_PAGE_SIZE = "uPageSize";
    private static final String U_TEXTURE_UNIT = "uTextureUnit";
    // light
    private static final String U_LIGHT_DIRECTION = "uLight.direction";
    private static final String U_LIGHT_AMBIENT = "uLight.ambient";
    private static final String U_LIGHT_DIFFUSE = "uLight.diffuse";
    private static final String U_LIGHT_SPECULAR = "uLight.specular";
    private static final String U_LIGHT_COLOR = "uLight.color";
    private static final String U_VIEW_POS = "uViewPos";

    private static final String A_POSITION = "aPosition";


    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int BYTES_PER_FLOAT = 4;

    private FloatBuffer mVertexData;

    private int mWidth;
    private int mHeight;
    private static ShaderProgram sProgram;

    public static void initProgram(Context context) {
        sProgram = new ShaderProgram(context,
                "flat_page.vert",
                "flat_page.frag");
        sProgram.compile();
    }

    private final float[] mModelMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];
    private final float[] mTemp = new float[32];

    public FlatPage(int width, int height) {
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


        final float[] translateMatrix = new float[16];
        final float[] scaleMatrix = new float[16];

        setIdentityM(translateMatrix, 0);
        setIdentityM(scaleMatrix, 0);

        // 调整xy
        setIdentityM(mModelMatrix, 0);
        translateM(translateMatrix, 0, -width / 2f, -height / 2f, 0f);
        scaleM(scaleMatrix, 0, 2.0f / width, -2.0f / height, 1.0f);
        multiplyMM(mModelMatrix, 0, scaleMatrix, 0, translateMatrix, 0);
    }

    @Override
    public void draw(final float[] eyePos, final Light light, float[] viewProjectionMatrix) {
        sProgram.use();

        multiplyMM(mMVPMatrix, 0, viewProjectionMatrix, 0, mModelMatrix, 0);
        sProgram.setUniformMatrix4fv(U_MVP_MATRIX, mMVPMatrix);
        sProgram.setUniform3fv(U_LIGHT_DIRECTION, light.getDirection());
        sProgram.setUniform3fv(U_LIGHT_AMBIENT, light.getAmbient());
        sProgram.setUniform3fv(U_LIGHT_DIFFUSE, light.getDiffuse());
        sProgram.setUniform3fv(U_LIGHT_SPECULAR, light.getSpecular());
        sProgram.setUniform3fv(U_LIGHT_COLOR, light.getColor());
        sProgram.setUniform3fv(U_VIEW_POS, eyePos);

        sProgram.setUniform2f(U_PAGE_SIZE, mWidth, mHeight);

        mVertexData.position(0);
        sProgram.setVertexAttribPointer(A_POSITION, POSITION_COMPONENT_COUNT, mVertexData);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mTextureId);
        sProgram.setUniform1i(U_TEXTURE_UNIT, 0);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, mVertexData.limit() / POSITION_COMPONENT_COUNT);
    }
}

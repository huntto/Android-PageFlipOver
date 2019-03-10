package com.ihuntto.bookreader.ui.gl.shape;

import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

public abstract class Page {
    protected int mTextureId;
    protected final float[] mModelMatrix = new float[16];

    public Page(int width, int height, int maxFoldHeight) {
        final float[] translateMatrix = new float[16];
        final float[] scaleMatrix = new float[16];

        setIdentityM(translateMatrix, 0);
        setIdentityM(scaleMatrix, 0);

        // 调整xy
        setIdentityM(mModelMatrix, 0);
        translateM(translateMatrix, 0, -width / 2f, -height / 2f, 0f);
        scaleM(scaleMatrix, 0, 2.0f / height, -2.0f / height, 1.0f);
        multiplyMM(mModelMatrix, 0, scaleMatrix, 0, translateMatrix, 0);

        final float[] temp1 = new float[16];
        setIdentityM(scaleMatrix, 0);
        scaleM(scaleMatrix, 0, -1.0f, 1.0f, 1.0f);
        multiplyMM(temp1, 0, scaleMatrix, 0, mModelMatrix, 0);
        System.arraycopy(temp1, 0, mModelMatrix, 0, 16);

        // 调整z
        final float[] temp2 = new float[16];
        setIdentityM(scaleMatrix, 0);
        scaleM(scaleMatrix, 0, 1.0f, 1.0f, -1.0f / maxFoldHeight / 10.0f);
        setIdentityM(translateMatrix, 0);
        translateM(translateMatrix, 0, 0.0f, 0.0f, 1.0f);
        multiplyMM(temp1, 0, translateMatrix, 0, scaleMatrix, 0);
        multiplyMM(temp2, 0, mModelMatrix, 0, temp1, 0);
        System.arraycopy(temp2, 0, mModelMatrix, 0, 16);
    }

    public void setTexture(int textureId) {
        mTextureId = textureId;
    }

    public abstract void draw(float[] viewMatrix, float[] projectionMatrix);
}

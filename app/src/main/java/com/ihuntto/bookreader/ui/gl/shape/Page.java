package com.ihuntto.bookreader.ui.gl.shape;

import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

public abstract class Page {
    protected int mTextureId;
    protected final float[] mModelMatrix = new float[16];

    protected float[] mLightDirection = new float[]{-1.0f, 0.0f, -8.0f};
    protected float[] mLightAmbient = new float[]{0.2f, 0.2f, 0.2f};
    protected float[] mLightDiffuse = new float[]{0.8f, 0.8f, 0.8f};
    // 镜面光尽量小
    protected float[] mLightSpecular = new float[]{0.1f, 0.1f, 0.1f};

    protected float[] mLightColor = new float[]{1.0f, 1.0f, 1.0f};

    public Page(int width, int height, int maxFoldHeight) {
        final float[] translateMatrix = new float[16];
        final float[] scaleMatrix = new float[16];

        setIdentityM(translateMatrix, 0);
        setIdentityM(scaleMatrix, 0);

        // 调整xy
        setIdentityM(mModelMatrix, 0);
        translateM(translateMatrix, 0, -width / 2f, -height / 2f, 0f);
        // 右手系统
        scaleM(scaleMatrix, 0, 2.0f / width, -2.0f / height, 1.0f / maxFoldHeight);
        multiplyMM(mModelMatrix, 0, scaleMatrix, 0, translateMatrix, 0);
    }

    public void setTexture(int textureId) {
        mTextureId = textureId;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public abstract void draw(final float[] eyePos, float[] viewProjectionMatrix);
}

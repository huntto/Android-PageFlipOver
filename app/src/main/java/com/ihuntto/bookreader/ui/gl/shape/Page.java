package com.ihuntto.bookreader.ui.gl.shape;

import com.ihuntto.bookreader.ui.gl.light.Light;

public abstract class Page {
    protected int mTextureId;

    public void setTexture(int textureId) {
        mTextureId = textureId;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public abstract void draw(final float[] eyePos, final Light light, float[] viewProjectionMatrix);
}

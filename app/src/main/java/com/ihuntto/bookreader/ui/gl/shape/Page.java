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

import com.ihuntto.bookreader.ui.gl.light.Light;

public abstract class Page {
    protected int mTextureId;

    public void setTexture(int textureId) {
        mTextureId = textureId;
    }

    public abstract void draw(final float[] eyePos, final Light light, float[] viewProjectionMatrix);

    public void drawShadow(final Light light, float[] viewProjectionMatrix) {
        throw new RuntimeException("Not supported");
    }
}

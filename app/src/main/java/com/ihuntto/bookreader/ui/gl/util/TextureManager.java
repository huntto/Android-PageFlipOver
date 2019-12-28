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

package com.ihuntto.bookreader.ui.gl.util;

import android.graphics.Bitmap;
import android.util.Log;

import com.ihuntto.bookreader.BuildConfig;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLUtils.texImage2D;

public class TextureManager {
    private static final boolean D = BuildConfig.DEBUG;
    private static final String TAG = TextureManager.class.getSimpleName();

    private int[] mTextureIds;

    public static TextureManager getInstance() {
        return TextureManagerHolder.sTextureManager;
    }

    private static class TextureManagerHolder {
        private static TextureManager sTextureManager = new TextureManager();
    }

    public void create(int capacity) {
        mTextureIds = new int[capacity];
    }

    public void destroy() {
        if (mTextureIds != null) {
            glDeleteTextures(mTextureIds.length, mTextureIds, 0);
            mTextureIds = null;
        }
    }

    public int updateTextureIfAbsent(int index, Bitmap bitmap) {
        if (mTextureIds != null) {
            if (mTextureIds[index] <= 0) {
                return updateTexture(index, bitmap);
            }
        }
        return 0;
    }

    public int updateTexture(int index, Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return 0;
        }

        final int[] textureObjectIds = new int[1];
        glGenTextures(1, textureObjectIds, 0);

        if (textureObjectIds[0] == 0) {
            if (D) {
                Log.w(TAG, "Cloud not generate a new OpenGL texture object.");
            }
            return 0;
        }

        glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

        glGenerateMipmap(GL_TEXTURE_2D);

        glBindTexture(GL_TEXTURE_2D, 0);

        mTextureIds[index] = textureObjectIds[0];
        return mTextureIds[index];
    }

    public int getTexture(int index) {
        if (mTextureIds != null) {
            return mTextureIds[index];
        } else {
            return 0;
        }
    }

}

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

package com.ihuntto.bookreader.ui.gl.light;

public class Light {

    // 平行光照方向
    protected final float[] mDirection = new float[3];
    // 环境光系数
    protected final float[] mAmbient = new float[3];
    // 散射光系数
    protected final float[] mDiffuse = new float[3];
    // 镜面光系数
    protected final float[] mSpecular = new float[3];
    // 点光源位置
    protected final float[] mPosition = new float[3];

    protected final float[] mColor = new float[3];

    protected Light() {
    }

    public static class Builder {
        private Light mLight;

        public Builder() {
            mLight = new Light();
        }

        public Builder direction(float dx, float dy, float dz) {
            mLight.mDirection[0] = dx;
            mLight.mDirection[1] = dy;
            mLight.mDirection[2] = dz;
            return this;
        }

        public Builder ambient(float ra, float ga, float ba) {
            mLight.mAmbient[0] = ra;
            mLight.mAmbient[1] = ga;
            mLight.mAmbient[2] = ba;
            return this;
        }

        public Builder diffuse(float rd, float gd, float bd) {
            mLight.mDiffuse[0] = rd;
            mLight.mDiffuse[1] = gd;
            mLight.mDiffuse[2] = bd;
            return this;
        }

        public Builder specular(float rs, float gs, float bs) {
            mLight.mSpecular[0] = rs;
            mLight.mSpecular[1] = gs;
            mLight.mSpecular[2] = bs;
            return this;
        }


        public Builder color(float r, float g, float b) {
            mLight.mColor[0] = r;
            mLight.mColor[1] = g;
            mLight.mColor[2] = b;
            return this;
        }

        public Builder position(float x, float y, float z) {
            mLight.mPosition[0] = x;
            mLight.mPosition[1] = y;
            mLight.mPosition[2] = z;
            return this;
        }

        public Light create() {
            return mLight;
        }
    }

    public float[] getDirection() {
        return mDirection;
    }

    public float[] getAmbient() {
        return mAmbient;
    }

    public float[] getDiffuse() {
        return mDiffuse;
    }

    public float[] getSpecular() {
        return mSpecular;
    }

    public float[] getPosition() {
        return mPosition;
    }

    public float[] getColor() {
        return mColor;
    }
}

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

package com.ihuntto.bookreader.flip;

import android.graphics.Bitmap;

public class FlipOverPage {
    private Bitmap mLeftPageBitmap;
    private Bitmap mCurrentPageBitmap;
    private Bitmap mRightPageBitmap;

    public FlipOverPage(Bitmap leftPageBitmap, Bitmap currentPageBitmap, Bitmap rightPageBitmap) {
        mLeftPageBitmap = leftPageBitmap;
        mCurrentPageBitmap = currentPageBitmap;
        mRightPageBitmap = rightPageBitmap;
    }

    public Bitmap getLeftPageBitmap() {
        return mLeftPageBitmap;
    }

    public Bitmap getCurrentPageBitmap() {
        return mCurrentPageBitmap;
    }

    public Bitmap getRightPageBitmap() {
        return mRightPageBitmap;
    }
}

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

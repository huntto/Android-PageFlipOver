package com.ihuntto.bookreader.ui.gl.shape;

import android.graphics.PointF;

public class TriangleShadow extends Page {

    private PointF mDragPoint = new PointF();
    private PointF mOriginPoint = new PointF();

    public TriangleShadow(int width, int height, int baseFoldHeight, int maxFoldHeight) {
        super(width, height, maxFoldHeight);

    }

    @Override
    public void draw(float[] eyePos, float[] viewProjectionMatrix) {

    }


    public void fold(float originX, float originY, float dragX, float dragY) {
        mOriginPoint.set(originX, originY);
        mDragPoint.set(dragX, dragY);
    }
}

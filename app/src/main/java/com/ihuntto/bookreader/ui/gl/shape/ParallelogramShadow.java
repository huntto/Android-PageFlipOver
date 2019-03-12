package com.ihuntto.bookreader.ui.gl.shape;

import android.graphics.PointF;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ParallelogramShadow extends Page {

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int BYTES_PER_FLOAT = 4;

    private int mWidth;
    private int mHeight;
    private int mFlatHeight;
    private PointF mDragPoint = new PointF();
    private PointF mOriginPoint = new PointF();

    private FloatBuffer mVertexData;
    private float[] mVertices;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mTemp = new float[32];

    public ParallelogramShadow(int width, int height, int flatHeight, int maxFoldHeight) {
        super(width, height, maxFoldHeight);

        mWidth = width;
        mHeight = height;
        mFlatHeight = flatHeight;

        mVertices = new float[4 * POSITION_COMPONENT_COUNT];

        mVertexData = ByteBuffer.allocateDirect(mVertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexData.put(mVertices);
    }

    public void fold(float originX, float originY, float dragX, float dragY) {
        mOriginPoint.set(originX, originY);
        mDragPoint.set(dragX, dragY);
    }

    @Override
    public void draw(float[] eyePos, float[] viewProjectionMatrix) {

    }
}

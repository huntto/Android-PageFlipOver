package com.ihuntto.bookreader.ui.gl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import com.ihuntto.bookreader.BuildConfig;
import com.ihuntto.bookreader.flip.FlipOver;
import com.ihuntto.bookreader.flip.FlipOverPage;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glEnable;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.perspectiveM;
import static android.opengl.Matrix.setLookAtM;

final class FlipOverRenderer implements GLSurfaceView.Renderer {
    private static final boolean D = BuildConfig.DEBUG;
    private static final String TAG = FlipOverRenderer.class.getSimpleName();

    private static final int STATE_FLIP_NONE = 0x00;
    private static final int STATE_FLIP_TO_LEFT = 0x01;
    private static final int STATE_FLIP_TO_RIGHT = 0x02;

    private int mFlipState = STATE_FLIP_NONE;

    private FlipOver.PageProvider mPageProvider;
    private int mCurrentPageIndex;
    private int mWidth;
    private int mHeight;
    private GLSurfaceView mGLSurfaceView;

    private float mAnchorY;

    private float mTargetX;
    private float mTargetY;

    private float mCurrentX;
    private float mCurrentY;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    private SparseArray<PageMesh> mPageMeshes;
    private Context mContext;

    public FlipOverRenderer(GLSurfaceView surfaceView) {
        mGLSurfaceView = surfaceView;
        mContext = surfaceView.getContext();
        mPageMeshes = new SparseArray<>();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        PageMesh.initProgram(mContext);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;

        GLES20.glViewport(0, 0, width, height);
        perspectiveM(mProjectionMatrix, 0, 45, (float) width / (float) height, 1f, 10f);
        setLookAtM(mViewMatrix, 0, 0f, 0f, -1.5f, 0f, 0f, 0f, 0f, 1f, 0f);

        PageMesh.updateMesh(width, height);
    }

    private void update() {
        if (!isFlipping()) {
            return;
        }
        if (D) {
            Log.d(TAG, "before update state=" + mFlipState
                    + " currentX=" + mCurrentX + "targetX=" + mTargetX
                    + " currentY=" + mCurrentY + " targetY=" + mTargetY);
        }

        float diffX = mTargetX - mCurrentX;
        while (Math.abs(diffX) > mWidth) {
            diffX /= 2;
        }
        mCurrentX += diffX / 2.5;
        mCurrentY += (mTargetY - mCurrentY + 1) / 2.5;

        if (mCurrentX < -mWidth) {
            if (mFlipState == STATE_FLIP_TO_LEFT) {
                mCurrentPageIndex++;
            }
            mFlipState = STATE_FLIP_NONE;
        } else if (mCurrentX > mWidth) {
            if (mFlipState == STATE_FLIP_TO_RIGHT) {
                mCurrentPageIndex--;
            }
            mFlipState = STATE_FLIP_NONE;
        }
        if (D) {
            Log.d(TAG, "after update state=" + mFlipState
                    + " currentX=" + mCurrentX + "targetX=" + mTargetX
                    + " currentY=" + mCurrentY + " targetY=" + mTargetY);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        update();

        final float[] viewProjectionMatrix = new float[16];
        multiplyMM(viewProjectionMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        if (!isFlipping()) {
            PageMesh pageMesh = getPageMesh(mCurrentPageIndex);
            pageMesh.flat();
            pageMesh.draw(viewProjectionMatrix);
        } else {
            GLES20.glClearColor(1.0f, 0.0f, 1.0f, 1.0f);
            PageMesh foldPage;
            PageMesh flatPage;
            if (mFlipState == STATE_FLIP_TO_LEFT) {
                foldPage = getPageMesh(mCurrentPageIndex);
                flatPage = getPageMesh(mCurrentPageIndex + 1);
            } else {
                foldPage = getPageMesh(mCurrentPageIndex - 1);
                flatPage = getPageMesh(mCurrentPageIndex);
            }
            flatPage.flat();
            foldPage.fold(mWidth, mAnchorY, mCurrentX, mCurrentY);
            flatPage.draw(viewProjectionMatrix);
            foldPage.draw(viewProjectionMatrix);
        }
        if (mFlipState != STATE_FLIP_NONE) {
            mGLSurfaceView.requestRender();
        }
    }

    @NonNull
    private PageMesh getPageMesh(int pageIndex) {
        PageMesh pageMesh = mPageMeshes.get(pageIndex);
        if (pageMesh == null) {
            pageMesh = new PageMesh();
            pageMesh.updateTexture(mPageProvider.updatePage(pageIndex, mWidth, mHeight).getCurrentPageBitmap());
            mPageMeshes.put(pageIndex, pageMesh);
        }
        return pageMesh;
    }

    public void setPageProvider(FlipOver.PageProvider pageProvider) {
        mPageProvider = pageProvider;
    }

    public void setCurrentPageIndex(int currentPageIndex) {
        mCurrentPageIndex = currentPageIndex;
    }

    public int getCurrentPageIndex() {
        return mCurrentPageIndex;
    }

    public void startFlipToSide(Side side, float anchorY) {
        if (mFlipState != STATE_FLIP_NONE) {
            return;
        }
        mAnchorY = anchorY;
        mCurrentY = anchorY;
        if (side == Side.RIGHT) {
            if (mCurrentPageIndex > 0) {
                mCurrentX = -mWidth;
                mFlipState = STATE_FLIP_TO_RIGHT;
            }
        } else if (side == Side.LEFT) {
            if (mCurrentPageIndex < mPageProvider.getPageCount() - 1) {
                mCurrentX = mWidth;
                mFlipState = STATE_FLIP_TO_LEFT;
            }
        }
        mGLSurfaceView.requestRender();
    }

    public void flipTo(float x, float y) {
        if (mFlipState == STATE_FLIP_NONE) {
            return;
        }
        mTargetX = x;
        mTargetY = y;
    }

    public enum Side {
        LEFT,
        RIGHT
    }

    public void endFlipToSide(Side side) {
        if (mFlipState == STATE_FLIP_NONE) {
            return;
        }
        mTargetY = mAnchorY;
        if (side == Side.LEFT) {
            mTargetX = -mWidth - 1;
        } else {
            mTargetX = mWidth + 1;
        }
    }

    public boolean isFlipping() {
        return mFlipState != STATE_FLIP_NONE;
    }
}

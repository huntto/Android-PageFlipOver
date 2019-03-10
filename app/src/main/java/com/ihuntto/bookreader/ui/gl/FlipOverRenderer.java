package com.ihuntto.bookreader.ui.gl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.ihuntto.bookreader.BuildConfig;
import com.ihuntto.bookreader.flip.FlipOver;
import com.ihuntto.bookreader.ui.gl.program.FlatPageShaderProgram;
import com.ihuntto.bookreader.ui.gl.program.FoldPageShaderProgram;
import com.ihuntto.bookreader.ui.gl.shape.FlatPage;
import com.ihuntto.bookreader.ui.gl.shape.FoldPage;
import com.ihuntto.bookreader.ui.gl.util.TextureManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glEnable;
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
    private int mMaxTargetX;
    private int mMinTargetX;
    private GLSurfaceView mGLSurfaceView;

    private float mAnchorY;

    private float mTargetX;
    private float mTargetY;

    private float mCurrentX;
    private float mCurrentY;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    private Context mContext;
    private FlatPage mFlatPage;
    private FoldPage mFoldPage;
    private int mConstraintX;

    private static class Color {
        final float r;
        final float g;
        final float b;
        final float a;

        public Color(float r, float g, float b, float a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }
    }

    private Color mBackgroundColor;

    private static class Vector {
        final float x;
        final float y;

        public Vector(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public FlipOverRenderer(GLSurfaceView surfaceView) {
        mGLSurfaceView = surfaceView;
        mContext = surfaceView.getContext();
        mBackgroundColor = new Color(0.9f, 0.9f, 0.9f, 1.0f);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(mBackgroundColor.r, mBackgroundColor.g, mBackgroundColor.b, mBackgroundColor.a);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glClearColor(mBackgroundColor.r, mBackgroundColor.g, mBackgroundColor.b, mBackgroundColor.a);
        mWidth = width;
        mHeight = height;
        mMaxTargetX = width + 1;
        mMinTargetX = -width - 1;

        GLES20.glViewport(0, 0, width, height);
        perspectiveM(mProjectionMatrix, 0, 45, (float) width / (float) height, 1f, 10f);
        setLookAtM(mViewMatrix, 0, 0f, 0f, -1.5f, 0f, 0f, 0f, 0f, 1f, 0f);

        int flatHeight = 0;
        int baseFoldHeight = 1;
        int maxFoldHeight = (int) (width / 5.0f + baseFoldHeight);
        ;
        FlatPageShaderProgram flatPageShaderProgram = new FlatPageShaderProgram(mContext);
        flatPageShaderProgram.compile();
        mFlatPage = new FlatPage(flatPageShaderProgram, width, height, flatHeight, maxFoldHeight);

        FoldPageShaderProgram foldPageShaderProgram = new FoldPageShaderProgram(mContext);
        foldPageShaderProgram.compile();
        mFoldPage = new FoldPage(foldPageShaderProgram, width, height, baseFoldHeight, maxFoldHeight);

        mConstraintX = maxFoldHeight;
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
        float diffY = mTargetY - mCurrentY;

        float dist = (float) Math.sqrt(diffX * diffX + diffY * diffY);
        if (dist < 3) {
            if (mTargetX == mMinTargetX) {
                if (mFlipState == STATE_FLIP_TO_LEFT) {
                    mCurrentPageIndex++;
                }
                mFlipState = STATE_FLIP_NONE;
            } else if (mTargetX == mMaxTargetX) {
                if (mFlipState == STATE_FLIP_TO_RIGHT) {
                    mCurrentPageIndex--;
                }
                mFlipState = STATE_FLIP_NONE;
            }
        } else {
            while (Math.abs(diffX) > mWidth / 2.5) {
                diffX /= 2;
            }
            // 要保证y要比x先到达
            float nextX = mCurrentX + diffX / 2.5f;
            float nextY = mCurrentY + diffY / 2;

            // 中点
            float x0 = (mWidth + nextX) / 2.0f;
            float y0 = (mAnchorY + nextY) / 2.0f;
            // 拉拽方向
            Vector dragVec = new Vector(nextX - mWidth, nextY - mAnchorY);
            // 中垂线方向 (x-x0, y-y0)
            // 中垂线方方向与拉拽方向垂直
            // (x-x0)*dragVec.x + (y-y0)*dragVec.y = 0
            // 求得上下边的交点
            float crossUpX = (y0 * dragVec.y + x0 * dragVec.x) / dragVec.x;
            float crossDownX = ((y0 - mHeight) * dragVec.y + x0 * dragVec.x) / dragVec.x;
            if ((crossUpX >= mConstraintX && crossDownX >= mConstraintX)
                    || (crossUpX < mConstraintX && crossDownX < mConstraintX)
                    || Math.abs(crossDownX - crossUpX) < mWidth / 4.0f) {
                mCurrentX = nextX;
                mCurrentY = nextY;
            }
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
        GLES20.glClearColor(mBackgroundColor.r, mBackgroundColor.g, mBackgroundColor.b, mBackgroundColor.a);

        update();

        if (!isFlipping()) {
            mFlatPage.setTexture(getPageTextureId(mCurrentPageIndex));
            mFlatPage.draw(mViewMatrix, mProjectionMatrix);
        } else {
            if (mFlipState == STATE_FLIP_TO_LEFT) {
                mFoldPage.setTexture(getPageTextureId(mCurrentPageIndex));
                mFlatPage.setTexture(getPageTextureId(mCurrentPageIndex + 1));
            } else {
                mFoldPage.setTexture(getPageTextureId(mCurrentPageIndex - 1));
                mFlatPage.setTexture(getPageTextureId(mCurrentPageIndex));
            }

            mFlatPage.draw(mViewMatrix, mProjectionMatrix);
            mFoldPage.fold(mWidth, mAnchorY, mCurrentX, mCurrentY);
            mFoldPage.draw(mViewMatrix, mProjectionMatrix);
        }
        if (mFlipState != STATE_FLIP_NONE) {
            mGLSurfaceView.requestRender();
        }
    }

    private int getPageTextureId(int pageIndex) {
        int textureId = TextureManager.getInstance().getTexture(pageIndex);
        if (textureId == 0) {
            textureId = TextureManager.getInstance()
                    .updateTexture(pageIndex, mPageProvider.updatePage(pageIndex, mWidth, mHeight).getCurrentPageBitmap());
        }
        return textureId;
    }

    public void setPageProvider(FlipOver.PageProvider pageProvider) {
        mPageProvider = pageProvider;
        TextureManager.getInstance().create(mPageProvider.getPageCount());
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
            mTargetX = mMinTargetX;
        } else {
            mTargetX = mMaxTargetX;
        }
    }

    public boolean isFlipping() {
        return mFlipState != STATE_FLIP_NONE;
    }
}

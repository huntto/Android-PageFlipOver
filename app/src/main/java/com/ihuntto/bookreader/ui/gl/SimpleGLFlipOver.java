package com.ihuntto.bookreader.ui.gl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import com.ihuntto.bookreader.flip.FlipOver;

public class SimpleGLFlipOver extends GLSurfaceView implements FlipOver {
    private static final long MIN_CLICK_INTERVAL_MILLIS = 200;
    private long mClickDownTime;

    private OnPageFlipListener mOnPageFlipListener;
    private PageProvider mPageProvider;

    private int mTouchSlop;
    private int mActivePointerId = -1;
    private float mDownMotionX;
    private float mDownMotionY;

    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mMinimumVelocity;

    private FlipOverRenderer mFlipOverRenderer;

    public SimpleGLFlipOver(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SimpleGLFlipOver(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setEGLContextClientVersion(2);
        mFlipOverRenderer = new FlipOverRenderer(this);
        setRenderer(mFlipOverRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);

        ViewConfiguration configuration = ViewConfiguration.get(context);
        float density = context.getResources().getDisplayMetrics().density;
        mMinimumVelocity = (int) (400.0F * density);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mTouchSlop = configuration.getScaledPagingTouchSlop();
    }

    @Override
    public void setPageProvider(PageProvider pageProvider) {
        mPageProvider = pageProvider;
        mFlipOverRenderer.setPageProvider(pageProvider);
        mFlipOverRenderer.setCurrentPageIndex(0);
        requestRender();
    }

    @Override
    public void setOnPageFlipListener(OnPageFlipListener onPageFlipListener) {
        mOnPageFlipListener = onPageFlipListener;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return onTouchDown(event);
            case MotionEvent.ACTION_POINTER_DOWN:
                return onTouchPointerDown(event);
            case MotionEvent.ACTION_MOVE:
                return onTouchMove(event);
            case MotionEvent.ACTION_POINTER_UP:
                return onTouchPointerUp(event);
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                return onTouchUp(event);
            default:
                break;
        }
        return true;
    }


    private boolean onTouchDown(MotionEvent event) {
        if (mFlipOverRenderer.isFlipping()) {
            return false;
        }
        mActivePointerId = event.getPointerId(0);
        mClickDownTime = System.currentTimeMillis();
        mDownMotionX = event.getX();
        mDownMotionY = event.getY();
        mVelocityTracker.clear();
        mVelocityTracker.addMovement(event);
        return true;
    }

    private boolean onTouchPointerDown(MotionEvent event) {
        int activePointerIndex = event.getActionIndex();
        mDownMotionX = event.getX(activePointerIndex);
        mDownMotionY = event.getY(activePointerIndex);
        mActivePointerId = event.getPointerId(activePointerIndex);
        return true;
    }

    private boolean onTouchMove(MotionEvent event) {
        if (mPageProvider == null) {
            return false;
        }
        if (mActivePointerId == -1) {
            return false;
        }
        int pointerIndex = event.findPointerIndex(mActivePointerId);
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);
        if (!mFlipOverRenderer.isFlipping()) {
            float xDiff = Math.abs(x - mDownMotionX);
            float yDiff = Math.abs(y - mDownMotionY);
            if (xDiff > (float) mTouchSlop && xDiff > yDiff) {
                if (x > mDownMotionX) {
                    mFlipOverRenderer.startFlipToSide(FlipOverRenderer.Side.RIGHT, mDownMotionY);
                } else if (x < mDownMotionX) {
                    mFlipOverRenderer.startFlipToSide(FlipOverRenderer.Side.LEFT, mDownMotionY);
                }
                if (mFlipOverRenderer.isFlipping()) {
                    mOnPageFlipListener.onFlipStart();
                    mFlipOverRenderer.flipTo(x, y);
                    requestRender();
                }
            }
        } else {
            mFlipOverRenderer.flipTo(x, y);
        }
        return true;
    }

    private boolean onTouchPointerUp(MotionEvent event) {
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            this.mActivePointerId = event.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
        return true;
    }

    private boolean onTouchUp(MotionEvent event) {
        int activePointerIndex = event.findPointerIndex(this.mActivePointerId);
        VelocityTracker velocityTracker = this.mVelocityTracker;
        velocityTracker.computeCurrentVelocity(1000, (float) mMaximumVelocity);
        int velocityX = (int) velocityTracker.getXVelocity(mActivePointerId);
        float x = event.getX(activePointerIndex);
        if (mFlipOverRenderer.isFlipping()) {
            if (Math.abs(velocityX) < mMinimumVelocity) {
                if (x < getWidth() / 2) {
                    // flip to left
                    mFlipOverRenderer.endFlipToSide(FlipOverRenderer.Side.LEFT);
                } else {
                    // flip to right
                    mFlipOverRenderer.endFlipToSide(FlipOverRenderer.Side.RIGHT);
                }
            } else {
                if (velocityX < 0) {
                    mFlipOverRenderer.endFlipToSide(FlipOverRenderer.Side.LEFT);
                } else {
                    mFlipOverRenderer.endFlipToSide(FlipOverRenderer.Side.RIGHT);
                }
            }
        }

        if (!mFlipOverRenderer.isFlipping()
                && Math.abs(velocityX) < mMinimumVelocity
                && System.currentTimeMillis() - mClickDownTime < MIN_CLICK_INTERVAL_MILLIS) {
            handleClickPosition(event.getX(activePointerIndex), event.getY(activePointerIndex));
            return true;
        }
        return false;
    }

    private void handleClickPosition(float x, float y) {
        int leftBoundary = getWidth() / 3;
        int rightBoundary = leftBoundary * 2;
        if (x < leftBoundary) {
            performClickLeftArea();
        } else if (x > rightBoundary) {
            performClickRightArea();
        } else {
            performClickCenterArea();
        }
    }

    private void performClickLeftArea() {
        mFlipOverRenderer.startFlipToSide(FlipOverRenderer.Side.RIGHT, mDownMotionY);
        mFlipOverRenderer.endFlipToSide(FlipOverRenderer.Side.RIGHT);
        requestRender();

        if (mOnPageFlipListener != null) {
            mOnPageFlipListener.onFlipStart();
        }
    }

    private void performClickRightArea() {
        // flip to left
        mFlipOverRenderer.startFlipToSide(FlipOverRenderer.Side.LEFT, mDownMotionY);
        mFlipOverRenderer.endFlipToSide(FlipOverRenderer.Side.LEFT);
        requestRender();

        if (mOnPageFlipListener != null) {
            mOnPageFlipListener.onFlipStart();
        }
    }

    private void performClickCenterArea() {
        if (mOnPageFlipListener != null) {
            mOnPageFlipListener.onPageClick();
        }
    }

}

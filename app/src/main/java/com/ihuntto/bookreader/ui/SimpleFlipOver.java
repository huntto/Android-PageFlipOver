package com.ihuntto.bookreader.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import com.ihuntto.bookreader.flip.FlipOver;
import com.ihuntto.bookreader.flip.FlipOverPage;

public class SimpleFlipOver extends View implements FlipOver {
    private static final int STATE_FLIP_NONE = 0x00;
    private static final int STATE_FLIP_TO_LEFT = 0x01;
    private static final int STATE_FLIP_TO_RIGHT = 0x02;

    private int mFlipState = STATE_FLIP_NONE;

    private static final long MIN_CLICK_INTERVAL_MILLIS = 200;
    private long mClickDownTime;

    private OnPageFlipListener mOnPageFlipListener;
    private PageProvider mPageProvider;
    private int mCurrentPageIndex = 0;

    private int mTouchSlop;
    private int mActivePointerId = -1;
    private float mDownMotionX;
    private float mDownMotionY;
    private int mAnimateMinStep;
    private int mTargetX;
    private int mPageSplitX;

    private Rect mBitmapDrawSrcRect;
    private Rect mBitmapDrawDstRect;
    private Bitmap mLeftBitmap;
    private Bitmap mRightBitmap;

    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mMinimumVelocity;

    public SimpleFlipOver(Context context) {
        super(context);
        init(context);
    }

    public SimpleFlipOver(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SimpleFlipOver(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public SimpleFlipOver(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        ViewConfiguration configuration = ViewConfiguration.get(context);
        float density = context.getResources().getDisplayMetrics().density;
        mMinimumVelocity = (int) (400.0F * density);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mTouchSlop = configuration.getScaledPagingTouchSlop();

        mAnimateMinStep = context.getResources().getDisplayMetrics().widthPixels / 100;

        mBitmapDrawSrcRect = new Rect();
        mBitmapDrawDstRect = new Rect();
    }

    @Override
    public void setPageProvider(PageProvider pageProvider) {
        mPageProvider = pageProvider;
        invalidate();
    }

    @Override
    public void setOnPageFlipListener(OnPageFlipListener onPageFlipListener) {
        mOnPageFlipListener = onPageFlipListener;
    }

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
        if (mFlipState != STATE_FLIP_NONE) {
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
        if (mFlipState == STATE_FLIP_NONE) {
            float x = event.getX(pointerIndex);
            float xDiff = Math.abs(x - mDownMotionX);
            float y = event.getY(pointerIndex);
            float yDiff = Math.abs(y - mDownMotionY);
            if (xDiff > (float) mTouchSlop && xDiff > yDiff) {
                FlipOverPage page = mPageProvider.updatePage(mCurrentPageIndex, getWidth(), getHeight());
                if (x > mDownMotionX && mCurrentPageIndex > 0) {
                    mFlipState = STATE_FLIP_TO_RIGHT;
                    mLeftBitmap = page.getLeftPageBitmap();
                    mRightBitmap = page.getCurrentPageBitmap();
                    mPageSplitX = 0;
                } else if (x < mDownMotionX && mCurrentPageIndex < mPageProvider.getPageCount() - 1) {
                    mFlipState = STATE_FLIP_TO_LEFT;
                    mLeftBitmap = page.getCurrentPageBitmap();
                    mRightBitmap = page.getRightPageBitmap();
                    mPageSplitX = getWidth();
                }
                if (mFlipState != STATE_FLIP_NONE && mOnPageFlipListener != null) {
                    mOnPageFlipListener.onFlipStart();
                    postInvalidate();
                }
            }
        }
        if (mFlipState != STATE_FLIP_NONE) {
            mTargetX = (int) event.getX(pointerIndex);
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
        int velocity = (int) velocityTracker.getXVelocity(mActivePointerId);
        if (mFlipState != STATE_FLIP_NONE) {
            if (Math.abs(velocity) > mMinimumVelocity) {
                if (velocity > 0) {
                    mTargetX = getWidth();
                } else {
                    mTargetX = 0;
                }
            } else {
                float x = event.getX();
                if (x < getWidth() / 2) {
                    mTargetX = 0;
                } else {
                    mTargetX = getWidth();
                }
            }
        }

        if (mFlipState == STATE_FLIP_NONE
                && Math.abs(velocity) < mMinimumVelocity
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
        if (mPageProvider != null && mCurrentPageIndex > 0) {
            FlipOverPage page = mPageProvider.updatePage(mCurrentPageIndex, getWidth(), getHeight());
            mLeftBitmap = page.getLeftPageBitmap();
            mRightBitmap = page.getCurrentPageBitmap();
            mTargetX = getWidth();
            mPageSplitX = 0;
            mFlipState = STATE_FLIP_TO_RIGHT;
            postInvalidate();
        }
        if (mOnPageFlipListener != null) {
            mOnPageFlipListener.onFlipStart();
        }
    }

    private void performClickRightArea() {
        if (mPageProvider != null && mCurrentPageIndex < mPageProvider.getPageCount() - 1) {
            FlipOverPage page = mPageProvider.updatePage(mCurrentPageIndex, getWidth(), getHeight());
            mLeftBitmap = page.getCurrentPageBitmap();
            mRightBitmap = page.getRightPageBitmap();
            mTargetX = 0;
            mPageSplitX = getWidth();
            mFlipState = STATE_FLIP_TO_LEFT;
            postInvalidate();
        }
        if (mOnPageFlipListener != null) {
            mOnPageFlipListener.onFlipStart();
        }
    }

    private void performClickCenterArea() {
        if (mOnPageFlipListener != null) {
            mOnPageFlipListener.onPageClick();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mPageProvider == null) {
            return;
        }
        update();
        if (mFlipState != STATE_FLIP_NONE) {
            drawPages(canvas);
            postInvalidate();
        } else {
            drawCurrentPage(canvas);
        }
    }

    private void drawCurrentPage(Canvas canvas) {
        Bitmap bitmap = mPageProvider.updatePage(mCurrentPageIndex, getWidth(), getHeight())
                .getCurrentPageBitmap();
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
    }

    private void drawPages(Canvas canvas) {
        mBitmapDrawSrcRect.set(getWidth() - mPageSplitX, 0, getWidth(), getHeight());
        mBitmapDrawDstRect.set(0, 0, mPageSplitX, getHeight());

        canvas.drawBitmap(mLeftBitmap, mBitmapDrawSrcRect, mBitmapDrawDstRect, null);
        canvas.drawBitmap(mRightBitmap, mPageSplitX, 0, null);
    }

    private void update() {
        if (mFlipState == STATE_FLIP_NONE) {
            return;
        }

        float length = Math.abs(mPageSplitX - mTargetX);
        if (length < mAnimateMinStep) {
            mPageSplitX = mTargetX;
        } else {
            float step = length / 2;
            if (mPageSplitX > mTargetX) {
                mPageSplitX -= step;
            } else {
                mPageSplitX += step;
            }
        }

        if (mPageSplitX == 0 || mPageSplitX == getWidth()) {
            if (mFlipState == STATE_FLIP_TO_LEFT && mPageSplitX == 0) {
                mCurrentPageIndex++;
            } else if (mFlipState == STATE_FLIP_TO_RIGHT && mPageSplitX == getWidth()) {
                mCurrentPageIndex--;
            }
            if (mCurrentPageIndex < 0) {
                mCurrentPageIndex = 0;
            } else if (mCurrentPageIndex >= mPageProvider.getPageCount()) {
                mCurrentPageIndex = mPageProvider.getPageCount() - 1;
            }
            mFlipState = STATE_FLIP_NONE;
        }
    }
}

package com.ihuntto.bookreader.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import com.ihuntto.bookreader.flip.FlipOver;

public class SimulateFlipOver extends View implements FlipOver {
    private OnPageFlipListener mOnPageFlipListener;
    private PageProvider mPageProvider;
    private VelocityTracker mVelocityTracker;
    private float mLastMotionX;
    private float mLastMotionY;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private int mActivePointerId = -1;
    private boolean mIsBeingDragged;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int mTouchSlop;

    public SimulateFlipOver(Context context) {
        super(context);
        init(context);
    }

    public SimulateFlipOver(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SimulateFlipOver(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public SimulateFlipOver(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledPagingTouchSlop();
    }

    @Override
    public void setPageProvider(PageProvider pageProvider) {
        mPageProvider = pageProvider;
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
        int action = event.getActionMasked();
        int activePointerIndex;
        float x, y;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = mInitialMotionX = event.getX();
                mLastMotionY = mInitialMotionY = event.getY();
                mActivePointerId = event.getPointerId(0);
                break;
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, (float) mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getXVelocity(mActivePointerId);

                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mIsBeingDragged) {
                    activePointerIndex = event.findPointerIndex(mActivePointerId);
                    if (activePointerIndex == -1) {
                        break;
                    }

                    x = event.getX(activePointerIndex);
                    float xDiff = Math.abs(x - mLastMotionX);
                    y = event.getY(activePointerIndex);
                    float yDiff = Math.abs(y - mLastMotionY);

                    if (Math.sqrt(xDiff* xDiff + yDiff * yDiff) > (float) mTouchSlop) {
                        mIsBeingDragged = true;
                        mLastMotionX = x;
                        mLastMotionY = y;
                        ViewParent parent = getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                }

                if (mIsBeingDragged) {
                    activePointerIndex = event.findPointerIndex(mActivePointerId);
                    x = event.getX(activePointerIndex);
                    y = event.getY(activePointerIndex);
                    performDrag(x, y);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {

                }
        }
        return true;
    }

    private void performDrag(float x, float y) {

    }
}

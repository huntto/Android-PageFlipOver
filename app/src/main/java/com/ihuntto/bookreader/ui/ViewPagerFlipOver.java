package com.ihuntto.bookreader.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.ihuntto.bookreader.R;
import com.ihuntto.bookreader.flip.FlipOver;

public class ViewPagerFlipOver extends ViewPager implements FlipOver {
    private static final long MIN_CLICK_INTERVAL_MILLIS = 200;
    private long mClickDownTime;
    private int mCurrentState;

    private OnPageFlipListener mOnPageFlipListener;
    private PagerAdapter mPagerAdapter;

    public ViewPagerFlipOver(@NonNull Context context) {
        super(context);
    }

    public ViewPagerFlipOver(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setPageProvider(final PageProvider pageProvider) {
        if (pageProvider == null) {
            removeOnPageChangeListener(mOnPageChangeListener);
            return;
        }
        mPagerAdapter = new FlipOverPagerAdapter(pageProvider);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setAdapter(mPagerAdapter);
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        setPageTransformer(true, new DepthPageTransformer());
        addOnPageChangeListener(mOnPageChangeListener);
    }

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {

        }

        @Override
        public void onPageScrollStateChanged(int i) {
            mCurrentState = i;
            if (mCurrentState == SCROLL_STATE_DRAGGING) {
                if (mOnPageFlipListener != null) {
                    mOnPageFlipListener.onFlipStart();
                }
            }
        }
    };

    // https://developer.android.com/training/animation/screen-slide
    private static class DepthPageTransformer implements ViewPager.PageTransformer {

        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1f);
                view.setTranslationX(0f);
                view.setScaleX(1f);
                view.setScaleY(1f);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
        }
    }

    @Override
    public void setOnPageFlipListener(OnPageFlipListener onPageFlipListener) {
        mOnPageFlipListener = onPageFlipListener;
    }

    private class FlipOverPagerAdapter extends PagerAdapter {
        private PageProvider mPageProvider;

        public FlipOverPagerAdapter(PageProvider pageProvider) {
            mPageProvider = pageProvider;
        }

        @Override
        public int getCount() {
            return mPageProvider.getPageCount();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = View.inflate(ViewPagerFlipOver.this.getContext(),
                    R.layout.layout_item_view_pager_flip_over, null);
            ImageView imageView = view.findViewById(R.id.view_pager_flip_over_item);
            Bitmap bitmap = mPageProvider.updatePage(position, container.getWidth(), container.getHeight())
                    .getCurrentPageBitmap();
            imageView.setImageBitmap(bitmap);
            container.addView(view);
            return view;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mClickDownTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                if (mCurrentState == SCROLL_STATE_IDLE &&
                        System.currentTimeMillis() - mClickDownTime < MIN_CLICK_INTERVAL_MILLIS) {
                    handleClickPosition(ev.getX(), ev.getY());
                    return true;
                }
                break;

        }
        return super.onTouchEvent(ev);
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
        int leftItem = getCurrentItem() - 1;
        if (leftItem > -1) {
            setCurrentItem(leftItem, true);
        }
    }

    private void performClickRightArea() {
        int rightItem = getCurrentItem() + 1;
        if (rightItem < mPagerAdapter.getCount()) {
            setCurrentItem(rightItem, true);
        }
    }

    private void performClickCenterArea() {
        if (mOnPageFlipListener != null) {
            mOnPageFlipListener.onPageClick();
        }
    }
}

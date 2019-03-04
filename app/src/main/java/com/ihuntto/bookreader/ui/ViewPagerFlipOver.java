package com.ihuntto.bookreader.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.ihuntto.bookreader.R;
import com.ihuntto.bookreader.flip.FlipOver;

public class ViewPagerFlipOver extends ViewPager implements FlipOver {
    private OnPageFlipListener mOnPageFlipListener;

    public ViewPagerFlipOver(@NonNull Context context) {
        super(context);
    }

    public ViewPagerFlipOver(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setPageProvider(final PageProvider pageProvider) {
        if (pageProvider == null) {
            return;
        }
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setAdapter(new FlipOverPagerAdapter(pageProvider));
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        setPageTransformer(true, new DepthPageTransformer());
    }

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
}

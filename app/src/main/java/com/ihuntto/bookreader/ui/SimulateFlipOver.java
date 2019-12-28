/*
 *    Copyright 2019 Huntto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.ihuntto.bookreader.ui;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.ihuntto.bookreader.R;
import com.ihuntto.bookreader.flip.FlipOver;
import com.ihuntto.bookreader.flip.FlipOverPage;

import fi.harism.curl.CurlPage;
import fi.harism.curl.CurlView;

public class SimulateFlipOver extends FrameLayout implements FlipOver {
    private OnPageFlipListener mOnPageFlipListener;
    private CurlView mCurlView;

    public SimulateFlipOver(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SimulateFlipOver(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SimulateFlipOver(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public SimulateFlipOver(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_simulate_flip_over, this);
        mCurlView = findViewById(R.id.page_curl_view);
    }

    @Override
    public void setPageProvider(final PageProvider pageProvider) {
        mCurlView.setCurrentIndex(0);
        mCurlView.setBackgroundColor(0xCCCCCC);
        mCurlView.setRenderLeftPage(false);
        mCurlView.setViewMode(CurlView.SHOW_ONE_PAGE);
        mCurlView.setPageProvider(new CurlView.PageProvider() {
            @Override
            public int getPageCount() {
                if (pageProvider != null) {
                    return pageProvider.getPageCount();
                }
                return 0;
            }

            @Override
            public void updatePage(CurlPage page, int width, int height, int index) {
                if (pageProvider != null) {
                    FlipOverPage flipOverPage = pageProvider.updatePage(index, width, height);
                    page.setTexture(flipOverPage.getCurrentPageBitmap(), CurlPage.SIDE_FRONT);
                    page.setColor(Color.rgb(0xee, 0xee, 0xee), CurlPage.SIDE_BACK);
                } else {
                    page.setColor(Color.rgb(0xee, 0xee, 0xee), CurlPage.SIDE_BOTH);
                }
            }
        });
    }

    @Override
    public void setOnPageFlipListener(OnPageFlipListener onPageFlipListener) {
        mOnPageFlipListener = onPageFlipListener;
    }

    @Override
    protected void onDetachedFromWindow() {
        mCurlView.onPause();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        mCurlView.onResume();
        super.onAttachedToWindow();
    }
}

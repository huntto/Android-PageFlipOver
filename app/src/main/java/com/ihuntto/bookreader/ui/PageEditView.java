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

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ihuntto.bookreader.R;

public class PageEditView extends FrameLayout implements View.OnClickListener {
    private static final long ANIMATE_DURATION = 200;
    private static final int VISIBILITY_STATE_DISMISSED = 0x01;
    private static final int VISIBILITY_STATE_SHOWED = 0x02;
    private static final int VISIBILITY_STATE_ANIMATING = 0x03;

    private int mVisibilityState = VISIBILITY_STATE_SHOWED;

    private LinearLayout mToolbarLayout;
    private ImageButton mFontBtn;
    private ImageButton mBrightBtn;
    private ImageButton mTtsBtn;

    public PageEditView(Context context) {
        super(context);
        init(context);
    }

    public PageEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PageEditView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_page_edit_view, this);
        mToolbarLayout = findViewById(R.id.layout_toolbar);
        mFontBtn = findViewById(R.id.btn_font);
        mBrightBtn = findViewById(R.id.btn_bright);
        mTtsBtn = findViewById(R.id.btn_tts);

        mFontBtn.setOnClickListener(this);
        mBrightBtn.setOnClickListener(this);
        mTtsBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_font:
                handleOnSetFont();
                break;
            case R.id.btn_bright:
                handleOnSetBright();
                break;
            case R.id.btn_tts:
                handleOnSetTts();
                break;
        }
        Toast.makeText(getContext(), v.getContentDescription(), Toast.LENGTH_SHORT).show();
    }

    private void handleOnSetFont() {

    }

    private void handleOnSetBright() {

    }

    private void handleOnSetTts() {

    }

    public void switchVisibility() {
        if (!show()) {
            dismiss();
        }
    }

    public boolean show() {
        if (mVisibilityState != VISIBILITY_STATE_DISMISSED) {
            return false;
        }
        ObjectAnimator translateIn = ObjectAnimator.ofFloat(mToolbarLayout, "translationY",
                mToolbarLayout.getHeight(), 0);

        translateIn.setInterpolator(new LinearInterpolator());
        translateIn.setDuration(ANIMATE_DURATION);
        translateIn.start();

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mTtsBtn, "alpha",
                0f, 1f);
        fadeIn.setInterpolator(new LinearInterpolator());
        fadeIn.setDuration(ANIMATE_DURATION);
        fadeIn.start();

        fadeIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mVisibilityState = VISIBILITY_STATE_ANIMATING;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mVisibilityState = VISIBILITY_STATE_SHOWED;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return true;
    }

    public boolean dismiss() {
        if (mVisibilityState != VISIBILITY_STATE_SHOWED) {
            return false;
        }
        ObjectAnimator translateOut = ObjectAnimator.ofFloat(mToolbarLayout, "translationY",
                0, mToolbarLayout.getHeight());
        translateOut.setInterpolator(new LinearInterpolator());
        translateOut.setDuration(ANIMATE_DURATION);

        translateOut.start();

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(mTtsBtn, "alpha",
                1f, 0f);
        fadeOut.setInterpolator(new LinearInterpolator());
        fadeOut.setDuration(ANIMATE_DURATION);
        fadeOut.start();

        fadeOut.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mVisibilityState = VISIBILITY_STATE_ANIMATING;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mVisibilityState = VISIBILITY_STATE_DISMISSED;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return true;
    }
}

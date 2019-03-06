package com.ihuntto.bookreader.ui.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.ihuntto.bookreader.flip.FlipOver;

public class SimpleGLFlipOver extends GLSurfaceView implements FlipOver {

    private OnPageFlipListener mOnPageFlipListener;
    private int mCurrentPageIndex = 0;
    private FlipOverRenderer mFlipOverRenderer;

    public SimpleGLFlipOver(@NonNull Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setEGLContextClientVersion(2);
        mFlipOverRenderer = new FlipOverRenderer();
        setRenderer(mFlipOverRenderer);
    }

    public SimpleGLFlipOver(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setPageProvider(PageProvider pageProvider) {
        mFlipOverRenderer.setPageProvider(pageProvider);
        mFlipOverRenderer.setCurrentPageIndex(mCurrentPageIndex);
    }

    @Override
    public void setOnPageFlipListener(OnPageFlipListener onPageFlipListener) {
        mOnPageFlipListener = onPageFlipListener;
    }
}

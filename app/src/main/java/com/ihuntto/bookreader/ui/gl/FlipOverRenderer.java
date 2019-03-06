package com.ihuntto.bookreader.ui.gl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.ihuntto.bookreader.flip.FlipOver;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.orthoM;
import static android.opengl.Matrix.perspectiveM;
import static android.opengl.Matrix.setLookAtM;

final class FlipOverRenderer implements GLSurfaceView.Renderer {
    private FlipOver.PageProvider mPageProvider;
    private int mCurrentPageIndex;
    private int mWidth;
    private int mHeight;

    private Bitmap mLeftBitmap;
    private Bitmap mRightBitmap;
    private Bitmap mBitmap;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    private List<PageMesh> mPageMeshes;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mPageMeshes = new ArrayList<>();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;

        GLES20.glViewport(0, 0, width, height);

        perspectiveM(mProjectionMatrix, 0, 45, (float) width / (float) height, 1f, 10f);
        setLookAtM(mViewMatrix, 0, 0f, 0f, -2f, 0f, 0f, 0f, 0f, 1f, 0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        final float[] viewProjectionMatrix = new float[16];
        multiplyMM(viewProjectionMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        for (PageMesh page : mPageMeshes) {
            page.draw(viewProjectionMatrix);
        }
    }

    public void setPageProvider(FlipOver.PageProvider pageProvider) {
        mPageProvider = pageProvider;
    }

    public void setCurrentPageIndex(int currentPageIndex) {
        mCurrentPageIndex = currentPageIndex;
    }
}

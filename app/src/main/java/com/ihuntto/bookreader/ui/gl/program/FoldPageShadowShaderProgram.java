package com.ihuntto.bookreader.ui.gl.program;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ihuntto.bookreader.R;
import com.ihuntto.bookreader.ui.gl.util.TextResourceReader;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;

public class FoldPageShadowShaderProgram extends ShaderProgram {

    private static final String U_MATRIX = "uMatrix";
    private static final String U_ORIGIN_POINT = "uOriginPoint";
    private static final String U_DRAG_POINT = "uDragPoint";
    private static final String U_PAGE_SIZE = "uPageSize";
    private static final String U_MAX_FOLD_HEIGHT = "uMaxFoldHeight";
    private static final String U_BASE_FOLD_HEIGHT = "uBaseFoldHeight";
    private static final String U_LIGHT_POS = "uLightPos";

    private static final String A_POSITION = "aPosition";

    private int mMatrixLocation;
    private int mOriginLocation;
    private int mDragLocation;
    private int mPageSizeLocation;
    private int mMaxFoldHeightLocation;
    private int mBaseFoldHeightLocation;
    private int mLightPosLocation;

    private int mPositionLocation;

    private String mVertexShaderSource;
    private String mFragmentShaderSource;

    public FoldPageShadowShaderProgram(Context context) {
        mVertexShaderSource = TextResourceReader.readTextFromResource(context, R.raw.fold_page_shadow_vertex_shader);
        mFragmentShaderSource = TextResourceReader.readTextFromResource(context, R.raw.fold_page_shadow_fragment_shader);
    }

    @NonNull
    @Override
    protected String getVertexShaderSource() {
        return mVertexShaderSource;
    }

    @NonNull
    @Override
    protected String getFragmentShaderSource() {
        return mFragmentShaderSource;
    }

    @Override
    public void compile() {
        super.compile();
        this.use();
        mMatrixLocation = glGetUniformLocation(mProgram, U_MATRIX);
        mOriginLocation = glGetUniformLocation(mProgram, U_ORIGIN_POINT);
        mDragLocation = glGetUniformLocation(mProgram, U_DRAG_POINT);
        mPageSizeLocation = glGetUniformLocation(mProgram, U_PAGE_SIZE);
        mMaxFoldHeightLocation = glGetUniformLocation(mProgram, U_MAX_FOLD_HEIGHT);
        mBaseFoldHeightLocation = glGetUniformLocation(mProgram, U_BASE_FOLD_HEIGHT);
        mLightPosLocation = glGetUniformLocation(mProgram, U_LIGHT_POS);

        mPositionLocation = glGetAttribLocation(mProgram, A_POSITION);
    }

    public int getMatrixLocation() {
        return mMatrixLocation;
    }

    public int getOriginLocation() {
        return mOriginLocation;
    }

    public int getDragLocation() {
        return mDragLocation;
    }

    public int getPageSizeLocation() {
        return mPageSizeLocation;
    }

    public int getMaxFoldHeightLocation() {
        return mMaxFoldHeightLocation;
    }

    public int getBaseFoldHeightLocation() {
        return mBaseFoldHeightLocation;
    }

    public int getPositionLocation() {
        return mPositionLocation;
    }

    public int getLightPosLocation() {
        return mLightPosLocation;
    }
}

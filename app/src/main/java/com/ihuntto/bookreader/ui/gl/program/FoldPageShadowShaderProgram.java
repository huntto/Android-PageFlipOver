package com.ihuntto.bookreader.ui.gl.program;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ihuntto.bookreader.R;
import com.ihuntto.bookreader.ui.gl.util.TextResourceReader;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;

public class FoldPageShadowShaderProgram extends ShaderProgram {

    private static final String U_MVP_MATRIX = "uMVPMatrix";
    private static final String U_MODEL_MATRIX = "uModelMatrix";
    private static final String U_ORIGIN_POINT = "uOriginPoint";
    private static final String U_DRAG_POINT = "uDragPoint";
    private static final String U_MAX_FOLD_HEIGHT = "uMaxFoldHeight";
    private static final String U_BASE_FOLD_HEIGHT = "uBaseFoldHeight";
    private static final String U_LIGHT_POS = "uLightPos";

    private static final String A_POSITION = "aPosition";

    private int mMVPMatrixLocation;
    private int mModelMatrixLocation;
    private int mOriginLocation;
    private int mDragLocation;
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
        mMVPMatrixLocation = glGetUniformLocation(mProgram, U_MVP_MATRIX);
        mModelMatrixLocation = glGetUniformLocation(mProgram, U_MODEL_MATRIX);
        mOriginLocation = glGetUniformLocation(mProgram, U_ORIGIN_POINT);
        mDragLocation = glGetUniformLocation(mProgram, U_DRAG_POINT);
        mMaxFoldHeightLocation = glGetUniformLocation(mProgram, U_MAX_FOLD_HEIGHT);
        mBaseFoldHeightLocation = glGetUniformLocation(mProgram, U_BASE_FOLD_HEIGHT);
        mLightPosLocation = glGetUniformLocation(mProgram, U_LIGHT_POS);

        mPositionLocation = glGetAttribLocation(mProgram, A_POSITION);
    }

    public int getMVPMatrixLocation() {
        return mMVPMatrixLocation;
    }

    public int getModelMatrixLocation() {
        return mModelMatrixLocation;
    }

    public int getOriginLocation() {
        return mOriginLocation;
    }

    public int getDragLocation() {
        return mDragLocation;
    }

    public int getMaxFoldHeightLocation() {
        return mMaxFoldHeightLocation;
    }

    public int getBaseFoldHeightLocation() {
        return mBaseFoldHeightLocation;
    }

    public int getLightPosLocation() {
        return mLightPosLocation;
    }

    public int getPositionLocation() {
        return mPositionLocation;
    }
}

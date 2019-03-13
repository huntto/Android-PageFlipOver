package com.ihuntto.bookreader.ui.gl.program;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ihuntto.bookreader.R;
import com.ihuntto.bookreader.ui.gl.util.TextResourceReader;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;

public class FoldPageShaderProgram extends ShaderProgram {

    private static final String U_MVP_MATRIX = "uMVPMatrix";
    private static final String U_TEXTURE_UNIT = "uTextureUnit";
    private static final String U_ORIGIN_POINT = "uOriginPoint";
    private static final String U_DRAG_POINT = "uDragPoint";
    private static final String U_PAGE_SIZE = "uPageSize";
    private static final String U_MAX_FOLD_HEIGHT = "uMaxFoldHeight";
    private static final String U_BASE_FOLD_HEIGHT = "uBaseFoldHeight";
    // light
    private static final String U_LIGHT_DIRECTION = "uLight.direction";
    private static final String U_LIGHT_AMBIENT = "uLight.ambient";
    private static final String U_LIGHT_DIFFUSE = "uLight.diffuse";
    private static final String U_LIGHT_SPECULAR = "uLight.specular";
    private static final String U_LIGHT_COLOR = "uLight.color";
    private static final String U_VIEW_POS = "uViewPos";

    private static final String A_POSITION = "aPosition";

    private int mMVPMatrixLocation;
    private int mTextureUnitLocation;
    private int mOriginLocation;
    private int mDragLocation;
    private int mPageSizeLocation;
    private int mMaxFoldHeightLocation;
    private int mBaseFoldHeightLocation;

    private int mLightDirectionLocation;
    private int mLightAmbientLocation;
    private int mLightDiffuseLocation;
    private int mLightSpecularLocation;
    private int mLightColorLocation;
    private int mViewPosLocation;

    private int mPositionLocation;

    protected String mVertexShaderSource;
    protected String mFragmentShaderSource;

    protected FoldPageShaderProgram() {
    }

    public FoldPageShaderProgram(Context context) {
        mVertexShaderSource = TextResourceReader.readTextFromResource(context, R.raw.fold_page_vertex_shader);
        mFragmentShaderSource = TextResourceReader.readTextFromResource(context, R.raw.fold_page_fragment_shader);
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
        mTextureUnitLocation = glGetUniformLocation(mProgram, U_TEXTURE_UNIT);
        mOriginLocation = glGetUniformLocation(mProgram, U_ORIGIN_POINT);
        mDragLocation = glGetUniformLocation(mProgram, U_DRAG_POINT);
        mPageSizeLocation = glGetUniformLocation(mProgram, U_PAGE_SIZE);
        mMaxFoldHeightLocation = glGetUniformLocation(mProgram, U_MAX_FOLD_HEIGHT);
        mBaseFoldHeightLocation = glGetUniformLocation(mProgram, U_BASE_FOLD_HEIGHT);

        mLightDirectionLocation = glGetUniformLocation(mProgram, U_LIGHT_DIRECTION);
        mLightAmbientLocation = glGetUniformLocation(mProgram, U_LIGHT_AMBIENT);
        mLightDiffuseLocation = glGetUniformLocation(mProgram, U_LIGHT_DIFFUSE);
        mLightSpecularLocation = glGetUniformLocation(mProgram, U_LIGHT_SPECULAR);
        mLightColorLocation = glGetUniformLocation(mProgram, U_LIGHT_COLOR);
        mViewPosLocation = glGetUniformLocation(mProgram, U_VIEW_POS);

        mPositionLocation = glGetAttribLocation(mProgram, A_POSITION);
    }

    public int getMVPMatrixLocation() {
        return mMVPMatrixLocation;
    }

    public int getTextureUnitLocation() {
        return mTextureUnitLocation;
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

    public int getLightDirectionLocation() {
        return mLightDirectionLocation;
    }

    public int getLightAmbientLocation() {
        return mLightAmbientLocation;
    }

    public int getLightDiffuseLocation() {
        return mLightDiffuseLocation;
    }

    public int getLightSpecularLocation() {
        return mLightSpecularLocation;
    }

    public int getLightColorLocation() {
        return mLightColorLocation;
    }

    public int getViewPosLocation() {
        return mViewPosLocation;
    }
}

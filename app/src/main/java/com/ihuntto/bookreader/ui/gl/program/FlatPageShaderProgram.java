package com.ihuntto.bookreader.ui.gl.program;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ihuntto.bookreader.R;
import com.ihuntto.bookreader.ui.gl.util.TextResourceReader;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;

public class FlatPageShaderProgram extends ShaderProgram {
    private String mVertexShaderSource;
    private String mFragmentShaderSource;

    private static final String U_MATRIX = "uMatrix";
    private static final String U_LIGHT_MVP_MATRIX = "uLightMVPMatrix";
    private static final String U_PAGE_SIZE = "uPageSize";
    private static final String U_TEXTURE_UNIT = "uTextureUnit";
    private static final String U_SHADOW_TEXTURE_UNIT = "uShadowTextureUnit";
    // light
    private static final String U_LIGHT_DIRECTION = "uLight.direction";
    private static final String U_LIGHT_AMBIENT = "uLight.ambient";
    private static final String U_LIGHT_DIFFUSE = "uLight.diffuse";
    private static final String U_LIGHT_SPECULAR = "uLight.specular";
    private static final String U_LIGHT_COLOR = "uLight.color";
    private static final String U_VIEW_POS = "uViewPos";
    private static final String U_LIGHT_POS = "uLightPos";
    private static final String U_DRAW_SHADOW = "uDrawShadow";


    private static final String A_POSITION = "aPosition";

    private int mMatrixLocation;
    private int mLightMVPMatrixLocation;
    private int mPageSizeLocation;
    private int mTextureUnitLocation;
    private int mShadowTextureUnitLocation;
    private int mPositionLocation;

    private int mLightDirectionLocation;
    private int mLightAmbientLocation;
    private int mLightDiffuseLocation;
    private int mLightSpecularLocation;
    private int mLightColorLocation;
    private int mViewPosLocation;
    private int mLightPosLocation;
    private int mDrawShadowLocation;

    public FlatPageShaderProgram(Context context) {
        mVertexShaderSource = TextResourceReader.readTextFromResource(context, R.raw.flat_page_vertex_shader);
        mFragmentShaderSource = TextResourceReader.readTextFromResource(context, R.raw.flat_page_fragment_shader);
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
        mLightMVPMatrixLocation = glGetUniformLocation(mProgram, U_LIGHT_MVP_MATRIX);
        mPageSizeLocation = glGetUniformLocation(mProgram, U_PAGE_SIZE);
        mTextureUnitLocation = glGetUniformLocation(mProgram, U_TEXTURE_UNIT);
        mShadowTextureUnitLocation = glGetUniformLocation(mProgram, U_SHADOW_TEXTURE_UNIT);
        mPositionLocation = glGetAttribLocation(mProgram, A_POSITION);
        mLightDirectionLocation = glGetUniformLocation(mProgram, U_LIGHT_DIRECTION);
        mLightAmbientLocation = glGetUniformLocation(mProgram, U_LIGHT_AMBIENT);
        mLightDiffuseLocation = glGetUniformLocation(mProgram, U_LIGHT_DIFFUSE);
        mLightSpecularLocation = glGetUniformLocation(mProgram, U_LIGHT_SPECULAR);
        mLightColorLocation = glGetUniformLocation(mProgram, U_LIGHT_COLOR);
        mViewPosLocation = glGetUniformLocation(mProgram, U_VIEW_POS);
        mLightPosLocation = glGetUniformLocation(mProgram, U_LIGHT_POS);
        mDrawShadowLocation = glGetUniformLocation(mProgram, U_DRAW_SHADOW);
    }

    public int getMatrixLocation() {
        return mMatrixLocation;
    }

    public int getPageSizeLocation() {
        return mPageSizeLocation;
    }

    public int getTextureUnitLocation() {
        return mTextureUnitLocation;
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

    public int getShadowTextureUnitLocation() {
        return mShadowTextureUnitLocation;
    }

    public int getLightMVPMatrixLocation() {
        return mLightMVPMatrixLocation;
    }

    public int getLightPosLocation() {
        return mLightPosLocation;
    }

    public int getDrawShadowLocation() {
        return mDrawShadowLocation;
    }
}

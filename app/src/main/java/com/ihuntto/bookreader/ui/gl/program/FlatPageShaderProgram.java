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

    private final String U_MATRIX = "uMatrix";
    private final String U_PAGE_SIZE = "uPageSize";
    private final String U_TEXTURE_UNIT = "uTextureUnit";

    private final String A_POSITION = "aPosition";

    private int mMatrixLocation;
    private int mPageSizeLocation;
    private int mTextureUnitLocation;
    private int mPositionLocation;

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
        mMatrixLocation = glGetUniformLocation(mProgram, U_MATRIX);
        mPageSizeLocation = glGetUniformLocation(mProgram, U_PAGE_SIZE);
        mTextureUnitLocation = glGetUniformLocation(mProgram, U_TEXTURE_UNIT);
        mPositionLocation = glGetAttribLocation(mProgram, A_POSITION);
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
}

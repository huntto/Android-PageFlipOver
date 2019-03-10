package com.ihuntto.bookreader.ui.gl.program;

import android.support.annotation.NonNull;

import com.ihuntto.bookreader.BuildConfig;
import com.ihuntto.bookreader.ui.gl.util.ShaderHelper;

import static android.opengl.GLES20.glUseProgram;

abstract class ShaderProgram {
    private static final boolean D = BuildConfig.DEBUG;
    protected int mProgram;

    public void compile() {
        String vertexShaderSource = getVertexShaderSource();
        String fragmentShaderSource = getFragmentShaderSource();

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);
        mProgram = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        if (D) {
            ShaderHelper.validateProgram(mProgram);
        }
    }

    protected abstract @NonNull
    String getVertexShaderSource();

    protected abstract @NonNull
    String getFragmentShaderSource();

    public void use() {
        glUseProgram(mProgram);
    }
}

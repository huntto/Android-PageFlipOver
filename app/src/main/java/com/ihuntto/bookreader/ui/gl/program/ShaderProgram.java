package com.ihuntto.bookreader.ui.gl.program;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;

import com.ihuntto.bookreader.BuildConfig;
import com.ihuntto.bookreader.ui.gl.util.ShaderHelper;
import com.ihuntto.bookreader.ui.gl.util.TextResourceReader;

import java.nio.FloatBuffer;

public class ShaderProgram {
    private static final boolean D = BuildConfig.DEBUG;
    private int mProgram;

    private String mVertexShaderSource;
    private String mFragmentShaderSource;

    public ShaderProgram(Context context, int vertexShaderRawId, int fragmentShaderRawId) {
        mVertexShaderSource = TextResourceReader.readTextFromResource(context, vertexShaderRawId);
        mFragmentShaderSource = TextResourceReader.readTextFromResource(context, fragmentShaderRawId);
    }

    public ShaderProgram(Context context, String vertexShaderAssetPath, String fragmentShaderAssetPath) {
        Resources resources = context.getResources();
        mVertexShaderSource = TextResourceReader.readTextFromAsset(resources, vertexShaderAssetPath);
        mFragmentShaderSource = TextResourceReader.readTextFromAsset(resources, fragmentShaderAssetPath);
    }

    public void compile() {
        int vertexShader = ShaderHelper.compileVertexShader(mVertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(mFragmentShaderSource);
        mProgram = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        if (D) {
            ShaderHelper.validateProgram(mProgram);
        }
    }


    public void use() {
        GLES20.glUseProgram(mProgram);
    }

    public void setUniformMatrix4fv(String name, float[] matrix) {
        GLES20.glUniformMatrix4fv(
                GLES20.glGetUniformLocation(mProgram, name),
                1,
                false,
                matrix,
                0);
    }

    public void setUniform3fv(String name, float[] value) {
        GLES20.glUniform3fv(
                GLES20.glGetUniformLocation(mProgram, name),
                1,
                value,
                0);
    }

    public void setUniform2fv(String name, float[] value) {
        GLES20.glUniform2fv(
                GLES20.glGetUniformLocation(mProgram, name),
                1,
                value,
                0);
    }

    public void setUniform2f(String name, float x, float y) {
        GLES20.glUniform2f(
                GLES20.glGetUniformLocation(mProgram, name),
                x,
                y);
    }

    public void setUniform1f(String name, float v) {
        GLES20.glUniform1f(
                GLES20.glGetUniformLocation(mProgram, name),
                v);
    }

    public void setUniform1i(String name, int v) {
        GLES20.glUniform1i(
                GLES20.glGetUniformLocation(mProgram, name),
                v);
    }

    public void setVertexAttribPointer(String name, int size, FloatBuffer buffer) {
        int location = GLES20.glGetAttribLocation(mProgram, name);
        GLES20.glVertexAttribPointer(
                location,
                size,
                GLES20.GL_FLOAT,
                false,
                0,
                buffer);
        GLES20.glEnableVertexAttribArray(location);
    }

}

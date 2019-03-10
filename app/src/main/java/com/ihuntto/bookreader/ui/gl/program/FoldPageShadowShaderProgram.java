package com.ihuntto.bookreader.ui.gl.program;

import android.content.Context;

import com.ihuntto.bookreader.R;
import com.ihuntto.bookreader.ui.gl.util.TextResourceReader;

public class FoldPageShadowShaderProgram extends FoldPageShaderProgram {
    public FoldPageShadowShaderProgram(Context context) {
        super();
        mVertexShaderSource = TextResourceReader.readTextFromResource(context, R.raw.fold_page_shadow_vertex_shader);
        mFragmentShaderSource = TextResourceReader.readTextFromResource(context, R.raw.fold_page_shadow_fragment_shader);
    }
}

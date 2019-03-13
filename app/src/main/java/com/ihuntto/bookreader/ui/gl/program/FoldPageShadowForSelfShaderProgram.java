package com.ihuntto.bookreader.ui.gl.program;

import android.content.Context;

import com.ihuntto.bookreader.R;
import com.ihuntto.bookreader.ui.gl.util.TextResourceReader;

public class FoldPageShadowForSelfShaderProgram extends FoldPageShaderProgram {

    public FoldPageShadowForSelfShaderProgram(Context context) {
        mVertexShaderSource = TextResourceReader.readTextFromResource(context, R.raw.fold_page_shadow_for_self_vertex_shader);
        mFragmentShaderSource = TextResourceReader.readTextFromResource(context, R.raw.fold_page_shadow_for_self_fragment_shader);

    }
}

package com.ihuntto.bookreader.ui.gl.program;

import android.content.Context;

import com.ihuntto.bookreader.R;
import com.ihuntto.bookreader.ui.gl.util.TextResourceReader;

public class FoldPageShadowForFlatShaderProgram extends FoldPageShaderProgram {

    public FoldPageShadowForFlatShaderProgram(Context context) {
        mVertexShaderSource = TextResourceReader.readTextFromResource(context, R.raw.fold_page_shadow_for_flat_vertex_shader);
        mFragmentShaderSource = TextResourceReader.readTextFromResource(context, R.raw.fold_page_shadow_for_flat_fragment_shader);

    }
}

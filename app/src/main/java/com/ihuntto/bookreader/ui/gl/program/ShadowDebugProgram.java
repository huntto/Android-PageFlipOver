package com.ihuntto.bookreader.ui.gl.program;

import android.content.Context;

import com.ihuntto.bookreader.R;
import com.ihuntto.bookreader.ui.gl.util.TextResourceReader;

public class ShadowDebugProgram extends FlatPageShaderProgram {

    public ShadowDebugProgram(Context context) {
        super();
        mVertexShaderSource = TextResourceReader.readTextFromResource(context, R.raw.flat_page_vertex_shader);
        mFragmentShaderSource = TextResourceReader.readTextFromResource(context, R.raw.debug_shadow_fragment_shader);
    }

}

precision mediump float;

varying float vIsFold;
varying float vAlphaRatio;

void main() {
    if (vIsFold > 0.5) {
        gl_FragColor = vec4(vec3(0.5), 0.4 * vAlphaRatio);
    } else {
        discard;
    }
}

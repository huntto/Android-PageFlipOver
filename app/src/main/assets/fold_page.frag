precision mediump float;

uniform sampler2D uTextureUnit;
varying vec2 vTextureCoordinates;
varying vec4 vBlendColor;
varying float vIsMix;
void main() {
    if (vIsMix > 0.5) {
        gl_FragColor = mix(texture2D(uTextureUnit, vTextureCoordinates), vec4(1.0), 0.85) * vBlendColor * vec4(0.95, 0.95, 0.95, 1.0);
    } else {
        gl_FragColor = texture2D(uTextureUnit, vTextureCoordinates)* vBlendColor;
    }
}

precision mediump float;

uniform float uFlat;
uniform sampler2D uTextureUnit;
varying vec2 vTextureCoordinates;
varying vec4 vBlendColor;
varying float vIsMix;
void main() {
    if (uFlat > 0.5) {
        gl_FragColor = texture2D(uTextureUnit, vTextureCoordinates);
    } else {
        if (vIsMix > 0.5) {
            gl_FragColor = mix(texture2D(uTextureUnit, vTextureCoordinates), vec4(1.0), 0.85) * vBlendColor;
        } else {
            gl_FragColor = texture2D(uTextureUnit, vTextureCoordinates)* vBlendColor;
        }
    }
}

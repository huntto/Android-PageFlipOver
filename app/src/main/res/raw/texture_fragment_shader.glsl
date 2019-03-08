precision mediump float;

uniform float uFlat;
uniform sampler2D uTextureUnit;
varying vec2 vTextureCoordinates;
varying vec4 vBlendColor;

void main() {
    if (uFlat > 0.5) {
        gl_FragColor = texture2D(uTextureUnit, vTextureCoordinates);
    } else {
        gl_FragColor = texture2D(uTextureUnit, vTextureCoordinates);
    }
}

precision mediump float;

uniform sampler2D uTextureUnit;
varying vec2 vTextureCoordinates;
varying vec4 vBlendColor;
void main() {
    gl_FragColor = texture2D(uTextureUnit, vTextureCoordinates) * vBlendColor;
}

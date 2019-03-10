precision mediump float;

uniform sampler2D uTextureUnit;
varying vec2 vTextureCoordinates;
void main() {
    gl_FragColor = vec4(vec3(texture2D(uTextureUnit, vTextureCoordinates).r), 1.0);
}

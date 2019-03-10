precision mediump float;

uniform mat4 uMatrix;

// 页面尺寸
uniform vec2 uPageSize;

attribute vec3 aPosition;

varying vec2 vTextureCoordinates;

void main() {
    vTextureCoordinates = vec2(aPosition.x / uPageSize.x, aPosition.y / uPageSize.y);
    gl_Position = uMatrix * vec4(aPosition, 1.0);
}

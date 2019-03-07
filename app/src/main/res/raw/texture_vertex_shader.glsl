uniform mat4 uMatrix;
uniform float uFlat;
uniform vec2 uFoldLinePoint1;
uniform vec2 uFoldLinePoint2;
uniform vec2 uSize;

attribute vec2 aPosition;

varying vec2 vTextureCoordinates;

void main() {
    vTextureCoordinates = vec2(aPosition.x / uSize.x, aPosition.y / uSize.y);
    if (uFlat > 0.5) {
        gl_Position = uMatrix * vec4(-aPosition.x, aPosition.y, 1.0, 1.0);
    } else {
        gl_Position = uMatrix * vec4(-aPosition.x, aPosition.y, 0.9999, 1.0);
    }
}

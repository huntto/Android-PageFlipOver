uniform mat4 uMatrix;
uniform float uFlat;
uniform vec2 uOriginPoint;
uniform vec2 uDragPoint;
uniform vec2 uSize;

attribute vec2 aPosition;

varying vec2 vTextureCoordinates;
varying vec4 vBlendColor;

void main() {
    vTextureCoordinates = vec2(aPosition.x / uSize.x, aPosition.y / uSize.y);
    if (uFlat > 0.5) {
        gl_Position = uMatrix * vec4(-aPosition.x, aPosition.y, 1.0, 1.0);
    } else {
        // 中点
        float x0 = (uDragPoint.x + uOriginPoint.x) / 2.0;
        float y0 = (uDragPoint.y + uOriginPoint.y) / 2.0;
        // 拉拽方向
        vec2 dragVec = uDragPoint - uOriginPoint;
        // 中垂线方向 (x-x0, y-y0)
        // 中垂线方方向与拉拽方向垂直
        // (x-x0)*dragVec.x + (y-y0)*dragVec.y = 0
        // 将origin点代入方程
        float origin = (uOriginPoint.x - x0) * dragVec.x + (uOriginPoint.y - y0) * dragVec.y;
        // 再将当前点代入方程
        float current = (aPosition.x - x0) * dragVec.x + (aPosition.y - y0) * dragVec.y;
        // 如果origin和current符号相同，则在中垂线同侧，否则异侧
        if (origin * current > 0.0) {
            vBlendColor = vec4(1.0, 0.0, 1.0, 1.0);
        } else {
            vBlendColor = vec4(1.0, 1.0, 1.0, 1.0);
        }

        gl_Position = uMatrix * vec4(-aPosition.x, aPosition.y, 0.9999, 1.0);
    }
}

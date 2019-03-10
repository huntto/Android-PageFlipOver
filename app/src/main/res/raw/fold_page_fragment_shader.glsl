precision mediump float;

uniform sampler2D uTextureUnit;
varying vec2 vTextureCoordinates;
varying vec4 vBlendColor;
varying float vIsMix;

uniform vec2 uOriginPoint;
uniform vec2 uDragPoint;
uniform vec2 uPageSize;

varying vec2 vPosition;

void main() {
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
    float current = (vPosition.x - x0) * dragVec.x + (vPosition.y - y0) * dragVec.y;

    float dragVecDist = sqrt(dragVec.x * dragVec.x + dragVec.y * dragVec.y);
    // 当前点到中垂线的距离
    float dist = abs(current) / dragVecDist;

    // 求相对于中垂线的对称点
    // 求得拉拽方向的单位向量
    vec2 normalizedDragVec = vec2(dragVec.x/dragVecDist, dragVec.y/dragVecDist);
    // 如果origin和current符号相同，则在中垂线同侧，否则异侧
    bool needFold = origin * current > 0.0;

    // 模拟阴影
    vec4 blendColor = vBlendColor;
    if (!needFold) {
        // 计算对称点
        vec2 symmetric = vPosition - (dist * 2.0) * normalizedDragVec;
        float offset = uPageSize.x * 0.015;
        if (symmetric.x < uPageSize.x + offset && symmetric.y < uPageSize.y + 2.0*offset && symmetric.y > -2.0*offset) {
            if (blendColor.r > 0.8) {
                blendColor = vec4(vec3(0.8), 1.0);
            }
        }
    }
    if (vIsMix > 0.5) {
        gl_FragColor = mix(texture2D(uTextureUnit, vTextureCoordinates), vec4(1.0), 0.85) * blendColor * vec4(0.95, 0.95, 0.95, 1.0);
    } else {
        gl_FragColor = texture2D(uTextureUnit, vTextureCoordinates)* blendColor;
    }
}

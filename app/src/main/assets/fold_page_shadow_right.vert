precision mediump float;

const float PI = 3.1415927;

// MVP矩阵
uniform mat4 uMVPMatrix;

// 平展情况下，触摸页边的点
uniform vec2 uOriginPoint;
// 实时拖拽点
uniform vec2 uDragPoint;
// 页面尺寸
uniform vec2 uPageSize;
// 折叠高度
uniform float uFoldHeight;

varying float vAlphaRatio;

attribute vec2 aPosition;

void main() {
    vAlphaRatio = 1.0;

    vec3 newPosition = vec3(aPosition.xy, 0.0);

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

    // 当前点到中垂线的距离
    float dist = abs(current) / sqrt(dragVec.x * dragVec.x + dragVec.y * dragVec.y);

    // 求相对于中垂线的对称点
    // 求得拉拽方向的单位向量
    vec2 normalizedDragVec = normalize(dragVec);
    // 如果origin和current符号相同，则在中垂线同侧，否则异侧
    bool needFold = origin * current > 0.0;
    if (needFold) {
        // 当前点移动到对称点位置
        vec2 symmetric = aPosition + (dist * 2.0) * normalizedDragVec;
        newPosition = vec3(symmetric.xy, newPosition.z);
    }

    // 压缩
    float radius = uFoldHeight / 2.0;
    float maxDist = PI/2.0 * radius;
    if (dist < maxDist) {
        float radian = (maxDist - dist) / radius;
        float d = radius * sin(radian);
        float offsetDist = (maxDist - dist) - d;
        vec2 offsetPosition = vec2(newPosition.xy) + offsetDist * normalizedDragVec;
        newPosition.x = offsetPosition.x;
        newPosition.y = offsetPosition.y;

        vAlphaRatio = pow(cos(radian), 2.0);
    }

    // 计算当前y对应在中垂线上的x
    float cx =  (newPosition.y-y0)*dragVec.y / dragVec.x + x0;
    // 向右平移x
    float minOffset = uPageSize.x / 64.0;
    newPosition.x += minOffset + x0 / uPageSize.x * minOffset;

    gl_Position = uMVPMatrix * vec4(newPosition.xyz, 1.0);
}

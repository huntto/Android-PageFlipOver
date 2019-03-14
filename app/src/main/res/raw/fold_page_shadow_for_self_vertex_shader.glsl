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
// 折叠时的最大高度
uniform float uMaxFoldHeight;
// 折叠时的基本高度
uniform float uBaseFoldHeight;

attribute vec2 aPosition;

varying float vIsFold;

void main() {

    vec3 newPosition = vec3(aPosition.xy, uBaseFoldHeight);

    float downRatio = 0.75;
    float oldFoldHeight = uMaxFoldHeight - uBaseFoldHeight;
    float newFoldHeight = oldFoldHeight * downRatio;
    float newBaseFoldHeight = uBaseFoldHeight + (oldFoldHeight - newFoldHeight) / 2.0;
    float newMaxFoldHeight = uMaxFoldHeight - (oldFoldHeight - newFoldHeight) / 2.0;

    float r1 = (uMaxFoldHeight - uBaseFoldHeight) / 2.0;
    float r2 = r1 * downRatio;

    float newDragPointOffset = PI * r1 - 2.0 * (r1 - r2) - PI * r2;

    vIsFold = 0.0;
    vec3 normal = vec3(0.0, 0.0, 1.0);

    vec2 newDragPoint = uDragPoint + newDragPointOffset * normalize(uDragPoint - uOriginPoint);

    // 中点
    float x0 = (newDragPoint.x + uOriginPoint.x) / 2.0;
    float y0 = (newDragPoint.y + uOriginPoint.y) / 2.0;
    // 拉拽方向
    vec2 dragVec = newDragPoint - uOriginPoint;
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
        newPosition = vec3(symmetric.xy, newMaxFoldHeight);
        vIsFold = 1.0;
    }

    // 压缩
    float radius = (newMaxFoldHeight - newBaseFoldHeight) / 2.0;
    float maxDist = PI/2.0 * radius;
    float simpleLight = 1.0;
    if (dist < maxDist) {
        float alpha = (maxDist - dist) / radius;
        float d = radius * sin(alpha);
        float offsetDist = (maxDist - dist) - d;
        vec2 offsetPosition = vec2(newPosition.xy) + offsetDist * normalizedDragVec;
        newPosition.x = offsetPosition.x;
        newPosition.y = offsetPosition.y;

        float h = radius * cos(alpha);
        float height;

        vec2 centerPoint = vec2(newPosition.xy) + (maxDist - dist) * normalizedDragVec;
        if (needFold) {
            height = h + radius;
            normal = newPosition - vec3(centerPoint, radius + newBaseFoldHeight);
        } else {
            height = radius - h;
            normal = -newPosition + vec3(centerPoint, radius + newBaseFoldHeight);
        }
        newPosition.z = height + newBaseFoldHeight;
    }

    gl_Position = uMVPMatrix * vec4(newPosition.x, newPosition.y, uBaseFoldHeight + (uMaxFoldHeight - uBaseFoldHeight) / 2.0, 1.0);
}
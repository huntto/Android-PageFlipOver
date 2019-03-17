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

attribute vec2 aPosition;

varying float vIsFold;
varying float vAlphaRatio;

void main() {
    vIsFold = 0.0;
    vec2 newPosition = vec2(aPosition.xy);

    // 压缩折叠的基准高度和最大高度
    float downRatio = 0.64;
    float oldFoldHeight = uFoldHeight;
    float newFoldHeight = oldFoldHeight * downRatio;

    // 重新计算拖拽点
    float r1 = uFoldHeight / 2.0;
    float r2 = r1 * downRatio;
    float newDragPointOffset = PI * r1 - 2.0 * (r1 - r2) - PI * r2;
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
        newPosition = aPosition + (dist * 2.0) * normalizedDragVec;
        vIsFold = 1.0;
    }

    // 计算阴影边缘透明度
    if (needFold) {
        vAlphaRatio = 1.0;
        float edgeOffset = newDragPointOffset * 0.8;

        if (aPosition.x > uPageSize.x - edgeOffset) {
            vAlphaRatio *= (uPageSize.x - aPosition.x) / edgeOffset;
        } else if (aPosition.x < edgeOffset){
            vAlphaRatio *= aPosition.x / edgeOffset;
        }
        if (aPosition.y > uPageSize.y - edgeOffset) {
            vAlphaRatio *= (uPageSize.y - aPosition.y) / edgeOffset;
        } else if (aPosition.y < edgeOffset) {
            vAlphaRatio *= aPosition.y / edgeOffset;
        }
        vAlphaRatio = pow(vAlphaRatio, 2.0);
    }

    // 压缩
    float radius = newFoldHeight / 2.0;
    float maxDist = PI/2.0 * radius;
    float simpleLight = 1.0;
    if (dist < maxDist) {
        float radian = (maxDist - dist) / radius;
        float d = radius * sin(radian);
        float offsetDist = (maxDist - dist) - d;
        vec2 offsetPosition = vec2(newPosition.xy) + offsetDist * normalizedDragVec;
        newPosition.x = offsetPosition.x;
        newPosition.y = offsetPosition.y;
    }

    gl_Position = uMVPMatrix * vec4(newPosition.xy, 0.0, 1.0);
}

uniform mat4 uViewMatrix;
uniform mat4 uProjectionMatrix;
uniform mat4 uModelMatrix;

uniform float uFlat;
uniform vec2 uOriginPoint;
uniform vec2 uDragPoint;
uniform vec2 uSize;

attribute vec2 aPosition;

varying vec2 vTextureCoordinates;
varying vec4 vBlendColor;

const float BACK_Z = 1.0;
const float FRONT_Z = 0.9;
const float PI = 3.1415927;

varying float vIsMix;

void main() {
    vTextureCoordinates = vec2(aPosition.x / uSize.x, aPosition.y / uSize.y);
    if (uFlat > 0.5) {
        gl_Position = uProjectionMatrix * uViewMatrix * uModelMatrix * vec4(aPosition.x, aPosition.y, BACK_Z + 0.0001, 1.0);
    } else {
        vec3 newPosition = vec3(aPosition.xy, BACK_Z);
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

        vIsMix = 0.0;
        // 求相对于中垂线的对称点
        // 求得拉拽方向的单位向量
        vec2 normalizedDragVec = normalize(dragVec);
        // 如果origin和current符号相同，则在中垂线同侧，否则异侧
        bool needFold = origin * current > 0.0;
        if (needFold) {
            // 当前点移动到对称点位置
            vec2 symmetric = aPosition + (dist * 2.0) * normalizedDragVec;
            newPosition = vec3(symmetric.xy, FRONT_Z);
            vIsMix = 1.0;
        }

        float radius = uSize.x / 10.0;
        float maxDist = PI/2.0 * radius;
        if (dist < maxDist) {
            vBlendColor = vec4(1.0);
            float alpha = (maxDist - dist) / radius;
            float d = radius * sin(alpha);
            float offsetDist = (maxDist - dist) - d;
            vec2 offsetPosition = vec2(newPosition.xy) + offsetDist * normalizedDragVec;
            newPosition.x = offsetPosition.x;
            newPosition.y = offsetPosition.y;

            float h = radius * cos(alpha);
            float height;
            if (needFold) {
                height = h + radius;
            } else {
                height = radius - h;
            }
            newPosition.z = BACK_Z - height / (2.0 * radius) * abs(BACK_Z - FRONT_Z);
            float simpleLight = (h + radius) / (2.0 * radius);
            vBlendColor = vec4(simpleLight, simpleLight, simpleLight, 1.0);
        } else {
            vBlendColor = vec4(1.0);
        }

        gl_Position = uProjectionMatrix * uViewMatrix * uModelMatrix * vec4(newPosition.x, newPosition.y, newPosition.z, 1.0);
    }
}

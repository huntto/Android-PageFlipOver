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

varying vec2 vTextureCoordinates;
varying vec4 vBlendColor;
varying float vIsMix;

struct Light {
    // 定向光
    vec3 direction;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    vec3 color;
};

uniform Light uLight;
uniform vec3 uViewPos;

void main() {
    vTextureCoordinates = vec2(aPosition.x / uPageSize.x, aPosition.y / uPageSize.y);
    vec3 newPosition = vec3(aPosition.xy, 0.0);

    vec3 normal = vec3(0.0, 0.0, 1.0);
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
    float halfFoldHeight = uFoldHeight / 2.0;
    if (needFold) {
        // 当前点移动到对称点位置
        vec2 symmetric = aPosition + (dist * 2.0) * normalizedDragVec;
        newPosition = vec3(symmetric.xy, halfFoldHeight);
        vIsMix = 1.0;
    } else {
        newPosition.z = -halfFoldHeight;
        vIsMix = 0.0;
    }

    // 压缩
    float radius = halfFoldHeight;
    float maxDist = PI/2.0 * radius;
    if (dist < maxDist) {
        float alpha = (maxDist - dist) / radius;
        float d = radius * sin(alpha);
        float offsetDist = (maxDist - dist) - d;
        vec2 offsetPosition = vec2(newPosition.xy) + offsetDist * normalizedDragVec;
        newPosition.x = offsetPosition.x;
        newPosition.y = offsetPosition.y;

        float h = radius * cos(alpha);
        vec2 centerPoint = vec2(newPosition.xy) + (maxDist - dist) * normalizedDragVec;
        if (needFold) {
            newPosition.z = h;
            normal = newPosition - vec3(centerPoint, 0.0);
        } else {
            newPosition.z = -h;
            normal = -newPosition + vec3(centerPoint, 0.0);
        }
    }
    newPosition.z += halfFoldHeight;

    normal = normalize(normal);
    // 环境光
    vec3 ambient = uLight.ambient * uLight.color;

    // 漫反射
    vec3 direction=normalize(uLight.direction);
    float diff = max(dot(normal,-direction), 0.0);
    vec3 diffuse =uLight.diffuse * diff * uLight.color;

    // 镜面光
    vec3 viewDir = normalize(uViewPos - newPosition);
    vec3 reflectDir = reflect(-direction, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);
    vec3 specular = uLight.specular * spec * uLight.color;

    vBlendColor = vec4(ambient + diffuse + specular, 1.0);
    gl_Position = uMVPMatrix * vec4(newPosition.x, newPosition.y, newPosition.z, 1.0);
}

precision mediump float;

uniform mat4 uMatrix;

// 页面尺寸
uniform vec2 uPageSize;

attribute vec3 aPosition;

varying vec2 vTextureCoordinates;
varying vec4 vBlendColor;

struct Light {
// 定向光
    vec3 direction;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    vec3 color;
};

uniform Light uLight;

void main() {
    vTextureCoordinates = vec2(aPosition.x / uPageSize.x, aPosition.y / uPageSize.y);
    gl_Position = uMatrix * vec4(aPosition, 1.0);

    vec3 normal = normalize(vec3(0.0, 0.0, 1.0));
    // 环境光
    vec3 ambient = uLight.ambient * uLight.color;

    // 漫反射
    vec3 direction=normalize(uLight.direction);
    float diff = max(dot(normal,-direction), 0.0);
    vec3 diffuse =uLight.diffuse * diff * uLight.color;

    // 镜面光
    vec3 viewDir = normalize(vec3(0.0, 0.0, 200.0) - aPosition);
    vec3 reflectDir = reflect(-direction, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);
    vec3 specular = uLight.specular * spec * uLight.color;

    vBlendColor = vec4(ambient + diffuse + specular, 1.0);
}

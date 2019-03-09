precision mediump float;

uniform sampler2D uTextureUnit;
varying vec2 vTextureCoordinates;
varying vec4 vBlendColor;
varying float vIsMix;

struct Light {
    // 定向光
    vec3 direction;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

varying vec3 vNormal;
varying vec3 vFragPos;
uniform Light uLight;
uniform vec3 uViewPos;

void main() {
    vec4 objectColor = vec4(1.0);
    if (vIsMix > 0.5) {
        objectColor = mix(texture2D(uTextureUnit, vTextureCoordinates), vec4(1.0), 0.85) * vBlendColor;
    } else {
        objectColor = texture2D(uTextureUnit, vTextureCoordinates)* vBlendColor;
    }

    // ambient
    vec3 ambient = 0.2 * objectColor.rgb;

    // diffuse
    vec3 norm = normalize(vNormal);
    vec3 lightDir = normalize(-uLight.direction);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = uLight.diffuse * diff * objectColor.rgb;

    // specular
    vec3 viewDir = normalize(uViewPos - vFragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);
    vec3 specular = uLight.specular * spec * objectColor.rgb;

    vec3 result = ambient + specular;

    gl_FragColor = vec4(result, 1.0);
}

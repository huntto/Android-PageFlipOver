precision mediump float;

varying vec4 vPosition;
uniform highp vec3 uLightPos;

void main() {
    float dist = distance(vPosition.xyz, uLightPos);

    // 整数部分
    float intPart = floor(dist);
    float highIntPart = floor(intPart / 256.0);
    float lowIntPart = mod(intPart, 256.0);

    // 小数部分
    float fractPart = fract(dist);
    fractPart = floor(fractPart*1024.0);
    float highFractPart = floor(fractPart / 32.0);
    float lowFractPart = mod(fractPart, 32.0);

    float r = highIntPart / 256.0;
    float g = lowIntPart / 256.0;
    float b = highFractPart / 32.0;
    float a = lowFractPart / 32.0;

    gl_FragColor = vec4(r, g, b, a);
}

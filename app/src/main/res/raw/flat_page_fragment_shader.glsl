precision mediump float;

uniform sampler2D uTextureUnit;
varying vec2 vTextureCoordinates;
varying vec4 vBlendColor;

uniform sampler2D uShadowTextureUnit;
uniform highp vec3 uLightPos;
uniform mat4 uLightMVPMatrix;

varying vec4 vPosition;
varying vec4 vOriginPosition;

uniform float uDrawShadow;

void main() {
    if (uDrawShadow > 0.5) {
        vec4 lightPos = uLightMVPMatrix * vec4(vOriginPosition.xyz, 1.0);
        lightPos = lightPos / lightPos.w;
        float s=(lightPos.s+1.0)/2.0;
        float t=(lightPos.t+1.0)/2.0;
        gl_FragColor = texture2D(uShadowTextureUnit, vec2(s,t));
    } else {
        gl_FragColor = texture2D(uTextureUnit, vTextureCoordinates) * vBlendColor;
    }
}

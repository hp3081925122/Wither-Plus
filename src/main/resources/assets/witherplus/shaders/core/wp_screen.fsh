#version 150

uniform sampler2D Sampler0;
uniform vec4 WpParams;
uniform vec4 WpTint;
uniform vec4 WpFlash;

in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec2 uv = texCoord0;
    vec3 col = texture(Sampler0, uv).rgb;
    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    vec3 grey = vec3(lum) * vec3(0.9, 0.97, 1.06);
    col = mix(col, grey, clamp(WpParams.x, 0.0, 1.0));
    float d = length(uv - 0.5);
    float vig = smoothstep(0.25, 0.85, d) * WpParams.z;
    col = mix(col, WpTint.rgb, clamp(vig * WpTint.a, 0.0, 1.0));
    col = mix(col, WpFlash.rgb, clamp(WpParams.y, 0.0, 1.0) * 0.85);
    fragColor = vec4(col, 1.0);
}

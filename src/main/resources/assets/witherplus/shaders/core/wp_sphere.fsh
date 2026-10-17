#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float GameTime;
uniform vec4 WpParams;

in vec2 texCoord0;
in vec4 vertexColor;
in vec3 viewNormal;
in vec3 viewDir;

out vec4 fragColor;

void main() {
    float t = GameTime * 1200.0;
    vec3 nrm = normalize(viewNormal);
    float facing = abs(dot(nrm, normalize(viewDir)));
    float fres = 1.0 - facing;
    float mode = WpParams.x;
    float power = max(WpParams.y, 0.1);
    float scale = WpParams.z;
    float speed = WpParams.w;
    float n1 = texture(Sampler0, texCoord0 * scale + vec2(t * speed, t * speed * 0.6)).r;
    float n2 = texture(Sampler0, texCoord0 * scale * 2.0 - vec2(t * speed * 0.8, 0.0)).g;
    vec3 col = vertexColor.rgb;
    float a = 0.0;
    if (mode < 0.5) {
        float rim = pow(fres, power);
        float swirl = n1 * n2;
        col = mix(vertexColor.rgb * 0.05 * swirl, vertexColor.rgb * (1.4 + n1), rim);
        col = mix(col, vec3(1.0), pow(rim, 6.0) * 0.6);
        a = 1.0;
    } else {
        float rim = pow(fres, power);
        float cell = texture(Sampler0, texCoord0 * vec2(scale * 2.0, scale) + vec2(t * speed * 0.3, 0.0)).b;
        float lines = 1.0 - smoothstep(0.0, 0.07, cell);
        a = rim * 0.85 + lines * 0.25 * (0.3 + rim) + n1 * n2 * 0.12;
        col = mix(col, vec3(1.0), pow(rim, 3.0) * 0.5);
    }
    a *= vertexColor.a * ColorModulator.a;
    if (a < 0.004) {
        discard;
    }
    fragColor = vec4(col * ColorModulator.rgb, clamp(a, 0.0, 1.0));
}

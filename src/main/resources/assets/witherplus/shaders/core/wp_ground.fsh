#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float GameTime;
uniform vec4 WpParams;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float segDist(vec2 p, vec2 a, vec2 b) {
    vec2 pa = p - a;
    vec2 ba = b - a;
    float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
    return length(pa - ba * h);
}

void main() {
    float t = GameTime * 1200.0;
    vec2 p = texCoord0 * 2.0 - 1.0;
    float r = length(p);
    float mode = WpParams.x;
    float prog = WpParams.y;
    float seedValue = WpParams.z;
    float extra = WpParams.w;
    vec2 seed = vec2(seedValue, seedValue * 1.37);
    float n = texture(Sampler0, p * 0.35 + seed).r;
    float n2 = texture(Sampler0, p * 0.8 + seed * 2.0).g;
    float cell = texture(Sampler0, p * 0.45 + seed).b;
    float ang = atan(p.y, p.x) / 6.2831853;
    float inDisc = 1.0 - step(1.0, r);
    vec3 col = vertexColor.rgb;
    float a = 0.0;
    if (mode < 0.5) {
        float edge = r + (n - 0.5) * 0.6;
        a = smoothstep(1.0, 0.5, edge) * (0.65 + 0.35 * n2);
        col = vec3(0.03, 0.035, 0.04) + vertexColor.rgb * 0.04;
    } else if (mode < 1.5) {
        float edge = r + (n - 0.5) * 0.55;
        float rim = exp(-abs(edge - 0.75) * 8.0);
        float fl = texture(Sampler0, p * 0.6 + seed + vec2(t * 0.03, -t * 0.09)).r;
        float flames = smoothstep(0.5, 0.85, fl) * smoothstep(0.95, 0.2, edge);
        float ember = (1.0 - smoothstep(0.0, 0.08, cell)) * smoothstep(0.9, 0.3, edge) * (0.5 + 0.5 * sin(t * 4.0 + n * 25.0));
        a = rim * 0.55 + flames * 0.9 + ember * 0.6;
    } else if (mode < 2.5) {
        float rot = t * 0.004 + seedValue;
        float rings = exp(-abs(r - 0.96) * 70.0) + exp(-abs(r - 0.82) * 80.0) * 0.8 + exp(-abs(r - 0.48) * 70.0) * 0.7 + exp(-abs(r - 0.18) * 60.0) * 0.6;
        float runeAng = ang * 28.0 + t * 0.02;
        float runeId = floor(runeAng);
        float runeMask = step(0.45, texture(Sampler0, vec2(runeId * 0.137, 0.31)).r);
        float runeShape = step(0.2, fract(runeAng)) * step(fract(runeAng), 0.8) * step(0.85, r) * step(r, 0.93);
        float runeDetail = step(0.5, texture(Sampler0, vec2(runeAng * 0.21, r * 3.0)).b);
        float runes = runeShape * runeMask * (0.5 + 0.5 * runeDetail);
        float star = 1.0;
        for (int k = 0; k < 5; k++) {
            float a0 = rot * -2.0 + float(k) * 1.2566371;
            float a1 = rot * -2.0 + float(k + 2) * 1.2566371;
            vec2 v0 = vec2(cos(a0), sin(a0)) * 0.8;
            vec2 v1 = vec2(cos(a1), sin(a1)) * 0.8;
            star = min(star, segDist(p, v0, v1));
        }
        float lines = exp(-star * 70.0);
        float spokes = exp(-abs(fract(ang * 12.0 - rot) - 0.5) * 40.0) * step(0.48, r) * step(r, 0.82) * 0.35;
        float fill = 0.07 * smoothstep(1.0, 0.0, r) * (0.6 + 0.4 * n);
        float flicker = 0.85 + 0.15 * sin(t * 0.5 + n * 6.0);
        a = (rings + runes + lines * 0.9 + spokes + fill) * inDisc * flicker * (0.35 + 0.65 * prog);
        col = mix(vertexColor.rgb, vec3(1.0), lines * 0.3);
    } else if (mode < 3.5) {
        float ring = exp(-abs(r - 0.94) * 40.0);
        float fill = (1.0 - step(prog, r)) * 0.2 * (0.6 + 0.4 * n);
        float front = exp(-abs(r - prog) * 30.0) * 0.7;
        float dash = step(0.5, fract(ang * 18.0 - t * 0.4));
        a = (ring * (0.6 + 0.4 * dash) + fill + front) * inDisc;
    } else {
        vec2 q = p * extra;
        vec2 id = floor(q);
        vec2 f = fract(q);
        float parity = mod(id.x + id.y, 2.0);
        float match = 1.0 - step(0.5, abs(parity - seedValue));
        float border = min(min(f.x, 1.0 - f.x), min(f.y, 1.0 - f.y));
        float edge = exp(-border * 25.0);
        float pulse = 1.0;
        if (prog > 0.8) {
            pulse = 0.65 + 0.35 * sin(t * 2.2);
        }
        float fill = match * (0.1 + 0.65 * prog * prog) * (0.65 + 0.35 * n) * pulse;
        a = (fill + edge * match * (0.4 + 0.6 * prog) + edge * (1.0 - match) * 0.12) * inDisc;
    }
    a *= vertexColor.a * ColorModulator.a;
    if (a < 0.004) {
        discard;
    }
    fragColor = vec4(col * ColorModulator.rgb, clamp(a, 0.0, 1.0));
}

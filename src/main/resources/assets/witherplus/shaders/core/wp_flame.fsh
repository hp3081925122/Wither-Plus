#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float GameTime;
uniform vec4 WpParams;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float boxSdf(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

void main() {
    float t = GameTime * 1200.0;
    float mode = WpParams.x;
    float speed = WpParams.y;
    float scale = max(WpParams.z, 0.01);
    float extra = WpParams.w;
    vec2 uv = texCoord0;
    vec3 base = vertexColor.rgb;
    float glow = 0.0;
    float hot = 0.0;

    if (mode > 1.5 && mode < 2.5) {
        vec2 p = uv * 2.0 - 1.0;
        float d = length(p);
        float n = texture(Sampler0, p * 0.25 * scale + vec2(t * speed * 0.03, -t * speed * 0.02)).r;
        float n2 = texture(Sampler0, p * 0.5 * scale - vec2(t * speed * 0.02, 0.0) + vec2(0.37, 0.11)).g;
        float shape = smoothstep(1.0, 0.25, d + (n - 0.5) * 0.9) * (0.55 + 0.45 * n2);
        vec3 smoke = base * (0.55 + 0.6 * n);
        float a = shape * vertexColor.a * ColorModulator.a;
        if (a < 0.004) {
            discard;
        }
        fragColor = vec4(smoke * ColorModulator.rgb, clamp(a, 0.0, 1.0));
        return;
    }

    if (mode > 5.5 && mode < 6.5) {
        float along = uv.x;
        float around = uv.y;
        float cell = texture(Sampler0, vec2(around * 2.0, along * 0.6) + vec2(extra, 0.0)).b;
        float crack = 1.0 - smoothstep(0.0, 0.07, cell);
        float ridge = 0.86 + 0.14 * sin(along * 34.0);
        float shade = mix(0.42, 1.0, smoothstep(0.0, 0.3, along));
        float light = 0.68 + 0.32 * sin(around * 6.2831853);
        vec3 col = base * shade * ridge * light;
        col = mix(col, col * 0.35, crack * 0.7);
        col += vec3(0.36, 0.94, 1.0) * crack * smoothstep(0.1, 0.6, along) * 0.8;
        float a = vertexColor.a * ColorModulator.a;
        if (a < 0.004) {
            discard;
        }
        fragColor = vec4(col * ColorModulator.rgb, clamp(a, 0.0, 1.0));
        return;
    }

    if (mode < 0.5) {
        float across = abs(uv.y * 2.0 - 1.0);
        float n1 = texture(Sampler0, vec2(uv.x * scale - t * speed, uv.y * 0.5 + t * 0.07)).r;
        float n2 = texture(Sampler0, vec2(uv.x * scale * 0.5 - t * speed * 0.6, uv.y * 0.9 - t * 0.13)).g;
        float body = 1.0 - across;
        float flame = smoothstep(0.0, 0.5, body * (0.6 + 0.9 * n1) - (1.0 - n2) * 0.35);
        glow = flame * (0.7 + 0.6 * n1);
        hot = pow(body, 4.0) * smoothstep(0.35, 0.9, n1) * (1.0 + extra);
    } else if (mode < 1.5) {
        vec2 p = uv * 2.0 - 1.0;
        float d = length(p);
        float ang = atan(p.y, p.x) / 6.2831853;
        float n = texture(Sampler0, vec2(ang * 2.0 + t * speed * 0.2, d * scale - t * speed)).r;
        float core = clamp(1.0 - d, 0.0, 1.0);
        glow = pow(core, 1.5) * (0.6 + 0.9 * n) * smoothstep(1.0, 0.55, d + (n - 0.5) * 0.4);
        hot = pow(core, 4.0) * (1.0 + extra);
    } else if (mode < 3.5) {
        vec2 p = uv * 2.0 - 1.0;
        vec2 jitter = vec2(texture(Sampler0, uv * 0.6 + vec2(0.0, -t * 0.05)).r - 0.5, texture(Sampler0, uv * 0.6 + vec2(0.5, -t * 0.04)).g - 0.5);
        p += jitter * 0.06;
        float cranium = length((p - vec2(0.0, 0.16)) * vec2(1.0, 1.05)) - 0.6;
        float jaw = boxSdf(p - vec2(0.0, -0.42), vec2(0.3, 0.2), 0.08);
        float s = min(cranium, jaw);
        float eyeL = length((p - vec2(-0.24, 0.06)) * vec2(1.0, 1.25)) - 0.17;
        float eyeR = length((p - vec2(0.24, 0.06)) * vec2(1.0, 1.25)) - 0.17;
        float nose = length((p - vec2(0.0, -0.2)) * vec2(1.7, 1.0)) - 0.07;
        float eyes = min(eyeL, eyeR);
        float holes = min(eyes, nose);
        float teeth = step(0.72, abs(fract((p.x + 0.3) * 6.0) - 0.5) * 2.0) * step(abs(p.y + 0.44), 0.07) * step(abs(p.x), 0.28);
        s = max(s, -holes);
        float inside = smoothstep(0.03, -0.03, s) * (1.0 - teeth * 0.8);
        float rim = exp(-abs(s) * 18.0);
        float n = texture(Sampler0, p * 0.5 + vec2(0.0, -t * speed * 0.1)).r;
        float halo = exp(-max(s, 0.0) * 5.0) * smoothstep(0.35, 0.8, n) * smoothstep(-0.6, 0.7, p.y) * (1.0 - step(s, 0.0));
        float eyeGlow = smoothstep(0.04, -0.08, eyes) + exp(-max(eyes, 0.0) * 14.0) * 0.5;
        glow = inside * (0.22 + 0.45 * n) + rim * 0.9 + halo * 0.9 + eyeGlow * 1.3;
        hot = eyeGlow + rim * 0.25;
    } else if (mode < 4.5) {
        float h = uv.x;
        float n1 = texture(Sampler0, vec2(uv.y * scale, h * 0.8 - t * speed)).r;
        float n2 = texture(Sampler0, vec2(uv.y * scale * 2.0 + 0.3, h * 1.6 - t * speed * 1.7)).g;
        float tongue = smoothstep(0.0, 0.25, (1.0 - h) * (0.55 + 1.4 * n1 * n2) - h * 0.3);
        glow = tongue * (0.6 + 0.8 * n1) * smoothstep(0.0, 0.06, h);
        hot = pow(1.0 - h, 3.0) * n1 * (1.0 + extra);
    } else if (mode > 6.5) {
        float h = uv.x;
        float seed = floor(uv.y);
        float x = fract(uv.y) * 2.0 - 1.0;
        vec2 off = vec2(seed * 0.137, seed * 0.291);
        float n1 = texture(Sampler0, vec2(x * 0.18 * scale, h * 0.55 - t * speed) + off).r;
        float n2 = texture(Sampler0, vec2(x * 0.35 * scale + 0.41, h * 1.2 - t * speed * 1.7) + off).g;
        float n3 = texture(Sampler0, vec2(x * 0.7 * scale + 0.73, h * 2.4 - t * speed * 2.6) + off).r;
        float sway = (n1 - 0.5) * 0.9 * h * h;
        float width = pow(max(1.0 - h, 0.0), 0.75) * (0.55 + 0.6 * n2);
        float d = abs(x - sway) / max(width, 0.001);
        float edge = smoothstep(1.0, 0.25, d + (n3 - 0.5) * 0.5);
        float foot = smoothstep(0.0, 0.22, h);
        float lick = smoothstep(0.0, 0.35, (1.0 - h) * (0.5 + 1.3 * n1 * n2) - h * 0.15);
        glow = edge * foot * lick * (0.55 + 0.75 * n3);
        hot = pow(clamp(1.0 - d, 0.0, 1.0), 2.0) * pow(1.0 - h, 1.5) * (0.6 + extra);
    } else {
        float idx = floor(uv.x * scale);
        float lx = fract(uv.x * scale) * 2.0 - 1.0;
        float ly = uv.y * 2.0 - 1.0;
        float link = 0.0;
        if (mod(idx, 2.0) < 0.5) {
            float d = length(vec2(lx * 0.95, ly * 1.35));
            link = exp(-abs(d - 0.72) * 14.0);
        } else {
            link = exp(-abs(ly) * 9.0) * (1.0 - smoothstep(0.85, 1.0, abs(lx)));
        }
        float flow = 0.6 + 0.7 * texture(Sampler0, vec2(uv.x * 0.05 - t * speed, 0.5)).r;
        glow = link * flow * 1.2 + 0.12 * (1.0 - abs(ly));
        hot = pow(link, 3.0) * flow;
    }

    vec3 col = mix(base, vec3(1.0), clamp(hot, 0.0, 1.0) * 0.8);
    float alpha = clamp(glow, 0.0, 1.0) * vertexColor.a * ColorModulator.a;
    if (alpha < 0.004) {
        discard;
    }
    fragColor = vec4(col * ColorModulator.rgb, alpha);
}

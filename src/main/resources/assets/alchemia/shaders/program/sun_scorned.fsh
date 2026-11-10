#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

float luma(vec3 colour) {
    return dot(colour, vec3(0.299, 0.587, 0.114));
}

void main() {
    vec4 colour = texture(DiffuseSampler, texCoord);
    float light = luma(colour.rgb);

    float kept = 0.5 + min(0.2, light) * 2.5;
    vec3 toned = mix(vec3(light), colour.rgb, kept);

    float overall = 0.0;
    for (int across = 1; across <= 9; across++) {
        for (int down = 1; down <= 9; down++) {
            overall += luma(texture(DiffuseSampler, vec2(across, down) / 10.0).rgb);
        }
    }
    overall /= 81.0;
    vec3 exposed = toned * clamp(0.3 / max(overall, 0.001), 0.7, 4.0);
    vec3 settled = exposed * 0.4 + toned * 0.6;

    vec3 around = vec3(0.0);
    for (int i = -2; i <= 2; i++) {
        float reach = float(i) * 2.0;
        around += texture(DiffuseSampler, texCoord + vec2(reach, reach) * oneTexel * 0.7).rgb;
        around += texture(DiffuseSampler, texCoord + vec2(reach, -reach) * oneTexel * 0.7).rgb;
        around += texture(DiffuseSampler, texCoord + vec2(0.0, reach) * oneTexel).rgb;
        around += texture(DiffuseSampler, texCoord + vec2(reach, 0.0) * oneTexel).rgb;
    }
    around /= 20.0;
    vec3 glow = around * around * (0.015 - light * 0.01) * 5.0;

    fragColor = vec4((settled + glow) * 0.9, 1.0);
}

#version 150

layout(std140) uniform MHBasePosition {
    mat4 u_Translation;
    vec3 u_Layout; // (halfWidth, halfHeight, cornerRadius)
};
layout(std140) uniform MHNowPlayingThemeColor {
    vec4 u_Primary;
    vec4 u_Secondary;
    vec4 u_Bright;
    vec4 u_Dark;
};
layout(std140) uniform MHDynamicStatus {
    vec4 u_Dynamic1; // (timestamp, playedProgress, switchProgress)
};

in vec2 f_Position;
in vec4 f_Color;

out vec4 fragColor;

float aastep(float x) {
    vec2 grad = vec2(dFdx(x), dFdy(x));
    float afwidth = 0.7 * length(grad);
    return smoothstep(-afwidth, afwidth, x);
}

void main() {
    float halfWidth  = u_Layout[0];
    float halfHeight = u_Layout[1];
    float radius     = u_Layout[2];
    float timestamp  = u_Dynamic1[0];

    // Simplified gradient animation using sine/cosine instead of expensive noise
    vec2 uv = f_Position / vec2(halfWidth * 2.0, halfHeight * 2.0);
    float t = timestamp * 0.02;

    // Simple animated gradient
    float gradient = sin(uv.x * 3.14159 + t) * 0.5 + 0.5;
    float gradient2 = cos(uv.y * 3.14159 + t * 0.7) * 0.5 + 0.5;

    // Mix colors based on simple gradients
    vec3 c0 = u_Dark.rgb;
    vec3 c1 = u_Primary.rgb;
    vec3 c2 = u_Secondary.rgb;
    vec3 c3 = u_Bright.rgb;

    vec3 rgb = mix(
        mix(c0, c1, gradient),
        mix(c2, c3, gradient),
        gradient2
    );

    float alpha = mix(
        mix(u_Dark.a, u_Primary.a, gradient),
        mix(u_Secondary.a, u_Bright.a, gradient),
        gradient2
    );

    // Rounded rectangle mask
    vec2 halfSize = vec2(halfWidth, halfHeight);
    vec2 d = abs(f_Position) - halfSize + radius;
    float dis = length(max(d, 0.0)) + min(max(d.x, d.y), 0.0) - radius;
    float mask = 1.0 - aastep(dis);

    fragColor = vec4(rgb, alpha * mask);
}

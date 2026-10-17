#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
layout(std140) uniform WpUniforms {
    vec4 WpParams;
    vec4 WpTint;
    vec4 WpFlash;
    vec2 ScreenSize;
    float GameTime;
};


in vec3 Position;
in vec2 UV0;
in vec4 Color;


out vec2 texCoord0;
out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord0 = UV0;
    vertexColor = Color;
}

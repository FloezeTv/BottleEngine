#version 330 core

in vec2 texCoord;
flat in int texIndex;

out vec4 fragColor;

uniform vec3 color;
uniform sampler2DArray textures;

void main() {
	fragColor = vec4(color, texture(textures, vec3(texCoord, texIndex)).r);
}

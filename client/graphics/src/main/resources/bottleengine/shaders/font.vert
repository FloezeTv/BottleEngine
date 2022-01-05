#version 330 core

layout (location = 0) in vec2 pos;
layout (location = 1) in vec2 tex;
layout (location = 2) in int texIdx;

out vec2 texCoord;
flat out int texIndex;

uniform mat4 model;

void set_position(mat4 model, vec3 position);

void main() {
	texCoord = tex;
	texIndex = texIdx;

	set_position(model, vec3(pos, 0.0));
}

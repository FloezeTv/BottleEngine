#version 330 core

layout (location = 0) in vec3 pos;
layout (location = 1) in vec2 tex;

out vec2 texCoord;

uniform mat4 model;

void set_position(mat4 model, vec3 position);

void main() {
	texCoord = tex;
	set_position(model, pos);
}

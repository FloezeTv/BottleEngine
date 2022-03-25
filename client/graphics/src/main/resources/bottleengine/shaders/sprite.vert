#version 330 core

layout (location = 0) in vec3 pos;
layout (location = 1) in vec2 tex;

out vec2 texCoord;

uniform mat4 model;

void set_position(mat4 model, vec3 position);

void set_normal(mat4 model, vec3 normal);

void main() {
	texCoord = tex;
	set_position(model, pos);
	set_normal(model, vec3(0, 0, 1));
}

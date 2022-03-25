#version 330 core

layout (location = 0) in vec3 pos;

out vec2 coord;

uniform mat4 model;

void set_position(mat4 model, vec3 position);

void set_normal(mat4 model, vec3 normal);

void main() {
	set_position(model, pos);
	coord = pos.xy;
	set_normal(model, vec3(0, 0, 1));
}

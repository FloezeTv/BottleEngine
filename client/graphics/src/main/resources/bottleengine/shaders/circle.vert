#version 330 core

layout (location = 0) in vec3 pos;

out vec2 coord;

uniform mat4 model;

void set_position(mat4 model, vec3 position);

void main() {
	set_position(model, pos);
	coord = pos.xy;
}

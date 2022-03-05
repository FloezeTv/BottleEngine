#version 330 core

in vec2 coord;

out vec4 fragColor;

uniform vec4 color;

const float radius = 0.5;
const vec2 center = vec2(0, 0);

void main() {
	if (distance(center, coord) <= radius) {
		fragColor = color;
	} else {
		fragColor = vec4(0);
	}
}

#version 330 core

in vec2 coord;

out vec4 fragColor;

uniform vec4 color;

const float radius = 0.5;
const vec2 center = vec2(0, 0);

void main() {
	float distance = distance(center, coord);
	float delta = fwidth(distance);
	float alpha = smoothstep(radius, radius - delta, distance);
	fragColor = vec4(color.xyz, color.w * alpha);
}

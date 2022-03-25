#version 330 core

in vec2 coord;

out vec4 fragColor;

uniform vec4 color;

uniform float specular;

const float radius = 0.5;
const vec2 center = vec2(0, 0);

void render_albedo(vec4 albedo);

void copy_position();

void copy_normal();

void render_specular(float specular);

void main() {
	float distance = distance(center, coord);
	float delta = fwidth(distance);
	float alpha = smoothstep(radius, radius - delta, distance);
	vec4 col = vec4(color.xyz, color.w * alpha);
	render_albedo(col);
	copy_position();
	copy_normal();
	render_specular(col.a * specular);
}

#version 330 core

out vec4 fragColor;

uniform vec4 color;

uniform float specular;

void render_albedo(vec4 albedo);

void copy_position();

void copy_normal();

void render_specular(float specular);

void main() {
	render_albedo(color);
	copy_position();
	copy_normal();
	render_specular(color.a * specular);
}

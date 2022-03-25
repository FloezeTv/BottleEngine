#version 330 core

in vec2 texCoord;

out vec4 fragColor;

uniform sampler2D texture;

uniform float specular;

void render_albedo(vec4 albedo);

void copy_position();

void copy_normal();

void render_specular(float specular);

void main() {
	vec4 color = texture2D(texture, texCoord);
	render_albedo(color);
	copy_position();
	copy_normal();
	render_specular(color.a * specular);
}

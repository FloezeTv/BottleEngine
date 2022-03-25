#version 330 core

in vec2 texCoord;
flat in int texIndex;

out vec4 fragColor;

uniform vec3 color;
uniform sampler2DArray textures;

uniform float specular;

void render_albedo(vec4 albedo);

void copy_position();

void copy_normal();

void render_specular(float specular);

void main() {
	vec4 col = vec4(color, texture(textures, vec3(texCoord, texIndex)).r);
	render_albedo(col);
	copy_position();
	copy_normal();
	render_specular(col.a * specular);
}

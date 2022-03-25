#version 330 core

in VS_OUT {
	int tile;
} gs_in[];

uniform ivec2 tileSetSize;
uniform mat4 model;

out vec2 texCoord;

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

void set_position(mat4 model, vec3 position);

void set_normal(mat4 model, vec3 normal);

void main() {
	float tx = float(gs_in[0].tile % tileSetSize.x) / tileSetSize.x;
	float ty = float(gs_in[0].tile / tileSetSize.x) / tileSetSize.y;
	float tw = 1.0 / tileSetSize.x;
	float th = 1.0 / tileSetSize.y;

	vec3 pos = gl_in[0].gl_Position.xyz;

	set_position(model, vec3(pos.x, pos.y, pos.z));
	texCoord = vec2(tx, ty);
	set_normal(model, vec3(0, 0, 1));
	EmitVertex();

	set_position(model, vec3(pos.x, pos.y + 1, pos.z));
	texCoord = vec2(tx, ty + th);
	set_normal(model, vec3(0, 0, 1));
	EmitVertex();

	set_position(model, vec3(pos.x + 1, pos.y, pos.z));
	texCoord = vec2(tx + tw, ty);
	set_normal(model, vec3(0, 0, 1));
	EmitVertex();

	set_position(model, vec3(pos.x + 1, pos.y + 1, pos.z));
	texCoord = vec2(tx + tw, ty + th);
	set_normal(model, vec3(0, 0, 1));
	EmitVertex();

	EndPrimitive();
}

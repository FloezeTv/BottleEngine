#version 330 core

layout (location = 0) in int tileId;

uniform ivec2 tileMapSize;

out VS_OUT {
	int tile;
} vs_out;

void main() {
	int x = gl_VertexID % tileMapSize.x;
	int y = gl_VertexID / tileMapSize.x;

	gl_Position = vec4(x, y, 0, 1);

	vs_out.tile = tileId;
}

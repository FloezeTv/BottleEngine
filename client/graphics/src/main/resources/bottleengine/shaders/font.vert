#version 330 core

layout (location = 0) in vec2 pos;
layout (location = 1) in vec2 tex;
layout (location = 2) in int texIdx;

out vec2 texCoord;
out int texIndex;

layout (std140) uniform Camera {
	mat4 projection;
	mat4 view;
};
uniform mat4 model;

void main() {
	texCoord = tex;
	texIndex = texIdx;

	gl_Position = projection * view * model * vec4(pos, 0.0, 1.0);
}

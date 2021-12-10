#version 330 core

layout (location = 0) in vec3 pos;
layout (location = 1) in vec2 tex;

out vec2 texCoord;

layout (std140) uniform Camera {
	mat4 projection;
	mat4 view;
};
uniform mat4 model;

void main() {
	texCoord = tex;
	gl_Position = projection * view * model * vec4(pos, 1.0);
}

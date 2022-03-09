#version 330 core

in vec2 texCoord;

uniform sampler2D texture;

out vec4 fragColor;

void main() {
	fragColor = texture2D(texture, texCoord);
}

#version 330 core

in vec2 texCoord;

out vec4 fragColor;

uniform sampler2D albedo;
uniform sampler2D position;
uniform sampler2D normal;
uniform sampler2D specular;

uniform float strength;

void main() {
	vec4 color = texture2D(albedo, texCoord);
	fragColor.rgb = color.rgb * strength;
	fragColor.a = color.a;
}

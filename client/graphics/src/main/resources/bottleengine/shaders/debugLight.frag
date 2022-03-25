#version 330 core

in vec2 texCoord;

out vec4 fragColor;

uniform sampler2D albedo;
uniform sampler2D position;
uniform sampler2D normal;
uniform sampler2D specular;

void main() {
	vec2 coord = texCoord;

	coord *= 2;
	if(coord.x > 1) { // Right
		coord.x -= 1;
		if(coord.y > 1) { // Top
			coord.y -= 1;
			// Normal
			fragColor = vec4(vec3(texture2D(normal, coord)), 1.0);
		} else { // Bottom
			// Specular
			fragColor = vec4(vec3(texture2D(specular, coord).r), 1.0);
		}
	} else { // Left
		if(coord.y > 1) { // Top
			coord.y -= 1;
			// Position
			fragColor = vec4(vec3(texture2D(position, coord)), 1.0);
		} else { // Bottom
			// Albedo
			fragColor = vec4(vec3(texture2D(albedo, coord)), 1.0);
		}
	}
}

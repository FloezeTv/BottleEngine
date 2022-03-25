#version 330 core

in vec2 texCoord;

out vec4 fragColor;

uniform sampler2D albedo;
uniform sampler2D position;
uniform sampler2D normal;
uniform sampler2D specular;

uniform vec3 lightPosition;
uniform vec3 lightColor;
uniform float strength;
uniform float linearAttenuation;
uniform float quadraticAttenuation;

vec3 get_camera_position();

void main() {
	vec3 FragPos = texture(position, texCoord).rgb;
	vec3 Normal = normalize(texture(normal, texCoord).rgb);
	vec3 Albedo = texture(albedo, texCoord).rgb;
	float Specular = texture(specular, texCoord).r;

	// diffuse
	vec3 lightDir = normalize(lightPosition - FragPos);
	vec3 diffuse = max(dot(Normal, lightDir), 0.0) * Albedo * lightColor;

	// specular
	vec3 viewDir = normalize(get_camera_position() - FragPos);
	vec3 halfwayDir = normalize(lightDir + viewDir);  
	float spec = pow(max(dot(Normal, halfwayDir), 0.0), 16.0);
	vec3 specular = lightColor * spec * Specular;

	// attenuation
	float distance = length(lightPosition - FragPos);
	float attenuation = strength / (1.0 + linearAttenuation * distance + quadraticAttenuation * distance * distance);
	diffuse *= attenuation;
	specular *= attenuation;

	// final Color
	fragColor = vec4(diffuse + specular, 1.0);
}

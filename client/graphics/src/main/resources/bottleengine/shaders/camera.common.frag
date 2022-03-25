#version 330 core

#define ALBEDO_BIT 1 << 0
#define POSITION_BIT 1 << 1
#define NORMAL_BIT 1 << 2
#define SPECULAR_BIT 1 << 3

layout (location = 0) out vec4 gAlbedo; // The albedo output
layout (location = 1) out vec4 gPosition; // The position output
layout (location = 2) out vec3 gNormal; // The normal output
layout (location = 3) out float gSpecular; // The specular output

layout (std140, binding = 0) uniform Camera {
	mat4 cView; // Camera view matrix
	mat4 cProjection; // Camera projection matrix
	vec3 cPosition; // Camera position
};

layout (std140, binding = 1) uniform Renderer {
	int toRender; // mask of what things to render
};

in vec4 vPosition; // vertex world position input
in vec3 vNormal; // vertex normal input

/*
 * Gets the camera position
 */
vec3 get_camera_position() {
	return cPosition;
}

/*
 * Checks if all parts of a mask should be rendered
 */
bool should_render(int mask) {
	return (toRender & mask) != 0;
}

/*
 * Checks if albedo should be rendered
 */
bool should_render_albedo() {
	return should_render(ALBEDO_BIT);
}

/*
 * Checks if position should be rendered
 */
bool should_render_position() {
	return should_render(POSITION_BIT);
}

/*
 * Checks if normal should be rendered
 */
bool should_render_normal() {
	return should_render(NORMAL_BIT);
}

/*
 * Checks if specular should be rendered
 */
bool should_render_specular() {
	return should_render(SPECULAR_BIT);
}

/*
 * Renders albedo
 */
void render_albedo(vec4 albedo) {
	if(should_render_albedo())
		gAlbedo = albedo;
}

/*
 * Renders position
 */
void render_position(vec4 position) {
	if(should_render_position())
		gPosition = position;
}

/*
 * Renders normal
 */
void render_normal(vec3 normal) {
	if(should_render_normal())
		gNormal = normal;
}

/*
 * Renders specular
 */
void render_specular(float specular) {
	if(should_render_specular())
		gSpecular = specular;
}

/*
 * Copies position from position set in vertex shader
 */
void copy_position() {
	render_position(vPosition);
}

/*
 * Copies normal from normal set in vertex shader
 */
void copy_normal() {
	render_normal(normalize(vNormal));
}
#version 330 core

#define ALBEDO_BIT 1 << 0
#define POSITION_BIT 1 << 1
#define NORMAL_BIT 1 << 2
#define SPECULAR_BIT 1 << 3

layout (std140, binding = 0) uniform Camera {
	mat4 cView; // Camera view matrix
	mat4 cProjection; // Camera projection matrix
	vec3 cPosition; // Camera position
};

layout (std140, binding = 1) uniform Renderer {
	int toRender; // mask of what things to render
};

out vec4 vPosition; // vertex world position output
out vec3 vNormal; // vertex normal output

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
 * Calculates the position in the world using the model matrix
 */
vec4 calculate_position_world(mat4 model, vec3 position) {
	return model * vec4(position, 1.0);
}

/*
 * Calculates the vec4 for OpenGL including the camera
 */
vec4 calculate_position(mat4 model, vec3 position) {
	return cProjection * cView * calculate_position_world(model, position);
}

/*
 * Sets the gl_Position to the result of calculate_position
 */
void set_position(mat4 model, vec3 position) {
	gl_Position = calculate_position(model, position);
	if(should_render_position())
		vPosition = calculate_position_world(model, position);
}

/*
 * Calculates the normal vector using a model matrix
 */
vec3 calculate_normal(mat4 model, vec3 normal) {
	return transpose(inverse(mat3(model))) * normal;
}

/*
 * Sets the normal vector to the result of calculate_normal
 */
void set_normal(mat4 model, vec3 normal) {
	if(should_render_normal())
		vNormal = calculate_normal(model, normal);
}

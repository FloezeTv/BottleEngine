#version 330 core

layout (std140) uniform Camera {
	mat4 view;
	mat4 projection;
};

/*
 * Calculates the vec4 for OpenGL including the camera
 */
vec4 calculate_position(mat4 model, vec3 position) {
	return projection * view * model * vec4(position, 1.0);
}

/*
 * Sets the gl_Position to the result of calculate_position
 */
void set_position(mat4 model, vec3 position) {
	gl_Position = calculate_position(model, position);
}

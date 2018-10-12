typedef struct Vec3 {
	float x;
	float y;
	float z;
} Vec3;

__kernel void vecAdd(__global const Vec3 *v0, __global const Vec3 *v1, __global Vec3 *res) {
	res->x = 5;
}
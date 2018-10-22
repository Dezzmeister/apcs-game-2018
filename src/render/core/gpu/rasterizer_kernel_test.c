typedef struct Vec3 {
	float x;
	float y;
	float z;
} Vec3;

typedef struct Vec2 {
	float x;
	float y;
} Vec2;

typedef struct Line {
	Vec3* v0;
	Vec3* v1;
} Line;

__kernel void rasterizeLine(__global const Vec2 *uv0,
							__global const Vec2 *uv1,
							__global const Vec2 *uv2,
							__global const int *texture,
							__global const Vec3 *v03,
							__global const Vec3 *v13,
							__global const Vec3 *v23,
							__global const Vec3 *cameraPos,
							__global const Vec2 *cameraDir,
							__global const Vec2 *cameraPlane,
							__global const Vec3 *bv0,
							__global const Vec3 *bv1,
							__global int *screen,
							__global int *zbuf,
							__global const int *auxIntData,
							__global const float *auxFloatData)
{
	int y = auxData[7] + get_global_id(0);
	
}
/*
	This program is a barycentric single-line triangle rasterizer kernel for an OpenCL device,
	preferably a GPU.
	
	Kernel Arguments: (assume all are pointers unless specified otherwise)
		uv0: uv0 UV coordinate of Triangle
		uv1: uv1 UV coordinate of Triangle
		uv2: uv2 UV coordinate of Triangle
		texture: texture pixels array of Triangle if it exists, null pointer otherwise
		
		v03: coordinate of Triangle's first vertex in world
		v13: coordinate of Triangle's second vertex in world
		v23: coordinate of Triangles third vertex in world
		
		cameraPos: 3D coordinate of camera in world
		cameraDir: 2D dir vector
		cameraPlane: 2D viewplane vector
		
		bv0: Triangle's bv0 coordinate, precalculated for barycentric weight calculation
		bv1: Triangle's bv1 coordinate, precalculated for barycentric weight calculation
		
		screen: screen pixels to be drawn to
		zbuf: z-buffer
		
		auxIntData: contains various int values:
			0 -> texWidth: width of Triangle texture
			1 -> texHeight: height of Triangle texture
			2 -> darkenBy: color value by which to darken the Triangle
			3 -> color: default color of the Triangle
			4 -> HUD_TRUE_HEIGHT: height of the rendered screen without HUD
			5 -> WIDTH: width of rendered screen
			6 -> HEIGHT: height of rendered screen with HUD
			7 -> startY: screen y value of top pixel/stripe
			8 -> minX: screen x value of leftmost pixel/stripe
			9 -> maxX: screen x value of rightmost pixel/stripe
			10 -> shadeType: depth shading model to use:
				0 -> NONE
				1 -> LINEAR
				2 -> QUADRATIC
			11 -> true3DTexturesEnabled: whether texture or default color should be used
			12 -> FULL_FOG_DISTANCE: distance for depth shading to reach max darkness
			13 -> SHADE_THRESHOLD: max depth shade value
		auxFloatData: contains various float values:
			0 -> d00: precalculated for barycentric weight calculation
			1 -> d01: precalculated for barycentric weight calculation
			2 -> d11: precalculated for barycentric weight calculation
			3 -> invDenom: precalculated for barycentric weight calculation
*/
#define NULL 0

#define NONE 0
#define LINEAR 1
#define QUADRATIC 2

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

Vec3* reverseProject(int x, int y);
Vec3* linePlaneIntersection(Line* l, Vec3* pv0, Vec3* pv1, Vec3* pv2);
Vec3* getNormal(Vec3* v0, Vec3* v1, Vec3* v2);
Vec3* cross(Vec3* v0, Vec3* v1);
Vec3* minus(Vec3* v0, Vec3* v1);
Vec3* plus(Vec3* v0, Vec3* v1);
Vec2* plus2(Vec2* v0, Vec2* v1);
Vec3* scale(Vec3* v, float f);
Vec2* scale2(Vec2* v, float f);
Vec3* computeBarycentricWeights(Vec3* v, Vec3* tv0, Vec3* bv0, Vec3* bv1, float* aux);
float distance(Vec3* v0, Vec3* v1);
int shade(int shadeType, int FULL_FOG_DISTANCE, int SHADE_THRESHOLD, float distance, int color);

int shade(int shadeType, int FULL_FOG_DISTANCE, int SHADE_THRESHOLD, float distance, int color) {
	float darkenBy = 0;
	
	float _x = distance >= FULL_FOG_DISTANCE ? FULL_FOG_DISTANCE : distance;
	float a = SHADE_THRESHOLD / 100.0f;
	float b = 2 * FULL_FOG_DISTANCE;
	darkenBy = (-(a * _x) * (_x - b));
	
	int red = (color >> 16) & 0xFF;
	int green = (color >> 8) & 0xFF;
	int blue = color & 0xFF;
	
	red -= (red - darkenBy >= 0) ? darkenBy : red;
	green -= (green - darkenBy >= 0) ? darkenBy : green;
	blue -= (blue - darkenBy >= 0) ? darkenBy : blue;
	
	color = (red << 16) | (green << 8) | blue;
	
	return color
}

inline float dot(Vec3 *a, Vec3 *b) {
	return ((a->x * b->x) + (a->y * b->y) + (a->z * b->z));
}

Vec3* reverseProject(int x, int y, int WIDTH, int HEIGHT, Vec2 *dir, Vec2 *plane, Vec3 *camera) {
	float xNorm = (2 * x) / ((float)WIDTH) - 1.0f;
	
	float rdirx = dir->x + plane->x * xNorm;
	float rdiry = dir->y + plane->y * xNorm;
	
	float zNorm = 1 - (y / (float)HEIGHT);
	
	Vec3 res;
	res.x = rdirx + camera->x;
	res.y = rdiry + camera->y;
	res.z = zNorm;
	
	return &res;
}

Vec3* scale(Vec3* v, float f) {
	Vec3 res;
	res.x = v->x * f;
	res.y = v->y * f;
	res.z = v->z * f;
	
	return &res;
}

Vec2* scale2(Vec2* v, float f) {
	Vec2 res;
	res.x = v->x * f;
	res.y = v->y * f;
	
	return &res;
}

Vec3* plus(Vec3* v0, Vec3* v1) {
	Vec3 res;
	res.x = v0->x + v1->x;
	res.y = v0->y + v1->y;
	res.z = v0->z + v1->z;
	
	return &res;
}

Vec3* minus(Vec3* v0, Vec3* v1) {
	Vec3 res;
	res.x = v0->x - v1->x;
	res.y = v0->y - v1->y;
	res.z = v0->z - v1->z;
	
	return &res;
}

Vec3* cross(Vec3* v0, Vec3* v1) {
	Vec3 res;
	res.x = (v0->y * v1->z) - (v0->z * v1->y);
	res.y = (v0->z * v1->x) - (v0->x * v1->z);
	res.z = (v0->x * v1->y) - (v0->y * v1->x);
	
	return &res;
}

Vec2* plus2(Vec2* v0, Vec2* v1) {
	Vec2 res;
	res.x = v0->x + v1->x;
	res.y = v0->y + v1->y;
	
	return &res;
}

inline Vec3* getNormal(Vec3* v0, Vec3* v1, Vec3* v2) {
	return cross(minus(v1,v0), minus(v2,v0));
}

float distance(Vec3* v0, Vec3* v1) {
	return sqrt(((v0->x - v1->x) * (v0->x - v1->x)) + ((v0->y - v1->y) * (v0->y - v1->y)) + ((v0->z - v1->z) * (v0->z - v1->z)));
}

Vec3* linePlaneIntersection(Line* l, Vec3* pv0, Vec3* pv1, Vec3* pv2) {
	Vec3* p0 = l->v0;
	Vec3* p1 = l->v1;
	Vec3* p_co = pv0;
	Vec3* p_no = getNormal(pv0,pv1,pv2);
	float epsilon = 0.000001f;
	
	Vec3* u = minus(p0,p1);
	float dot = dot(p_no, u);
	
	if(fabs(dot) > epsilon) {
		Vec3* w = minus(p0,p_co);
		float fac = -dot(p_no,w)/dot;
		
		if (fac > 0) {
			return NULL;
		}
		
		u = scale(u,fac);
		return plus(p0,u);
	}
	
	return NULL;
}

Vec3* computeBarycentricWeights(Vec3* v, Vec3* tv0, Vec3* bv0, Vec3* bv1, float* aux) {
	Vec3* v0 = bv0;
	Vec3* v1 = bv1;
	Vec3* v2 = minus(v,tv0);
	float d20 = dot(v2,v0);
	float d21 = dot(v2,v1);
	
	float w1 = (aux[2] * d20 - aux[1] * d21) * aux[3];
	
	if (w1 < 0) {
		return NULL;
	}
	
	float w2 = (aux[0] * d21 - aux[1] * d20) * aux[3];
	
	if (w2 < 0) {
		return NULL;
	}
	
	float w0 = 1.0f - w1 - w2;
	
	if (w0 < 0) {
		return NULL;
	} else {
		Vec3 res;
		res.x = w0;
		res.y = w1;
		res.z = w2;
		
		return &res;
	}
}

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
	
	bool drawn = false;
	for (int x = xMin; x < xMax; x++) {
		bool successfullyDrawn = false;
		
		Vec3* inWorld = reverseProject(x,y,	auxIntData[5], auxIntData[6], cameraDir, cameraPlane, cameraPos);
		
		Line line;
		line.v0 = cameraPos;
		line.v1 = inWorld;
		
		Vec3* intersected = linePlaneIntersection(line,v03,v13,v12);
		
		if (intersected != null) {
			Vec3* weights = computeBarycentricWeights(intersected, v03, bv0, bv1, auxFloatData);
			
			if (weights != NULL) {
				float distance = distance(intersected,cameraPos);
				
				if (distance < zbuf[x + y * auxIntData[5]]) {
					zbuf[x + y * auxIntData[5]] = distance;
					
					int color = auxIntData[3];
					
					if (uv0 != null && auxIntData[11]) {
						Vec2* tuv0 = scale2(uv0, weights->x);
						Vec2* tuv1 = scale2(uv1, weights->y);
						Vec2* tuv2 = scale2(uv2, weights->z);
						
						Vec2* normTexCoord = plus(plus(tuv0,tuv1),tuv2);
						
						int texX = (int)(normTexCoord->x * auxIntData[0]);
						int texY = (int)(normTexCoord->y * auxIntData[1]);
						int index = texX + texY * auxIntData[0];
						
						if (index < auxIntData[14]) {
							color = texture[index];
						}
					}
					int darkenBy = auxIntData[2];
					
					int red = (color >> 16) & 0xFF;
					int green = (color >> 8) & 0xFF;
					int blue = color & 0xFF;
					
					red -= (red - darkenBy >= 0) ? darkenBy : red;
					green -= (green - darkenBy >= 0) ? darkenBy : green;
					blue -= (blue - darkenBy >= 0) ? darkenBy : blue;
					
					color = (red << 16) | (green << 8) | blue;
					
					screen[x + y * auxIntData[5]] = shade(auxIntData[10], auxIntData[12], auxIntData[13], distance, color);
				}
				successfullyDrawn = true;
			}
		}
		
		if (successfullyDrawn) {
			drawn = true;
			successfullyDrawn = false;
		} else {
			if (drawn) {
				return;
			}
		}
	}
}
//int shade(int shadeType, int FULL_FOG_DISTANCE, int SHADE_THRESHOLD, float distance, int color);
	
	
	
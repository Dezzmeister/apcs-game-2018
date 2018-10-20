#define dot(a, b) ((a->x * b->x) + (a->y * b->y) + (a->z * b->z))

typedef struct Vec3 {
	float x;
	float y;
	float z;
} Vec3;

typedef struct Line {
	Vec3* v0;
} Line;

inline int inlined(float r) {
	return r * r;
}

Vec3* jojVector() {
	Vec3 joj;
	joj.x = 1;
	joj.y = 1;
	joj.z = 334;
	//Vec3* ptr = &joj;
	
	return &joj;
}

Vec3* null() {
	return 0;
}

__kernel void vecAdd(__global const Vec3 *v0, __global const Vec3 *v1, __global Vec3 *res) {
	res->x = v0->x;
	float f = dot(v0,v1);
	Vec3* ptr = jojVector();
	res->y = ptr->z;
	//res->y = jojVector()->y;
	res->z = inlined(v1->x);
	
	/*
	Vec3 poo;
	poo.x = 50;
	poo.y = 55;
	poo.z = 60;
	
	Line lol;
	lol.v0 = &poo;
	
	Line* yoazy = &lol;
	
	Vec3* ref = yoazy->v0;
	
	res->x = ref->x;
	
	Vec3* yee = null();
	*/
}
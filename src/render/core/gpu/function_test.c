#define TRUE 1
#define FALSE 0

struct Vec3 {
	float x;
	float y;
	float z;
};

float* createArray() {
	float result[5];
	
	return result;
}

float* anotherStupidFunction(int i);

__kernel void dotTest(__global const float *v0, __global const float *v1, __global float *result) {	
	__private float b[3];
	b[0] = 5;
	b[1] = 50;
	b[2] = 500;
	result[0] = b[2];
	
	float *ptr;
	ptr = createArray();
	
	float *ptr2;
	ptr2 = anotherStupidFunction(5);
	
	result[1] = ptr2[1];
}

float* anotherStupidFunction(int i) {
	float f = i + 0.35f;
	float result[2];
	result[0] = f;
	
	int index = 1;
	result[index] = 0.45f;
	
	return result;
}
package render.core.gpu;

import static org.jocl.CL.CL_MEM_COPY_HOST_PTR;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_MEM_READ_WRITE;
import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clBuildProgram;
import static org.jocl.CL.clCreateBuffer;
import static org.jocl.CL.clCreateCommandQueueWithProperties;
import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clCreateProgramWithSource;
import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clEnqueueReadBuffer;
import static org.jocl.CL.clSetKernelArg;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_program;
import org.jocl.cl_queue_properties;
import org.jocl.struct.Buffers;
import org.jocl.struct.SizeofStruct;
import org.jocl.struct.Struct;

import render.math.Triangle;
import render.math.Vector2;
import render.math.Vector3;

public class GPURasterizer2 {
	public static final int VEC2_SIZE = SizeofStruct.sizeof(Vec2.class);
	public static final int VEC3_SIZE = SizeofStruct.sizeof(Vec3.class);
	
	private cl_context context = GPU.context;
	private cl_device_id device = GPU.device;
	
	private cl_queue_properties commandQueueProperties;
	private cl_command_queue commandQueue;
	
	String sourcepath;
	String source;
	cl_program program;
	cl_kernel kernel;
	
	Triangle triangle;
	
	final Vec2 uv0 = new Vec2();
	final Vec2 uv1 = new Vec2();
	final Vec2 uv2 = new Vec2();
	
	final Vec3 v03 = new Vec3();
	final Vec3 v13 = new Vec3();
	final Vec3 v23 = new Vec3();
	
	final Vec2 cameraDir = new Vec2();
	final Vec2 cameraPlane = new Vec2();
	final Vec3 cameraPos = new Vec3();
	
	final Vec3 bv0 = new Vec3();
	final Vec3 bv1 = new Vec3();
	
	int[] texture;
	
	int[] screen;
	float[] zbuf;
	int HUD_TRUE_HEIGHT;
	int WIDTH;
	int HEIGHT;
	int startY;
	int endY;
	int minX;
	int maxX;
	int shadeType;
	boolean true3DTexturesEnabled;
	int FULL_FOG_DISTANCE;
	int SHADE_THRESHOLD;
	
	int[] auxIntData = new int[15];
	float[] auxFloatData = new float[4];
	
	GPUMem uv0mem = new GPUMem();
	GPUMem uv1mem = new GPUMem();
	GPUMem uv2mem = new GPUMem();
	
	GPUMem v03mem = new GPUMem();
	GPUMem v13mem = new GPUMem();
	GPUMem v23mem = new GPUMem();
	
	GPUMem cameraDirmem = new GPUMem();
	GPUMem cameraPlanemem = new GPUMem();
	GPUMem cameraPosmem = new GPUMem();
	
	GPUMem bv0mem = new GPUMem();
	GPUMem bv1mem = new GPUMem();
	
	GPUMem screenmem = new GPUMem();
	GPUMem zbufmem = new GPUMem();
	GPUMem texmem = new GPUMem();
	
	GPUMem auxIntDatamem = new GPUMem();
	GPUMem auxFloatDatamem = new GPUMem();
	
	private class GPUMem {
		Pointer ptr;
		cl_mem mem;
		
		void initStructMem(Struct struct, CLMemStructInitializer initializer) {
			ptr = allocate(struct);
			mem = initializer.create(ptr);
		}
	}
	
	@FunctionalInterface
	private interface CLMemStructInitializer {
		cl_mem create(Pointer ptr);
	}
	
	public GPURasterizer2(String _sourcepath, String kernelName) {
		sourcepath = _sourcepath;
		
		try {
			source = Files.readAllLines(Paths.get(sourcepath)).stream().reduce((a, b) -> a + b + "\n").get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		program = clCreateProgramWithSource(context, 1, new String[] {source}, null, null);
		clBuildProgram(program, 0, null, null, null, null);
		kernel = clCreateKernel(program, kernelName, null);
		
		commandQueueProperties = new cl_queue_properties();
		
		commandQueue = clCreateCommandQueueWithProperties(context, device, commandQueueProperties, null);
		
		initializeMemory();
	}
	
	public void setShadeType(int _shadeType) {
		shadeType = _shadeType;
	}
	
	public void prepareForFrame() {
		zbufmem.mem = clCreateBuffer(context, CL_MEM_READ_WRITE, Sizeof.cl_float * zbuf.length, null, null);
		clSetKernelArg(kernel, 13, Sizeof.cl_mem, Pointer.to(zbufmem.mem));
		
		screenmem.mem = clCreateBuffer(context, CL_MEM_READ_WRITE, Sizeof.cl_int * screen.length, null, null);
		clSetKernelArg(kernel, 12, Sizeof.cl_mem, Pointer.to(screenmem.mem));
	}
	
	public void prepareForRaster(int _startY, int _endY, int _minX, int _maxX) {
		startY = _startY;
		endY = _endY;
		minX = _minX;
		maxX = _maxX;
		
		auxIntData[7] = startY;
		auxIntData[8] = minX;
		auxIntData[9] = maxX;
		
		auxIntDatamem.mem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * auxIntData.length, auxIntDatamem.ptr, null);
		clSetKernelArg(kernel, 14, Sizeof.cl_mem, Pointer.to(auxIntDatamem.mem));
		
		auxFloatDatamem.mem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * auxFloatData.length, auxFloatDatamem.ptr, null);
		clSetKernelArg(kernel, 15, Sizeof.cl_mem, Pointer.to(auxFloatDatamem.mem));
		
		texmem.mem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * texture.length, texmem.ptr, null);
		clSetKernelArg(kernel, 3, Sizeof.cl_mem, Pointer.to(texmem.mem));
	}
	
	public void rasterize() {
		long[] global_work_size = new long[] {endY-startY};
		long[] local_work_size = new long[] {1};
		
		clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size, local_work_size, 0, null, null);
		
		clEnqueueReadBuffer(commandQueue, zbufmem.mem, CL_TRUE, 0, Sizeof.cl_float * zbuf.length, Pointer.to(zbuf), 0, null, null);
		clEnqueueReadBuffer(commandQueue, screenmem.mem, CL_TRUE, 0, Sizeof.cl_int * screen.length, Pointer.to(screen), 0, null, null);
		
		//TODO: Free the device memory
		
	}
	
	public void setCameraVectors(Vector3 pos, Vector2 dir, Vector2 plane) {
		cameraDir.x = dir.x;
		cameraDir.y = dir.y;
		
		cameraPlane.x = plane.x;
		cameraPlane.y = plane.y;
		
		cameraPos.x = pos.x;
		cameraPos.y = pos.y;
		cameraPos.z = pos.z;
		
		cameraDirmem.initStructMem(cameraDir, this::vec2mem);
		cameraPlanemem.initStructMem(cameraPlane, this::vec2mem);
		cameraPosmem.initStructMem(cameraPos, this::vec3mem);
		
		clSetKernelArg(kernel, 7, Sizeof.cl_mem, Pointer.to(cameraPosmem.mem));
		clSetKernelArg(kernel, 8, Sizeof.cl_mem, Pointer.to(cameraDirmem.mem));
		clSetKernelArg(kernel, 9, Sizeof.cl_mem, Pointer.to(cameraPlanemem.mem));	
	}
	
	private void initializeMemory() {
		
		cameraDirmem.initStructMem(cameraDir, this::vec2mem);
		cameraPlanemem.initStructMem(cameraPlane, this::vec2mem);
		cameraPosmem.initStructMem(cameraPos, this::vec3mem);
		
		screenmem.ptr = Pointer.to(screen);
		zbufmem.ptr = Pointer.to(zbuf);
		
		auxIntDatamem.ptr = Pointer.to(auxIntData);
		auxFloatDatamem.ptr = Pointer.to(auxFloatData);
	}
	
	private cl_mem vec2mem(Pointer ptr) {
		return clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, VEC2_SIZE, ptr, null);
	}
	
	private cl_mem vec3mem(Pointer ptr) {
		return clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, VEC3_SIZE, ptr, null);
	}
	
	private static Pointer allocate(Struct s) {
		ByteBuffer buffer = Buffers.allocateBuffer(s);
		Buffers.writeToBuffer(buffer, s);
		
		return Pointer.to(buffer);
	}
	
	public void setTriangle(Triangle t) {
		uv0.x = t.uv0.x;
		uv0.y = t.uv0.y;
		
		uv1.x = t.uv1.x;
		uv1.y = t.uv1.y;
		
		uv2.x = t.uv2.x;
		uv2.y = t.uv2.y;
		
		uv0mem.initStructMem(uv0, this::vec2mem);
		uv1mem.initStructMem(uv1, this::vec2mem);
		uv2mem.initStructMem(uv2, this::vec2mem);
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(uv0mem.mem));
		clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(uv1mem.mem));
		clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(uv2mem.mem));
		
		v03.x = t.v0.x;
		v03.y = t.v0.y;
		v03.z = t.v0.z;
		
		v13.x = t.v1.x;
		v13.y = t.v1.y;
		v13.z = t.v1.z;
		
		v23.x = t.v2.x;
		v23.y = t.v2.y;
		v23.z = t.v2.z;
		
		v03mem.initStructMem(v03, this::vec3mem);
		v13mem.initStructMem(v13, this::vec3mem);
		v23mem.initStructMem(v23, this::vec3mem);
		clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(v03mem.mem));
		clSetKernelArg(kernel, 5, Sizeof.cl_mem, Pointer.to(v13mem.mem));
		clSetKernelArg(kernel, 6, Sizeof.cl_mem, Pointer.to(v23mem.mem));
		
		bv0.x = t.bv0.x;
		bv0.y = t.bv0.y;
		bv0.z = t.bv0.z;
		
		bv1.x = t.bv1.x;
		bv1.y = t.bv1.y;
		bv1.z = t.bv1.z;
		
		bv0mem.initStructMem(bv0, this::vec3mem);
		bv1mem.initStructMem(bv1, this::vec3mem);
		//10
		clSetKernelArg(kernel, 10, Sizeof.cl_mem, Pointer.to(bv0mem.mem));
		clSetKernelArg(kernel, 11, Sizeof.cl_mem, Pointer.to(bv1mem.mem));
		
		auxIntData[0] = t.texture.width;
		auxIntData[1] = t.texture.height;
		auxIntData[2] = t.darkenBy;
		auxIntData[14] = t.texture.pixels.length;
		
		texture = t.texture.pixels;
		texmem.ptr = Pointer.to(texture);
		
		auxFloatData[0] = t.d00;
		auxFloatData[1] = t.d01;
		auxFloatData[2] = t.d11;
		auxFloatData[3] = t.invDenom;
	}
	
	public GPURasterizer2 setIntData(int _HUD_TRUE_HEIGHT, int _WIDTH, int _HEIGHT, int _shadeType, boolean _true3DTexturesEnabled, int _FULL_FOG_DISTANCE, int _SHADE_THRESHOLD) {
		HUD_TRUE_HEIGHT = _HUD_TRUE_HEIGHT;
		WIDTH = _WIDTH;
		HEIGHT = _HEIGHT;
		shadeType = _shadeType;
		true3DTexturesEnabled = _true3DTexturesEnabled;
		FULL_FOG_DISTANCE = _FULL_FOG_DISTANCE;
		SHADE_THRESHOLD = _SHADE_THRESHOLD;
		
		auxIntData[4] = HUD_TRUE_HEIGHT;
		auxIntData[5] = WIDTH;
		auxIntData[6] = HEIGHT;
		auxIntData[10] = shadeType;
		auxIntData[11] = true3DTexturesEnabled ? 1 : 0;
		auxIntData[12] = FULL_FOG_DISTANCE;
		auxIntData[13] = SHADE_THRESHOLD;
		
		return this;
	}
	
	public GPURasterizer2 setZBuffer(float[] _zbuf) {
		zbuf = _zbuf;
		zbufmem.ptr = Pointer.to(zbuf);
		return this;
	}
	
	public GPURasterizer2 setScreen(int[] _screen) {
		screen = _screen;
		screenmem.ptr = Pointer.to(screen);
		return this;
	}
}

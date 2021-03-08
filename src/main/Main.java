package main;

import static org.jocl.CL.CL_CONTEXT_PLATFORM;
import static org.jocl.CL.CL_DEVICE_TYPE_ALL;
import static org.jocl.CL.CL_MEM_COPY_HOST_PTR;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_MEM_READ_WRITE;
import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clBuildProgram;
import static org.jocl.CL.clCreateBuffer;
import static org.jocl.CL.clCreateCommandQueueWithProperties;
import static org.jocl.CL.clCreateContext;
import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clCreateProgramWithSource;
import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clEnqueueReadBuffer;
import static org.jocl.CL.clGetDeviceIDs;
import static org.jocl.CL.clGetPlatformIDs;
import static org.jocl.CL.clReleaseCommandQueue;
import static org.jocl.CL.clReleaseContext;
import static org.jocl.CL.clReleaseKernel;
import static org.jocl.CL.clReleaseMemObject;
import static org.jocl.CL.clReleaseProgram;
import static org.jocl.CL.clSetKernelArg;
import static render.core.Block.SPACE;
import static render.core.Block.DwightElements.CUBICLE_X;
import static render.core.Block.DwightElements.CUBICLE_Y;
import static render.core.WorldMap.DEFAULT_CEILING;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;
import org.jocl.cl_program;
import org.jocl.cl_queue_properties;
import org.jocl.struct.Buffers;
import org.jocl.struct.SizeofStruct;

import audio.soundjunk.SoundManager;
import audio.soundjunk.localized.Speaker;
import game.BoundedStat;
import game.ViewModels;
import image.Entity;
import image.GeneralTexture;
import image.HUD;
import image.Item;
import image.SquareTexture;
import image.filters.BenisFilter;
import mapGen.MapGenerator;
import render.core.Block;
import render.core.Camera;
import render.core.Raycaster;
import render.core.Wall;
import render.core.WorldMap;
import render.core.gpu.GPU;
import render.core.gpu.Vec3;
import render.core.true3D.Model;
import render.core.true3D.Transformer;
import render.math.Matrix4;
import render.math.RenderUtils;
import render.math.Triangle;
import render.math.Vector2;
import render.math.Vector3;
import render.math.geometry.OBJModel;

public class Main {
	// use Covington font for plaques

	public static final int COFFEE_MELEE_COST = 5;
	public static final int COFFEE_CANNON_COST = 10;
	
	public static void main(String[] args) {
		test();
		// dwightGame();
		// cubicleTest();
		// gpuTest();
		// gpuTest2();
		// gpuTestWithFile();
		// gpuStructTest();
		// pathsTest();
		// Arrays.toString(map.getIntMap());
		// vectorTest();
		// matrixTest();
		// angleTest();
	}
	
	static void gpuStructTest() {
		String path = "src/render/core/gpu/struct_test.c";
		String source = null;
		
		try {
			source = Files.readAllLines(Paths.get(path)).stream().reduce((a, b) -> a + b + "\n").get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Vec3 v0 = new Vector3(1,5,4).toStruct();
		Vec3 v1 = new Vector3(2,9,8).toStruct();
		v1 = Vec3.of(new Vector3(2,9,8));
		Vec3 v2 = new Vec3();
		int structSize = SizeofStruct.sizeof(Vec3.class);
		
		ByteBuffer v0b = Buffers.allocateBuffer(v0);
		Buffers.writeToBuffer(v0b, v0);
		
		ByteBuffer v1b = Buffers.allocateBuffer(v1);
		Buffers.writeToBuffer(v1b, v1);
		
		ByteBuffer v2b = Buffers.allocateBuffer(v2);
		Buffers.writeToBuffer(v2b,  v2);
		
		cl_program program = clCreateProgramWithSource(GPU.context, 1, new String[] {source}, null, null);
		clBuildProgram(program, 0, null, null, null, null);
		cl_kernel kernel = clCreateKernel(program, "vecAdd", null);
		
		long[] global_work_size = new long[] {1};
		long[] local_work_size = new long[] {1};
		
		cl_queue_properties commandQueueProperties = new cl_queue_properties();
		
		cl_command_queue commandQueue = clCreateCommandQueueWithProperties(GPU.context, GPU.device, commandQueueProperties, null);
		
		cl_mem[] memObjects = new cl_mem[3];
		memObjects[0] = clCreateBuffer(GPU.context, CL.CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, structSize, Pointer.to(v0b), null);
		memObjects[1] = clCreateBuffer(GPU.context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, structSize, Pointer.to(v1b), null);
		memObjects[2] = clCreateBuffer(GPU.context, CL_MEM_READ_WRITE, structSize, null, null);
		
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects[0]));
		clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(memObjects[1]));
		clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memObjects[2]));
		
		clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size, local_work_size, 0, null, null);
		
		clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0, structSize, Pointer.to(v2b), 0, null, null);
		
		v2b.rewind();
		Buffers.readFromBuffer(v2b,  v2);
		System.out.println(v2.x);
		System.out.println(v2.y);
		System.out.println(v2.z);
		
		v0.x = 5000;
		
		ByteBuffer v0b2 = Buffers.allocateBuffer(v0);
		Buffers.writeToBuffer(v0b2, v0);
		
		cl_mem testmem = clCreateBuffer(GPU.context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, structSize, Pointer.to(v0b2), null);
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(testmem));
		
		clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size, local_work_size, 0, null, null);
		
		clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0, structSize, Pointer.to(v2b), 0, null, null);
		
		v2b.rewind();
		Buffers.readFromBuffer(v2b,  v2);
		System.out.println(v2.x);
		System.out.println(v2.y);
		System.out.println(v2.z);
		
		clReleaseMemObject(memObjects[0]);
		clReleaseMemObject(memObjects[1]);
		clReleaseMemObject(memObjects[2]);
		clReleaseKernel(kernel);
		clReleaseProgram(program);
		clReleaseCommandQueue(commandQueue);
		clReleaseContext(GPU.context);
		
		
	}
	
	static void gpuTestWithFile() {
		String path = "src/render/core/gpu/function_test.c";
		String source = null;
		
		try {
			source = Files.readAllLines(Paths.get(path)).stream().reduce((a, b) -> a + b + "\n").get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		float[] v0 = {12,7,4};
		float[] v1 = {9,8,14};
		float[] v2 = new float[3];
		
		Pointer srcA = Pointer.to(v0);
		Pointer srcB = Pointer.to(v1);
		Pointer dest = Pointer.to(v2);
		
		cl_program program = clCreateProgramWithSource(GPU.context, 1, new String[] {source}, null, null);
		clBuildProgram(program, 0, null, null, null, null);
		cl_kernel kernel = clCreateKernel(program, "dotTest", null);
		
		long[] global_work_size = new long[] {1};
		long[] local_work_size = new long[] {1};
		
		cl_queue_properties commandQueueProperties = new cl_queue_properties();
		
		cl_command_queue commandQueue = clCreateCommandQueueWithProperties(GPU.context, GPU.device, commandQueueProperties, null);
		
		cl_mem[] memObjects = new cl_mem[3];
		memObjects[0] = clCreateBuffer(GPU.context, CL.CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * 3, srcA, null);
		memObjects[1] = clCreateBuffer(GPU.context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * 3, srcB, null);
		memObjects[2] = clCreateBuffer(GPU.context, CL_MEM_READ_WRITE, Sizeof.cl_float * 3, null, null);
		
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects[0]));
		clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(memObjects[1]));
		clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memObjects[2]));
		
		clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size, local_work_size, 0, null, null);
		
		clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0, Sizeof.cl_float * 3, dest, 0, null, null);
		
		clReleaseMemObject(memObjects[0]);
		clReleaseMemObject(memObjects[1]);
		clReleaseMemObject(memObjects[2]);
		clReleaseKernel(kernel);
		clReleaseProgram(program);
		clReleaseCommandQueue(commandQueue);
		clReleaseContext(GPU.context);
		
		System.out.println(v2[0]);
		System.out.println(v2[1]);
		System.out.println(v2[2]);
	}
	
	static void gpuTest2() {
		String source = "__kernel void "+
						"korn(__global const float *a,"+
						"     __global const float *b,"+
						"	  __global float *c)"+
					    "{"+
						"	int gid = get_global_id(0);"+
					    "	c[0] = gid + ((int)(a[0] + b[0])) | 3;"+
						"}";
		
		cl_program program = clCreateProgramWithSource(GPU.context, 1, new String[] {source}, null, null);
		clBuildProgram(program, 0, null, null, null, null);
		cl_kernel kernel = clCreateKernel(program, "korn", null);
		
		long[] global_work_size = new long[] {1};
		long[] local_work_size = new long[] {1};
		
		cl_queue_properties commandQueueProperties = new cl_queue_properties();
		
		cl_command_queue commandQueue = clCreateCommandQueueWithProperties(GPU.context, GPU.device, commandQueueProperties, null);
		
		float a = 5;
		float b = 7;
		float[] c = new float[1];
				
		Pointer srcA = Pointer.to(new float[] {a});
		Pointer srcB = Pointer.to(new float[] {b});
		Pointer srcC = Pointer.to(c);
		
		cl_mem[] memObjects = new cl_mem[3];
		memObjects[0] = clCreateBuffer(GPU.context, CL.CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float, srcA, null);
		memObjects[1] = clCreateBuffer(GPU.context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float, srcB, null);
		memObjects[2] = clCreateBuffer(GPU.context, CL_MEM_READ_WRITE, Sizeof.cl_float, null, null);
		
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects[0]));
		clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(memObjects[1]));
		clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memObjects[2]));
		
		clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size, local_work_size, 0, null, null);
		
		clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0, Sizeof.cl_float, srcC, 0, null, null);
		
		clReleaseMemObject(memObjects[0]);
		clReleaseMemObject(memObjects[1]);
		clReleaseMemObject(memObjects[2]);
		clReleaseKernel(kernel);
		clReleaseProgram(program);
		clReleaseCommandQueue(commandQueue);
		clReleaseContext(GPU.context);
		
		System.out.println(c[0]);		
	}
	
	static void gpuTest() {
		
		String source = 
				"__kernel void "+
				"jojKernel(__global const float *a,"+
				"          __global const float *b,"+
				"          __global float *c)"+
				"{"+
				"	int gid = get_global_id(0);"+
				"	c[gid] = a[gid] * b[gid];"+
				"}";
		
		int n = 20;
		float[] aArray = new float[n];
		float[] bArray = new float[n];
		float[] cArray = new float[n];
		
		for (int i = 0; i < n; i++) {
			aArray[i] = i;
			bArray[i] = i;
		}
		
		Pointer srcA = Pointer.to(aArray);
		Pointer srcB = Pointer.to(bArray);
		Pointer srcC = Pointer.to(cArray);
		
		final int platformIndex = 0;
		final long deviceType = CL_DEVICE_TYPE_ALL;
		final int deviceIndex = 0;
		
		CL.setExceptionsEnabled(true);
		
		int[] platformsArray = new int[1];
		clGetPlatformIDs(0, null, platformsArray);
		int numPlatforms = platformsArray[0];
		
		cl_platform_id[] platforms = new cl_platform_id[numPlatforms];
		clGetPlatformIDs(platforms.length, platforms, null);
		cl_platform_id platform = platforms[platformIndex];
		
		cl_context_properties contextProperties = new cl_context_properties();
		contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
		
		int[] devicesArray = new int[1];
		clGetDeviceIDs(platform, deviceType, 0, null, devicesArray);
		int numDevices = devicesArray[0];
		
		cl_device_id[] devices= new cl_device_id[numDevices];
		clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
		cl_device_id device = devices[deviceIndex];
		
		cl_context context = clCreateContext(contextProperties, 1, new cl_device_id[] {device}, null, null, null);
		
		cl_queue_properties commandQueueProperties = new cl_queue_properties();
		
		
		//@SuppressWarnings("deprecation")
		//cl_command_queue commandQueue = clCreateCommandQueue(context, device, 0, null);
		cl_command_queue commandQueue = clCreateCommandQueueWithProperties(context, device, commandQueueProperties, null);
		
		cl_mem[] memObjects = new cl_mem[3];
		memObjects[0] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * n, srcA, null);
		memObjects[1] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * n, srcB, null);
		memObjects[2] = clCreateBuffer(context, CL_MEM_READ_WRITE, Sizeof.cl_float * n, null, null);
		
		cl_program program = clCreateProgramWithSource(context, 1, new String[] {source}, null, null);
		clBuildProgram(program, 0, null, null, null, null);
		cl_kernel kernel = clCreateKernel(program, "jojKernel", null);
		
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects[0]));
		clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(memObjects[1]));
		clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memObjects[2]));
		
		long[] global_work_size = new long[] {n};
		long[] local_work_size = new long[] {1};
		
		clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size, local_work_size, 0, null, null);
		
		clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0, n * Sizeof.cl_float, srcC, 0, null, null);
		
		clReleaseMemObject(memObjects[0]);
		clReleaseMemObject(memObjects[1]);
		clReleaseMemObject(memObjects[2]);
		clReleaseKernel(kernel);
		clReleaseProgram(program);
		clReleaseCommandQueue(commandQueue);
		clReleaseContext(context);
		
		System.out.println(java.util.Arrays.toString(cArray));
	}

	static void pathsTest() {
		Path path = Paths.get("C:/folder/joj/sos/file.txt");

		Path directory = path.getParent();
		System.out.println(directory);
	}

	static void angleTest() {
		Wall X_AXIS = new Wall(0, 0, 1, 0);

		Wall w1 = new Wall(0, 0, 0, 1);
		Wall w2 = new Wall(0, 0, 0, -1);
		Wall w3 = new Wall(0, 0, 500, 0);

		System.out.println(RenderUtils.altAngleBetweenLines(X_AXIS, w1));
		System.out.println(RenderUtils.altAngleBetweenLines(X_AXIS, w2));
		System.out.println(RenderUtils.altAngleBetweenLines(X_AXIS, w3));
	}

	static void matrixTest() {
		Vector3 point = new Vector3(10, 25, 0.5f);

		Matrix4 scaler = Transformer.createScaleMatrix(1, 1, 1);
		Matrix4 translator = Transformer.createTranslationMatrix(24, 0, 0);
		Matrix4 rotator = Transformer.createZRotationMatrix(1.5f);

		Matrix4 transform = scaler.multiply(rotator).multiply(translator);

		System.out.println(transform.transform(point));
	}
	
	static void dwightGame() {
		int width = GameConstants.SCREEN_WIDTH;
		int height = GameConstants.SCREEN_HEIGHT;

		final BoundedStat health = new BoundedStat(0, GameConstants.MAX_HEALTH);
		final BoundedStat coffee = new BoundedStat(0, GameConstants.MAX_COFFEE);
		
		MapGenerator map = new MapGenerator(GameConstants.MAP_SIZE, GameConstants.MAP_SIZE, Block.DwightElements.DWIGHTSPEC);
		map.generate();
		
		WorldMap world = map.getFinalWorldMap();
		Vector2 startPos = map.getRandomStartPos();
		Vector2 goalPos = map.getGoalPos();
		System.out.println("Distance: " + Vector2.distance(startPos, goalPos));

		SoundManager manager = new SoundManager();
		manager.addSound("giorgio", "assets/music/chase.ogg");
		manager.addSound("funeral", "assets/music/funeral.ogg");
		manager.play("giorgio");
		
		Game game = new Game(width, height).noCursor();
		game.log("Seed: " + map.mapSeed);
		game.setSoundManager(manager);
		game.setHealthStat(health);
		game.setCoffeeStat(coffee);
		game.setGoalPos(goalPos);
		
		// String testSound = "assets/soundfx/boom.wav";
		String step1 = "assets/soundfx/footsteps/step1.wav";
		String step2 = "assets/soundfx/footsteps/step2.wav";
		String step3 = "assets/soundfx/footsteps/step3.wav";
		game.setStepPaths(step1, step2, step3);

		Camera camera = new Camera().setPos(startPos).setDir(new Vector2(-0.75f, 0)).setPlane(new Vector2(0, 0.5f));

		Raycaster raycaster = new Raycaster(game, camera, world, width, height, GameConstants.RENDER_SIZE,
				GameConstants.RENDER_SIZE, GameConstants.RAYCAST_THREADS);
		raycaster.setShadeType(GameConstants.SHADE_TYPE);
		raycaster.enableTrue3DTextures();

		game.setCurrentViewModel(ViewModels.CUP_VIEWMODEL);

		HUD hud = new HUD("assets/overlays/hud.png", health, coffee).fitTo(HUD.Fit.BOTTOM).autoFindBars()
				.autoFindTransparency();

		raycaster.setHUD(hud);

		game.setRaycaster(raycaster);
		raycaster.start();
		
		Thread gameThread = game.startAndRun();
	}

	static void cubicleTest() {
		int width = GameConstants.SCREEN_WIDTH;
		int height = GameConstants.SCREEN_HEIGHT;

		SoundManager manager = new SoundManager();

		Game game = new Game(width, height).noCursor();
		game.setSoundManager(manager);

		manager.addSound("hitman", "assets/music/exploration.ogg", new Speaker(4, 4));
		manager.play("hitman");

		Camera camera = new Camera().setPos(new Vector2(2, 2)).setDir(new Vector2(-0.75f, 0))
				.setPlane(new Vector2(0, 0.5f));

		SquareTexture coffeeBean = new SquareTexture("assets/textures/small bean.png", 56);
		Entity bean = new Item(coffeeBean, new Vector2(9, 9), camera).setDrawableBounds(0, 0, 50, 50);

		SquareTexture joj = new SquareTexture("assets/textures/unnamed.jpg", 2365);
		Block gendron = new Block("gendron").applyTexture(joj).tileFront(7, 3);

		GeneralTexture customTexture = new GeneralTexture("assets/textures/sos1024.png", 1024, 1024);
		Block custom = new Block("custom test").customize(
				new Wall(0.25f, 0.25f, 0.75f, 0.25f).setTexture(joj.asGeneralTexture()).tile(2, 4),
				new Wall(0.75f, 0.25f, 0.75f, 0.75f).setTexture(customTexture),
				new Wall(0.75f, 0.75f, 0.25f, 0.75f).setTexture(customTexture),
				new Wall(0.25f, 0.75f, 0.25f, 0.25f).setTexture(customTexture));

		SquareTexture rectangles = new SquareTexture("assets/textures/sos1024.png", 1024);
		Block block = new Block("test").applyTexture(rectangles);

		Triangle testTriangle = new Triangle(new Vector3(0.2f, 0.2f, 0.2f), new Vector3(0.4f, 0.5f, 0.4f),
				new Vector3(0.4f, 0.7f, 0.5f), 0xFF00FF00);
		Model model = new Model(testTriangle);

		GeneralTexture testTexture = new GeneralTexture("assets/textures/medavoy.png", 216, 288);

		Triangle wall1 = new Triangle(new Vector3(0.25f, 0.25f, 0.0f), new Vector3(0.75f, 0.25f, 0.0f),
				new Vector3(0.50f, 0.50f, 0.4f), 0xFF00FF00)
						.setUVCoords(new Vector2(0, 1), new Vector2(1, 1), new Vector2(0.5f, 0))
						.setTexture(testTexture);
		
		Triangle wall2 = new Triangle(new Vector3(0.25f, 0.25f, 0.0f), new Vector3(0.25f, 0.75f, 0.0f),
				new Vector3(0.50f, 0.50f, 0.4f), 0xFF0000FF);

		Model testWalls = new Model(wall1, wall2).transform(GameConstants.getAspectScaleMatrix());

		Block testModel = new Block("testModel").defineAsModel(testWalls);
		Model torus = new OBJModel("assets/OLD_models/torus.obj")
					     .transform(Transformer.createScaleMatrix(0.01f, 0.01f, 0.01f))
					     .transform(GameConstants.getAspectScaleMatrix())
					     .shadeAll(120,0.7f);
		
		Model cylinder = new OBJModel("assets/OLD_models/cylinder.obj")
						 .transform(Transformer.createScaleMatrix(0.01f, 0.01f, 0.01f))
						 .transform(GameConstants.getAspectScaleMatrix())
						 .shadeAll(120, 1);
		
		Block TORUS = new Block("torus").defineAsModel(torus);
		Block CYLINDER = new Block("cylinder").defineAsModel(cylinder);
		
		float hKitScaleF = 0.35f;
		Matrix4 healthKitScaler = GameConstants.getAspectScaleMatrix()
								  .multiply(Transformer.createScaleMatrix(0.01f, 0.01f, 0.01f))
								  .multiply(Transformer.createTranslationMatrix(-0.5f,-0.5f, 0))
								  .multiply(Transformer.createScaleMatrix(hKitScaleF, hKitScaleF, hKitScaleF))
								  .multiply(Transformer.createTranslationMatrix(0.5f, 0.5f, 0.35f));
		
		Block HEALTHKIT = new Block("healthkit").defineAsModel(Block.DwightElements.HEALTHKIT_MODEL);
		
		Block[][] blocks = {
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, Block.DwightElements.CHAIR_BLOCK, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, CUBICLE_Y, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, CUBICLE_X, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, testModel, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, TORUS, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, block, SPACE, block, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, block, SPACE, block, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, CYLINDER, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, HEALTHKIT, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, block, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, block, Block.DwightElements.TABLE_BLOCK, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, gendron, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
						SPACE, SPACE, SPACE, SPACE, SPACE, SPACE}};

		WorldMap world = new WorldMap(blocks).setBorder(block);

		final BoundedStat health = new BoundedStat(0, 100);
		final BoundedStat coffee = new BoundedStat(0, 100);

		game.setHealthStat(health);
		game.setCoffeeStat(coffee);
		game.setCurrentViewModel(ViewModels.CUP_VIEWMODEL);

		Raycaster raycaster = new Raycaster(game, camera, world, width, height, GameConstants.RENDER_SIZE,
				GameConstants.RENDER_SIZE, 4);
		raycaster.setShadeType(GameConstants.SHADE_TYPE);

		HUD hud = new HUD("assets/overlays/hud.png", health, coffee).fitTo(HUD.Fit.BOTTOM).autoFindBars()
				.autoFindTransparency();

		raycaster.setHUD(hud);

		// world.addEntity(bean);

		game.setRaycaster(raycaster);
		raycaster.start();
		
		Thread gameThread = game.startAndRun();
		
		// sleep(1000);
		// manager.setGain("hitman", -30);
		
	}

	static void test() {
		int width = 1200;
		int height = 600;

		Game game = new Game(width, height).noCursor();
		Camera camera = new Camera().setPos(new Vector2(2, 2)).setDir(new Vector2(-0.75f, 0))
				.setPlane(new Vector2(0, 0.5f));

		GeneralTexture customTexture = new GeneralTexture("assets/textures/sos1024.png", 1024, 1024);
		SquareTexture joj = new SquareTexture("assets/textures/unnamed.jpg", 2365);
		SquareTexture michael = new SquareTexture("assets/textures/g-dawg!!!!!!!.jpg", 1633);
		SquareTexture java = new SquareTexture("assets/textures/got java_.png", 100);

		Block block = new Block("test").applyTexture(michael).tileFront(10, 10).tileSide(4, 4);
		Block gendron = new Block("gendron").applyTexture(joj).tileFront(7, 3);
		Block fake = new Block("fake block").applyTexture(michael).fakeBlock().tileSide(3, 3);
		Block soup = new Block("soup").applyTexture(java);

		Block custom = new Block("custom test").customize(
				new Wall(0.25f, 0.25f, 0.75f, 0.25f).setTexture(michael.asGeneralTexture()).tile(2, 4),
				new Wall(0.75f, 0.25f, 0.75f, 0.75f).setTexture(customTexture),
				new Wall(0.75f, 0.75f, 0.25f, 0.75f).setTexture(customTexture),
				new Wall(0.25f, 0.75f, 0.25f, 0.25f).setTexture(customTexture));

		Block[][] worldArray = {{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE}, {SPACE, SPACE, soup, SPACE, SPACE, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE}, {SPACE, SPACE, gendron, SPACE, custom, SPACE},
				{SPACE, SPACE, SPACE, SPACE, SPACE, SPACE}, {SPACE, SPACE, SPACE, SPACE, SPACE, SPACE},};

		SquareTexture blue = new SquareTexture("assets/textures/blue32.png", 32);
		SquareTexture gray = new SquareTexture("assets/textures/gray32.png", 32);
		SquareTexture red = new SquareTexture("assets/textures/red32.png", 32);

		SquareTexture[][] floorMap = {{gray, gray, blue, gray, gray, gray}, {gray, gray, blue, red, red, gray},
				{blue, blue, java, red, red, gray}, {gray, red, red, blue, blue, blue},
				{gray, red, red, blue, gray, gray}, {gray, gray, gray, blue, gray, gray},};
		
		SquareTexture[][] ceilMap = {
				{DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING},
				{DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING},
				{DEFAULT_CEILING, DEFAULT_CEILING, michael, michael, DEFAULT_CEILING, DEFAULT_CEILING},
				{DEFAULT_CEILING, DEFAULT_CEILING, michael, michael, DEFAULT_CEILING, DEFAULT_CEILING},
				{DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING},
				{DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING, DEFAULT_CEILING}};

		WorldMap world = new WorldMap(worldArray, floorMap, ceilMap).setBorder(block);
		final BoundedStat health = new BoundedStat(0, 100);
		final BoundedStat coffee = new BoundedStat(0, 100);

		game.setHealthStat(health);
		game.setCoffeeStat(coffee);
		//game.setGoalPos(new Vector2(-100, -100));
		
		final int rDim = 600;
		final int rWidth = rDim;
		final int rHeight = rDim;
		
		Raycaster raycaster = new Raycaster(game, camera, world, width, height, rWidth, rHeight, 8);
		HUD hud = new HUD("assets/overlays/hud.png", health, coffee).fitTo(HUD.Fit.BOTTOM).autoFindBars()
				.autoFindTransparency();

		raycaster.setHUD(hud);
		
		// raycaster.addFilters(new BootiFilter());
		// raycaster.addFilters(new ChannelReducer(), new Booti2Filter(64), new Gaussian(rWidth, rHeight), new ChannelExpander());
		raycaster.addFilters(new BenisFilter(rWidth, rHeight, 32));

		game.setRaycaster(raycaster);
		raycaster.start();
		
		// Messenger.post("RENDER_ENABLE");
		// game.run();

		// Had trouble with this!!!!
		// Menu start = new Menu();
		// game.pane.add(start.getPanel());
		Thread gameThread = game.startAndRun();

	}
	
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {

		}
	}
}

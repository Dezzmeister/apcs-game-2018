package render.core.gpu;

import static org.jocl.CL.*;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_platform_id;
import org.jocl.cl_queue_properties;

public class GPU {
	public static final int platformIndex = 0;
	public static final long deviceType = CL_DEVICE_TYPE_GPU;
	public static final int deviceIndex = 0;
	
	public static cl_context context;
	public static cl_device_id device;
	
	static {
		initialize();
	}
	
	private static void initialize() {
		CL.setExceptionsEnabled(true);
		
		int[] numPlatformsArray = new int[1];
		clGetPlatformIDs(0, null, numPlatformsArray);
		int numPlatforms = numPlatformsArray[0];
		
		cl_platform_id[] platforms = new cl_platform_id[numPlatforms];
		clGetPlatformIDs(platforms.length, platforms, null);
		cl_platform_id platform = platforms[platformIndex];
		
		long[] sizeArray = {0};
		clGetPlatformInfo(platform, CL_PLATFORM_VERSION, 0, null, sizeArray);
		byte[] buffer = new byte[(int)sizeArray[0]];
		clGetPlatformInfo(platform, CL_PLATFORM_VERSION, buffer.length, Pointer.to(buffer), null);
		String versionString = new String(buffer, 0, buffer.length - 1);
		System.out.println("OpenCL Platform Version: " + versionString);

		cl_context_properties contextProperties = new cl_context_properties();
		contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
		
		int numDevicesArray[] = new int[1];
		clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
		int numDevices = numDevicesArray[0];
		
		cl_device_id[] devices = new cl_device_id[numDevices];
		clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
		device = devices[deviceIndex];
		
		context = clCreateContext(contextProperties, 1, new cl_device_id[] {device}, null, null, null);
	}
}

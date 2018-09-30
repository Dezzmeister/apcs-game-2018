package render.core.true3D;

import render.math.Matrix4;

public final class Transformer {
	
	public static Matrix4 createTranslationMatrix(float x, float y, float z) {
		return new Matrix4(new float[] {
			1, 0, 0, 0,
			0, 1, 0, 0,
			0, 0, 1, 0,
			x, y, z, 1
		});
	}
	
	public static Matrix4 createXRotationMatrix(float theta) {
		float s = (float) Math.sin(theta);
		float c = (float) Math.cos(theta);
		
		return new Matrix4(new float[] {
			1, 0, 0, 0,
			0, c, s, 0,
			0, -s, c, 0,
			0, 0, 0, 1
		});
	}
	
	public static Matrix4 createYRotationMatrix(float phi) {
		float s = (float) Math.sin(phi);
		float c = (float) Math.cos(phi);
		
		return new Matrix4(new float[] {
			c, 0, -s, 0,
			0, 1, 0, 0,
			s, 0, c, 0,
			0, 0, 0, 1
		});
	}
	
	public static Matrix4 createZRotationMatrix(float psi) {
		float s = (float) Math.sin(psi);
		float c = (float) Math.cos(psi);
		
		return new Matrix4(new float[] {
			c, s, 0, 0,
			-s, c, 0, 0,
			0, 0, 1, 0,
			0, 0, 0, 1
		});
	}
	
	public static Matrix4 createScaleMatrix(float x, float y, float z) {
		return new Matrix4(new float[] {
			x, 0, 0, 0,
			0, y, 0, 0,
			0, 0, z, 0,
			0, 0, 0, 1
		});
	}
	
	/**
	 * Creates a projection matrix for which positive y is depth, and z is up/down
	 * 
	 * @param near
	 * @param far
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 * @return
	 */
	public static Matrix4 createYDepthProjectionMatrix(Frustum f) {
		float bottom = f.bottom;
		float top = f.top;
		float left = f.left;
		float right = f.right;
		float near = f.near;
		float far = f.far;
		
		float yDiff = far - near;
		float xDiff = right - left;
		float zDiff = top - bottom;
		
		float A = -(far + near)/yDiff;
		float B = (-2 * far * near)/yDiff;
		float C = (2 * near)/xDiff;
		float D = (right + left)/xDiff;
		float E = (2 * near)/zDiff;
		float F = (top + bottom)/zDiff;
		
		Matrix4 projectionMatrix = new Matrix4(new float[] {
			C, D, 0, 0,
			0, A, 0, B,
			0, F, -E, 0,
			0, -1, 0, 0
		});
		
		return projectionMatrix;
	}
	
	public static Matrix4 createZDepthProjectionMatrix(Frustum f) {
		float bottom = f.bottom;
		float top = f.top;
		float left = f.left;
		float right = f.right;
		float near = f.near;
		float far = f.far;
		
		float yDiff = far - near;
		float xDiff = right - left;
		float zDiff = top - bottom;
		
		float A = -(far + near)/yDiff;
		float B = (-2 * far * near)/yDiff;
		float C = (2 * near)/xDiff;
		float D = (right + left)/xDiff;
		float E = (2 * near)/zDiff;
		float F = (top + bottom)/zDiff;
		
		Matrix4 projectionMatrix = new Matrix4(new float[] {
			C, 0, D, 0,
			0, E, F, 0,
			0, 0, A, B, 
			0, 0, -1, 0
		});
		
		return projectionMatrix;
	}
}

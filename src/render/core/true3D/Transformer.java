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
}

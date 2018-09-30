package render.math;

public class Matrix4 {
	public static final Matrix4 IDENTITY = new Matrix4(new float[] {
			1, 0, 0, 0,
			0, 1, 0, 0,
			0, 0, 1, 0,
			0, 0, 0, 1
	});
	
	public float[] values = new float[16];
	
	public Matrix4(float[] _values) {
		if (_values.length==16) {
			values = _values;
		} else {
			System.out.println("Matrix4 array should have 16 values!");
		}
	}
	
	public Matrix4 multiply(float scalar) {
		float[] result = new float[16];
		for (int i = 0; i < values.length; i++) {
			result[i] = values[i] * scalar;
		}
		return new Matrix4(result);
	}
	
	public Matrix4 multiply(Matrix4 matrix) {
		float[] result = new float[16];
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                for (int i = 0; i < 4; i++) {
                    result[row * 4 + col] +=
                        this.values[row * 4 + i] * matrix.values[i * 4 + col];
                }
            }
        }
        return new Matrix4(result);
	}
	
	public Vector3 transform(Vector3 in) {
		return new Vector3(
                in.x * values[0] + in.y * values[4] + in.z * values[8] + in.w * values[12],
                in.x * values[1] + in.y * values[5] + in.z * values[9] + in.w * values[13],
                in.x * values[2] + in.y * values[6] + in.z * values[10] + in.w * values[14],
                in.x * values[3] + in.y * values[7] + in.z * values[11] + in.w * values[15]
                );
	}
	
	public Triangle transform(Triangle t) {
		Vector3 v0 = transform(t.v0);
		Vector3 v1 = transform(t.v1);
		Vector3 v2 = transform(t.v2);
		
		Triangle triangle = new Triangle(v0, v1, v2, t.color);
		triangle.setUVCoords(t.uv0, t.uv1, t.uv2);
		triangle.setTexture(t.texture);
		
		return triangle;
	}
	
	public float determinant() {
		float[] dets = new float[4];
		for (int top = 0; top < 4; top++) {
			float[] mat = new float[9];
			int index = 0;
			for (int i = 4; i < 16; i++) {
				if ((i - top) % 4 != 0) {
					mat[index++] = values[i];
				}
			}
			Matrix3 matrix = new Matrix3(mat);
			dets[top] = values[top] * matrix.determinant();
		}
		
		return dets[0]-dets[1]+dets[2]-dets[3];
	}
	
	/**
	 * Calculates a 4x4 matrix, for which each element of the matrix is the determinant of the 3x3 matrix
	 * formed by the elements not in that element's row or column. Used in calculating the inverse.
	 * 
	 * @return matrix of minors, calculated from this matrix
	 */
	public Matrix4 matrixOfMinors() {
		float[] minors = new float[16];
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 4; x++) {
				float[] mat = new float[9];
				int index = 0;
				for (int _y = 0; _y < 4; _y++) {
					for (int _x = 0; _x < 4; _x++) {
						if (x!=_x && y!=_y) {
							mat[index++] = values[_x+_y*4];
						}
					}
				}
				minors[x+y*4] = new Matrix3(mat).determinant();
			}
		}
		
		return new Matrix4(minors);
	}
	
	/**
	 * Calculates a 4x4 matrix for which the signs are flipped only for elements in a "checkerboard" pattern.
	 * Used in calculating the inverse.
	 * 
	 * @return matrix of cofactors, calculated from this matrix
	 */
	public Matrix4 matrixOfCofactors() {
		float[] cofactors = new float[16];
		
		for (int i = 0; i < 16; i++) {
			if (i/4 % 2 == 0) {
				cofactors[i] = (i % 2 == 1) ? -values[i] : values[i];
			} else {
				cofactors[i] = (i % 2 == 1) ? values[i] : -values[i];
			}
		}
		
		return new Matrix4(cofactors);
	}
	
	/**
	 * Calculates a 4x4 matrix for which all elements are transposed across the diagonal.
	 * Used in calculating the inverse.
	 * 
	 * @return adjugate matrix, calculated from this matrix
	 */
	public Matrix4 adjugate() {
		float[] adjugate = new float[16];
		
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 4; x++) {
				adjugate[x+y*4] = values[y+x*4];
			}
		}
		
		return new Matrix4(adjugate);
	}
	
	public Matrix4 inverse() {
		Matrix4 mat = matrixOfMinors().matrixOfCofactors().adjugate();
		float invdet = 1/determinant();
		
		return mat.multiply(invdet);
	}
}
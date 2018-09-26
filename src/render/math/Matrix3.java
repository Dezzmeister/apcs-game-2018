package render.math;

public class Matrix3 {
	public static final Matrix3 IDENTITY = new Matrix3(new float[] {
			1, 0, 0,
			0, 1, 0,
			0, 0, 1,
	});
	
	public float[] values = new float[9];
	
	public Matrix3(float[] _values) {
		if (_values.length == 9) {
			values = _values;
		} else {
			System.out.println("Matrix3 array should have 9 values!");
		}
	}
	
	public Matrix3 multiply(float scalar) {
		float[] result = new float[9];
		for (int i = 0; i < values.length; i++) {
			result[i] = values[i] * scalar;
		}
		return new Matrix3(result);
	}
	
	public Matrix3 multiply(Matrix3 matrix) {
		float[] result = new float[9];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                for (int i = 0; i < 3; i++) {
                    result[row * 3 + col] +=
                        this.values[row * 3 + i] * matrix.values[i * 3 + col];
                }
            }
        }
        return new Matrix3(result);
	}
	
	public Vector3 transform(Vector3 in) {
		return new Vector3(
                in.x * values[0] + in.y * values[3] + in.z * values[6],
                in.x * values[1] + in.y * values[4] + in.z * values[7],
                in.x * values[2] + in.y * values[5] + in.z * values[8]
                );
	}
	
	public float determinant() {
		float a = values[0]*((values[4]*values[8])-(values[5]*values[7]));
		float b = values[1]*((values[3]*values[8])-(values[5]*values[6]));
		float c = values[2]*((values[3]*values[7])-(values[4]*values[6]));
		
		return a-b+c;
	}
}

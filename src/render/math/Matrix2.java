package render.math;


public class Matrix2 {
	public static final Matrix2 IDENTITY = new Matrix2(new float[]{1,0,0,1});
	
	private float[] values = {1,0,0,1};
	
	/**
	 * Creates a 2x2 Matrix.
	 * 
	 * @param _values
	 * @throws Exception 
	 */
	public Matrix2(float[] _values) {
		if (_values.length == 4) {
			values = _values;
		} else {
			System.out.println("You can't create a 2x2 Matrix with anything other than 4 values!" + "\n" +
							   "Using Identity Matrix.");
		}
	}
	
	public float determinant() {
		return (values[0] * values[3]) - (values[1] * values[2]);
	}
}

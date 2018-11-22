package main.entities;

import main.GameConstants;
import render.core.true3D.Model;
import render.core.true3D.Transformer;
import render.math.Matrix4;
import render.math.Vector2;
import render.math.geometry.OBJModel;

public class Healthkit extends Pickup {
	private static final Model HEALTHKIT_MODEL;
	public float cylinderRadiusSquared;
	private static final float BASE_PSI = 0.017f;
	private static final float MAX_ADDED_HEIGHT = 0.17f;
	private static final float MIN_ADDED_HEIGHT = 0.05f;
	private float addedHeight = 0;
	private static final float SINE_PERIOD = 2;
	private float sineClock = 0;
	
	static {		
		float hKitScaleF = 0.35f;
		Matrix4 healthKitScaler = GameConstants.getAspectScaleMatrix()
				  .multiply(Transformer.createScaleMatrix(0.01f, 0.01f, 0.01f))
				  .multiply(Transformer.createTranslationMatrix(-0.5f,-0.5f, 0))
				  .multiply(Transformer.createScaleMatrix(hKitScaleF, hKitScaleF, hKitScaleF))
				  .multiply(Transformer.createTranslationMatrix(0.5f, 0.5f, 0.35f));
		
		HEALTHKIT_MODEL = new OBJModel("assets/models/healthkit/healthkit.obj").transform(healthKitScaler).shadeAll(100, 0.4f);
	}
	
	public Healthkit(Vector2 _pos) {
		super(HEALTHKIT_MODEL.transform(Matrix4.IDENTITY), GameConstants.DEFAULT_HEALTHKIT_CYLINDER_RADIUS, _pos);
		cylinderRadiusSquared = boundary.radius * boundary.radius;
	}
	
	private static final float SINE_B = (float) ((2 * Math.PI) / SINE_PERIOD);
	private static final float SINE_AMP = MAX_ADDED_HEIGHT - MIN_ADDED_HEIGHT;
	private static final float SINE_VSHIFT = (MAX_ADDED_HEIGHT - MIN_ADDED_HEIGHT) / 2;
	
	@Override
	public void animateFrame(double delta) {
		updateSineClock(delta);
		
		Matrix4 TRANSLATE_IN = Transformer.createTranslationMatrix(-0.5f, -0.5f, -addedHeight);
		
		addedHeight = (float) (SINE_AMP * Math.sin(SINE_B * sineClock) + SINE_VSHIFT);
		
		Matrix4 TRANSLATE_OUT = Transformer.createTranslationMatrix(0.5f, 0.5f, addedHeight);
		
		Matrix4 rotator = Transformer.createZRotationMatrix(BASE_PSI * (float)delta);
		
		Matrix4 transformer = TRANSLATE_IN.multiply(rotator).multiply(TRANSLATE_OUT);
		model.transformAndApply(transformer);
	}
	
	private static final float SINE_ADJUSTER = 0.01f;
	
	private void updateSineClock(double delta) {
		sineClock += delta * SINE_ADJUSTER;
		sineClock %= SINE_PERIOD;
	}
}

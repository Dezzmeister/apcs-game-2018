package main.entities;

import main.GameConstants;
import render.core.true3D.Model;
import render.core.true3D.Transformer;
import render.math.Matrix4;
import render.math.Vector2;
import render.math.geometry.OBJModel;

public class Healthkit extends Pickup {
	private static final Model HEALTHKIT_MODEL;
	private static final float CYLINDER_RADIUS = 0.25f;
	
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
		super(HEALTHKIT_MODEL, CYLINDER_RADIUS, _pos);
	}

	@Override
	public void animateFrame(float delta) {
		Matrix4 translateIn = Transformer.createTranslationMatrix(-0.5f, -0.5f, 0);
		Matrix4 translateOut = translateIn.inverse();
		
	}	
}

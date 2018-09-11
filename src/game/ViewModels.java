package game;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import image.SquareTexture;
import image.ViewModel;
import image.ViewModel.Animation;

public class ViewModels {
	
	public static final SquareTexture idleKnife0 = new SquareTexture("assets/overlays/knife/idle0.png",100);
	private static final SquareTexture[] idleKnifeFrames = {idleKnife0};
	public static final Animation idleKnifeAnim = new Animation(idleKnifeFrames, 100);
	public static final ViewModel knifeViewModel = new ViewModel(idleKnifeAnim, idleKnifeAnim, idleKnifeAnim, 0.8f);
	
	public static final SquareTexture idleCup0 = new SquareTexture("assets/overlays/cup/idle0.png",100);
	
	public static ViewModel loadFromRootPath(String path, int imgSize, int frameIntervalInMillis, float modelScale) {
		Animation idle = loadAnimationOfType(path, "idle", imgSize, frameIntervalInMillis);
		Animation primary = loadAnimationOfType(path, "primary", imgSize, frameIntervalInMillis);
		Animation secondary = loadAnimationOfType(path, "secondary", imgSize, frameIntervalInMillis);
		
		return new ViewModel(idle, primary, secondary, modelScale);
	}
	
	private static Animation loadAnimationOfType(String path, String type, int imgSize, int frameInterval) {
		List<SquareTexture> list = new ArrayList<SquareTexture>();
		
		int index = 0;
		while (new File(path + "/" + type + index + ".png").exists()) {
			list.add(new SquareTexture(path + "/" + type + index + ".png", imgSize));
			index++;
		}
		
		if (!list.isEmpty()) {
			SquareTexture[] frames = new SquareTexture[list.size()];
			for (int i = 0; i < list.size(); i++) {
				frames[i] = list.get(i);
			}
			return new Animation(frames, frameInterval);
		} else {
			return null;
		}
	}
}

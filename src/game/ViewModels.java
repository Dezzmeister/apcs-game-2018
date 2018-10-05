package game;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import image.SquareTexture;
import image.ViewModel;
import image.ViewModel.Animation;

public class ViewModels {

	public static final ViewModel KNIFE_VIEWMODEL = loadFromRootPath("assets/overlays/knife", 100, 100, 0.8f);

	private static final Animation CUP_IDLE = loadAnimationOfType("assets/overlays/coffee", "idle", 100, 200);
	private static final Animation CUP_PRIMARY = loadAnimationOfType("assets/overlays/coffee", "primary", 100, 200);
	private static final Animation CUP_SECONDARY = loadAnimationOfType("assets/overlays/coffee", "secondary", 100, 150);

	public static final ViewModel CUP_VIEWMODEL = new ViewModel(CUP_IDLE, CUP_PRIMARY, CUP_SECONDARY, 0.4f);
	public static Animation emptyCup = new Animation(
			new SquareTexture[] {new SquareTexture("assets/overlays/coffee/empty.png", 100)}, 100);

	static {
		CUP_VIEWMODEL.addState("empty", emptyCup);
	}

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

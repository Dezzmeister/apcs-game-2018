package image;

/*
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
*/
public class Items implements Entity {

	private GeneralTexture item;

	public Items(String _path, int _width, int _height) {
		item = new GeneralTexture(_path, _width, _height);
	}

	@Override
	public GeneralTexture getTexture() {
		return item;
	}

	public class BeanThing extends Items {

		public BeanThing(String _path, int _width, int _height) {
			super(_path, _width, _height);
		}

	}

	public class SpeedUpThing extends Items {

		public SpeedUpThing(String _path, int _width, int _height) {
			super(_path, _width, _height);
		}

	}

	public class ImmunityThing extends Items {

		public ImmunityThing(String _path, int _width, int _height) {
			super(_path, _width, _height);
		}

	}

	public class ExtraBeanThing extends Items {

		public ExtraBeanThing(String _path, int _width, int _height) {
			super(_path, _width, _height);
		}

	}

}
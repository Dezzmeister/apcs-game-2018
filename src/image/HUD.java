package image;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import game.BoundedStat;

public class HUD {
	public static final int HEALTH_COLOR = 0xFFFF0000;
	public static final int COFFEE_COLOR = 0xFF875438;
	
	public int pixels[];
	public final int WIDTH;
	public final int HEIGHT;
	private Fit fit = Fit.BOTTOM;
	private Rectangle healthBar;
	private Rectangle coffeeBar;
	private final BoundedStat health;
	private final BoundedStat coffee;
	public float beginAt = 0.0f;
	
	public HUD(String path, BoundedStat _health, BoundedStat _coffee) {
		int w = -1;
		int h = -1;
		
		try {
			BufferedImage image = ImageIO.read(new File(path));
			w = image.getWidth();
			h = image.getHeight();
			
			BufferedImage newImage = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
			Graphics2D g = newImage.createGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();
			
			pixels = new int[w * h];
			
			newImage.getRGB(0, 0, w, h, pixels, 0, w);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		WIDTH = w;
		HEIGHT = h;
		
		health = _health;
		coffee = _coffee;
	}
	
	public void setHealthBar(Rectangle _healthBar) {
		healthBar = _healthBar;
	}
	
	public void setCoffeeBar(Rectangle _coffeeBar) {
		coffeeBar = _coffeeBar;
	}
	
	public int getHealth() {
		return health.get();
	}
	
	public int getCoffee() {
		return coffee.get();
	}
	
	public Rectangle healthBar() {
		return healthBar;
	}
	
	public Rectangle coffeeBar() {
		return coffeeBar;
	}
	
	public HUD fitTo(Fit _fit) {
		fit = _fit;
		
		return this;
	}
	
	public HUD autoFindBars() {
		Point health1 = null;
		Point health2 = null;
		Point coffee1 = null;
		Point coffee2 = null;
		
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				if (pixels[x + y * WIDTH] == HEALTH_COLOR && health1 == null) {
					health1 = new Point(x,y);
				} else if (pixels[x + y * WIDTH] == COFFEE_COLOR && coffee1 == null) {
					coffee1 = new Point(x,y);
				} else if (x > 0 && pixels[x + y * WIDTH] != HEALTH_COLOR && pixels[(x - 1) + y * WIDTH] == HEALTH_COLOR && y < HEIGHT-1 && pixels[(x - 1) + ((y + 1) * WIDTH)] != HEALTH_COLOR && health2 == null) {
					health2 = new Point(x,y);
				} else if (x > 0 && pixels[x + y * WIDTH] != COFFEE_COLOR && pixels[(x - 1) + y * WIDTH] == COFFEE_COLOR && y < HEIGHT-1 && pixels[(x - 1) + ((y + 1) * WIDTH)] != COFFEE_COLOR && coffee2 == null) {
					coffee2 = new Point(x,y);
				}
			}
		}
		
		int healthWidth = health2.x - health1.x;
		int healthHeight = health2.y - health1.y;
		
		int coffeeWidth = coffee2.x - coffee1.x;
		int coffeeHeight = coffee2.y - coffee1.y;
		
		healthBar = new Rectangle(health1.x, health1.y, healthWidth, healthHeight);
		coffeeBar = new Rectangle(coffee1.x, coffee1.y, coffeeWidth, coffeeHeight);
		
		return this;
	}
	
	public HUD autoFindTransparency() {
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				if (pixels[x + y * WIDTH] != Texture.ALPHA) {
					beginAt = y/(float)HEIGHT;
					return this;
				}
			}
		}
		
		return this;
	}
	
	public Fit fittedTo() {
		return fit;
	}
	
	public enum Fit {
		BOTTOM
	}
}

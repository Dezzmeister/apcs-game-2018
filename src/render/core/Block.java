package render.core;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import image.GeneralTexture;
import image.SquareTexture;
import render.core.true3D.Model;
import render.core.true3D.Transformer;
import render.math.Matrix4;
import render.math.Triangle;
import render.math.Vector2;
import render.math.Vector3;
import render.math.geometry.Quad;

/**
 * Represents a 1x1 space in a world map. Can be either a full block or a custom
 * "sector" containing custom Walls.
 *
 * @author Joe Desmond
 */
public class Block {

	public static final SquareTexture DEFAULT_TEXTURE = new SquareTexture("assets/textures/default32.png", 32);
	public static final Block SPACE = new Block("space").fakeBlock().makeInvisible();
	
	/**
	 * If this Block is custom, this will contain this Block's custom Walls.
	 */
	public Wall[] walls = null;
	public Model model = null;
	private boolean solid = true;
	private boolean visible = true;
	private int proximity = -1;
	public String name;
	public SquareTexture frontTexture = DEFAULT_TEXTURE;
	public SquareTexture sideTexture = DEFAULT_TEXTURE;
	public float frontXTiles = 1;
	public float frontYTiles = 1;
	public float sideXTiles = 1;
	public float sideYTiles = 1;
	
	public Block(String _name) {
		name = _name;
	}
	
	/**
	 * Allows a Block to be rendered as a group of custom Walls defined relative to
	 * block space instead of a default block. If solid, collision will still occur
	 * with block boundaries.
	 *
	 * @param _walls
	 *            varArgs parameter: can be a Wall[] or any number of Walls
	 * @return this Block
	 */
	public Block customize(Wall... _walls) {
		walls = _walls;
		return this;
	}
	
	public Block defineAsModel(Model _model) {
		model = _model;
		return this;
	}
	
	/**
	 * Makes this block non-solid.
	 */
	public Block fakeBlock() {
		solid = false;
		return this;
	}
	
	public void solidify() {
		solid = true;
	}
	
	public Block makeInvisible() {
		visible = false;
		return this;
	}
	
	public void makeVisible() {
		visible = true;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	/**
	 * Returns true if this Block is not fake. Fake blocks appear solid; however,
	 * they can be walked through and walking through them will render the surrounding world as
	 * if the fake block doesn't exist.
	 * 
	 * @return true if this block is solid
	 */
	public boolean isSolid() {
		return solid;
	}
	
	/**
	 * Returns true if this block is defined as a collection of Walls instead of a
	 * full Block.
	 *
	 * @return true if this Block is Walls
	 */
	public boolean isCustom() {
		return walls != null;
	}
	
	public boolean isModel() {
		return model != null;
	}
	
	public Block setProximity(int visibleDistance) {
		proximity = visibleDistance;
		
		return this;
	}
	
	public int getProximity() {
		return proximity;
	}
	
	public Block tileFront(float _xTiles, float _yTiles) {
		frontXTiles = _xTiles;
		frontYTiles = _yTiles;
		
		return this;
	}
	
	public Block tileSide(float _xTiles, float _yTiles) {
		sideXTiles = _xTiles;
		sideYTiles = _yTiles;
		
		return this;
	}
	
	/**
	 * Apply this texture to the front and back (x-facing sides) of the block.
	 *
	 * @param front
	 *            Texture to be applied
	 * @return this Block, as part of the fluent interface
	 */
	public Block applyFrontTexture(SquareTexture front) {
		frontTexture = front;
		return this;
	}
	
	/**
	 * Apply this texture to the y-facing sides of the block.
	 *
	 * @param side
	 *            Texture to be applied
	 * @return this Block, as part of the fluent interface
	 */
	public Block applySideTexture(SquareTexture side) {
		sideTexture = side;
		return this;
	}
	
	/**
	 * Applies one texture to all sides of the Block.
	 *
	 * @param texture
	 *            Texture to be applied
	 * @return this Block, as part of the fluent interface
	 */
	public Block applyTexture(SquareTexture texture) {
		frontTexture = texture;
		sideTexture = texture;
		return this;
	}
	
	/**
	 * Contains Blocks defined as cubicle walls, for use in our office-themed maps.
	 * The CUBICLE and SIDE textures are not final (though they are
	 * <code>final</code>).
	 *
	 * @author Joe Desmond
	 */
	public static class DwightElements {
		
		public static final SquareTexture STANDARD_WALL = new SquareTexture("assets/textures/wall512.png",512);
		public static final Block STANDARD_WALL_BLOCK = new Block("Dungeon Wall").applyTexture(STANDARD_WALL);
		
		public static final SquareTexture STANDARD_ROOM_FLOOR = new SquareTexture("assets/textures/checkeredfloor512.png",512);
		public static final SquareTexture STANDARD_HALL_FLOOR = new SquareTexture("assets/textures/dirt800.png",800);
		
		public static final SquareTexture STANDARD_HALL_CEILING = new SquareTexture("assets/textures/stone900.png",900);
		
		public static final SquareTexture DWIGHT_1252 = new SquareTexture("assets/textures/dwight1252.png",1252);
		public static final SquareTexture DWITE_546 = new SquareTexture("assets/textures/dwite546.png",546);
		public static final Block DWIGHT_BLOCK = new Block("Dwight Block").applyFrontTexture(DWIGHT_1252).applySideTexture(DWITE_546).setProximity(5);
		
		public static final GeneralTexture BARS = new SquareTexture("assets/textures/bars512.png",512).asGeneralTexture();
		public static final Block HORIZONTAL_BARS = new Block("horizontal bars").customize(new Wall(0.0f,0.5f,1.0f,0.5f).setTexture(BARS));
		public static final Block VERTICAL_BARS = new Block("vertical bars").customize(new Wall(0.5f,0.0f,0.5f,1.0f).setTexture(BARS));
		
		public static final SquareTexture MOSE_WALL = new SquareTexture("assets/textures/mosewall1024.png",1024);
		public static final Block MOSE_BLOCK = new Block("Mose Block").applyTexture(MOSE_WALL);
		
		public static final GeneralTexture pillarTexture = new GeneralTexture("assets/textures/pillar.png",600,1200);
		public static final Block PILLAR = new Block("pillar").customize(
											new Wall(0.25f, 0.25f, 0.75f, 0.25f).setTexture(pillarTexture),
											new Wall(0.75f, 0.25f, 0.75f, 0.75f).setTexture(pillarTexture),
											new Wall(0.75f, 0.75f, 0.25f, 0.75f).setTexture(pillarTexture),
											new Wall(0.25f, 0.75f, 0.25f, 0.25f).setTexture(pillarTexture));
		
		public static final Block ROOM_SPACE = new Block("room space").fakeBlock().makeInvisible();
		
		public static final SquareTexture SECRET_WALL = new SquareTexture("assets/textures/secretwall512.png",512);
		public static final Block SECRET_DOOR = new Block("secret door").applyTexture(SECRET_WALL).fakeBlock();
		
		public static final SquareTexture MEDAVOY = new SquareTexture("assets/textures/medavoywall512.png",512);
		public static final Block MEDAVOY_WALL = new Block("medavoy").applyTexture(MEDAVOY);
		
		public static final SquareTexture SIPOWICZ = new SquareTexture("assets/textures/sipowiczwall512.png",512);
		public static final Block SIPOWICZ_WALL = new Block("sipowicz").applyTexture(SIPOWICZ);
		
		public static final SquareTexture SIMONE = new SquareTexture("assets/textures/simonewall512.png",512);
		public static final Block SIMONE_WALL = new Block("simone").applyTexture(SIMONE);
		
		public static final SquareTexture BLOOD_FLOOR = new SquareTexture("assets/textures/dirt400_bloody.png",400);

		public static final GeneralTexture CUBICLE = new GeneralTexture("assets/textures/joj32.png", 32, 32);
		public static final GeneralTexture SIDE = new GeneralTexture("assets/textures/side.png", 2, 20);
		
		public static final SquareTexture HUD_OVERLAY = new SquareTexture("assets/overlays/hud.png",200);
		
		public static BufferedImage DEATH = null;
		
		public static final GeneralTexture ILLUMINATI_TEXTURE = new GeneralTexture("assets/textures/illuminati.png",412,425);
		
		public static final Triangle ILLUMINATI_1 = new Triangle(new Vector3(0.25f,0.25f,0.0f),
														   new Vector3(0.75f,0.25f,0.0f),
														   new Vector3(0.50f,0.50f,0.8f),
														   0xFF00FF00)
											  .setUVCoords(new Vector2(0,1),
													  	   new Vector2(1,1),
													  	   new Vector2(0.53f,0))
											  .setTexture(ILLUMINATI_TEXTURE);
		
		public static final Triangle ILLUMINATI_2 = new Triangle(new Vector3(0.25f,0.25f,0.0f),
														   new Vector3(0.25f,0.75f,0.0f),
														   new Vector3(0.50f,0.50f,0.8f),
														   0xFF00FF00)
											  .setUVCoords(new Vector2(1,1),
													       new Vector2(0,1),
													       new Vector2(0.53f,0))
											  .setTexture(ILLUMINATI_TEXTURE);
		
		public static final Triangle ILLUMINATI_3 = new Triangle(new Vector3(0.25f,0.75f,0.0f),
																 new Vector3(0.75f,0.75f,0.0f),
																 new Vector3(0.50f,0.50f,0.8f),
																 0xFF00FF00)
												    .setUVCoords(new Vector2(1,1),
												    			 new Vector2(0,1),
												    			 new Vector2(0.53f,0))
												    .setTexture(ILLUMINATI_TEXTURE);
		
		public static final Triangle ILLUMINATI_4 = new Triangle(new Vector3(0.75f,0.75f,0.0f),
																 new Vector3(0.75f,0.25f,0.0f),
																 new Vector3(0.50f,0.50f,0.8f),
																 0xFF00FF00)
													.setUVCoords(new Vector2(1,1),
																 new Vector2(0,1),
																 new Vector2(0.53f,0))
													.setTexture(ILLUMINATI_TEXTURE);
		
		public static final Model ILLUMINATI_SPIRE = new Model(ILLUMINATI_1, ILLUMINATI_2, ILLUMINATI_3, ILLUMINATI_4);
		
		public static final Block ILLUMINATI_SPIRE_BLOCK = new Block("illuminati spire block").defineAsModel(ILLUMINATI_SPIRE);
		
		private static final float legw = 0.075f;
		private static final float legh = 0.25f;
		private static final int legcolor = 0xFF8B4513;
		private static final float inset = 0.3f;
		private static final float tableh = 0.05f;
		private static final float topinset = 0.25f;
		private static final int tabletopcolor = 0xFFA0522D;
		
		private static Model TABLE_LEG_0 = new Model().add(new Quad(new Vector3(0,0,0), new Vector3(legw,0,0), new Vector3(legw,0,legh), new Vector3(0,0,legh), legcolor))
													  .add(new Quad(new Vector3(0,0,0), new Vector3(0,legw,0), new Vector3(0,legw,legh), new Vector3(0,0,legh), legcolor))
													  .add(new Quad(new Vector3(legw,0,0), new Vector3(legw,legw,0), new Vector3(legw,legw,legh), new Vector3(legw,0,legh), legcolor))
													  .add(new Quad(new Vector3(0,legw,0), new Vector3(legw,legw,0), new Vector3(legw,legw,legh), new Vector3(0,legw,legh), legcolor));
		
		private static final GeneralTexture tableTopTexture = new GeneralTexture("assets/textures/tabletop.png",100,100);
		private static final Quad tabletopQuad = new Quad(new Vector3(topinset,topinset,tableh), new Vector3(1-topinset,topinset,tableh), new Vector3(1-topinset,1-topinset,tableh), new Vector3(topinset,1-topinset,tableh), tabletopcolor)
												 .setTexture(tableTopTexture)
												 .setUVCoords(new Vector2(0,0), new Vector2(0,1), new Vector2(1,1), new Vector2(1,0));
		
		private static Model TABLETOP = new Model().add(new Quad(new Vector3(topinset,topinset,0), new Vector3(1 - topinset,topinset,0), new Vector3(1-topinset,topinset,tableh), new Vector3(topinset,topinset,tableh), tabletopcolor))
												   .add(new Quad(new Vector3(topinset,topinset,0), new Vector3(topinset,1-topinset,0), new Vector3(topinset,1-topinset,tableh), new Vector3(topinset,topinset,tableh), tabletopcolor))
												   .add(new Quad(new Vector3(1-topinset,topinset,0), new Vector3(1-topinset,1-topinset,0), new Vector3(1-topinset,1-topinset,tableh), new Vector3(1-topinset,topinset,tableh), tabletopcolor))
												   .add(new Quad(new Vector3(topinset,1-topinset,0), new Vector3(1-topinset,1-topinset,0), new Vector3(1-topinset,1-topinset,tableh), new Vector3(topinset,1-topinset,tableh), tabletopcolor))
												   .add(tabletopQuad);
		
		private static final Matrix4 translateIn = Transformer.createTranslationMatrix(inset, inset, 0);
		
		private static final Matrix4 translateUp = Transformer.createTranslationMatrix(0, 0, legh);
		
		public static final Block TABLE_BLOCK;
				
		public static final Model TABLE_MODEL;
		public static final Block CHAIR_BLOCK = createChair();
		
		public static final Block[] OFFICE_MISC;
		
		static {
			TABLE_LEG_0 = TABLE_LEG_0.transform(translateIn);
			TABLETOP = TABLETOP.transform(translateUp);
			
			Model TABLE_LEG_1 = TABLE_LEG_0.transform(Transformer.createTranslationMatrix(1 - ((2 * inset) + legw), 0, 0));
			Model TABLE_LEG_2 = TABLE_LEG_0.transform(Transformer.createTranslationMatrix(1 - ((2 * inset) + legw), 1 - ((2 * inset) + legw), 0));
			Model TABLE_LEG_3 = TABLE_LEG_0.transform(Transformer.createTranslationMatrix(0, 1 - ((2 * inset) + legw), 0));
			
			TABLE_MODEL = new Model().add(TABLE_LEG_0).add(TABLE_LEG_1).add(TABLE_LEG_2).add(TABLE_LEG_3).add(TABLETOP);
			
			TABLE_BLOCK = new Block("wooden table").defineAsModel(TABLE_MODEL);
			
			
			OFFICE_MISC = new Block[]{TABLE_BLOCK, CHAIR_BLOCK};		
			
			try {
				DEATH = ImageIO.read(new File("assets/overlays/death.png"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private static Block createChair() {
			float legh = 0.2f;
			float legw = 0.05f;
			float legin = legh/2.0f;
			int legcolor = 0xFFC0C0C0;
			int legcolor2 = 0xFFB6B6B6;
			
			Quad q0 = new Quad(new Vector3(0,0,0), new Vector3(legw,0,0), new Vector3(legw+legin,0,legh), new Vector3(legin,0,legh), legcolor2);
			Quad q1 = new Quad(new Vector3(0,0,0), new Vector3(0,legw,0), new Vector3(legin,legw,legh), new Vector3(legin,0,legh), legcolor);
			Quad q2 = new Quad(new Vector3(legw,0,0), new Vector3(legw,legw,0), new Vector3(legin+legw,legw,legh), new Vector3(legin+legw,0,legh), legcolor);
			Quad q3 = new Quad(new Vector3(0,legw,0), new Vector3(legw,legw,0), new Vector3(legin+legw,legw,legh), new Vector3(legin,legw,legh), legcolor2);
			
			Model leg = new Model().add(q0).add(q1).add(q2).add(q3);
			
			float inset = 0.35f;
			
			Matrix4 translateIn = Transformer.createTranslationMatrix(inset, inset, 0);
			
			Model LEG_0 = leg.transform(translateIn);
			Model LEG_1 = LEG_0.transform(Transformer.createTranslationMatrix(0, 1 - ((2 * inset) + legw), 0));
			Model LEG_PAIR_0 = new Model().add(LEG_0).add(LEG_1);
			
			Model CHAIR_MODEL = new Model().add(LEG_0).add(LEG_1);
			return new Block("chair").defineAsModel(CHAIR_MODEL);
		}
		
		private static float thickness = 0.1f;
		/**
		 * A Cubicle Wall aligned on the Y axis.
		 */
		public static final Block CUBICLE_Y = new Block("cubicle y").customize(
				new Wall(0.5f - thickness / 2.0f, 0.0f, 0.5f - thickness / 2.0f, 1.0f).setTexture(CUBICLE),
				new Wall(0.5f + thickness / 2.0f, 0.0f, 0.5f + thickness / 2.0f, 1.0f).setTexture(CUBICLE),
				new Wall(0.5f - thickness / 2.0f, 0.0f, 0.5f + thickness / 2.0f, 0.0f).setTexture(SIDE),
				new Wall(0.5f - thickness / 2.0f, 1.0f, 0.5f + thickness / 2.0f, 1.0f).setTexture(SIDE));
		
		/**
		 * A Cubicle wall aligned on the X axis.
		 */
		public static final Block CUBICLE_X = new Block("cubicle x").customize(
				new Wall(0.0f, 0.5f - thickness / 2.0f, 0.0f, 0.5f + thickness / 2.0f).setTexture(SIDE),
				new Wall(1.0f, 0.5f - thickness / 2.0f, 1.0f, 0.5f + thickness / 2.0f).setTexture(SIDE),
				new Wall(0.0f, 0.5f - thickness / 2.0f, 1.0f, 0.5f - thickness / 2.0f).setTexture(CUBICLE),
				new Wall(0.0f, 0.5f + thickness / 2.0f, 1.0f, 0.5f + thickness / 2.0f).setTexture(CUBICLE));
		
		/**
		 * A 4-way cubicle intersection.
		 */
		public static final Block CUBICLE_CROSS = new Block("cubicle cross").customize(
				new Wall(0.0f, 0.5f - thickness / 2.0f, 0.0f, 0.5f + thickness / 2.0f).setTexture(SIDE),
				new Wall(1.0f, 0.5f - thickness / 2.0f, 1.0f, 0.5f + thickness / 2.0f).setTexture(SIDE),
				new Wall(0.0f, 0.5f - thickness / 2.0f, 1.0f, 0.5f - thickness / 2.0f).setTexture(CUBICLE),
				new Wall(0.0f, 0.5f + thickness / 2.0f, 1.0f, 0.5f + thickness / 2.0f).setTexture(CUBICLE),
				new Wall(0.5f - thickness / 2.0f, 0.0f, 0.5f - thickness / 2.0f, 1.0f).setTexture(CUBICLE),
				new Wall(0.5f + thickness / 2.0f, 0.0f, 0.5f + thickness / 2.0f, 1.0f).setTexture(CUBICLE),
				new Wall(0.5f - thickness / 2.0f, 0.0f, 0.5f + thickness / 2.0f, 0.0f).setTexture(SIDE),
				new Wall(0.5f - thickness / 2.0f, 1.0f, 0.5f + thickness / 2.0f, 1.0f).setTexture(SIDE));
	}
}

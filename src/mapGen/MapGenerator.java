package mapGen;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import audio.soundjunk.localized.Speaker;
import image.SquareTexture;
import render.core.Block;
import render.core.WorldMap;
import render.math.Vector2;

/**
 *
 *
 * @author Joe Amendolare
 */
public class MapGenerator {

	private final int WIDTH;
	private final int HEIGHT;
	
	private final int rooms;
	private int roomSizeMin = 5;
	private int roomSizeMax = 20;
	
	private final int[][] intMap;
	private final Block[][] blockMap;
	private final SquareTexture[][] floorMap;
	private final SquareTexture[][] ceilMap;
	
	private final MapSpecification spec;
	
	private WorldMap finalMap;
	private Vector2 startPos;
	
	public MapGenerator(int _width, int _height, MapSpecification specification) {
		WIDTH = _width;
		HEIGHT = _height;
		
		intMap = new int[HEIGHT][WIDTH];
		blockMap = new Block[HEIGHT][WIDTH];
		floorMap = new SquareTexture[HEIGHT][WIDTH];
		ceilMap = new SquareTexture[HEIGHT][WIDTH];
		spec = specification;
		
		rooms = (WIDTH * HEIGHT) / 1333;
		
		generateIntMap();
		convertIntMap();
		debug_saveDebugImage();
	}
	
	public static class MapSpecification {
		public final Block mainWallBlock;
		public final SquareTexture hallFloor;
		public final SquareTexture hallCeil;
		public final SquareTexture roomFloor;
		public final SquareTexture roomCeil;
		
		public MapSpecification(Block _mainWallBlock, SquareTexture _hallFloor, SquareTexture _hallCeil, SquareTexture _roomFloor, SquareTexture _roomCeil) {
			mainWallBlock = _mainWallBlock;
			hallFloor = _hallFloor;
			hallCeil = _hallCeil;
			roomFloor = _roomFloor;
			roomCeil = _roomCeil;
		}
	}
	
	private void convertIntMap() {
		for (int row = 0; row < intMap.length; row++) {
			for (int col = 0; col < intMap[row].length; col++) {
				Block block;
				SquareTexture floor;
				SquareTexture ceil;
				
				switch (intMap[row][col]) {
					case 0:
						block = spec.mainWallBlock;
						floor = Block.DEFAULT_TEXTURE;
						ceil = Block.DEFAULT_TEXTURE;
						break;
					case 1:
						block = Block.SPACE;
						floor = spec.hallFloor;
						ceil = spec.hallCeil;
						break;
					case 11:
						block = Block.CubicleWalls.DWIGHT_BLOCK;
						floor = spec.roomFloor;
						ceil = spec.roomCeil;
						break;
					case 16:
						block = Block.CubicleWalls.HORIZONTAL_BARS;
						floor = spec.hallFloor;
						ceil = spec.hallCeil;
						break;
					case 17:
						block = Block.CubicleWalls.VERTICAL_BARS;
						floor = spec.hallFloor;
						ceil = spec.hallCeil;
						break;
					default:
						block = Block.SPACE;
						floor = spec.roomFloor;
						ceil = spec.roomCeil;
						break;
				}
				
				blockMap[row][col] = block;
				floorMap[row][col] = floor;
				ceilMap[row][col] = ceil;
			}
		}
	}
	
	public WorldMap getFinalWorldMap() {
		if (finalMap == null) {
			finalMap = new WorldMap(blockMap, floorMap, ceilMap).setBorder(spec.mainWallBlock);
		}
		
		return finalMap;
	}
	
	public Vector2 getRandomStartPos() {
		if (startPos == null) {
			for (int row = intMap.length/2; row < intMap.length; row++) {
				for (int col = intMap[row].length/2; col < intMap[row].length; col++) {
					if (intMap[row][col]==3) {
						startPos = new Vector2(col,row);
						return startPos;
					}
				}
			}
			
			for (int row = 0; row < intMap.length; row++) {
				for (int col = 0; col < intMap[row].length; col++) {
					if (intMap[row][col]==3) {
						startPos = new Vector2(col,row);
						return startPos;
					}
				}
			}
		}
		
		return startPos;
	}
	
	public void debug_saveDebugImage() {
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		for (int row = 0; row < intMap.length; row++) {
			for (int col = 0; col < intMap[row].length; col++) {
				int id = intMap[row][col];
				int color = 0;
				
				switch (id) {
					case 0:
						color = 0;
						break;
					case 1:
						color = 0xFFFFFF;
						break;
					case 2:
						color = 0xFF00FF;
						break;
					case 3:
						color = 0x00FFFF;
						break;
					case 4:
						color = 0xFFFF00;
						break;
					case 5:
						color = 0x0000FF;
						break;
					case 6:
						color = 0x00FF00;
						break;
					case 7:
						color = 0xFF0000;
						break;
					case 8:
						color = 0xAABBCC;
						break;
					case 9:
						color = 0xCCBBAA;
						break;
					case 10:
						color = 0xAABBAA;
						break;
					case 11:
						color = 0xBBAABB;
						break;
					case 12:
						color = 0xEEDDCC;
						break;
					case 13:
						color = 0xCCDDEE;
						break;
					case 14:
						color = 0x227799;
						break;
					case 15:
						color = 0x997722;
						break;
					case 16:
					case 17:
						color = 0xBBBBBB;
						break;
				}
				
				image.setRGB(col, row, color);
			}
		}
		
		try {
			File file = new File("map.png");
			ImageIO.write(image, "png", file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Block[][] getBlockMap() {
		return blockMap;
	}
	
	private void debug_printIntMap() {
		System.out.println(Arrays.deepToString(intMap).replace("], ", "]\n"));
	}

	private void generateIntMap() {
		
		ArrayList<ArrayList<Junction>> cWalls = new ArrayList<ArrayList<Junction>>();
		
		int i;
		ArrayList<Junction> jMap = new ArrayList<Junction>();
		ArrayList<Junction> fullJMap = new ArrayList<Junction>();
		
		jMap.add(new Junction(2, intMap.length / 2, 2));
		fullJMap.add(new Junction(2, intMap.length / 2, 2));
		for (int k = 0; k < intMap.length; k++) {
			for (int p = 0; p < intMap.length; p++) {
				if (k == 0 || k == intMap.length - 1 || p == 0 || p == intMap.length - 1) {
					intMap[k][p] = 1;
				}
			}
		}
		
		while (jMap.size() > 0) {
			int dir = (int) random(4);
			int L = (int) random(15) + 4;
			Junction temp = jMap.get(jMap.size() - 1);
			intMap[temp.xPos][temp.yPos] = 1;
			if (temp.north && temp.south && temp.east && temp.west) {
				jMap.remove(jMap.size() - 1);
				
			} else {
				if (dir == 0 && !temp.north) {
					jMap.get(jMap.size() - 1).north = true;
					for (i = 1; i < L && temp.checkSpace(i, dir, intMap); i++) {
						intMap[temp.xPos][temp.yPos - i] = 1;
					}
					if (i == 1) {
					} else {
						jMap.add(new Junction(temp.xPos, temp.yPos - i, 0));
						fullJMap.add(new Junction(temp.xPos, temp.yPos - i, 0));
					}
				} else if (dir == 1 && !temp.south) {
					jMap.get(jMap.size() - 1).south = true;
					for (i = 1; i < L && temp.checkSpace(i, dir, intMap); i++) {
						intMap[temp.xPos][temp.yPos + i] = 1;
					}
					if (i == 1) {
					} else {
						jMap.add(new Junction(temp.xPos, temp.yPos + i, 1));
						fullJMap.add(new Junction(temp.xPos, temp.yPos + i, 1));
					}
				} else if (dir == 2 && !temp.west) {
					jMap.get(jMap.size() - 1).west = true;
					for (i = 1; i < L && temp.checkSpace(i, dir, intMap); i++) {
						intMap[temp.xPos - i][temp.yPos] = 1;
					}
					if (i == 1) {
					} else {
						jMap.add(new Junction(temp.xPos - i, temp.yPos, 2));
						fullJMap.add(new Junction(temp.xPos - i, temp.yPos, 2));
					}
				} else if (dir == 3 && !temp.east) {
					jMap.get(jMap.size() - 1).east = true;
					for (i = 1; i < L && temp.checkSpace(i, dir, intMap); i++) {
						intMap[temp.xPos + i][temp.yPos] = 1;
					}
					if (i == 1) {
					} else {
						jMap.add(new Junction(temp.xPos + i, temp.yPos, 3));
						jMap.add(new Junction(temp.xPos + i, temp.yPos, 3));
					}
				}
			}
			
		}
		
		for (int j = 0; j < rooms; j++) {
			Junction temp = fullJMap.get(2 + (int) random(fullJMap.size() - 3));
			if (temp.xPos + 1 + roomSizeMax > intMap.length || temp.yPos + 1 + roomSizeMax > intMap[0].length) {
				j--;
			} else {
				int x = 1 + roomSizeMin + (int) random(roomSizeMax - roomSizeMin);
				int y = 1 + roomSizeMin + (int) random(roomSizeMax - roomSizeMin);
				cWalls.add(temp.genRoom(intMap, x, y));
			}
			
		}
		
		for (int k = 0; k < intMap.length; k++) {
			for (int p = 0; p < intMap.length; p++) {
				if (k == 0 || k == intMap.length - 1 || p == 0 || p == intMap.length - 1) {
					intMap[k][p] = 0;
				}
			}
		}
		
		for (int b = 0; b < cWalls.size(); b++) {
			for (int t = 0; t < cWalls.get(b).size(); t++) {
				intMap[cWalls.get(b).get(t).xPos][cWalls.get(b).get(t).yPos] = cWalls.get(b).get(t).getAround(intMap);
				
			}
		}
		
		intMap[fullJMap.get((fullJMap.size() - 1) / 2).xPos][fullJMap.get((fullJMap.size() - 1) / 2).yPos] = 2;
		
		generateBars(100);
	}
	
	private void generateBars(int spawnChance) {
		for (int row = 0; row < intMap.length; row++) {
			for (int col = 0; col < intMap[row].length; col++) {
				int rand = (int)(Math.random() * spawnChance);
				
				if (rand == 1) {
					outer: for (int _row = row; _row < intMap.length; _row++) {
						for (int _col = col; _col < intMap[_row].length; _col++) {
							int id = intMap[_row][_col];
							
							if (id == 1) {
								if (_row > 0 && _row < intMap.length - 1 && _col > 0 && _col < intMap[_row].length - 1) {
									if (intMap[_row-1][_col] == 0 && intMap[_row+1][_col] == 0 && intMap[_row][_col-1] == 1 && intMap[_row][_col+1] == 1) {
										intMap[_row][_col] = 17;
										break outer;
									} else if (intMap[_row][_col-1] == 0 && intMap[_row][_col+1] == 0 && intMap[_row-1][_col] == 1 && intMap[_row+1][_col] == 1) {
										intMap[_row][_col] = 16;
										break outer;
									}
								}
							}
						}
					}					
				}
			}
		}
	}
	
	private double random(int c) {
		
		return c * Math.random();
	}
}

package mapGen;

import java.util.ArrayList;
import java.util.Arrays;

import render.core.Block;

/**
 *
 *
 * @author Joe Amendolare
 */
public class MapGenerator {

	private final int WIDTH;
	private final int HEIGHT;
	
	private int rooms = 30;
	private int roomSizeMin = 5;
	private int roomSizeMax = 13;
	
	private final int[][] intMap;
	private final Block[][] blockMap;
	// private SquareTexture
	
	public MapGenerator(int _width, int _height) {
		WIDTH = _width;
		HEIGHT = _height;
		
		intMap = new int[HEIGHT][WIDTH];
		blockMap = new Block[HEIGHT][WIDTH];
		
		generateIntMap();
		convertIntMap();
	}
	
	public void soos() {
		
	}
	
	public Block[][] getBlockMap() {
		return blockMap;
	}
	
	private void convertIntMap() {
		// TODO
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
	}
	
	private double random(int c) {
		
		return c * Math.random();
	}
}

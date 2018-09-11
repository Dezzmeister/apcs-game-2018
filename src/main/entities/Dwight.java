package main.entities;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.FutureTask;

import image.Entity;
import image.SquareTexture;
import render.core.Block;
import render.core.WorldMap;
import render.math.Vector2;

public class Dwight extends Entity {
	public static final SquareTexture DEFAULT_TEXTURE = new SquareTexture("assets/textures/dwight_purple.png",200);
	public SquareTexture texture = DEFAULT_TEXTURE;
	public float speed = 0.03f;
	private WorldMap world;
	private Queue<Node> path = null;
	
	public Dwight(Vector2 _pos) {
		super(_pos);
	}

	@Override
	public SquareTexture getActiveTexture() {
		return texture;
	}
	
	public void generatePath(Vector2 player, WorldMap _world) {
		world = _world;
		
		FutureTask<Queue<Node>> future = new FutureTask<Queue<Node>>(() -> determinePathTo(player));
		
		future.run();
		
		try {
			path = future.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int xDiff = 0;
	private int yDiff = 0;
	public void moveToPlayer(double delta) {
		float moveSpeed = (float)(speed * delta);
		Node current = new Node((int)pos.x,(int)pos.y);
		
		if (path != null && path.peek() == null) {
			xDiff = 0;
			yDiff = 0;
		} else if (path != null && path.peek().equals(current)) {
			path.remove();
			
			Node next = path.peek();
			if (next == null) {
				xDiff = 0;
				yDiff = 0;
			} else {
				xDiff = next.x - current.x;
				yDiff = next.y - current.y;
			}
		}
		
		pos.x += (moveSpeed * xDiff);
		pos.y += (moveSpeed * yDiff);
		
		if (xDiff == 0) {
			pos.x = current.x + 0.5f;
		}
		if (yDiff == 0) {
			pos.y = current.y + 0.5f;
		}
	}
	
	private Queue<Node> determinePathTo(Vector2 player) {
		
		Node start = new Node((int)pos.x,(int)pos.y);
		Node goal = new Node((int)player.x,(int)player.y);
		
		Map<Node, Float> fMap = new HashMap<Node, Float>();
		Map<Node, Float> gMap = new HashMap<Node, Float>();
		Map<Node, Float> hMap = new HashMap<Node, Float>();
		
		Queue<Node> open = new PriorityQueue<Node>((a, b) -> 
			Float.compare(fMap.getOrDefault(a, 0f), fMap.getOrDefault(b, 0f))
		);
		
		Set<Node> closed = new HashSet<Node>();
		Map<Node, Node> parentMap = new HashMap<Node, Node>();
		Set<Node> goals = new HashSet<Node>();
		
		goals.add(goal);
		open.add(start);
		
		parentMap.put(start,start);
		
		while (!open.isEmpty()) {
			Node node = open.poll();
			
			closed.add(node);
			
			if (goals.contains(node)) {
				return backtrace(node, parentMap);
			}
			
			identifySuccessors(node, goal, goals, open, closed, parentMap, fMap, gMap, hMap);
		}
		
		return null;
	}
	
	private Queue<Node> backtrace(Node node, Map<Node, Node> parentMap) {
		LinkedList<Node> path = new LinkedList<Node>();
		path.add(node);
		
		int previousX, previousY, currentX, currentY;
		int dx,dy;
		int steps;
		Node temp;
		
		while (parentMap.containsKey(node)) {
			previousX = parentMap.get(node).x;
			previousY = parentMap.get(node).y;
			currentX = node.x;
			currentY = node.y;
			
			steps = Integer.max(Math.abs(previousX - currentX), Math.abs(previousY - currentY));
			
			dx = Integer.compare(previousX, currentX);
			dy = Integer.compare(previousY, currentY);
			
			temp = node;
			
			for (int i = 0; i < steps; i++) {
				int tx = temp.x + dx;
				int ty = temp.y + dy;
				
				temp = new Node(tx,ty);
				path.addFirst(temp);
			}
			
			Node parent = parentMap.get(node);
			
			if (node.equals(parent)) {
				break;
			} else {
				node = parent;
			}
		}
		
		return path;
	}
	
	private void identifySuccessors(Node node, Node goal, Set<Node> goals, Queue<Node> open, Set<Node> closed, Map<Node, Node> parentMap,
			Map<Node, Float> fMap, Map<Node, Float> gMap, Map<Node, Float> hMap) {
		
		Collection<Node> neighbors = findNeighbors(node, parentMap);
		
		float d;
		float ng;
		
		for (Node neighbor : neighbors) {
			Node jumpNode = jump(neighbor, node, goals);
			
			if (jumpNode == null || closed.contains(jumpNode)) {
				continue;
			}
			
			d = distance(jumpNode, node);
			ng = gMap.getOrDefault(node, 0f) + d;
			
			if (!open.contains(jumpNode) || ng < gMap.getOrDefault(jumpNode,  0f)) {
				gMap.put(jumpNode, ng);
				hMap.put(jumpNode, heuristic(jumpNode,goal));
				fMap.put(jumpNode, gMap.getOrDefault(jumpNode, 0f) + hMap.getOrDefault(jumpNode, 0f));
				
				parentMap.put(jumpNode, node);
				
				if (!open.contains(jumpNode)) {
					open.offer(jumpNode);
				}
			}
		}
	}
	
	private Node jump(Node neighbor, Node current, Set<Node> goals) {
		if (neighbor == null || !isWalkable(neighbor.x,neighbor.y)) {
			return null;
		}
		if (goals.contains(neighbor)) {
			return neighbor;
		}
		
		int dx = neighbor.x - current.x;
		int dy = neighbor.y - current.y;
		
		if (dx != 0) {
			if ((isWalkable(neighbor.x,neighbor.y + 1) && !isWalkable(neighbor.x - dx, neighbor.y + 1)) ||
			    (isWalkable(neighbor.x, neighbor.y - 1) && !isWalkable(neighbor.x - dx, neighbor.y - 1))) {
				
				return neighbor;
			} 
		} else if (dy != 0) {
			if ((isWalkable(neighbor.x + 1, neighbor.y) && !isWalkable(neighbor.x + 1, neighbor.y - dy)) ||
				(isWalkable(neighbor.x - 1, neighbor.y) && !isWalkable(neighbor.x - 1, neighbor.y - dy))) {
					
				return neighbor;
			}
				
			if (jump(new Node(neighbor.x + 1, neighbor.y), neighbor, goals) != null ||
				jump(new Node(neighbor.x - 1, neighbor.y), neighbor, goals) != null) {
					
				return neighbor;
			}
		} else {
			return null;
		}
		
		return jump(new Node(neighbor.x + dx, neighbor.y + dy), neighbor, goals);
	}
	
	private Set<Node> findNeighbors(Node node, Map<Node, Node> parentMap) {
		Set<Node> neighbors = new HashSet<Node>();
		
		Node parent = parentMap.get(node);
		
		if (parent != null) {
			final int x = node.x;
			final int y = node.y;
			
			final int dx = (x - parent.x) / Math.max(Math.abs(x - parent.x), 1);
			final int dy = (y - parent.y) / Math.max(Math.abs(y - parent.y), 1);
			
			if (dx != 0) {
				if (isWalkable(x + dx, y)) {
					neighbors.add(new Node(x + dx, y));
				}
				if (isWalkable(x, y+1)) {
					neighbors.add(new Node(x, y + 1));
				}
				if (isWalkable(x, y - 1)) {
					neighbors.add(new Node(x, y - 1));
				}
			} else if (dy != 0) {
				if (isWalkable(x, y + dy)) {
					neighbors.add(new Node(x, y + dy));
				}
				if (isWalkable(x + 1, y)) {
					neighbors.add(new Node(x + 1, y));
				}
				if (isWalkable(x - 1, y)) {
					neighbors.add(new Node(x - 1, y));
				}
			} else {
				neighbors.addAll(getAllNeighborsOf(node));
			}
		}
		
		return neighbors;
	}
	
	//"Manhattan" distance
	private float heuristic(Node node, Node goal) {
		return Math.abs(goal.x - node.x) + Math.abs(goal.y - node.y);
	}
	
	private Collection<Node> getAllNeighborsOf(Node node) {
		Set<Node> neighbors = new HashSet<Node>();
		
		int x = node.x;
		int y = node.y;
		
		if (x - 1 >= 0 && x + 1 < world.width && y - 1 >= 0 && y + 1 < world.height) {
			neighbors.add(new Node(x - 1, y));
			neighbors.add(new Node(x, y - 1));
			neighbors.add(new Node(x, y + 1));
			neighbors.add(new Node(x + 1, y));
		}
		
		return neighbors;
	}
	
	private boolean isWalkable(int x, int y) {
		if (x < 0 || y < 0 || x >= world.width || y >= world.height) {
			return false;
		}
		
		Block block = world.getBlockAt(x, y);
		
		if (!block.isSolid() && !block.isVisible()) {
			return true;
		}
		
		return false;
	}
	
	private class Node {
		public int x;
		public int y;
		
		public Node(int _x, int _y) {
			x = _x;
			y = _y;
		}
		
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			
			if (!(o instanceof Node)) {
				return false;
			}
			
			Node node = (Node) o;
			
			return x == node.x && y == node.y;
		}
		
		public int hashCode() {
			return Objects.hash(x,y);
		}
	}
	
	public static float distance(Node n0, Node n1) {
		return (float) Math.sqrt(((n0.x - n1.x) * (n0.x - n1.x)) + ((n0.y - n1.y) * (n0.y - n1.y)));
	}
}

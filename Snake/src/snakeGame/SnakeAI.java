package snakeGame;

import java.util.ArrayList;
import java.util.Collections;

import javafx.geometry.Point3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import snakeGame.Snake.Square;
import space_filling_curves.MooreCurve;
import squareBoard.ArrayTable2D;
import squareBoard.IntPoint2D;

//Title: Snake w/ Safe Auto option
//Author: Joshua Click
//Abstract: Fast auto-play for snake game
//Date: Last updated 13/1/2020
//Log:
//0.1: Broke snake AI into separate class(3/1/2020)
//0.2: added MooreDistance
//0.3: added A* shortcut routing (5/1/2020)
//0.4: made shortcut routing pay attention to safety margins after eating
//0.4.1:Made shortcut routing pay attention to snake bits already in safety margins(13/1/2020)
//0.5: Added shortcut switch
//0.5.1:Cleaned up memory a bit in routeTable
//0.5.2:Rewrote rendering(14/1/2020)

public class SnakeAI {
	private IntPoint2D board_dimensions;
	private ArrayList<IntPoint2D> moore_curve;
	private ArrayTable2D<Integer> moore_table;
	
	private ArrayTable2D<Integer> safety_table;
	
	private boolean shortcut;
	private boolean alt_route;
	private ArrayTable2D<Integer> route_table;
	
	public SnakeAI(int order) {
		shortcut = false;
		this.board_dimensions = new IntPoint2D((int)Math.pow(2, order + 1), (int)Math.pow(2, order + 1));
		
		moore_curve = MooreCurve.MooreCurve(order, 0);
		moore_table = new ArrayTable2D<Integer>(board_dimensions);
		int h = 0;
		
		for (int i = 0; i < moore_curve.size() - 1; i++) {
			IntPoint2D p1 = moore_curve.get(i);
			IntPoint2D p2 = moore_curve.get((i + 1));
			if(p1.getX() > p2.getX()) {
				h = 3;
			}
			else if (p1.getX() < p2.getX()) {
				h = 1;
			}
			else {
				if (p1.getY() > p2.getY()) {
					h = 2;
				}
				else {
					h = 0;
				}
			}
			moore_table.setFromXY(p1, Integer.valueOf(h)); 
		}
		
		safety_table = new ArrayTable2D<Integer>(board_dimensions);
		route_table = new ArrayTable2D<Integer>(board_dimensions);
		
		safety_table.setList(new ArrayList<Integer>(Collections.nCopies(safety_table.getList().size(), Integer.MAX_VALUE)));
	}
	
	public void updateRoute(ArrayTable2D<Square> board, IntPoint2D head, IntPoint2D food, int length) {
		if (shortcut) {
			int r_distance = routeTable(board, head, food, length);
			int ssm = snakeInSafetyMargin(board);
			int md = mooreDistance(head, food);
			if (r_distance > ssm && r_distance < md) {
				alt_route = true;
			}else {
				alt_route = false;
			}
			System.out.println("Shortcut:" + r_distance + " Moore:" + md + " Margin Spent:" + ssm);
		}
		else {
			alt_route = false;
		}
	}

	private int mooreDistance(IntPoint2D head, IntPoint2D food) {
		IntPoint2D s_head = new IntPoint2D(head);
		int out = 0;
		while (!s_head.equals(food)) {
			out++;
			switch(moore_table.getFromXY(s_head)) {
			case 0: s_head.setY(s_head.getY() + 1); break;
			case 1: s_head.setX(s_head.getX() + 1); break;
			case 2: s_head.setY(s_head.getY() - 1); break;
			case 3: s_head.setX(s_head.getX() - 1); break;
			}
		}
		return out;
	}
	
	private void safetyTable(IntPoint2D head, IntPoint2D food, int length){
		safety_table.setList(new ArrayList<Integer>(Collections.nCopies(safety_table.getList().size(), Integer.MAX_VALUE)));
		IntPoint2D s_head = new IntPoint2D(food);
		int i = 1;
		do {
			switch(moore_table.getFromXY(s_head)) {
			case 0: s_head.setY(s_head.getY() + 1); break;
			case 1: s_head.setX(s_head.getX() + 1); break;
			case 2: s_head.setY(s_head.getY() - 1); break;
			case 3: s_head.setX(s_head.getX() - 1); break;
			}
			safety_table.setFromXY(s_head, i);
			i++;
		} while(i <= length);
	}
	
	private int snakeInSafetyMargin(ArrayTable2D<Square> board) {
		int out = -1;
		for (int i = 0; i < board.getList().size(); i++) {
			if (safety_table.getFromIndex(i) != Integer.MAX_VALUE && board.getFromIndex(i).snake_state != 0) {
				if (out < board.getFromIndex(i).snake_state - safety_table.getFromIndex(i)) {
					out = board.getFromIndex(i).snake_state - safety_table.getFromIndex(i);
				}
			}	
		}
		return out;
	}

	//A* routing
	//Returns the length of the route, or infinity if there is none.
	private int routeTable(ArrayTable2D<Square> board, IntPoint2D head, IntPoint2D food, int length) {
		safetyTable(head, food, length);
		
		System.out.println("Head:" + head);
		System.out.println("Food:" + food);
		
		route_table.clear();
		ArrayList<IntPoint2D> open = new ArrayList<IntPoint2D>();
		
		ArrayTable2D<IntPoint2D> parent = new ArrayTable2D<IntPoint2D>(board_dimensions);
		ArrayTable2D<Integer> g_cost = new ArrayTable2D<Integer>(board_dimensions.getX(), new ArrayList<Integer>(Collections.nCopies(board_dimensions.getArea(), Integer.MAX_VALUE)));
		ArrayTable2D<Double> f_cost = new ArrayTable2D<Double>(board_dimensions.getX(), new ArrayList<Double>(Collections.nCopies(board_dimensions.getArea(), Double.MAX_VALUE)));
		
		IntPoint2D r_head = new IntPoint2D(head);

		g_cost.setFromXY(head, 1);
		f_cost.setFromXY(head, head.getDistance(food));
		
		int iterations = board_dimensions.getArea();
		ArrayList<IntPoint2D> neighbors = new ArrayList<IntPoint2D>();
		while (iterations > 0) {
			if (r_head.equals(food)){
				//Retrace!
				do {
					IntPoint2D p1 = parent.getFromXY(r_head);
					int h;
					if(r_head.getX() > p1.getX()) {
						h = 1;
					}
					else if (r_head.getX() < p1.getX()) {
						h = 3;
					}
					else {
						if (r_head.getY() > p1.getY()) {
							h = 0;
						}
						else {
							h = 2;
						}
					} 
					route_table.setFromXY(p1, h);
					r_head.set(p1);
				}while (!r_head.equals(head));
				System.out.println("Found route:" + g_cost.getFromXY(food));
				route_table.setFromXY(food, moore_table.getFromXY(food));
				int out = g_cost.getFromXY(food);
				open = null;
				parent = null;
				g_cost = null;
				f_cost = null;
				
				return out;
			} 

			neighbors.clear();
			neighbors.add(new IntPoint2D(r_head.getX(), r_head.getY() + 1));
			neighbors.add(new IntPoint2D(r_head.getX() + 1, r_head.getY()));
			neighbors.add(new IntPoint2D(r_head.getX(), r_head.getY() - 1));
			neighbors.add(new IntPoint2D(r_head.getX() - 1, r_head.getY()));
			
			for (IntPoint2D neighbor: neighbors) {
				if (board.inBounds(neighbor) && board.getFromXY(neighbor).snake_state < g_cost.getFromXY(r_head) && safety_table.getFromXY(neighbor) > length - neighbor.getDistance(food)) {
					double tf_cost = g_cost.getFromXY(neighbor) + 1 + neighbor.getDistance(food);
					if (tf_cost < f_cost.getFromXY(neighbor)) {
						g_cost.setFromXY(neighbor, g_cost.getFromXY(r_head) + 1);
						f_cost.setFromXY(neighbor, tf_cost);
						parent.setFromXY(neighbor, new IntPoint2D(r_head));
						if (!open.contains(neighbor)) {
							open.add(neighbor);
						}
					}
				}	
			}
			
			if (open.isEmpty()) {
				System.out.println("No open nodes");
				return Integer.MAX_VALUE;
			}
			else{
				System.out.println("g_score:"+ g_cost.getFromXY(r_head));
				double f_score = f_cost.getFromXY(open.get(0));
				int index = 0;
				System.out.println("open:" + open.size() + " nodes");
				for (int i = 0; i < open.size(); i++) {
					if (f_score > f_cost.getFromXY(open.get(i))){
						f_score = f_cost.getFromXY(open.get(i));
						index = i;
					}
				}
				r_head.set(open.get(index));
				open.remove(index);
			}
			iterations--;
		}
		System.out.println(iterations + ":No route found");
		return Integer.MAX_VALUE;
	}
	

	public int setHeading(IntPoint2D head) {
		if (alt_route) {
			return route_table.getFromXY(head);
		}
		return moore_table.getFromXY(head);
	}

	public void render(Canvas graphics, Point3D c_mult) {
		renderMooreCurve(graphics, c_mult);
		if (shortcut) {
			renderSafetyTable(graphics, c_mult);
			renderRouteTable(graphics, c_mult);
		}
	}
	
	private void renderMooreCurve(Canvas graphics, Point3D c_mult) {
		graphics.getGraphicsContext2D().setStroke(Color.BEIGE);
		for (int i = 0; i < moore_curve.size() - 1; i++) {
			IntPoint2D p1 = moore_curve.get(i);
			IntPoint2D p2 = moore_curve.get(i + 1);
			graphics.getGraphicsContext2D().strokeLine(p1.getX()*c_mult.getX() + c_mult.getZ()/2, p1.getY()*c_mult.getY() + c_mult.getZ()/2, p2.getX()*c_mult.getX() + c_mult.getZ()/2, p2.getY()*c_mult.getY() + c_mult.getZ()/2);
		}
	}
	
	private void renderSafetyTable(Canvas graphics, Point3D c_mult) {
		graphics.getGraphicsContext2D().setFill(Color.LAWNGREEN);
		for (int i = 0; i < safety_table.getList().size(); i++) {
			if (safety_table.getFromIndex(i) < Integer.MAX_VALUE) {
				graphics.getGraphicsContext2D().fillText(Integer.toString(safety_table.getFromIndex(i)), safety_table.getXYFromIndex(i).getX()*c_mult.getX() + c_mult.getZ()/2, safety_table.getXYFromIndex(i).getY()*c_mult.getY() + c_mult.getZ()/2);
			}
		}	
	}
	
	private void renderRouteTable(Canvas graphics, Point3D c_mult) {
		graphics.getGraphicsContext2D().setStroke(Color.RED);
		graphics.getGraphicsContext2D().setFill(Color.RED);
		IntPoint2D p1, p2 = new IntPoint2D(0,0);
		for (int i = 0; i < route_table.getList().size(); i++) {
			if (route_table.getFromIndex(i) != null) {
				p1 = route_table.getXYFromIndex(i);
				p2 = route_table.getXYFromIndex(i);
				switch(route_table.getFromIndex(i)) {
				case 0: p2.setY(p2.getY() + 1); break;
				case 1: p2.setX(p2.getX() + 1); break;
				case 2: p2.setY(p2.getY() - 1); break;
				case 3: p2.setX(p2.getX() - 1); break;
				}
				graphics.getGraphicsContext2D().fillRect(p1.getX()*c_mult.getX() + c_mult.getZ()/4, p1.getY()*c_mult.getY() + c_mult.getZ()/4, c_mult.getZ()/2, c_mult.getZ()/2);
				graphics.getGraphicsContext2D().strokeLine(p1.getX()*c_mult.getX() + c_mult.getZ()/2, p1.getY()*c_mult.getY() + c_mult.getZ()/2, p2.getX()*c_mult.getX() + c_mult.getZ()/2, p2.getY()*c_mult.getY() + c_mult.getZ()/2);
			}
		}
		
	}
	
	public void setShortcut(boolean in) {
		this.shortcut = in;
	}
	public boolean getShortcut() {
		return this.shortcut;
	}

}

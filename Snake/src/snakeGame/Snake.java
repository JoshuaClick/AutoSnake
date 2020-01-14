package snakeGame;

import java.util.ArrayList;
import java.util.Random;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import squareBoard.ArrayTable2D;
import squareBoard.IntPoint2D;

//Title: Snake w/ Safe Auto option
//Author: Joshua Click
//Abstract: Implements a snake game with a fast auto-play.
//Date: Last updated 13/1/2020
//Log:
// 1.1: Began recording (1/1/2020)
// 1.2: Broke snake AI into separate class(3/1/2020)
// 1.3: Added heading indicator(13/1/2020)
// 1.4: Added support for resizing window(?)
// 1.4.1:Made effort to improve render memory usage(?)
// 1.5: Rewrote rendering

public class Snake {
	private ArrayTable2D<Square> board;
	
	private Random random;
	private IntPoint2D head;
	private int heading; //0:down, 1: right, 2:up, 3:left
	private int prior_heading;
	private int length;
	private IntPoint2D food;
	
	private int auto;//0:manual play, 1:safe autoplay, 2:autoplay w/ shortcuts
	private boolean debug;
	private SnakeAI snake_ai;
	
	Point3D c_mult;
	Canvas graphics;
	
	public Snake(int order) {
		this.auto = 1;
		this.debug = true;
		IntPoint2D board_dimensions = new IntPoint2D((int)Math.pow(2, order + 1), (int)Math.pow(2, order + 1));
		this.board = new ArrayTable2D<Square>(board_dimensions.getX(), board_dimensions.getY());
		ArrayList<Square> board_list = new ArrayList<Square>(board_dimensions.getArea());
		for (int i = 0; i < board_dimensions.getArea(); i++) {
			IntPoint2D coords = board.getXYFromIndex(i);
			board_list.add(i, new Square(coords));
		}
		board.setList(board_list);
		head = new IntPoint2D(0,0);
		food = new IntPoint2D(0,0);
		heading = 0;
		prior_heading = 0;
		length = 1;
		random = new Random();
		setFood();
		
		snake_ai = new SnakeAI(order);
		snake_ai.updateRoute(board, head, food, length);
	}

	public boolean iterateGame(){
		setHeading();
		setHead();
		if (!board.inBounds(head) || board.getFromXY(head).snake_state > 0) {
			System.out.println("You lose! Score:" + length);
			return false;
		}
		if (head.equals(food)) { 
			System.out.println("Found food! Score:" + length);
			if (length >= board.getList().size() - 1){
				System.out.println("You win!");
				return false;
			}
			setFood();
			length++;
			snake_ai.updateRoute(board, head, food, length);
		}
		else{
			for(Square s:board.getList()) {
				if (s.snake_state > 0) {
					s.snake_state--;
				}
			}
		}
		board.getFromXY(head).snake_state = length;
		System.out.println("S:"+ head + " F:"+ food);
		return true;
	}

	public void controlHeading(KeyCode in) {
		switch(in) {
		case S:if (prior_heading != 2) {heading = 0;}break;
		case D:if (prior_heading != 3) {heading = 1;}break;
		case W:if (prior_heading != 0) {heading = 2;}break;
		case A:if (prior_heading != 1) {heading = 3;}break;
		}
	}
	public void setDebug(boolean in) {
		debug = in;
	}
	public boolean getDebug() {
		return debug;
	}
	public void setAuto(int in) {
		auto = in;
		if (auto > 1) {
			snake_ai.setShortcut(true);
			snake_ai.updateRoute(board, head, food, length);
		}
		else {
			snake_ai.setShortcut(false);
		}
	}
	public int getAuto() {
		return auto;
	}
	
	private void setHeading() {
		if (auto > 0) {
			heading = snake_ai.setHeading(head);
		}
		prior_heading = heading;
	}
	
	private void setHead() {
		switch(heading) {
		case 0: head.setY(head.getY() + 1); break;
		case 1: head.setX(head.getX() + 1); break;
		case 2: head.setY(head.getY() - 1); break;
		case 3: head.setX(head.getX() - 1); break;
		}
	}
	
	private void setFood() {
		do {
			food.set(Math.abs(random.nextInt() % board.getColumns()) , Math.abs(random.nextInt() % board.getRows())) ;
		}while(food.equals(head) || board.getFromXY(food).snake_state > 0);
		board.getFromXY(food).snake_state = -1;
	}
	
	public void setScreen(Canvas graphics) {
		this.graphics = graphics;
		c_mult();
	}
	
	public void render() {
		graphics.getGraphicsContext2D().clearRect(0, 0, graphics.getWidth(), graphics.getHeight());
		for (Square s:board.getList()) {
			s.render(graphics);
		}
		renderHead(graphics);
		if(debug) {
			snake_ai.render(graphics, c_mult);
			
		}
	}
	public void renderHead(Canvas graphics) {
		IntPoint2D f_head = new IntPoint2D(head);
		switch((auto > 0) ? snake_ai.setHeading(head): heading) {
		case 0: f_head.setY(f_head.getY() + 1); break;
		case 1: f_head.setX(f_head.getX() + 1); break;
		case 2: f_head.setY(f_head.getY() - 1); break;
		case 3: f_head.setX(f_head.getX() - 1); break;
		}
		graphics.getGraphicsContext2D().setFill(Color.DARKSEAGREEN);
		graphics.getGraphicsContext2D().fillOval(c_mult.getX()*f_head.getX() + c_mult.getZ()/4, c_mult.getY()*f_head.getY() + c_mult.getZ()/4, c_mult.getZ()/2, c_mult.getZ()/2);
	}
	
	public void c_mult() {
		c_mult = new Point3D((graphics.getWidth() / board.getColumns()), (graphics.getHeight() / board.getRows()), (graphics.getWidth() > graphics.getHeight()) ? graphics.getWidth() / board.getRows()  : graphics.getHeight() / board.getColumns());
	}
	
	public int getLength() {
		return length;
	}
	public class Square {
		int snake_state;
		IntPoint2D coords;
		
		public Square(IntPoint2D coordinates) {
			snake_state = 0;
			coords = coordinates;
		}
		
		public void render(Canvas graphics) {
			if (snake_state >= length) {
				graphics.getGraphicsContext2D().setFill(Color.DARKGREEN);
			}
			else if (snake_state > 0) {
				graphics.getGraphicsContext2D().setFill(Color.GREEN);
			} 
			else if (snake_state == -1) {
				graphics.getGraphicsContext2D().setFill(Color.RED);
			}
			else {
				graphics.getGraphicsContext2D().setFill(Color.BLACK);
			}
			graphics.getGraphicsContext2D().fillRect(c_mult.getX()*coords.getX(), c_mult.getY()*coords.getY(), c_mult.getZ(), c_mult.getZ());
		}
	}
}

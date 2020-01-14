package space_filling_curves;

import java.util.ArrayList;

import squareBoard.IntPoint2D;

public class LSystemState_Int2D {
	ArrayList<IntPoint2D> list;
	IntPoint2D head;
	int heading; //
	
	public LSystemState_Int2D(int heading, IntPoint2D head) {
		this.heading = heading;
		this.head = head;
		this.list = new ArrayList<IntPoint2D>();
	}
}
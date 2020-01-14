package space_filling_curves;

import java.util.ArrayList;

import squareBoard.ArrayTable2D;
import squareBoard.IntPoint2D;

//Title: MooreCurve
//Author: Joshua Click
//Abstract: Implements the L-System algorithm for the Moore Curve
//Date: Last updated 31/12/2019

public class MooreCurve {
	
	//Axiom.
	//LFL+F+LFL
	public static ArrayList<IntPoint2D> MooreCurve(int order, int orientation) {
		IntPoint2D head = new IntPoint2D();
		if (orientation == 0) {
			head.set((int) (Math.pow(2, order) - 1), 0);
		}
		else if (orientation == 1) {
			head.set(0, (int) (Math.pow(2, order) - 1));
		}
		else if (orientation == 2) {
			head.set((int) (Math.pow(2, order) + 1), (int) (Math.pow(4, order) - 1));
		}
		else if (orientation == 3) {
			head.set((int) (Math.pow(4, order) - 1), (int) (Math.pow(2, order) + 1));
		}
		LSystemState_Int2D state = new LSystemState_Int2D(orientation, head);
		
		MooreCurve_L(order - 1, state);
		headForward(state);
		MooreCurve_L(order - 1, state);
		rotateClockwise(state);
		headForward(state);
		rotateClockwise(state);
		MooreCurve_L(order - 1, state);
		headForward(state);
		MooreCurve_L(order - 1, state);
		
		headForward(state);
		state.list.add(state.list.get(0));

		return state.list;
	}
	//L.
	//-RF+LFL+FR-
	static void MooreCurve_L(int order, LSystemState_Int2D state){
		if (order < 0) {
			return;
		}
			rotateCounterClockwise(state);
			MooreCurve_R(order - 1, state);
			headForward(state);
			rotateClockwise(state);
			MooreCurve_L(order - 1, state);
			headForward(state);
			MooreCurve_L(order - 1, state);
			rotateClockwise(state);
			headForward(state);
			MooreCurve_R(order - 1, state);
			rotateCounterClockwise(state);
		
	}
	//R.
	//+LF-RFR-FL+
	static void MooreCurve_R(int order, LSystemState_Int2D state){
		if (order < 0) {
			return;
		}
			rotateClockwise(state);
			MooreCurve_L(order - 1, state);
			headForward(state);
			rotateCounterClockwise(state);
			MooreCurve_R(order - 1, state);
			headForward(state);
			MooreCurve_R(order - 1, state);
			rotateCounterClockwise(state);
			headForward(state);
			MooreCurve_L(order - 1, state);
			rotateClockwise(state);
	}
	
	//Orientation: 0:down, 1: right, 2:up, 3:left
	public static void rotateClockwise(LSystemState_Int2D state) {
		state.heading = (state.heading + 1) % 4;
	}
	public static void rotateCounterClockwise(LSystemState_Int2D state) {
		state.heading = (state.heading - 1);
		if (state.heading < 0) {
			state.heading = 3;
		}
	}
	public static void headForward(LSystemState_Int2D state) {
		state.list.add(new IntPoint2D(state.head));
		//System.out.println("F  " + state.head + " " + state.heading);
		switch(state.heading) {
		case 0: state.head.setY(state.head.getY() + 1); break;
		case 1: state.head.setX(state.head.getX() + 1); break;
		case 2: state.head.setY(state.head.getY() - 1); break;
		case 3: state.head.setX(state.head.getX() - 1); break;
		}
		
	}
	
	
}


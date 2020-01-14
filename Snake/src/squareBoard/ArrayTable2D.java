package squareBoard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ArrayTable2D<A> {
	private ArrayList<A> list;
	private int columns;
	
	public ArrayTable2D() {
		list = new ArrayList<A>(16);
		columns = 4;
	}
	public ArrayTable2D(IntPoint2D dimensions) {
		list = new ArrayList<A>((Collection<A>) Collections.nCopies(dimensions.getArea(), null));
		this.columns = dimensions.getX();
	}
	public ArrayTable2D(int columns, int rows) {
		list = new ArrayList<A>((Collection<A>) Collections.nCopies((columns * rows), null));
		this.columns = columns;
	}
	public ArrayTable2D(List<A>in) {
		list = (ArrayList<A>) in;
		this.columns = (int) (Math.floor(Math.sqrt(in.size())) + 1);
	}
	public ArrayTable2D(int columns, List<A>in) {
		list = (ArrayList<A>) in;
		this.columns = columns;
	}
	public ArrayTable2D(ArrayTable2D<A> other) {
		list = other.getList();
		this.columns = other.getColumns();
	}
	
	public void setList(ArrayList<A> list) {
		this.list = list;
	}
	public ArrayList<A> getList(){
		return list;
	}
	
	public void setFromIndex(A in, int index) {
		list.set(index, in);
	}
	public void setFromXY(IntPoint2D xy, A in) {
		list.set(getIndexFromXY(xy), in);
	}
	
	public A getFromIndex(int index) {
		return list.get(index);
	}
	public A getFromXY(IntPoint2D in) {
		return list.get(getIndexFromXY(in));
	}
	
	public int getIndexFromXY(IntPoint2D in) {
		return in.getX() + columns*in.getY() ;
	}
	public IntPoint2D getXYFromIndex(int index) {
		return new IntPoint2D(index % columns, (int) Math.floor(index / columns));
	}
	
	public int getRows() {
		return list.size()/columns;
	}
	public int getColumns() {
		return columns;
	}
	
	public boolean inBounds(IntPoint2D in) {
		if (in.getX() < 0 || in.getY() < 0 ||in.getX() >= getColumns() || in.getY() >= getRows()) {
			return false;
		}
		return true;
	}
	public void clear() {
		list = new ArrayList<A>((Collection<A>) Collections.nCopies(list.size(), null));
	}
	
}

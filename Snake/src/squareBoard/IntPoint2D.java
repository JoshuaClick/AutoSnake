package squareBoard;

public class IntPoint2D {
	private int x;
	private int y;
	
	
	public IntPoint2D(int x, int y) {
		this.x = x;
		this.y = y;
		
	}
	
	public IntPoint2D() {
		x = 0;
		y = 0;
	}

	public IntPoint2D(IntPoint2D other) {
		this.x = other.getX();
		this.y = other.getY();
	}

	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public void set(IntPoint2D other) {
		this.x = other.getX();
		this.y = other.getY();
	}
	
	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	
	public int getArea() {
		return x*y;
	}
	public double getDistance(IntPoint2D other) {
		return Math.sqrt(Math.pow(x - other.getX(), 2) + Math.pow(y - other.getY(), 2));
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() == this.getClass() && 
				((IntPoint2D)obj).x == this.x &&
				((IntPoint2D)obj).y == this.y) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return x + ":" + y;
	}

}

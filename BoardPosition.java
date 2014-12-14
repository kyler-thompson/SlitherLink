/**
 * 
 * @author Kyler
 *
 */
public class BoardPosition {
	private int m;
	private int n;
	private char alignment;
	
	public BoardPosition() {
		
	}
	
	public BoardPosition(int m, int n, char alignment) {
		this.m = m;
		this.n = n;
		this.alignment = alignment;
	}

	public int getM() {
		return m;
	}

	public void setM(int m) {
		this.m = m;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public char getAlignment() {
		return alignment;
	}

	public void setAlignment(char alignment) {
		this.alignment = alignment;
	}
	
	public String toString() {
		String string;
		if (this.alignment == Edge.HORIZONTAL) {
			if (this.m == 0) {
				string = Integer.toString(m) + " " + Integer.toString(n) + " up";
			} else { 
				string = Integer.toString(m - 1) + " " + Integer.toString(n) + " down";
			}
		} else {
			if (n == 0) {
				string = Integer.toString(m) + " " + Integer.toString(n) + " left";
			} else {
				string = Integer.toString(m) + " " + Integer.toString(n - 1) + " right";
			}
		}
		return string;
	}
	
}

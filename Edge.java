/**
 * 
 * @author Kyler
 *
 */
public class Edge {
	public static final char HORIZONTAL = 'h';
	public static final char VERTICAL = 'v';
	public static final int EMPTY = 0;
	public static final int POSITIVE = 1;
	public static final int NEGATIVE = -1;
	private char alignment;
	private int m;
	private int n;
	private int value;
	
	public Edge() {
		
	}
	
	/**
	 * constructor
	 */
	public Edge(char alignment, int m, int n) {
		this.alignment = alignment;
		this.m = m;
		this.n = n;
		this.value = EMPTY;
	}

	public char getAlignment() {
		return alignment;
	}

	public void setAlignment(char alignment) {
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

	public boolean isPositive() {
		return (value == POSITIVE);
	}
	
	public boolean isNegative() {
		return (value == NEGATIVE);
	}
	
	public boolean isEmpty() {
		return (value == EMPTY);
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	/* set edge to positive */
	public void setPositive() {
		this.value = POSITIVE;
	}
	
	/* set edge to negative */
	public void setNegative() {
		this.value = NEGATIVE;
	}
	
	/* 'flip' edge value */
	public void toggleValue() {
		if (value == EMPTY || value == NEGATIVE) {
			value = POSITIVE;
		} else {
			value = NEGATIVE;
		}
	}
	
	/* true if edge is positive or negative */
	public boolean isKnown() {
		return !(value == EMPTY);
	}
	
	public Edge deepCopy() {
		Edge copy = new Edge(this.alignment, this.m, this.n);
		copy.value = this.value;
		return copy;
	}
	
	public String toString() {
		String result;
		if (this.alignment == Edge.HORIZONTAL) {
			if (this.m == 0) {
				result = Integer.toString(m) + " " + Integer.toString(n) + " Top    ";
			} else { 
				result = Integer.toString(m - 1) + " " + Integer.toString(n) + " Bottom ";
			}
		} else {
			if (n == 0) {
				result = Integer.toString(m) + " " + Integer.toString(n) + " Left   ";
			} else {
				result = Integer.toString(m) + " " + Integer.toString(n - 1) + " Right  ";
			}
		}
		return result;
//		return Integer.toString(m) + " " + Integer.toString(n) + " " + Character.toString(alignment);
	}
}

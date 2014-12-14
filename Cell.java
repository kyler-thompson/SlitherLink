
/**
 * 
 * @author Kyler
 *
 */
public class Cell {
	private boolean hasConstraint;		// whether or not this tile has a constraint
	private int constraint;				// the constraint, if there is one, [0, 3]
	private int row;
	private int col;
	
	/**
	 * empty contructor
	 */
	public Cell() {
		// empty
	}
	
	/**
	 * constructor for case without constraint
	 * 
	 * @param row
	 * @param col
	 */
	public Cell(int row, int col) {
		this.row = row;
		this.col = col;
		hasConstraint = false;
	}
	
	/**
	 * constructor for case with constraints
	 * 
	 * @param row
	 * @param col
	 * @param constraint
	 */
	public Cell(int row, int col, int constraint) {
		this.row = row;
		this.col = col;
		this.constraint = constraint;
		hasConstraint = true;
	}

	public boolean hasConstraint() {
		return hasConstraint;
	}

	public int getConstraint() {
		return constraint;
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}
	
	public Cell deepCopy() {
		Cell copy = new Cell();
		copy.hasConstraint = this.hasConstraint;
		copy.constraint = this.constraint;
		copy.row = this.row;
		copy.col = this.col;
		return copy;
	}
	
	public String toString() {
		return row + " " + col + " s"; 
	}
}

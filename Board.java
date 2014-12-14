import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * 
 * @author Kyler
 *
 */
public class Board {
	private ArrayList<ArrayList<Cell>> cells;
	private ArrayList<ArrayList<Edge>> edges_h;
	private ArrayList<ArrayList<Edge>> edges_v;
	
	/**
	 * empty constructor
	 */
	public Board() {
		/* empty */
	}
	
	public Board(String filename) {
		initList();
		Scanner scanner;
		try {
			scanner = new Scanner(new FileReader(filename));
			/* init squares */
			int m = 0;
			int n = 0;
			while (scanner.hasNext()) {
				cells.add(new ArrayList<Cell>());
				edges_v.add(new ArrayList<Edge>());
				String row = scanner.nextLine();
				n = row.length();
				for (int nn = 0; nn < row.length(); nn++) {
					char c = row.charAt(nn);
					if (c != '?' && c != ' ') {
						int cc = Character.getNumericValue(c);
						cells.get(m).add(nn, new Cell(m, nn, cc));
//						System.out.println("Board: new Square(" + m + ", " + nn + ", " + c + ")");
					} else {
						cells.get(m).add(nn, new Cell(m, nn));
//						System.out.println("Board: new Square(" + m + ", " + nn + ")");
					}
				}
				m++;
			}
			int i;
			int j;
			/* init edges_h */
			for(i = 0; i < m + 1; i++) {
				edges_h.add(i, new ArrayList<Edge>());
				for (j = 0; j < n; j++) {
					edges_h.get(i).add(j, new Edge(Edge.HORIZONTAL, i, j));
//					System.out.println("Board: new Edge(" + Edge.ORIENTATION_HORIZONTAL + ", " + i + ", " + j + ")");
				}
			}
			/* init edges_v */
			for(i = 0; i < m; i++) {
				edges_v.add(i, new ArrayList<Edge>());
				for (j = 0; j < n + 1; j++) {
					edges_v.get(i).add(j, new Edge(Edge.VERTICAL, i, j));
//					System.out.println("Board: new Edge(" + Edge.ORIENTATION_VERTICAL + ", " + i + ", " + j + ")");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void initList() {
		cells = new ArrayList<ArrayList<Cell>>();
		edges_h = new ArrayList<ArrayList<Edge>>();
		edges_v = new ArrayList<ArrayList<Edge>>();
	}

	public ArrayList<ArrayList<Cell>> getCells() {
		return cells;
	}

	public void setCells(ArrayList<ArrayList<Cell>> squares) {
		this.cells = squares;
	}
	
	/* return all cells in a single ArrayList */
	public ArrayList<Cell> getAllCells() {
		ArrayList<Cell> cells = new ArrayList<Cell>(this.cells.size() * this.cells.get(0).size());
		for (ArrayList<Cell> cell_row : this.cells) {
			cells.addAll(cell_row);
		}
		return cells;
	}

	public ArrayList<ArrayList<Edge>> getEdges_h() {
		return edges_h;
	}

	public void setEdges_h(ArrayList<ArrayList<Edge>> edges_h) {
		this.edges_h = edges_h;
	}

	public ArrayList<ArrayList<Edge>> getEdges_v() {
		return edges_v;
	}

	public void setEdges_v(ArrayList<ArrayList<Edge>> edges_v) {
		this.edges_v = edges_v;
	}
	
	/* return all edges in a single ArrayList */
	public ArrayList<Edge> getAllEdges() {
		ArrayList<Edge> edges = new ArrayList<Edge>((this.edges_h.size() * this.edges_h.get(0).size())
				+ (this.edges_v.size() * this.edges_v.get(0).size()));
		for (ArrayList<Edge> edgesH_row : edges_h) {
			edges.addAll(edgesH_row);
		}
		for (ArrayList<Edge> edgesV_row : edges_v) {
			edges.addAll(edgesV_row);
		}
		return edges;
	}
	
	public int getNumRows() {
		return cells.size();
	}
	
	public int getNumCols() {
		return cells.get(0).size();
	}
	
	/* return reference to specific cell */
	public Cell getCell(int m, int n) {
		return this.cells.get(m).get(n);
	}
	
	/* return all empty positions */
	public ArrayList<BoardPosition> getEmptyPositions() {
		ArrayList<Edge> emptyEdges = this.getEmptyEdges();
		ArrayList<BoardPosition> emptyPos = new ArrayList<BoardPosition>(emptyEdges.size());
		for (Edge edge : emptyEdges) {
			emptyPos.add(new BoardPosition(edge.getM(), edge.getN(), edge.getAlignment()));
		}
		return emptyPos;
	}
	
	/* returns all empty edges in one ArrayList */
	public ArrayList<Edge> getEmptyEdges() {
		ArrayList<Edge> allEdges = this.getAllEdges();
		ArrayList<Edge> emptyEdges = new ArrayList<Edge>(allEdges.size());
		for (Edge edge : allEdges) {
			if (edge.isEmpty()) {
				emptyEdges.add(edge);
			}
		}
		return emptyEdges;
	}
	
	/* return edge at board position */
	public Edge getEdge(BoardPosition bPos) {
		return getEdge(bPos.getM(), bPos.getN(), bPos.getAlignment());
	}
	
	/* return edge at position */
	public Edge getEdge(int m, int n, char alignment) {
		if (alignment == Edge.HORIZONTAL) {
			return this.edges_h.get(m).get(n);
		} else {
			return this.edges_v.get(m).get(n);
		}
	}
	
	/* return the edges surrounding the cell */
	public ArrayList<Edge> getIncidentEdges(Cell cell) {
		int m = cell.getRow();
		int n = cell.getCol();
		ArrayList<Edge> edges = new ArrayList<Edge>(4);
		/* add each edge */
		edges.add(getEdge(m, n, Edge.HORIZONTAL));
		edges.add(getEdge(m, n, Edge.VERTICAL));
		edges.add(getEdge(m+1, n, Edge.HORIZONTAL));
		edges.add(getEdge(m, n+1, Edge.VERTICAL));
		return edges;
	}
	
	/** 
	 * @return by-value copy of board
	 */
	public Board deepCopy() {
		Board copy = new Board();
		copy.setCells(new ArrayList<ArrayList<Cell>>(this.cells.size()));
		copy.setEdges_h(new ArrayList<ArrayList<Edge>>(this.edges_h.size()));
		copy.setEdges_v(new ArrayList<ArrayList<Edge>>(this.edges_v.size()));
		for (ArrayList<Cell> row_s : this.cells) {
			copy.getCells().add(new ArrayList<Cell>(row_s.size()));
			for (Cell square : row_s) {
				copy.getCells().get(this.cells.indexOf(row_s)).add(square.deepCopy());
			}
		}
		for (ArrayList<Edge> row_h : this.edges_h) {
			copy.getEdges_h().add(new ArrayList<Edge>(row_h.size()));
			for (Edge edge_h : row_h) {
				copy.getEdges_h().get(this.edges_h.indexOf(row_h)).add(edge_h.deepCopy());
			}
		}	
		for (ArrayList<Edge> row_v : this.edges_v) {
			copy.getEdges_v().add(new ArrayList<Edge>(row_v.size()));
			for (Edge edge_v : row_v) {
				copy.getEdges_v().get(this.edges_v.indexOf(row_v)).add(edge_v.deepCopy());
			}
		}
		return copy;
	}
	
	/**
	 * returns description of board state in user-friendly format
	 */
	public String toString() {
		String board = "  ";
		/* print column labels */
		for (int k = 0; k < cells.get(0).size(); k++) {
			board += " " + k % 10;
		}
		board += "\n" + "  +";
		/* first row of horizontal edges */
		for (Edge edge_h : edges_h.get(0)) {
			if (edge_h.isPositive()) {
				board += "-";
			} else if (edge_h.isNegative()) {
				board += "x";
			} else {
				board += " ";
			}
			board += "+";
		}
		board += "\n";
		for (ArrayList<Cell> row : cells) {
			/* print row label */
			board += cells.indexOf(row) % 10 + " ";
			/* first vertical edge of row */
			if (edges_v.get(cells.indexOf(row)).get(0).isPositive()) {
				board += "|"; 
			} else if (edges_v.get(cells.indexOf(row)).get(0).isNegative()) {
				board += "x";
			} else {
				board += " ";
			}
			/* for each square in the row */
			for (Cell square : row) {
				/* row of constraints and vertical edges */
				if (square.hasConstraint()) {
					board += square.getConstraint();
				} else {
					board += " ";
				}
				if (edges_v.get(cells.indexOf(row)).get(square.getCol() + 1).isPositive()) {
					board += "|";
				} else if (edges_v.get(cells.indexOf(row)).get(square.getCol() + 1).isNegative()) {
					board += "x";
				} else {
					board += " ";
				}
			}	
			board += "\n" + "  +";
			/* row of horizontal edges */
			for (Edge edge_h : edges_h.get(cells.indexOf(row) + 1)) {
				if (edge_h.isPositive()) {
					board += "-";
				} else if (edge_h.isNegative()) {
					board += "x";
				} else {
					board += " ";
				}
				board += "+";
			}
			board += "\n";
		}
		return board;
	}
}
import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 * @author Kyler
 *
 */
public class Solver {
	private Board board;
	private ArrayList<Edge> tracedEdges;
	private ArrayList<Edge> loop;
	
	public Solver() {
		this(null);
	}
	
	public Solver(Board board) {
		this.board = board;
		this.tracedEdges = new ArrayList<Edge>(50);
	}
	
	/**
	 * container for node coordinates
	 * 
	 * @author Kyler
	 */
	private class Node {
		private int i;
		private int j;
		
		public Node(int i, int j) {
			this.i = i;
			this.j = j;
		}
		
		public String toString() {
			return i + " " + j + " n";
		}
	}
	
	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}
	
	public ArrayList<Edge> getLoop() {
		return loop;
	}

	public void setLoop(ArrayList<Edge> loop) {
		this.loop = loop;
	}

	/* returns nodes extending in each legal direction of the passed node */
	private ArrayList<Edge> getIncidentEdges(Node node) {
		return getIncidentEdges(node.i, node.j);
	}
	
	/* returns nodes extending in each legal direction of the passed node coordinates */
	private ArrayList<Edge> getIncidentEdges(int i, int j) {
		ArrayList<Edge> incidentEdges = new ArrayList<Edge>(4);
		/* add left edge */
		if (j != 0) {
			incidentEdges.add(board.getEdge(i, j-1, Edge.HORIZONTAL));
		}
		/* add right edge */
		if (j != board.getCells().get(0).size()) {
			incidentEdges.add(board.getEdge(i, j, Edge.HORIZONTAL));
		}
		/* add top edge */
		if (i != 0) {
			incidentEdges.add(board.getEdge(i-1, j, Edge.VERTICAL));
		}
		/* add bottom edge */
		if (i != board.getCells().size()) {
			incidentEdges.add(board.getEdge(i, j, Edge.VERTICAL));
		}
		return incidentEdges;
	}
	
	/* returns incident nodes of passed edge */
	private ArrayList<Node> getIncidentNodes(Edge edge) {
		ArrayList<Node> incidentNodes = new ArrayList<Node>(2);
		int m = edge.getM();
		int n = edge.getN();
		/* edge is horizontal */
		if (edge.getAlignment() == Edge.HORIZONTAL) {
			incidentNodes.add(new Node(m, n));
			incidentNodes.add(new Node(m, n + 1));
		/* edge is vertical */
		} else {
			incidentNodes.add(new Node(m, n));
			incidentNodes.add(new Node(m + 1, n));
		}
		return incidentNodes;
	}
	
	/* returns all nodes of this Solver's board in one ArrayList */
	private ArrayList<Node> getAllNodes() {
		ArrayList<Node> nodes = new ArrayList<Node>((board.getNumRows() + 1) * (board.getNumCols() + 1));
		for (int i = 0; i < board.getNumRows() + 1; i++) {
			for (int j = 0; j < board.getNumCols() + 1; j++) {
				nodes.add(new Node(i, j));
			}
		}
		return nodes;
	}
	
	private boolean bruteForce(ArrayList<Edge> edges, boolean setNextEdge, int i) {
		if (i == edges.size()) {
			if (checkIfSolved()) {
				return true;
			} else {
				return false;
			}
		} else {
			if (setNextEdge) {
				edges.get(i).setPositive();
			} else {
				edges.get(i).setNegative();
			}
			return bruteForce(edges, true, i + 1) || bruteForce(edges, false, i + 1);
		}
	}
	
	/* attempts to solve this Solver's board with brute force */
	public void bruteSolve() {
		ArrayList<Edge> allEdges = this.board.getEmptyEdges();
		if (!bruteForce(allEdges, true, 0)) {
			if (!bruteForce(allEdges, false, 0)) {
				System.out.println(">> Solver: puzzle not solved");
			}
		}
	}
	
	/**
	 * returns whether or not the passed value is consistent for the
	 * passed board, at the passed board position
	 * 
	 * @return true if consistent, false otherwise
	 */
	private boolean tryEdge(Board board, BoardPosition bPos, int value) {
		/* solver for board */
		Solver solver = new Solver(board);
		/* set edge of board to value */
		Edge edge = board.getEdge(bPos.getM(), bPos.getN(), bPos.getAlignment());
		edge.setValue(value);
		/* try to apply rules */
		return solver.applyRules();
	}
	
	/**
	 * attempts to solve this Solver's board via constraint satisfaction
	 */
	public void SATsolve() {
		Iterator<BoardPosition> unknownPos;
		Iterator<BoardPosition> edgePos;
		BoardPosition bPos;
		Board resultBoardWithoutEdge;
		Board resultBoardWithEdge;
		boolean boardChanged;
		boolean edgeChanged;
		long startX = System.nanoTime();
		do {
			boardChanged = false;
			unknownPos = board.getEmptyPositions().iterator();
			while (unknownPos.hasNext()) {
				bPos = unknownPos.next();
				resultBoardWithEdge = board.deepCopy();
				resultBoardWithoutEdge = board.deepCopy();
				edgeChanged = false;
				if (!tryEdge(resultBoardWithEdge, bPos, Edge.POSITIVE)) {
//					System.out.println(">> Solver: Move " + bPos.toString() + " Negative");
					try {
						assertEdge(board.getEdge(bPos), Edge.NEGATIVE, "SAT.1");
					} catch (ConsistencyException e) {
						/* SHOULD NEVER HAPPEN */
						System.out.println(e.toString());
					}
					edgeChanged = true;
				} else if (!tryEdge(resultBoardWithoutEdge, bPos, Edge.NEGATIVE)) {
//					System.out.println(">> Solver: Move " + bPos.toString() + " Positive");
					try {
						assertEdge(board.getEdge(bPos), Edge.POSITIVE, "SAT.2");
					} catch (ConsistencyException e) {
						/* SHOULD NEVER HAPPEN */
						System.out.println(e.toString());
					}
					edgeChanged = true;
				} else {
					edgePos = board.getEmptyPositions().iterator();
					while (edgePos.hasNext()) {
						bPos = edgePos.next();
						if (!board.getEdge(bPos).isKnown() &&
								resultBoardWithoutEdge.getEdge(bPos).isKnown() &&
								resultBoardWithEdge.getEdge(bPos).isKnown() &&
								resultBoardWithEdge.getEdge(bPos).isPositive() == resultBoardWithoutEdge.getEdge(bPos).isPositive()) {
							try {
								int value = resultBoardWithEdge.getEdge(bPos).getValue();
//								if (value == Edge.POSITIVE) {
//									System.out.println(">> Solver: Move " + bPos.toString() + " Positive");
//								} else {
//									System.out.println(">> Solver: Move " + bPos.toString() + " Negative");
//								}
								assertEdge(board.getEdge(bPos), value, "SAT.3");
							} catch (ConsistencyException e) {
								/* SHOULD NEVER HAPPEN */
								System.out.println(e.toString());
							}
							edgeChanged = true;
						}
					}
				}
				if (edgeChanged) {
					unknownPos.remove();
					applyRules();
					boardChanged = true;
				}
			}
		} while (boardChanged);
		/* log SAT time */
		long stopX = System.nanoTime();
		double sec = (double)(stopX - startX) / 1000000000.0d;
		System.out.println(">> Solver: Finished."
				+ "\n\t" + "-> Time: " + Double.toString(sec));
	}
	
	/* returns true if cell constraint are exactly satisifed */
	private boolean checkCellConstraints() {
		int c;
		int countPos = 0;
//		int countNeg = 0;
		ArrayList<Edge> cellEdges;
		for (Cell cell : board.getAllCells()) {
			if (cell.hasConstraint()) {
				c = cell.getConstraint();
				countPos = 0;
//				countNeg = 0;
				cellEdges = board.getIncidentEdges(cell);
				for (Edge edge : cellEdges) {
					if (edge.isPositive()) {
						countPos++;
					}
//					else if (edge.isNegative()) {
//						countNeg++;
//					}
				}
				if (countPos != c) {
//					System.out.println(">> Solver: Cell Check, Fail");
					return false;
				}
			}
		}
//		System.out.println(">> Solver: Cell Check, Pass");
		return true;
	}
	
	/* returns true if every node has exactly 0 or 2 positive edges */
	private boolean checkIncidentEdges() {
		ArrayList<Edge> incEdges;
		int count;
		for (Node node : getAllNodes()) {
			incEdges = getIncidentEdges(node);
			count = 0;
			for (Edge edge : incEdges) {
				if (edge.isPositive()) {
					count++;
				}
			}
			if (!(count == 2 || count == 0)) {
//				System.out.println(">> Solver: Node Check, Fail");
				return false;
			}
		}
//		System.out.println(">> Solver: Node Check, Pass");
		return true;
	}
	
	/* checks loop.size() equals the number of set edges */
	private boolean checkExcessEdges() {
		if (loop != null) {
			 /* loop will have one duplicate entry, its starting point */
			int loopSize = loop.size() - 1;
			int count = 0;
			for (Edge edge : board.getAllEdges()) {
				if (edge.isPositive()) {
					count++;
				}
			}
			if (count != loopSize) {
//				System.out.println(">> Solver: Excess Edge Check, Fail");
				return false;
			}
//			System.out.println(">> Solver: Excess Edge Check, Pass");
			return true;
		}
//		System.out.println(">> Solver: Excess Edge Check, Fail");
		return false;
	}
	
	/* recursively extends a path along positive edges, searching for a loop */
	private boolean traceLoop(ArrayList<Edge> path) {
		ArrayList<Edge> extPath;
		if (path.size() > 3 && path.get(0).equals(path.get(path.size() - 1))) {
			this.loop = path;
			return true;
		} else {
			/* most recent edge added to path */
			Edge leadingEdge = path.get(path.size() - 1);
			/* incident nodes of leading edge */
			ArrayList<Node> nodes = getIncidentNodes(leadingEdge);
			for (Node node : nodes) {
				/* find next node's incident edges */
				ArrayList<Edge> nextEdges = getIncidentEdges(node.i, node.j);
				/* for each incident edge */
				for (Edge nextEdge : nextEdges) {
					if (nextEdge.isPositive() && 
							(!path.contains(nextEdge) || (path.size() > 3 && nextEdge.equals(path.get(0))))) {
						tracedEdges.add(nextEdge);
						extPath = new ArrayList<Edge>(50);
						extPath.addAll(path);
						extPath.add(nextEdge);
						return traceLoop(extPath);
					}
				}
			}
			return false;
		}
//		return false;
	}
	
	/* checks that a loop, any loop exist on the game board */
	private boolean checkLoopExist() {
		tracedEdges.clear();
		ArrayList<Edge> path;
		for (Edge edge : this.board.getAllEdges()) {
			if (edge.isPositive() && !tracedEdges.contains(edge)) {
				tracedEdges.add(edge);
				path = new ArrayList<Edge>(50);
				path.add(edge);
				if (traceLoop(path)) {
//					System.out.println(">> Solver: Loop Check, Pass");
					return true;
				}
			}
		}
//		System.out.println(">> Solver: Loop Check, Fail");
		return false;
	}
	
	/** 
	 * checks if this Solver's board is solved 
	 */
	public boolean checkIfSolved() {
		if (checkCellConstraints() && checkIncidentEdges() && checkLoopExist() && checkExcessEdges()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * trys to set edge to value, returns true if successful, false if
	 * edge is already set to that value, and throws an exception
	 * if the edge is known to be the opposite value
	 */
	private boolean assertEdge(Edge edge, int value, String tag) throws ConsistencyException {
		boolean changed = false;
		if (!edge.isKnown()) {
			edge.setValue(value);
			if (value == Edge.POSITIVE) {
				System.out.println(">> Solver: Move: " + edge.toString() + "Positive");
			} else {
				System.out.println(">> Solver: Move: " + edge.toString() + "Negative");
			}
			changed = true;
		} else if (edge.isKnown() && edge.getValue() != value) {
			throw new ConsistencyException(
					">> Solver: ConsistencyException @ " + edge.toString()
							+ "\n\t" + "-> rule tag: " + tag
							+ "\n\t" + "-> attemped: " + Integer.toString(value));
		}
		return changed;
	}
	
	/**
	 * applies ruleset to this Solver's board
	 * 
	 * @return true if there are no consistency exceptions
	 */
	public boolean applyRules() {
		try {
			ArrayList<Cell> cells = this.board.getAllCells();
			ArrayList<Node> nodes = getAllNodes();
			boolean boardChanged;
			int c;
			do {
				boardChanged = false;
				/* for each cell */
				for (Cell cell : cells) {
					boardChanged = boardChanged || rule_incidentEdges(cell);
					if (cell.hasConstraint()) {
						c = cell.getConstraint();
						if (c > 0) {
							boardChanged = boardChanged || rule_corners(cell);
							if (c == 3) {
								boardChanged = boardChanged || rule_adjacentThrees(cell);
								boardChanged = boardChanged || rule_diagonalThrees(cell);
								boardChanged = boardChanged || rule_lineToThree(cell);
							} else if (c == 2) {
								boardChanged = boardChanged || rule_blockedTwo(cell);
								boardChanged = boardChanged || rule_lineToTwo(cell);
								boardChanged = boardChanged || rule_incidentEdges(cell);
							} else if (c == 1) {
								boardChanged = boardChanged || rule_ones(cell);
							}
						} else {
							boardChanged = boardChanged || rule_zero(cell);
						}
					}
				}
				/* for each node */
				for (Node node : nodes) {
					boardChanged = boardChanged || rule_incidentEdges(node);
				}
				rule_falseLoop();
			} while (boardChanged);
		} catch (ConsistencyException ce) {
			/* handle exception */
//			System.out.println(ce.toString());
			return false;
		}
		return true;
	}
	
	/***********************************************************************************************************************
	 ***************************************************** RULES BELOW *****************************************************
	 ***********************************************************************************************************************/
	
	private void rule_falseLoop() throws ConsistencyException {
		if (checkLoopExist() && !checkExcessEdges()) {
			throw new ConsistencyException(">> Solver: ConsistencyException"
					+ "\n\t" + "-> rule tag: FL");
		}
	}
	
	private boolean rule_ones(Cell cell) throws ConsistencyException {
		final String RULETAG = "1";
		ArrayList<Edge> edges = new ArrayList<Edge>(2);
		int m = cell.getRow();
		int n = cell.getCol();
		boolean changed = false;
		/* cell against left board edge, but not in corner */
		if ((m > 0 && m < board.getNumRows() - 1) && n == 0) {
			/* check vertical edge above cell and against board edge */
			if (board.getEdge(m-1, n, Edge.VERTICAL).isPositive()) {
				edges.add(board.getEdge(m+1, n, Edge.HORIZONTAL));
				edges.add(board.getEdge(m, n+1, Edge.VERTICAL));
				for (Edge edge : edges) {
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".lbe.0");
				}
				edges.clear();
			}
			if (board.getEdge(m-1, n, Edge.VERTICAL).isNegative()) {
				edges.add(board.getEdge(m, n, Edge.HORIZONTAL));
				edges.add(board.getEdge(m, n, Edge.VERTICAL));
				for (Edge edge : edges) {
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".lbe.1");
				}
				edges.clear();
			}
			/* check vertical edge below cell and against board edge */
			if (board.getEdge(m+1, n, Edge.VERTICAL).isPositive()) {
				edges.add(board.getEdge(m, n, Edge.HORIZONTAL));
				edges.add(board.getEdge(m, n+1, Edge.VERTICAL));
				for (Edge edge : edges) {
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".lbe.2");
				}
				edges.clear();
			}
			if (board.getEdge(m+1, n, Edge.VERTICAL).isNegative()) {
				edges.add(board.getEdge(m+1, n, Edge.HORIZONTAL));
				edges.add(board.getEdge(m, n, Edge.VERTICAL));
				for (Edge edge : edges) {
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".lbe.3");
				}
				edges.clear();
			}
		/* cell against top board edge, but not in corner */
		} else if ((n > 0 && n < board.getNumCols() - 1) && m == 0) {
			/* check horizontal edge left of cell and against board edge */
			if (board.getEdge(m, n-1, Edge.HORIZONTAL).isPositive()) {
				edges.add(board.getEdge(m+1, n, Edge.HORIZONTAL));
				edges.add(board.getEdge(m, n+1, Edge.VERTICAL));
				for (Edge edge : edges) {
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".tbe.0");
				}
				edges.clear();
			}
			if (board.getEdge(m, n-1, Edge.HORIZONTAL).isNegative()) {
				edges.add(board.getEdge(m, n, Edge.HORIZONTAL));
				edges.add(board.getEdge(m, n, Edge.VERTICAL));
				for (Edge edge : edges) {
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".tbe.1");
				}
				edges.clear();
			}
			/* check horizontal edge right of cell and against board edge */
			if (board.getEdge(m, n+1, Edge.HORIZONTAL).isPositive()) {
				edges.add(board.getEdge(m+1, n, Edge.HORIZONTAL));
				edges.add(board.getEdge(m, n, Edge.VERTICAL));
				for (Edge edge : edges) {
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".tbe.2");
				}
				edges.clear();
			}
			if (board.getEdge(m, n+1, Edge.HORIZONTAL).isNegative()) {
				edges.add(board.getEdge(m, n, Edge.HORIZONTAL));
				edges.add(board.getEdge(m, n+1, Edge.VERTICAL));
				for (Edge edge : edges) {
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".tbe.3");
				}
				edges.clear();
			}
		/* cell against right board edge, but not in corner */
		} else if ((m > 0 && m < board.getNumRows() - 1) && n == board.getNumCols() - 1) {
			/* check vertical edge above cell and against board edge */
			if (board.getEdge(m-1, n+1, Edge.VERTICAL).isPositive()) {
				edges.add(board.getEdge(m+1, n, Edge.HORIZONTAL));
				edges.add(board.getEdge(m, n, Edge.VERTICAL));
				for (Edge edge : edges) {
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".rbe.0");
				}
				edges.clear();
			}
			if (board.getEdge(m-1, n+1, Edge.VERTICAL).isNegative()) {
				edges.add(board.getEdge(m, n, Edge.HORIZONTAL));
				edges.add(board.getEdge(m, n+1, Edge.VERTICAL));
				for (Edge edge : edges) {
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".rbe.1");
				}
				edges.clear();
			}
			/* check vertical edge below cell and against board edge */
			if (board.getEdge(m+1, n+1, Edge.VERTICAL).isPositive()) {
				edges.add(board.getEdge(m, n, Edge.HORIZONTAL));
				edges.add(board.getEdge(m, n, Edge.VERTICAL));
				for (Edge edge : edges) {
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".rbe.2");
				}
				edges.clear();
			}
			if (board.getEdge(m+1, n+1, Edge.VERTICAL).isNegative()) {
				edges.add(board.getEdge(m+1, n, Edge.HORIZONTAL));
				edges.add(board.getEdge(m, n+1, Edge.VERTICAL));
				for (Edge edge : edges) {
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".rbe.3");
				}
				edges.clear();
			}
		/* cell against bottom board edge, but not in corner */
		} else if ((n > 0 && n < board.getNumCols() - 1) && m == board.getNumRows() - 1) {
			/* check horizontal edge left of cell and against board edge */
			if (board.getEdge(m+1, n-1, Edge.HORIZONTAL).isPositive()) {
				edges.add(board.getEdge(m, n, Edge.HORIZONTAL));
				edges.add(board.getEdge(m, n+1, Edge.VERTICAL));
				for (Edge edge : edges) {
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".bbe.0");
				}
				edges.clear();
			}
			if (board.getEdge(m+1, n-1, Edge.HORIZONTAL).isNegative()) {
				edges.add(board.getEdge(m+1, n, Edge.HORIZONTAL));
				edges.add(board.getEdge(m, n, Edge.VERTICAL));
				for (Edge edge : edges) {
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".bbe.1");
				}
				edges.clear();
			}
			/* check horizontal edge right of cell and against board edge */
			if (board.getEdge(m+1, n+1, Edge.HORIZONTAL).isPositive()) {
				edges.add(board.getEdge(m, n, Edge.HORIZONTAL));
				edges.add(board.getEdge(m, n, Edge.VERTICAL));
				for (Edge edge : edges) {
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".bbe.0");
				}
				edges.clear();
			}
			if (board.getEdge(m+1, n+1, Edge.HORIZONTAL).isNegative()) {
				edges.add(board.getEdge(m+1, n, Edge.HORIZONTAL));
				edges.add(board.getEdge(m, n+1, Edge.VERTICAL));
				for (Edge edge : edges) {
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".bbe.1");
				}
				edges.clear();
			}
		}
		/* line and 'x' at top-left corner of cell */
		if ((m > 0 && n > 0) && 
				((board.getEdge(m, n-1, Edge.HORIZONTAL).isPositive() && board.getEdge(m-1, n, Edge.VERTICAL).isNegative()) ||
				(board.getEdge(m, n-1, Edge.HORIZONTAL).isNegative() && board.getEdge(m-1, n, Edge.VERTICAL).isPositive()))) {
			edges.add(board.getEdge(m+1, n, Edge.HORIZONTAL));
			edges.add(board.getEdge(m, n+1, Edge.VERTICAL));
			for (Edge edge : edges) {
				changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".tlc.lx");
			}
			edges.clear();
		}
		/* line and 'x' at top-right corner */
		if ((m > 0 && n < board.getNumCols() - 1) && 
				((board.getEdge(m, n+1, Edge.HORIZONTAL).isPositive() && board.getEdge(m-1, n+1, Edge.VERTICAL).isNegative()) ||
					(board.getEdge(m, n+1, Edge.HORIZONTAL).isNegative() && board.getEdge(m-1, n+1, Edge.VERTICAL).isPositive()))) {
			edges.add(board.getEdge(m+1, n, Edge.HORIZONTAL));
			edges.add(board.getEdge(m, n, Edge.VERTICAL));
			for (Edge edge : edges) {
				changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".trc.lx");
			}
			edges.clear();
		}
		/* line and 'x' at bottom-left corner */
		if ((m < board.getNumRows() - 1 && n > 0) && 
				((board.getEdge(m+1, n-1, Edge.HORIZONTAL).isPositive() && board.getEdge(m+1, n, Edge.VERTICAL).isNegative()) ||
				(board.getEdge(m+1, n-1, Edge.HORIZONTAL).isNegative() && board.getEdge(m+1, n, Edge.VERTICAL).isPositive()))) {
			edges.add(board.getEdge(m, n, Edge.HORIZONTAL));
			edges.add(board.getEdge(m, n+1, Edge.VERTICAL));
			for (Edge edge : edges) {
				changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".blc.lx");
			}
			edges.clear();
		}
		/* line and 'x' at bottom-right corner */
		if ((m < board.getNumRows() - 1 && n < board.getNumCols() - 1) && 
				((board.getEdge(m+1, n+1, Edge.HORIZONTAL).isPositive() && board.getEdge(m+1, n+1, Edge.VERTICAL).isNegative()) ||
				(board.getEdge(m+1, n+1, Edge.HORIZONTAL).isNegative() && board.getEdge(m+1, n+1, Edge.VERTICAL).isPositive()))) {
			edges.add(board.getEdge(m, n, Edge.HORIZONTAL));
			edges.add(board.getEdge(m, n, Edge.VERTICAL));
			for (Edge edge : edges) {
				changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".blc.lx");
			}
			edges.clear();
		}
		return changed;
	}
	
	private boolean rule_blockedTwo(Cell cell) throws ConsistencyException {
		final String RULETAG = "B2";
//		ArrayList<Edge> edges = new ArrayList<Edge>(2);
		Edge edge;
		int m = cell.getRow();
		int n = cell.getCol();
		int numRows = board.getNumRows();
		int numCols = board.getNumCols();
		boolean changed = false;
		/* check for negative edges to the left */
		if ((n == 0 || (board.getEdge(m, n-1, Edge.HORIZONTAL).isNegative() && board.getEdge(m+1, n-1, Edge.HORIZONTAL).isNegative()))
				&& (m > 0 && m < numRows - 1)) {
			if (board.getEdge(m-1, n, Edge.VERTICAL).isNegative()) {
				edge = board.getEdge(m+1, n, Edge.VERTICAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".pb.l.0");
			}
			if (board.getEdge(m+1, n, Edge.VERTICAL).isNegative()) {
				edge = board.getEdge(m-1, n, Edge.VERTICAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".pb.l.1");
			}
		}
		/* check for negative edges to the right */
		if ((n == numCols - 1 || (board.getEdge(m, n+1, Edge.HORIZONTAL).isNegative() && board.getEdge(m+1, n+1, Edge.HORIZONTAL).isNegative()))
				&& (m > 0 && m < numRows - 1)) {
			if (board.getEdge(m-1, n+1, Edge.VERTICAL).isNegative()) {
				edge = board.getEdge(m+1, n+1, Edge.VERTICAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".pb.r.0");
			}
			if (board.getEdge(m+1, n+1, Edge.VERTICAL).isNegative()) {
				edge = board.getEdge(m-1, n+1, Edge.VERTICAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".pb.r.1");
			}
		}
		/* check for negative edges above */
		if ((m == 0 || (board.getEdge(m-1, n, Edge.VERTICAL).isNegative() &&  board.getEdge(m-1, n+1, Edge.VERTICAL).isNegative()))
				&& (n > 0 && n < numCols - 1)) {
			if (board.getEdge(m, n-1, Edge.HORIZONTAL).isNegative()) {
				edge = board.getEdge(m, n+1, Edge.HORIZONTAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".pb.u.0");
			}
			if (board.getEdge(m, n+1, Edge.HORIZONTAL).isNegative()) {
				edge = board.getEdge(m, n-1, Edge.HORIZONTAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".pb.u.0");
			}
		}
		/* check for negative edges below*/
		if ((m == numRows - 1 || (board.getEdge(m+1, n, Edge.VERTICAL).isNegative() && board.getEdge(m+1, n+1, Edge.VERTICAL).isNegative()))
				&& (n > 0 && n < numCols - 1)) {
			if (board.getEdge(m+1, n-1, Edge.HORIZONTAL).isNegative()) {
				edge = board.getEdge(m+1, n+1, Edge.HORIZONTAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".pb.d.0");
			}
			if (board.getEdge(m+1, n+1, Edge.HORIZONTAL).isNegative()) {
				edge = board.getEdge(m+1, n-1, Edge.HORIZONTAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".pb.d.1");
			}
		}
		return changed;
	}
	
	private boolean rule_lineToTwo(Cell cell) throws ConsistencyException {
		final String RULETAG = "L2";
		ArrayList<Edge> edges = new ArrayList<>(2);
		int m = cell.getRow();
		int n = cell.getCol();
		int numRows = board.getNumRows();
		int numCols = board.getNumCols();
		boolean changed = false;
		boolean notInBoardCorner;
		/* check non-incident edges at top-left */
		notInBoardCorner = (m > 0 && n > 0);
		if (notInBoardCorner && board.getEdge(m, n-1, Edge.HORIZONTAL).isPositive() && board.getEdge(m-1, n, Edge.VERTICAL).isPositive()) {
			edges.add(board.getEdge(m+1, n, Edge.HORIZONTAL));
			edges.add(board.getEdge(m, n+1, Edge.VERTICAL));
			for (Edge edge : edges) {
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".tl.0");
			}
			edges.clear();
		} else if ((notInBoardCorner && board.getEdge(m, n-1, Edge.HORIZONTAL).isPositive() && board.getEdge(m-1, n, Edge.VERTICAL).isNegative())
				|| (notInBoardCorner && board.getEdge(m, n-1, Edge.HORIZONTAL).isNegative() && board.getEdge(m-1, n, Edge.VERTICAL).isPositive())
				|| (m == 0 && n > 0 && board.getEdge(m, n-1, Edge.HORIZONTAL).isPositive())
				|| (n == 0 && m > 0 && board.getEdge(m-1, n, Edge.VERTICAL).isPositive())) {
			/* try to determine vertical edge at bottom-right */
			if (m < numRows - 1) {
				if (n == numCols - 1 || board.getEdge(m+1, n+1, Edge.HORIZONTAL).isNegative()) {
					changed = changed || assertEdge(board.getEdge(m+1, n+1, Edge.VERTICAL), Edge.POSITIVE, RULETAG + ".tl.1");
				} else if (board.getEdge(m+1, n+1, Edge.HORIZONTAL).isPositive()) {
					changed = changed || assertEdge(board.getEdge(m+1, n+1, Edge.VERTICAL), Edge.NEGATIVE, RULETAG + ".tl.2");
				}
			}
			/* try to determine horizontal edge at bottom-right*/
			if (n < numCols - 1) {
				if (m == numRows - 1 || board.getEdge(m+1, n+1, Edge.VERTICAL).isNegative()) {
					changed = changed || assertEdge(board.getEdge(m+1, n+1, Edge.HORIZONTAL), Edge.POSITIVE, RULETAG + ".tl.3");
				} else if (board.getEdge(m+1, n+1, Edge.VERTICAL).isPositive()) {
					changed = changed || assertEdge(board.getEdge(m+1, n+1, Edge.HORIZONTAL), Edge.NEGATIVE, RULETAG + ".tl.4");
				}
			}
		}
		/* check non-incident edges at top-right */
		notInBoardCorner = (m > 0 && n < numCols - 1);
		if (notInBoardCorner && board.getEdge(m, n+1, Edge.HORIZONTAL).isPositive() && board.getEdge(m-1, n+1, Edge.VERTICAL).isPositive()) {
			edges.add(board.getEdge(m+1, n, Edge.HORIZONTAL));
			edges.add(board.getEdge(m, n, Edge.VERTICAL));
			for (Edge edge : edges) {
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".tr.0");
			}
			edges.clear();
		}  else if ((notInBoardCorner && board.getEdge(m, n+1, Edge.HORIZONTAL).isPositive() && board.getEdge(m-1, n+1, Edge.VERTICAL).isNegative())
				|| (notInBoardCorner && board.getEdge(m, n+1, Edge.HORIZONTAL).isNegative() && board.getEdge(m-1, n+1, Edge.VERTICAL).isPositive())
				|| (m == 0 && n < numCols - 1 && board.getEdge(m, n+1, Edge.HORIZONTAL).isPositive())
				|| (n == numCols - 1 && m > 0 && board.getEdge(m-1, n+1, Edge.VERTICAL).isPositive())) {
			/* try to determine vertical edge at bottom-left of cell */
			if (m < numRows - 1) {
				if (n == 0 || board.getEdge(m+1, n-1, Edge.HORIZONTAL).isNegative()) {
					changed = changed || assertEdge(board.getEdge(m+1, n, Edge.VERTICAL), Edge.POSITIVE, RULETAG + ".tr.1");
				} else if (board.getEdge(m+1, n-1, Edge.HORIZONTAL).isPositive()) {
					changed = changed || assertEdge(board.getEdge(m+1, n, Edge.VERTICAL), Edge.NEGATIVE, RULETAG + ".tr.2");
				}
			}
			/* try to determine horizontal edge at bottom-left of cell */
			if (n > 0) {
				if (m == numRows - 1 || board.getEdge(m+1, n, Edge.VERTICAL).isNegative()) {
					changed = changed || assertEdge(board.getEdge(m+1, n-1, Edge.HORIZONTAL), Edge.POSITIVE, RULETAG + ".tr.3");
				} else if (board.getEdge(m+1, n, Edge.VERTICAL).isPositive()) {
					changed = changed || assertEdge(board.getEdge(m+1, n-1, Edge.HORIZONTAL), Edge.NEGATIVE, RULETAG + ".tr.4");
				}
			}
		}
		/* check non-incident edges at bottom-left */
		notInBoardCorner = (m < numRows - 1 && n > 0);
		if (notInBoardCorner && board.getEdge(m+1, n-1, Edge.HORIZONTAL).isPositive() && board.getEdge(m+1, n, Edge.VERTICAL).isPositive()) {
			edges.add(board.getEdge(m, n, Edge.HORIZONTAL));
			edges.add(board.getEdge(m, n+1, Edge.VERTICAL));
			for (Edge edge : edges) {
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".bl.0");
			}
			edges.clear();
		} else if ((notInBoardCorner && board.getEdge(m+1, n-1, Edge.HORIZONTAL).isPositive() && board.getEdge(m+1, n, Edge.VERTICAL).isNegative())
				|| (notInBoardCorner && board.getEdge(m+1, n-1, Edge.HORIZONTAL).isNegative() && board.getEdge(m+1, n, Edge.VERTICAL).isPositive())
				|| (m == numRows - 1 && n > 0 && board.getEdge(m+1, n-1, Edge.HORIZONTAL).isPositive())
				|| (m < numRows - 1 && n == 0 && board.getEdge(m+1, n, Edge.VERTICAL).isPositive())) {
			/* try to determine vertical edge at top-right of cell */
			if (m > 0) {
				if (n == numCols - 1 || board.getEdge(m, n+1, Edge.HORIZONTAL).isNegative()) {
					changed = changed || assertEdge(board.getEdge(m-1, n+1, Edge.VERTICAL), Edge.POSITIVE, RULETAG + ".bl.1");
				} else if (board.getEdge(m, n+1, Edge.HORIZONTAL).isPositive()) {
					changed = changed || assertEdge(board.getEdge(m-1, n+1, Edge.VERTICAL), Edge.NEGATIVE, RULETAG + ".bl.2");
				}
			}
			/* try to determine horizontal edge at top-right of cell */
			if (n < numCols - 1) {
				if (m == 0 || board.getEdge(m-1, n+1, Edge.VERTICAL).isNegative()) {
					changed = changed || assertEdge(board.getEdge(m, n+1, Edge.HORIZONTAL), Edge.POSITIVE, RULETAG + ".bl.3");
				} else if (board.getEdge(m-1, n+1, Edge.VERTICAL).isPositive()) {
					changed = changed || assertEdge(board.getEdge(m, n+1, Edge.HORIZONTAL), Edge.NEGATIVE, RULETAG + ".bl.4");
				}
			}
		}
		/* check non-incident edges at bottom-right */
		notInBoardCorner = (m < numRows - 1 && n < numCols - 1);
		if (notInBoardCorner && board.getEdge(m+1, n+1, Edge.HORIZONTAL).isPositive() && board.getEdge(m+1, n+1, Edge.VERTICAL).isPositive()) {
			edges.add(board.getEdge(m, n, Edge.HORIZONTAL));
			edges.add(board.getEdge(m, n, Edge.VERTICAL));
			for (Edge edge : edges) {
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".br.0");
			}
			edges.clear();
		}
		else if ((notInBoardCorner && board.getEdge(m+1, n+1, Edge.HORIZONTAL).isPositive() && board.getEdge(m+1, n+1, Edge.VERTICAL).isNegative())
				|| (notInBoardCorner && board.getEdge(m+1, n+1, Edge.HORIZONTAL).isNegative() && board.getEdge(m+1, n+1, Edge.VERTICAL).isPositive())
				|| (m == numRows - 1 && n < numCols - 1 && board.getEdge(m+1, n+1, Edge.HORIZONTAL).isPositive())
				|| (m < numRows - 1 && n == numCols - 1 && board.getEdge(m+1, n+1, Edge.VERTICAL).isPositive())) {
			/* try to determine vertical edge at top-right of cell */
			if (m > 0) {
				if (n == 0 || board.getEdge(m, n-1, Edge.HORIZONTAL).isNegative()) {
					changed = changed || assertEdge(board.getEdge(m-1, n, Edge.VERTICAL), Edge.POSITIVE, RULETAG + ".br.1");
				} else if (board.getEdge(m, n-1, Edge.HORIZONTAL).isPositive()) {
					changed = changed || assertEdge(board.getEdge(m-1, n, Edge.VERTICAL), Edge.NEGATIVE, RULETAG + ".br.2");
				}
			}
			/* try to determine horizontal edge at top-right of cell */
			if (n > 0) {
				if (m == 0 || board.getEdge(m-1, n, Edge.VERTICAL).isNegative()) {
					changed = changed || assertEdge(board.getEdge(m, n-1, Edge.HORIZONTAL), Edge.POSITIVE, RULETAG + ".br.3");
				} else if (board.getEdge(m-1, n, Edge.VERTICAL).isPositive()) {
					changed = changed || assertEdge(board.getEdge(m, n-1, Edge.HORIZONTAL), Edge.NEGATIVE, RULETAG + ".br.4");
				}
			}
		}
		return changed;
	}
	
	private boolean rule_incidentEdges(Cell cell) throws ConsistencyException {
		final String RULETAG = "IE.S";
		boolean changed = false;
		ArrayList<Edge> edges = board.getIncidentEdges(cell);
		int c;
		/* the number of known set edges */
		int numKnownPos = 0;
		/* the number of known un-set edges */
		int numKnownNeg = 0;
		for (Edge edge : edges) {
			if (edge.isPositive()) {
				numKnownPos++;
			} else if (edge.isNegative()) {
				numKnownNeg++;
			}
		}
		/* consistency checks */
		if (numKnownPos == 4) {
			throw new ConsistencyException(">> Solver: ConsistencyException @ " + cell.toString()
					+ "\n\t" + "-> rule tag: " + RULETAG + ".0");
		}
		if (cell.hasConstraint()) {
			c = cell.getConstraint();
			if (numKnownPos > c) {
				throw new ConsistencyException(">> Solver: ConsistencyException @ " + cell.toString()
						+ "\n\t" + "-> rule tag: " + RULETAG + ".1");
			}
			if (numKnownNeg > 4 - c) {
				throw new ConsistencyException(">> Solver: ConsistencyException @ " + cell.toString()
						+ "\n\t" + "-> rule tag: " + RULETAG + ".2");
			}
			/* if there are unknown sides */
			if (numKnownPos + numKnownNeg != edges.size()) {
				/* if the number of set edges equals the squares constraints */
				if (numKnownPos == c) {
					for (Edge edge : edges) {
						if (!edge.isKnown()) {
							assertEdge(edge, Edge.NEGATIVE, RULETAG);
							changed = true;
						}
					}
				/* if the number of unset edges equals four minus the constraint */
				} else if (numKnownNeg == 4 - c) {
					for (Edge edge : edges) {
						if (!edge.isKnown()) {
							assertEdge(edge, Edge.POSITIVE, RULETAG);
							changed = true;
						}	
					}
				}
			}
		}
		return changed;
	}
	
	private boolean rule_incidentEdges(Node node) throws ConsistencyException {
		final String RULETAG = "IE.N";
		boolean changed = false;
		ArrayList<Edge> edges = getIncidentEdges(node);
		/* the number of edges surrounding the node */
		int numTotal = edges.size();
		/* the number of known set edges */
		int numKnownPos = 0;
		/* the number of known un-set edges */
		int numKnownNeg = 0;
		for (Edge edge : edges) {
			if (edge.isPositive()) {
				numKnownPos++;
			} else if (edge.isNegative()) {
				numKnownNeg++;
			}
		}
		/* consistency checks */
		if (numKnownPos == 1 && numKnownPos + numKnownNeg == numTotal) {
			throw new ConsistencyException(">> Solver: ConsistencyException @ " + node.toString()
					+ "\n\t" + "-> rule tag: " + RULETAG + ".0");
		}
		/* if there's ever more than two positive edges */
		if (numKnownPos > 2) {
			throw new ConsistencyException(">> Solver: ConsistencyException @ " + node.toString()
					+ "\n\t" + "-> rule tag: " + RULETAG + ".1");
		}
		/* if only one edge is unknown */
		if (numKnownNeg + numKnownPos == edges.size() - 1) {
			/* if all known edges are negative, un-set the remaining edge */
			if (numKnownNeg == edges.size() - 1) {
				for (Edge edge : edges) {
					if (!edge.isKnown()) {
						assertEdge(edge, Edge.NEGATIVE, RULETAG);
						changed = true;
					}
				}
			/* if one edge is positive, and the rest are negative, set the remaining edge */
			} else if (numKnownPos == 1 && numKnownNeg == edges.size() - 2) {
				for (Edge edge : edges) {
					if (!edge.isKnown()) {
						edge.setPositive();
						assertEdge(edge, Edge.POSITIVE, RULETAG);
						changed = true;
					}
				}
			}
		}
		/* if two edges are known-positive, un-set the remaining edges  */
		if (numKnownPos == 2) {
			for (Edge edge : edges) {
				if (!edge.isKnown()) {
					edge.setNegative();
					assertEdge(edge, Edge.NEGATIVE, RULETAG);
					changed = true;
				}
			}
		}
		/* if there are four incident edges, two are known-negative, and one is known-positive, set the remaining edge */
		if (edges.size() == 4 && numKnownNeg == 2 && numKnownPos == 1) {
			for (Edge edge : edges) {
				if (!edge.isKnown()) {
					assertEdge(edge, Edge.POSITIVE, RULETAG);
					changed = true;
				}
			}
		}
		return changed;
	}
	
	private boolean rule_zero(Cell cell) throws ConsistencyException {
		final String RULETAG = "Z";
		/* changed represents whether this rule has changed the board */
		boolean changed = false;
		/* assert each surrounding edge as negative */
		ArrayList<Edge> edges = board.getIncidentEdges(cell);
		for (Edge edge : edges) {
			assertEdge(edge, Edge.NEGATIVE, RULETAG + "." + Integer.toString(edges.indexOf(edge)));
		}
		return changed;
	}
	
	/* checks for adjacent cells with a constraint of 3 */
	private boolean rule_adjacentThrees(Cell cell) throws ConsistencyException{
		final String RULETAG = "A3";
		Cell s;
		Edge edge;
		int m = cell.getRow();
		int n = cell.getCol();
		int numRows = board.getNumRows();
		int numCols = board.getNumCols();
		boolean changed = false;
		/* if possible, check to the right */
		if (numCols > 2 && n < board.getNumCols() - 1) {
			s = board.getCell(m, n+1);
			/* if the cell to the right has a constraint of 3 */
			if (s.hasConstraint() && s.getConstraint() == 3) {
				/* set edges */
				edge = board.getEdge(m, n, Edge.VERTICAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG);
				edge = board.getEdge(m, n+1, Edge.VERTICAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG);
				edge = board.getEdge(m, n+2, Edge.VERTICAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG);
				/* reset edges */
				if (m > 0) {
					edge = board.getEdge(m-1, n+1, Edge.VERTICAL);
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG);
				}
				if (m < board.getNumRows() - 1) {
					edge = board.getEdge(m+1, n+1, Edge.VERTICAL);
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG);
				}
			}
		}
		/* if possible, check below */
		if (numRows > 2 && m < board.getNumRows() - 1) {
			s = board.getCell(m+1, n);
			if (s.hasConstraint() && s.getConstraint() == 3) {
				/* set edges */
				edge = board.getEdge(m, n, Edge.HORIZONTAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG);
				edge = board.getEdge(m+1, n, Edge.HORIZONTAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG);
				edge = board.getEdge(m+2, n, Edge.HORIZONTAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG);
				/* reset edges */
				if (n > 0) {
					edge = board.getEdge(m+1, n-1, Edge.HORIZONTAL);
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG);
				}
				if (n < board.getNumCols() - 1) {
					edge = board.getEdge(m+1, n+1, Edge.HORIZONTAL);
					changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG);
				}
			}
		}
		return changed;
	}
	
	private boolean rule_diagonalThrees(Cell cell) throws ConsistencyException {
		final String RULETAG = "D3";
		int m = cell.getRow();
		int n = cell.getCol();
		Cell s;
		int sM;
		int sN;
		ArrayList<Edge> edgesPos = new ArrayList<Edge>(4);
		boolean changed = false;
		/* if possible, check to the down and right */
		if (m < board.getNumRows() - 1 && n < board.getNumCols() - 1) {
			s = board.getCell(m+1, n+1);
			if (s.hasConstraint() && s.getConstraint() == 3) {
				sM = s.getRow();
				sN = s.getCol();
				/* set edges */
				edgesPos.add(board.getEdge(m, n, Edge.HORIZONTAL));
				edgesPos.add(board.getEdge(m, n, Edge.VERTICAL));
				edgesPos.add(board.getEdge(sM+1, sN, Edge.HORIZONTAL));
				edgesPos.add(board.getEdge(sM, sN+1, Edge.VERTICAL));
				for (Edge edge : edgesPos) {
					changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".r");
				}
				edgesPos.clear();
			}
		}
		/* if possible, check down and left */
		if (m < board.getNumRows() - 1 && n > 0) {
			s = board.getCell(m+1, n-1);
			if (s.hasConstraint() && s.getConstraint() == 3) {
				sM = s.getRow();
				sN = s.getCol();
				/* set edges */
				edgesPos.add(board.getEdge(m, n, Edge.HORIZONTAL));
				edgesPos.add(board.getEdge(m, n+1, Edge.VERTICAL));
				edgesPos.add(board.getEdge(sM+1, sN, Edge.HORIZONTAL));
				edgesPos.add(board.getEdge(sM, sN, Edge.VERTICAL));
				for (Edge edge : edgesPos) {
					changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".l");
				}
			}
		}
		return changed;
	}
	
	private boolean rule_lineToThree(Cell cell) throws ConsistencyException {
		final String RULETAG = "L3";
		int m = cell.getRow();
		int n = cell.getCol();
		ArrayList<Edge> edgesPos = new ArrayList<Edge>(2);
		boolean changed = false;
		/* line enters square at top-left node */
		if ((n > 0 && board.getEdge(m, n-1, Edge.HORIZONTAL).isPositive()) ||
				(m > 0 && board.getEdge(m-1, n, Edge.VERTICAL).isPositive())) {
			edgesPos.add(board.getEdge(m+1, n, Edge.HORIZONTAL));
			edgesPos.add(board.getEdge(m, n+1, Edge.VERTICAL));
			for (Edge edgePos : edgesPos) {
				changed = changed || assertEdge(edgePos, Edge.POSITIVE, RULETAG + ".tl");
			}
			edgesPos.clear();
		}
		/* line enters square at top-right node */
		if ((n < board.getNumCols() - 1 && board.getEdge(m, n+1, Edge.HORIZONTAL).isPositive()) ||
				(m > 0 && board.getEdge(m-1, n+1, Edge.VERTICAL).isPositive())) {
			edgesPos.add(board.getEdge(m+1, n, Edge.HORIZONTAL));
			edgesPos.add(board.getEdge(m, n, Edge.VERTICAL));
			for (Edge edgePos : edgesPos) {
				changed = changed || assertEdge(edgePos, Edge.POSITIVE, RULETAG + ".tr");
			}
			edgesPos.clear();
		}
		/* line enter square at bottom-left node */
		if ((n > 0 && board.getEdge(m+1, n-1, Edge.HORIZONTAL).isPositive()) ||
				(m < board.getNumRows() - 1 && board.getEdge(m+1, n, Edge.VERTICAL).isPositive())) {
			edgesPos.add(board.getEdge(m, n, Edge.HORIZONTAL));
			edgesPos.add(board.getEdge(m, n+1, Edge.VERTICAL));
			for (Edge edgePos : edgesPos) {
				changed = changed || assertEdge(edgePos, Edge.POSITIVE, RULETAG + ".bl");
			}
			edgesPos.clear();
		}
		/* line enters square at bottom-right node */
		if ((n < board.getNumCols() - 1 && board.getEdge(m+1, n+1, Edge.HORIZONTAL).isPositive()) ||
				(m < board.getNumRows() - 1 && board.getEdge(m+1, n+1, Edge.VERTICAL).isPositive())) {
			edgesPos.add(board.getEdge(m, n, Edge.HORIZONTAL));
			edgesPos.add(board.getEdge(m, n, Edge.VERTICAL));
			for (Edge edgePos : edgesPos) {
				changed = changed || assertEdge(edgePos, Edge.POSITIVE, RULETAG + ".br");
			}
		}
		return changed;
	}
	
	/* checks if square is a corner, sets edges according to constraint */
	private boolean rule_corners(Cell cell) throws ConsistencyException {
		final String RULETAG = "C";
		Edge edge;
		int c = cell.getConstraint();
		int m = cell.getRow();
		int n = cell.getCol();
		int numRows = board.getNumRows();
		int numCols = board.getNumCols(); 
		/* changed represents whether this rule has changed the board */
		boolean changed = false;
		boolean truCorner;
		/* funCorner represents whether a square is in a 'functional' corner, 
		 * meaning that at one of its nodes there are two negative edges that
		 * are not incident to the square itself */
		boolean funCorner;
		/* these two corners exist at an edge of the board */
		boolean vEdgeCorner;
		boolean hEdgeCorner;
		/* twoCorner represents the additional constraints a cell with constraint two requires to be a corner */
		boolean twoCorner;
		/* top-left corner */
		truCorner = (m == 0 && n == 0);
		funCorner = ((m > 0 && n > 0) && board.getEdge(m, n-1, Edge.HORIZONTAL).isNegative()
				&& board.getEdge(m-1, n, Edge.VERTICAL).isNegative());
		hEdgeCorner = (m == 0 && n > 0) && board.getEdge(m, n-1, Edge.HORIZONTAL).isNegative();
		vEdgeCorner = (m > 0 && n == 0) && board.getEdge(m-1, n, Edge.VERTICAL).isNegative();
		if (truCorner || funCorner || hEdgeCorner || vEdgeCorner) {			
			/* constraint of three */
			if (c == 3) {
				edge = board.getEdge(m, n, Edge.HORIZONTAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".tl.3.0");
				edge = board.getEdge(m, n, Edge.VERTICAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".tl.3.1");
			/* constraint of two */
			} else if (c == 2) {
				twoCorner = (funCorner && board.getEdge(m-1, n+1, Edge.VERTICAL).isNegative()
						&& board.getEdge(m+1, n-1, Edge.HORIZONTAL).isNegative())
						|| (hEdgeCorner && board.getEdge(m+1, n-1, Edge.HORIZONTAL).isNegative())
						|| (vEdgeCorner && board.getEdge(m-1, n+1, Edge.VERTICAL).isNegative());
				if (truCorner || twoCorner) {
					if (n < numCols - 1) {
						edge = board.getEdge(m, n+1, Edge.HORIZONTAL);
						changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".tl.2.0");
					}
					if (m < numCols - 1) {
						edge = board.getEdge(m+1, n, Edge.VERTICAL);
						changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".tl.2.1");
					}
				}
			/* constraint of one */
			} else if (c == 1) {
				edge = board.getEdge(m, n, Edge.HORIZONTAL);
				changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".tl.1.0");
				edge = board.getEdge(m, n, Edge.VERTICAL);
				changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".tl.1.1");
			}
		}
		/* top-right corner */
		truCorner = (m == 0 && n == numCols - 1);
		funCorner = (m > 0 && n < numCols - 1)
				&& board.getEdge(m, n+1, Edge.HORIZONTAL).isNegative()
				&& board.getEdge(m-1, n+1, Edge.VERTICAL).isNegative();
		hEdgeCorner = (m == 0 && n < numCols - 1) && board.getEdge(m, n+1, Edge.HORIZONTAL).isNegative();
		vEdgeCorner = (m > 0 && n == numCols - 1) && board.getEdge(m-1, n+1, Edge.VERTICAL).isNegative();
		if (!changed && (truCorner || funCorner || hEdgeCorner || vEdgeCorner)) {
			/* constraint of three */
			if (c == 3) {
				edge = board.getEdge(m, n, Edge.HORIZONTAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".tr.c3.0");
				edge = board.getEdge(m, n + 1, Edge.VERTICAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".tr.c3.1");
			/* constraint of two */
			} else if (c == 2) {
				twoCorner = (funCorner && board.getEdge(m+1, n+1, Edge.HORIZONTAL).isNegative()
						&& board.getEdge(m-1, n, Edge.VERTICAL).isNegative())
						|| (hEdgeCorner && board.getEdge(m+1, n+1, Edge.HORIZONTAL).isNegative())
						|| (vEdgeCorner && board.getEdge(m-1, n, Edge.VERTICAL).isNegative());
				
				if (truCorner || twoCorner) {
					if (n > 0) {
						edge = board.getEdge(m, n-1, Edge.HORIZONTAL);
						changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".tr.c2.0");
					}
					if (m < numRows - 1 && n < numCols - 1) {
						edge = board.getEdge(m+1, n+1, Edge.VERTICAL);
						changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".tr.c2.1");
					}
				}
			/* constraint of one */
			} else if (c == 1) {
				edge = board.getEdge(m, n, Edge.HORIZONTAL);
				changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".tr.c1.0");
				edge = board.getEdge(m, n + 1, Edge.VERTICAL);
				changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".tl.c1.1");
			}
		} 
		/* bottom-left corner */
		truCorner = (m == numRows - 1 && n == 0);
		funCorner = (m < numRows - 1 && n > 0) && board.getEdge(m+1, n-1, Edge.HORIZONTAL).isNegative()
				&& board.getEdge(m+1, n, Edge.VERTICAL).isNegative();
		hEdgeCorner = (m == numRows - 1 && n > 0) && board.getEdge(m+1, n-1, Edge.HORIZONTAL).isNegative();
		vEdgeCorner = (m < numRows - 1 && n == 0) && board.getEdge(m+1, n, Edge.VERTICAL).isNegative();
		if (!changed && (truCorner || funCorner || hEdgeCorner || vEdgeCorner)) {
			/* constraint of three */
			if (c == 3) {
				edge = board.getEdge(m + 1, n, Edge.HORIZONTAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".bl.c3.0");
				edge = board.getEdge(m, n, Edge.VERTICAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".bl.c3.1");
			/* constraint of two */
			} else if (c == 2) {
				twoCorner = (funCorner && board.getEdge(m, n-1, Edge.HORIZONTAL).isNegative()
						&& board.getEdge(m+1, n+1, Edge.VERTICAL).isNegative())
						|| (hEdgeCorner && board.getEdge(m, n-1, Edge.HORIZONTAL).isNegative())
						|| (vEdgeCorner && board.getEdge(m+1, n+1, Edge.VERTICAL).isNegative());
				if (truCorner || twoCorner) {
					if (m < numRows - 1 && n < numCols - 1) {
						edge = board.getEdge(m+1, n+1, Edge.HORIZONTAL);
						changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".bl.c2.0");
					}
					if (m > 0) {
						edge = board.getEdge(m-1, n, Edge.VERTICAL);
						changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".bl.c2.1");
					}
				}
			/* constraint of one */
			} else if (c == 1) {
				edge = board.getEdge(m + 1, n, Edge.HORIZONTAL);
				changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".bl.c1.0");
				edge = board.getEdge(m, n, Edge.VERTICAL);
				changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".bl.c1.1");
			}
		}
		/* bottom-right corner */
		truCorner = (m == numRows -1 && n == numCols - 1);
		funCorner = (m < numRows - 1) && (n < numCols - 1)
				&& board.getEdge(m+1, n+1, Edge.HORIZONTAL).isNegative()
				&& board.getEdge(m+1, n+1, Edge.VERTICAL).isNegative();
		hEdgeCorner = (m == numRows - 1 && n < numCols - 1) && board.getEdge(m+1, n+1, Edge.HORIZONTAL).isNegative();
		vEdgeCorner = (m < numRows - 1 && n == numCols - 1) && board.getEdge(m+1, n+1, Edge.VERTICAL).isNegative();
		if (!changed && ((m == board.getNumRows() - 1 && n == board.getNumCols() - 1) || funCorner)) {
			/* constraint of three */
			if (c == 3) {
				edge = board.getEdge(m + 1, n, Edge.HORIZONTAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".br.c3.0");
				edge = board.getEdge(m, n + 1, Edge.VERTICAL);
				changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".br.c3.1");
			/* constraint of two */
			} else if (c == 2) {
				twoCorner = (funCorner && board.getEdge(m, n+1, Edge.HORIZONTAL).isNegative()
						&& board.getEdge(m+1, n, Edge.VERTICAL).isNegative())
						|| (hEdgeCorner && board.getEdge(m, n+1, Edge.HORIZONTAL).isNegative())
						|| (vEdgeCorner && board.getEdge(m+1, n, Edge.VERTICAL).isNegative());
				if (truCorner || twoCorner) {
					if (n > 0) {
						edge = board.getEdge(m+1, n-1, Edge.HORIZONTAL);
						changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".br.c2.0");
					}
					if (m > 0) {
						edge = board.getEdge(m-1, n+1, Edge.VERTICAL);
						changed = changed || assertEdge(edge, Edge.POSITIVE, RULETAG + ".br.c2.1");
					}
				}
			/* constraint of one */
			} else if (c == 1) {
				edge = board.getEdge(m + 1, n, Edge.HORIZONTAL);
				changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".br.c1.0");
				edge = board.getEdge(m, n + 1, Edge.VERTICAL);
				changed = changed || assertEdge(edge, Edge.NEGATIVE, RULETAG + ".br.c1.1");
			}
		}
		return changed;
	}
}
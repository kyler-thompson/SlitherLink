
import java.io.*;
import java.util.Scanner;

/**
 * 
 * @author Kyler
 *
 */
public class SlitherLink {
	private static Board board;
	private static Solver solver;
	private static String filename;
	
	public static void main(String[] args) {
		if (args.length > 0) {
			/* filename */
			filename = args[0];
			/* init board and solver */
			reset();
			/* main loop */
			Scanner scanner = new Scanner(System.in);
			boolean running = true;
			do {
				printMenu();
				System.out.print(">> Select: ");
				switch (scanner.next()) {
				case "1":
					gameLoop();
					break;
				case "2":
					solve();
					break;
				case "3":
					printHelp();
					break;
				case "help":
					printHelp();
					break;
				case "4":
					running = false;
					break;
				case "exit":
					running = false;
					break;
				default:
					System.out.println(">> Select: invalid selection");
				}
			} while(running);
			System.out.println();
			scanner.close();
		} else {
			System.out.println("arg[0].length == 0");
		}
	}
	
	private static void reset() {
		board = new Board(filename);
		solver = new Solver(board);
	}
	
	private static void gameLoop() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		boolean isSolved = false;
		long startX = System.nanoTime();
		while(!isSolved) {
			try {
				printBoard(board, true);
				System.out.print(">> Move: ");
				String move = reader.readLine();
				if (move.equals("done")) {
					isSolved = solver.checkIfSolved();
					if (isSolved) {
						/* log total time */
						long stopX = System.nanoTime();
						double sec = (double)(stopX - startX) / 1000000000.0d;
						System.out.println("\n>> Puzzle Solved!\n\t-> Time: " + Double.toString(sec));
						/* print solved board */
						printBoard(board, false);
					} else {
						System.out.println(">> Sorry, Try Again!");
					}
				} else if (move.equals("exit")) {
					break;
				} else {
					makeMove(move);
				}
				System.out.print("\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/* reset */
		reset();
	}
	
	private static void solve() {
		printBoard(board, false);
		System.out.println();
		/* log start time */
		long startX = System.nanoTime();
		solver.SATsolve();
		printBoard(board, false);
		/* log total time */
		long stopX = System.nanoTime();
		double sec = (double)(stopX - startX) / 1000000000.0d;
		System.out.println("\n>> Puzzle Solved!\n\t-> Time: " + Double.toString(sec));
		printBoard(board, true);
		reset();
	}
	
	private static void printMenu() {
		System.out.print("\n");
		System.out.println("+---------------------+");
		System.out.println("|.*~* SlitherLink *~*.|");	
		System.out.println("+---------------------+");
		System.out.println("| 1. Human Player     |");	
		System.out.println("| 2. SAT Solve        |");
		System.out.println("| 3. Help             |");
		System.out.println("| 4. Exit             |");	
		System.out.println("+---------------------+");	
	}
	
	private static void printHelp() {
		System.out.println();
		System.out.println("i. making a move" + " \n" +
							"\t" + "moves are specified by two integers and a char," + "\n" +
							"\t" + "representing the choice of row, column, and dir-" + "\n" +
							"\t" + "ection, respectively. choices for direction are" + "\n" +
							"\t" + "l, r, t, b, representing left, right, top, and" + "\n" +
							"\t" + "bottom respectively." + "\n" +
							"\t" + "example: 1 2 r == row 1, column 2, right edge");
		System.out.println("ii. in-game commands" + "\n" +
							"\t" + "at any point during human play, rather than enter" + "\n" +
							"\t" + "a move, the player may enter 'done' to check their" + "\n" +
							"\t" + "solution, or 'exit' to leave the game.");
	}
	
	/**
	 * prints the game board to the terminal
	 * 
	 * @param board
	 * @param printNeat
	 *            whether or not 'negative' edges should be displayed
	 */
	private static void printBoard(Board board, boolean printNeat) {
		if (printNeat) {
			System.out.print("\n" + board.toString().replace('x', ' '));
		} else {
			System.out.print("\n" + board.toString());
		}
	}
	
	/**
	 * 
	 * @param move
	 * 		format: (int)row (int)col (String)dir
	 */
	private static void makeMove(String move) {
		/* parse move */
		Scanner scanner = new Scanner(move);
		scanner.useDelimiter(" ");
		try {
			int row = scanner.nextInt();
			int col = scanner.nextInt();
			String dir = scanner.next();
			if (row < board.getNumRows() && col < board.getNumCols()) {
				/* set the appropriate edge */
				Edge edge = null;
				if (dir.equals("l")) {
					edge = board.getEdges_v().get(row).get(col);
					edge.toggleValue();
				} else if (dir.equals("r")) {
					edge = board.getEdges_v().get(row).get(col + 1);
					edge.toggleValue();
				} else if (dir.equals("t")) {
					edge = board.getEdges_h().get(row).get(col);
					edge.toggleValue();
				} else if (dir.equals("b")) {
					edge = board.getEdges_h().get(row + 1).get(col);
					edge.toggleValue();
				}
			}
		} catch (Exception e) {
			System.out.println(">> Move: invalid move");
		}
		scanner.close();
	}
}

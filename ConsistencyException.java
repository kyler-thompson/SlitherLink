/**
 * 
 * @author Kyler
 *
 */
public class ConsistencyException extends Exception {
	
	private static final long serialVersionUID = 1L;
	private String message = null;
	
	public ConsistencyException() {
		super();
	}
	
	public ConsistencyException(String message) {
		super(message);
		this.message = message;
	}
	
	public ConsistencyException(Throwable cause) {
		super(cause);
	}
	
	public String toString() {
		return message;
	}	
}

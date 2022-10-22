/**
 * New exception in situation when Actors want to be in the cells that are occupied by the condition of the task.
 */
public class IncorrectPlace extends Exception {
    /**
     * Constructor for exception with string message
     * @param errorMessage - error message
     */
    public IncorrectPlace(String errorMessage) {
        super(errorMessage);
    }
}

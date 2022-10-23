import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import static java.lang.Math.*;

/**
 * New exception in situation when Actors want to be in the cells that are occupied by the condition of the task.
 */
class IncorrectPlace extends Exception {
    /**
     * Constructor for exception with string message
     * @param errorMessage - error message
     */
    public IncorrectPlace(String errorMessage) {
        super(errorMessage);
    }
}

/**
 * This class uses for constants in my program.
 * Symbols:
 * Blank - empty cell
 * KRAKEN - kraken's perception zone
 * KRAKEN_CELL - kraken
 * ROCK - rock cell
 * CHEST - Dead Man’s Chest Island
 * DAVY - Davy Jones's perception zone
 * DAVY_CELL - Davy Jones
 * PLAYER - Jack Sparrow
 * PATH - symbol for answer path
 * <p>
 * Numbers:
 * Assume that 1e9 is +Infinity
 */
final class Constants {
    /**
     * Symbol for blank cell
     */
    static public char BLANK = '-';

    /**
     * Symbol for Kraken's perception Zones
     */
    static public char KRAKEN = 'K';

    /**
     * Symbol for kraken "heart" cell
     */
    static public char KRAKEN_CELL = 'O';

    /**
     * Symbol for kraken "heart" and rock in one place
     */
    static public char KRAKEN_WITH_ROCK = 'W';

    /**
     * Symbol for rock cell
     */
    static public char ROCK = 'R';

    /**
     * Symbol for Tortuga cell
     */
    static public char TORTUGA = 'T';

    /**
     * Symbol for chest cell
     */
    static public char CHEST = 'C';

    /**
     * Symbol for Davy Jones's perception zone
     */
    static public char DAVY = 'D';

    /**
     * Symbol for Davy Jones
     */
    static public char DAVY_CELL = 'A';

    /**
     * Symbol for Jack Sparrow
     */
    static public char PLAYER = 'J';

    /**
     * Symbol for path
     */
    static public char PATH = '*';

    /**
     * Assume that 1e9 is +Infinity
     */
    static public int INF = (int) 1e9;
}

/**
 * Just a simple generic class that holds two values of same type
 *
 * @param <T> - type of values
 */
class Tuple<T> {
    /**
     * One of two values of the same type
     */
    private final T x;

    /**
     * One of two values of the same type
     */
    private final T y;

    /**
     * Constructor for tuple
     *
     * @param x - value of type T
     * @param y - value of type T
     */
    Tuple(T x, T y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Getter for one value of Tuple
     *
     * @return value of type T
     */
    T getX() {
        return x;
    }

    /**
     * Getter for one value of Tuple
     *
     * @return value of type T
     */
    T getY() {
        return y;
    }
}

/**
 * Supporting class for A* and BackTracking algorithm.
 * Consists of:
 * Current x and y coordinates of Jack
 * Current path length from start coordinates
 * Flag for a presence of a cask. So, does he have an ability to kill kraken
 * Flag for kraken death. So, if it set to true thus kraken has been already killed, so Jack can path through his cells
 */
class State {
    /**
     * Current x coordinate
     */
    public int currentX;

    /**
     * Current y coordinate
     */
    public int currentY;

    /**
     * Current path length from start cell
     */
    int pathLength;

    /**
     * Does Jack have a cask from Tortuga (Did he visit Tortuga?).
     * This variable shows if a jack has the ability to kill the Kraken.
     */
    public boolean cask = false;

    /**
     * Has Jack killed the Kraken? Is it alive?
     * This variable indicates whether the kraken is killed.
     */
    public boolean krakenIsDead = false;

    /**
     * Constructor just with coordinates, convenient for first status.
     *
     * @param currentX - position x
     * @param currentY - position y
     */
    State(int currentX, int currentY) {
        this.currentX = currentX;
        this.currentY = currentY;
        pathLength = 0;
    }

    /**
     * Constructor that sets all variables.
     *
     * @param currentX      - position x
     * @param currentY      - position y
     * @param cask           - cask flag
     * @param krakenIsDead - kraken death flag
     * @param pathLength    - length of path from begin to end
     */
    State(int currentX, int currentY, boolean cask, boolean krakenIsDead, int pathLength) {
        this(currentX, currentY);
        this.cask = cask;
        this.krakenIsDead = krakenIsDead;
        this.pathLength = pathLength;
    }

    /**
     * This function moves state to another coordinates and incenses path. So, when Jack moves, his coordinates change
     * and path length increases by one.
     *
     * @param x - new x coordinate
     * @param y - new y coordinate
     * @return new State with copy of other variables of class
     */
    State move(int x, int y) {
        return new State(x, y, this.cask, this.krakenIsDead, this.pathLength + 1);
    }

    /**
     * Getter for x coordinate
     * @return x coordinate
     */
    int getX() {
        return currentX;
    }

    /**
     * Getter for y coordinate
     * @return y coordinate
     */
    int getY() {
        return currentY;
    }
}

/**
 * Class board that holds all information about the whole field
 */
class Board {
    /**
     * Amount of rows in field
     */
    public int rows;

    /**
     * Amount of columns in field
     */
    public int columns;

    /**
     * Content of field matrix
     */
    private char[][] board;

    /**
     * Coordinate x of finish cell
     */
    public int finish_x;

    /**
     * Coordinate y of finish cell
     */
    public int finish_y;

    /**
     * Coordinate x of start cell
     */
    public int from_x = 0;

    /**
     * Coordinate y of start cell
     */
    public int from_y = 0;

    /**
     * Coordinate x of Tortuga cell
     */
    public int tortuga_x;

    /**
     * Coordinate y of Tortuga cell
     */
    public int tortuga_y;

    /**
     * Function that takes the cell and return its symbol. If coordinate is invalid returns null.
     *
     * @param x coordinate of cell
     * @param y coordinate of cell
     * @return character or null
     */
    Character getPiece(int x, int y) {
        if (isValidCoordinates(x, y)) {
            return board[x][y];
        }
        return null;
    }

    /**
     * Function that initialize the board. Allocate memory and place all blank cells in board.
     *
     * @param rows    - amount of rows
     * @param columns - amount of columns
     */
    private void initialize(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        board = new char[rows][columns];
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                board[i][j] = Constants.BLANK;
            }
        }
    }

    /**
     * If we create board without arraylist of coordinates (see other constructor) that it should generate random board.
     *
     * @param rows    - amount of rows
     * @param columns - amount of columns
     */
    Board(int rows, int columns) {
        initialize(rows, columns);
//        createSampleBoard4();
        generateMap();
    }

    /**
     * Constructor for board when we pass rows, columns and array of agents coordinates.
     * It checks that the user card is correct and meets all the conditions of the task.
     *
     * @param rows    - amount of rows
     * @param columns - amount of columns
     * @param arr     - arraylist of coordinates for agents (for ex. Rock, Tortuga, etc). In specific order:
     *                Jack Sparrow, Davy Jones, Kraken, Rock, Dead Man’s Chest, Tortuga
     * @throws IncorrectPlace exception in situation when Actors want to be in the cells that are occupied by the condition of the task.
     */
    Board(int rows, int columns, ArrayList<Tuple<Integer>> arr) throws IncorrectPlace {
        initialize(rows, columns);

        Tuple<Integer> Jack = arr.get(0);
        setPlayer(Jack.getX(), Jack.getY());


        Tuple<Integer> Kraken = arr.get(2);
        if (isOccupied(Kraken.getX(), Kraken.getY())) {
            throw new IncorrectPlace("Kraken invalid place");
        }
        setKraken(Kraken.getX(), Kraken.getY());

        Tuple<Integer> Davy = arr.get(1);
        if (isOccupied(Davy.getX(), Davy.getY())) {
            throw new IncorrectPlace("Davy invalid place");
        }
        setDavy(Davy.getX(), Davy.getY());

        Tuple<Integer> Rock = arr.get(3);
        if (isOccupied(Rock.getX(), Rock.getY()) && getPiece(Rock.getX(), Rock.getY()) != Constants.KRAKEN_CELL) {
            throw new IncorrectPlace("Rock invalid place");
        }
        setRock(Rock.getX(), Rock.getY());

        Tuple<Integer> Tortuga = arr.get(5);
        if (isOccupied(Tortuga.getX(), Tortuga.getY()) || isPerceptionCell(Tortuga.getX(), Tortuga.getY())) {
            throw new IncorrectPlace("Tortuga invalid place");
        }
        setTortuga(Tortuga.getX(), Tortuga.getY());

        Tuple<Integer> Chest = arr.get(4);
        if (isOccupied(Chest.getX(), Chest.getY()) || isPerceptionCell(Chest.getX(), Chest.getY())) {
            throw new IncorrectPlace("Chest invalid place");
        }
        setChest(Chest.getX(), Chest.getY());
    }

    /**
     * This function check for validity of coordinates, if the coordinate in board boundaries returns true
     * otherwise false.
     *
     * @param x coordinate
     * @param y coordinate
     * @return true if (x, y) in board else false
     */
    boolean isValidCoordinates(int x, int y) {
        return 0 <= x && x < rows && 0 <= y && y < columns;
    }

    /**
     * Tests for ability to kill Kraken.
     *
     * @param x coordinate
     * @param y coordinate
     * @return true if in this cell is kraken "heart" (so, we can kill him) else false
     */
    boolean isKrakenHeart(int x, int y) {
        char piece = getPiece(x, y);
        return piece == Constants.KRAKEN_CELL ||
                piece == Constants.KRAKEN_WITH_ROCK;
    }

    /**
     * Tests for Kraken cells.
     *
     * @param x coordinate
     * @param y coordinate
     * @return true if in this cell is Kraken's perception zone or Kraken "heart" cell
     */
    boolean isKrakenCell(int x, int y) {
        char piece = getPiece(x, y);
        return piece == Constants.KRAKEN_CELL ||
                piece == Constants.KRAKEN;
    }

    /**
     * Checks is the cell is perception zone of Kraken or Davy Jones.
     *
     * @param x coordinate
     * @param y coordinate
     * @return true if perception zone otherwise false
     */
    boolean isPerceptionCell(int x, int y) {
        char piece = getPiece(x, y);
        return piece == Constants.KRAKEN || piece == Constants.DAVY;
    }

    /**
     * Checks if Jack can die in this cell.
     *
     * @param x coordinate
     * @param y coordinate
     * @return true if there is an enemy in this cell otherwise false
     */
    boolean isEnemy(int x, int y) {
        char element = board[x][y];
        if (element == Constants.KRAKEN) {
            return true;
        }
        if (element == Constants.KRAKEN_CELL) {
            return true;
        }
        if (element == Constants.KRAKEN_WITH_ROCK) {
            return true;
        }
        if (element == Constants.ROCK) {
            return true;
        }
        if (element == Constants.DAVY) {
            return true;
        }
        if (element == Constants.DAVY_CELL) {
            return true;
        }
        return false;
    }

    /**
     * Supporting function for random number generation in interval [0, max].
     *
     * @param max - maximum number in interval
     * @return rand int x, where x less or equal max
     */
    private static int randInRange(int max) {

        if (max < 0) {
            throw new IllegalArgumentException("Max must be greater or equals than 0");
        }

        Random r = new Random();
        return r.nextInt(max);
    }

    /**
     * Supporting function that generates random map that follows conditions of the task.
     */
    private void generateMap() {
        setPlayer(from_x, from_y);
        int x = randInRange(rows), y = randInRange(columns);

        while (isOccupied(x, y)) {
            x = randInRange(rows);
            y = randInRange(columns);
        }
        setKraken(x, y);

        // set Davy Jones
        while (isOccupied(x, y)) {
            x = randInRange(rows);
            y = randInRange(columns);
        }
        setDavy(x, y);

        // set rock
        while (isOccupied(x, y) && getPiece(x, y) != Constants.KRAKEN_CELL) {
            x = randInRange(rows);
            y = randInRange(columns);
        }
        setRock(x, y);

        // set tortuga
        while (isOccupied(x, y) || isPerceptionCell(x, y)) {
            x = randInRange(rows);
            y = randInRange(columns);
        }
        setTortuga(x, y);

        // set Chest
        while (isOccupied(x, y) || isPerceptionCell(x, y)) {
            x = randInRange(rows);
            y = randInRange(columns);
        }
        setChest(x, y);
    }

    /**
     * Test for a free cell (not a danger zone).
     *
     * @param x coordinate
     * @param y coordinate
     * @return true if cell can be use for placing else false
     */
    private boolean isOccupied(int x, int y) {
        Character piece = getPiece(x, y);
        if (piece == null) {
            return false;
        }
        if (piece == Constants.KRAKEN || piece == Constants.DAVY || piece == Constants.BLANK)
            return false;
        return true;
    }

    /**
     * Function for setting Kraken, and it's perception zones in board.
     *
     * @param x coordinate
     * @param y coordinate
     */
    public void setKraken(int x, int y) {
        ArrayList<Tuple<Integer>> shifts = new ArrayList<>();
        shifts.add(new Tuple<>(1, 0));
        shifts.add(new Tuple<>(-1, 0));
        shifts.add(new Tuple<>(0, 1));
        shifts.add(new Tuple<>(0, -1));

        char setChar = Constants.KRAKEN_CELL;
        if (board[x][y] == Constants.ROCK) {
            setChar = Constants.KRAKEN_WITH_ROCK;
        }
        board[x][y] = setChar;
        setObj(x, y, shifts, Constants.KRAKEN);
    }

    /**
     * Function for set Rock zones in board.
     *
     * @param x coordinate
     * @param y coordinate
     */
    public void setRock(int x, int y) {
        ArrayList<Tuple<Integer>> shifts = new ArrayList<>();
        shifts.add(new Tuple<>(0, 0));

        char setChar = Constants.ROCK;
        if (board[x][y] == Constants.KRAKEN_CELL) {
            setChar = Constants.KRAKEN_WITH_ROCK;
        }
        setObj(x, y, shifts, setChar);
    }

    /**
     * Function for set Tortuga in board.
     *
     * @param x coordinate
     * @param y coordinate
     */
    public void setTortuga(int x, int y) {
        ArrayList<Tuple<Integer>> shifts = new ArrayList<>();
        shifts.add(new Tuple<>(0, 0));
        tortuga_x = x;
        tortuga_y = y;
        setObj(x, y, shifts, Constants.TORTUGA);
    }

    /**
     * Function for set Dead Man’s Chest in board.
     *
     * @param x coordinate
     * @param y coordinate
     */
    public void setChest(int x, int y) {
        finish_x = x;
        finish_y = y;
        ArrayList<Tuple<Integer>> shifts = new ArrayList<>();
        shifts.add(new Tuple<>(0, 0));

        setObj(x, y, shifts, Constants.CHEST);
    }

    /**
     * Function for setting Davy, and it's perception zones in board.
     *
     * @param x coordinate
     * @param y coordinate
     */
    public void setDavy(int x, int y) {
        ArrayList<Tuple<Integer>> shifts = new ArrayList<>();
        shifts.add(new Tuple<>(1, 0));
        shifts.add(new Tuple<>(-1, 0));
        shifts.add(new Tuple<>(0, 1));
        shifts.add(new Tuple<>(0, -1));

        shifts.add(new Tuple<>(1, 1));
        shifts.add(new Tuple<>(-1, 1));
        shifts.add(new Tuple<>(-1, -1));
        shifts.add(new Tuple<>(1, -1));

        board[x][y] = Constants.DAVY_CELL;

        for (Tuple<Integer> shift : shifts) {
            int new_x = x + shift.getX();
            int new_y = y + shift.getY();
            if (isValidCoordinates(new_x, new_y) && !isKrakenHeart(new_x, new_y)) {
                board[new_x][new_y] = Constants.DAVY;
            }
        }
    }

    /**
     * Function for set Jack Sparrow in board.
     *
     * @param x coordinate
     * @param y coordinate
     */
    public void setPlayer(int x, int y) {
        from_x = x;
        from_y = y;
        board[from_x][from_y] = Constants.PLAYER;
    }

    /**
     * Sets symbol SYM on board with shifts with respect to x, y.
     *
     * @param x      coordinate
     * @param y      coordinate
     * @param shifts array of shifts with respect to x and y for setting symbols
     * @param sym    to set
     */
    private void setObj(int x, int y, ArrayList<Tuple<Integer>> shifts, char sym) {
        for (Tuple<Integer> shift : shifts) {
            int new_x = x + shift.getX();
            int new_y = y + shift.getY();
            if (isValidCoordinates(new_x, new_y)) {
                board[new_x][new_y] = sym;
            }
        }
    }

    /**
     * Function that prints the board
     */
    public void printBoard() {
        System.out.print("  ");
        for (int i = 0; i < rows; i++) {
            System.out.printf("%d ", i);
        }
        System.out.print("\n");
        for (int i = 0; i < rows; i++) {
            System.out.printf("%d ", i);
            for (int j = 0; j < columns; j++) {
                System.out.printf("%c ", board[i][j]);
            }
            System.out.print("\n");
        }
    }
}

/**
 * Abstract class for the Search Algorithm, that has method that solves the problem when Jack starts from (from_x, from_y)
 * and wants to go to (finish_x, finish_y) using shifts on a given board.
 * Also, we can reconstruct the path using the method getPath.
 */
abstract class SearchAlgorithm {
    /**
     * (from_x, from_y) - position from where Jack starts
     * (finish_x, finish_y) - position to where Jack goes
     */
    int from_x, from_y, finish_x, finish_y;

    /**
     * array of shift for Jack movement with respect to (x, y)
     */
    ArrayList<Tuple<Integer>> shifts;

    /**
     * Board with actors
     */
    Board board;

    /**
     * Two-dimensional array in cell map[x][y] holds the shortest path to this coordinate
     */
    int[][] map;

    /**
     * variable that holds amount of time for algorithm execution
     */
    long amountTimeForExecution;

    /**
     * min path length after execution of algorithm
     */
    int pathLength;

    /**
     * array that holds coordinates for path
     */
    ArrayList<Tuple<Integer>> path;

    /**
     * Function that solves the problem when Jack starts from (from_x, from_y).
     *
     * @param from_x   coordinate
     * @param from_y   coordinate
     * @param finish_x coordinate
     * @param finish_y coordinate
     * @param shifts   array of shift for Jack movement with respect to (x, y)
     * @param board    for which we solve the problem
     * @return instance of Result class
     */
    abstract Result solve(int from_x, int from_y, int finish_x, int finish_y, ArrayList<Tuple<Integer>> shifts, Board board);

    /**
     * Function for reconstruct the path.
     *
     * @param from_x   coordinate
     * @param from_y   coordinate
     * @param finish_x coordinate
     * @param finish_y coordinate
     * @param map      dwo dimensional array where in cell with coordinate (x, y) written the minimal amount of steps that needed from (from_x, from_y) to be in
     * @param shifts   array of shift for Jack movement with respect to (x, y)
     * @return tuple of coordinates which make the path
     */
    ArrayList<Tuple<Integer>> getPath(int from_x, int from_y, int finish_x, int finish_y, int[][] map, ArrayList<Tuple<Integer>> shifts) {
        ArrayList<Tuple<Integer>> ans = new ArrayList<>();
        int x = finish_x;
        int y = finish_y;

        while (x != from_x || y != from_y) {
            boolean flag = false;
            for (Tuple<Integer> shift : shifts) {
                int new_x = x + shift.getX();
                int new_y = y + shift.getY();

                if (!(0 <= new_x && new_x < map.length && 0 <= new_y && new_y < map[0].length)) {
                    continue;
                }

                if (map[new_x][new_y] + 1 == map[x][y]) {
                    ans.add(0, new Tuple<>(x, y));
                    x = new_x;
                    y = new_y;
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                return null;
            }
        }
        ans.add(0, new Tuple<>(from_x, from_y));
        return ans;
    }

    //    default tryKillKraken()

    /**
     * Getter for path length
     *
     * @return path length
     */
    abstract int getPathLength();
}

/**
 * Supporting class for returning values from Search algorithm.
 */
class Result {
    /**
     * min path length after execution of algorithm
     */
    private final int pathLength;

    /**
     * array that holds coordinates for path
     */
    private final ArrayList<Tuple<Integer>> path;

    /**
     * variable that holds amount of time of algorithm execution
     */
    private final long amountTimeForExecution;

    /**
     * Constructor for Result class that just create an instance of it
     *
     * @param pathLength             - min path length after execution of algorithm
     * @param path                   - array that holds coordinates for path
     * @param amountTimeForExecution - variable that holds amount of time of algorithm execution
     */
    Result(int pathLength, ArrayList<Tuple<Integer>> path, long amountTimeForExecution) {
        this.pathLength = pathLength;
        this.path = path;
        this.amountTimeForExecution = amountTimeForExecution;
    }

    /**
     * Getter for path length.
     *
     * @return path length
     */
    int getPathLength() {
        return pathLength;
    }

    /**
     * Getter for array of path coordinates.
     *
     * @return array of path
     */
    ArrayList<Tuple<Integer>> getPath() {
        return path;
    }

    /**
     * Getter for amount time for execution of algorithm.
     *
     * @return amount of time
     */
    long getAmountTimeForExecution() {
        return amountTimeForExecution;
    }
}

/**
 * Implementation of A* algorithm
 */
class AStar extends SearchAlgorithm {
    /**
     * Node for priority queue that can be compared by f = g + h and h, where h - heuristics, g - amount of steps from begin.
     * Also, node has state inside.
     */
    static private class Node implements Comparable {
        /**
         * Current state
         */
        State state;

        /**
         * f = g + h
         * g - amount of steps from begin
         * h - heuristics
         */
        int g, h, f;

        /**
         * Constructor for Node class.
         *
         * @param state current
         * @param g     amount of steps from start coordinate
         * @param h     heuristics
         */
        Node(State state, int g, int h) {
            f = g + h;
            this.g = g;
            this.h = h;
            this.state = state;
        }

        @Override
        public int compareTo(Object o) {
            Node other = (Node) o;
            if (f != other.f)
                return (f - other.f);
            return h - other.h;
        }
    }

    /**
     * Getter for path length.
     *
     * @return INF if there is no path else path length
     */
    public int getPathLength() {
        return pathLength;
    }

    /**
     * Heuristic function that count straight path between to coordinates.
     *
     * @param from_x   coordinate
     * @param from_y   coordinate
     * @param finish_x coordinate
     * @param finish_y coordinate
     * @return length of straight path between to coordinates
     */
    int h(int from_x, int from_y, int finish_x, int finish_y) {
        int diag = min(abs(finish_x - from_x), abs(finish_y - from_y));
        return diag + abs(abs(from_x - finish_x) - abs(from_y - finish_y));
    }

    /**
     * Caller for function that solves the problem when Jack starts from (from_x, from_y) to (finish_x, finish_y).
     *
     * @param from_x   coordinate
     * @param from_y   coordinate
     * @param finish_x coordinate
     * @param finish_y coordinate
     * @param shifts   array of shift for Jack movement with respect to (x, y)
     * @param board    for which we solve the problem
     * @return instance of Result class
     */
    public Result solve(int from_x, int from_y, int finish_x, int finish_y, ArrayList<Tuple<Integer>> shifts, Board board) {
        long startTime = System.nanoTime();
        this.from_x = from_x;
        this.from_y = from_y;
        this.finish_x = finish_x;
        this.finish_y = finish_y;
        this.shifts = shifts;
        this.board = board;
        map = new int[board.rows][board.columns];
        for (int i = 0; i < board.rows; i++) {
            for (int j = 0; j < board.columns; j++) {
                map[i][j] = Constants.INF;
            }
        }

        solvePrivate();
        pathLength = map[finish_x][finish_y];
        if (pathLength != Constants.INF) {
            path = getPath(this.from_x, this.from_y, this.finish_x, this.finish_y, this.map, this.shifts);
        }

        long endTime = System.nanoTime();
        amountTimeForExecution = (endTime - startTime);
        return new Result(pathLength, path, amountTimeForExecution);
    }

    /**
     * Private solve function that is called by method solve.
     */
    public void solvePrivate() {
        map[from_x][from_y] = 0;
        PriorityQueue<Node> q = new PriorityQueue<>();

        q.add(new Node(new State(from_x, from_y), 0, h(from_x, from_y, finish_x, finish_y)));
        while (q.size() != 0) {
            Node cur = q.poll();
            State state = cur.state;
            int current_x = state.getX();
            int current_y = state.getY();
            if (current_x == board.tortuga_x && current_y == board.tortuga_y) {
                state.cask = true;
            }
            if (current_x == finish_x && current_y == finish_y) {
                return;
            }

            if (!state.krakenIsDead && state.cask) {
                for (Tuple<Integer> shift : shifts) {
                    int new_x = current_x + shift.getX();
                    int new_y = current_y + shift.getY();
                    if (!board.isValidCoordinates(new_x, new_y)) {
                        continue;
                    }
                    if (board.isKrakenHeart(new_x, new_y) && state.cask) {
                        state.krakenIsDead = true;
                        break;
                    }
                }
            }

            for (Tuple<Integer> shift : shifts) {
                int new_x = current_x + shift.getX();
                int new_y = current_y + shift.getY();

                if (!board.isValidCoordinates(new_x, new_y)) {
                    continue;
                }

                if (!((board.isKrakenCell(new_x, new_y) && state.krakenIsDead) || !board.isEnemy(new_x, new_y))) {
                    continue;
                }

                if (map[new_x][new_y] > state.pathLength + 1) {
                    map[new_x][new_y] = state.pathLength + 1;
                    q.add(new Node(state.move(new_x, new_y), state.pathLength + 1, h(new_x, new_y, finish_x, board.finish_y)));
                }
            }
        }
    }
}

/**
 * Implementation of Backtracking algorithm.
 */
class BackTracking extends SearchAlgorithm {
    /**
     * Caller for function that solves the problem when Jack starts from (from_x, from_y) to (finish_x, finish_y).
     *
     * @param from_x   coordinate
     * @param from_y   coordinate
     * @param finish_x coordinate
     * @param finish_y coordinate
     * @param shifts   array of shift for Jack movement
     * @param board    for which we solve the problem
     * @return instance of Result class
     */
    public Result solve(int from_x, int from_y, int finish_x, int finish_y, ArrayList<Tuple<Integer>> shifts, Board board) {
        long startTime = System.nanoTime();
        this.from_x = from_x;
        this.from_y = from_y;
        this.finish_x = finish_x;
        this.finish_y = finish_y;
        this.shifts = shifts;
        this.board = board;

        map = new int[board.rows][board.columns];

        for (int i = 0; i < board.rows; i++) {
            for (int j = 0; j < board.columns; j++) {
                map[i][j] = Constants.INF;
            }
        }
        State start = new State(from_x, from_y);
        backTrackingSearch(start);
        pathLength = map[finish_x][finish_y];
        if (pathLength != Constants.INF) {
            path = getPath(this.from_x, this.from_y, this.finish_x, this.finish_y, this.map, this.shifts);
        }
        long endTime = System.nanoTime();
        amountTimeForExecution = endTime - startTime;
        return new Result(pathLength, path, amountTimeForExecution);
    }

    /**
     * Private recursive function that solves the problem.
     *
     * @param state start state of algorithm
     */
    private void backTrackingSearch(State state) {
        int current_x = state.getX();
        int current_y = state.getY();

        if (current_x == finish_x && current_y == finish_y) {
            map[current_x][current_y] = min(map[current_x][current_y], state.pathLength);
            return;
        }

        if (map[current_x][current_y] > state.pathLength) {
            map[current_x][current_y] = state.pathLength;
        } else {
            return;
        }

        if (board.getPiece(current_x, current_y) == Constants.TORTUGA) {
            state.cask = true;
        }

        if (!state.krakenIsDead && state.cask) {
            for (Tuple<Integer> shift : shifts) {
                int new_x = current_x + shift.getX();
                int new_y = current_y + shift.getY();
                if (!board.isValidCoordinates(new_x, new_y)) {
                    continue;
                }
                if (board.isKrakenHeart(new_x, new_y) && state.cask) {
                    state.krakenIsDead = true;
                    break;
                }
            }
        }

        for (Tuple<Integer> shift : shifts) {
            int new_x = current_x + shift.getX();
            int new_y = current_y + shift.getY();

            if (!board.isValidCoordinates(new_x, new_y)) {
                continue;
            }

            if ((board.isKrakenCell(new_x, new_y) && state.krakenIsDead) || !board.isEnemy(new_x, new_y)) {
                if (map[new_x][new_y] > state.pathLength + 1) {
                    backTrackingSearch(state.move(new_x, new_y));
                }

            }
        }
    }

    /**
     * Getter for path length.
     *
     * @return path length
     */
    @Override
    public int getPathLength() {
        return pathLength;
    }
}

/**
 * Entry point class of program.
 */
public class Main {
    //    FileWriter fileWriter;
//    PrintWriter printWriter;

    /**
     * Supporting function for printing map.
     *
     * @param map 2 dimensional array
     */
    private void printMap(char[][] map) {
        System.out.print("-------------------\n");
        System.out.print("  ");
        for (int i = 0; i < map.length; i++) {
            System.out.printf("%d ", i);
        }
        System.out.print("\n");
        for (int i = 0; i < map.length; i++) {
            System.out.printf("%d ", i);
            for (int j = 0; j < map[0].length; j++) {
                System.out.printf("%c ", map[i][j]);
            }
            System.out.print("\n");
        }
        System.out.print("-------------------\n");
    }

    /**
     * Supporing function for generation answer map with path.
     *
     * @param path    - array of coordinates of path
     * @param rows    - amount of rows
     * @param columns - amount of columns
     * @return new generated map with path
     */
    private char[][] getPathMap(ArrayList<Tuple<Integer>> path, int rows, int columns) {
        char[][] ans_map = new char[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                ans_map[i][j] = Constants.BLANK;
            }
        }
        for (Tuple<Integer> integerTuple : path) {
            int x = integerTuple.getX();
            int y = integerTuple.getY();
            ans_map[x][y] = Constants.PATH;
        }
        return ans_map;
    }

    /**
     * Method that solves the problem with some board and search algorithm.
     * We solve problem in two ways:
     * 1 - direct path to Dead Men's chest
     * 2 - go to Tortuga and then to Dead Men's chest
     * This function executes in two ways and chose the way with the least path length.
     *
     * @param board     - arbitrary board
     * @param algorithm - Backtracking or A* instance of class
     */
    void solve(Board board, SearchAlgorithm algorithm) {
//        try {
//            if(algorithm instanceof AStar) {
//                fileWriter = new FileWriter("AStar_final_final.txt", true);
//                printWriter = new PrintWriter(fileWriter, true);
//            }
//            else {
//                fileWriter = new FileWriter("BackTrack_final_final.txt", true);
//                printWriter = new PrintWriter(fileWriter, true);
//            }
//        }
//         catch (Exception e) {
//            e.printStackTrace();
//         }
//
        ArrayList<Tuple<Integer>> shifts = new ArrayList<>();

        shifts.add(new Tuple<>(1, 0));
        shifts.add(new Tuple<>(-1, 0));
        shifts.add(new Tuple<>(0, 1));
        shifts.add(new Tuple<>(0, -1));

        shifts.add(new Tuple<>(1, 1));
        shifts.add(new Tuple<>(-1, 1));
        shifts.add(new Tuple<>(-1, -1));
        shifts.add(new Tuple<>(1, -1));

        // direct path to chest
        Result direct = algorithm.solve(board.from_x, board.from_y, board.finish_x, board.finish_y, shifts, board);

        // through Tortuga
        Result toTortuga = algorithm.solve(board.from_x, board.from_y, board.tortuga_x, board.tortuga_y, shifts, board);
        Result fromTortugaToFinish = algorithm.solve(board.tortuga_x, board.tortuga_y, board.finish_x, board.finish_y, shifts, board);

        long ans_time = direct.getAmountTimeForExecution() + toTortuga.getAmountTimeForExecution() + fromTortugaToFinish.getAmountTimeForExecution();

        if (direct.getPathLength() == Constants.INF &&
                (toTortuga.getPathLength() == Constants.INF || fromTortugaToFinish.getPathLength() == Constants.INF)) {
            System.out.print("Lose\n");
//            printWriter.printf("%d L\n", ans_time);
            return;
        }


        ArrayList<Tuple<Integer>> ans_path;

        if (direct.getPathLength() < toTortuga.getPathLength() + fromTortugaToFinish.getPathLength()) {
            ans_path = direct.getPath();
        } else {
            ans_path = toTortuga.getPath();
            ans_path.remove(ans_path.size() - 1);
            ans_path.addAll(fromTortugaToFinish.getPath());
        }

        System.out.printf("Win\n%d\n", ans_path.size() - 1);
        for (Tuple<Integer> integerTuple : ans_path) {
            System.out.printf("[%d,%d] ", integerTuple.getX(), integerTuple.getY());
        }
        System.out.print("\n");
        char[][] pathMap = getPathMap(ans_path, board.rows, board.columns);
        printMap(pathMap);
        System.out.printf("%f ms\n", (double) ans_time / 1e6);
//        printWriter.printf("%d W\n", ans_time);
    }

    /**
     * Entry point function of program. Provide user interface, parsing for coordinates and perception scenario, validating input.
     *
     * @param args - arguments from command line (is not used in this program)
     * @throws FileNotFoundException if outputAStar.txt or outputBacktracking.txt if there are do not exist
     */
    public static void main(String[] args) throws FileNotFoundException {
        PrintStream stdout = System.out;
        Main m = new Main();
        Scanner in = new Scanner(System.in);
        int inputType = 3;
        do {
            System.out.print("Select option for map:\n1 - for random generation\n2 - read from input.txt\n");
            System.out.print("> ");
            try {
                inputType = in.nextInt();
            } catch (InputMismatchException e) {
                System.out.print("Error: Not a number\n");
                continue;
            }

            if (inputType != 1 && inputType != 2) {
                System.out.print("Error: Incorrect input\n");
            }
        } while (inputType != 1 && inputType != 2);
        int perceptionScenario;
        if (inputType == 1) {
            do {
                System.out.print("Enter perception scenario from console (1 or 2):\n");
                System.out.print("> ");
                perceptionScenario = in.nextInt();
                if (perceptionScenario != 1 && perceptionScenario != 2) {
                    System.out.print("Error: Incorrect input\n");
                }
            } while (perceptionScenario != 1 && perceptionScenario != 2);
            Board board = new Board(9, 9);

            System.setOut(new PrintStream("outputAStar.txt"));
            m.solve(board, new AStar());
            System.setOut(new PrintStream("outputBacktracking.txt"));
            m.solve(board, new BackTracking());
            System.setOut(stdout);

            return;
        }
        Scanner scannerInput;
        try {
            scannerInput = new Scanner(new File("input.txt"));
        } catch (IOException e) {
            System.out.print("Error: No input.txt file");
            return;
        }

        String coords = scannerInput.nextLine();
        if (!Pattern.matches("^(\\[\\d,\\d]\\s){5}\\[\\d,\\d]$", coords)) {
            System.out.print("Error: Wrong coordinates in input.txt\n");
            return;
        }
        try {
            perceptionScenario = scannerInput.nextInt();
            if (!(perceptionScenario == 1 || perceptionScenario == 2)) {
                throw new InputMismatchException();
            }
        } catch (InputMismatchException e) {
            System.out.print("Error: Wrong perception scenario in input.txt\n");
            return;
        }
        boolean endOfFile = false;
        try {
            scannerInput.nextLine();
        } catch (NoSuchElementException e) {
            endOfFile = true;
        }
        if (!endOfFile) {
            System.out.print("Error: File contains more than 2 lines\n");
            return;
        }

        ArrayList<Tuple<Integer>> coordinates = new ArrayList<>();
        for (String token : coords.split(" ")) {
            token = token.replace("[", "");
            token = token.replace("]", "");

            String[] digits = token.split(",");
            coordinates.add(new Tuple<>(Integer.parseInt(digits[0]), Integer.parseInt(digits[1])));
        }

        Board board;
        try {
            board = new Board(9, 9, coordinates);
        } catch (IncorrectPlace e) {
            System.out.print(e.getMessage());
            return;
        }
        board.printBoard();
        System.setOut(new PrintStream("outputAStar.txt"));
        m.solve(board, new AStar());
        System.setOut(new PrintStream("outputBacktracking.txt"));
        m.solve(board, new BackTracking());
        System.setOut(stdout);
    }

}
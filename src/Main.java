import java.io.*;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.zip.CheckedInputStream;

import static java.lang.Math.*;

final class Constants {
    static public char BLANK = '-';
    static public char KRAKEN = 'K';
    static public char KRAKEN_CELL = 'O';
    static public char KRAKEN_WITH_ROCK = 'W';
    static public char ROCK = 'R';
    static public char TORTUGA = 'T';
    static public char CHEST = 'C';
    static public char DAVY = 'D';
    static public char DAVY_CELL = 'A';
    static public char PLAYER = 'J';
    static public char PATH = '*';
    static public int INF = (int) 1e9;
}

class Tuple<T> {
    private final T x;
    private final T y;

    Tuple(T x, T y) {
        this.x = x;
        this.y = y;
    }

    T getX() {
        return x;
    }

    T getY() {
        return y;
    }

    void pprint() {
        System.out.println(x);
        System.out.println(y);
    }
}

class State {
    public int current_x;
    public int current_y;
    int path_length;
    public boolean cask = false;
    public boolean kraken_is_dead = false;

    State(int current_x, int current_y) {
        this.current_x = current_x;
        this.current_y = current_y;
        path_length = 0;
    }

    State(int current_x, int current_y, boolean cask) {
        this.current_x = current_x;
        this.current_y = current_y;
        this.cask = cask;
        path_length = 0;
    }

    State(int current_x, int current_y, boolean cask, boolean kraken_is_dead, int path_length) {
        this(current_x, current_y, cask);
        this.kraken_is_dead = kraken_is_dead;
        this.path_length = path_length;
    }

    State move(int x, int y) {
        return new State(x, y, this.cask, this.kraken_is_dead, this.path_length + 1);
    }
}

class Board {
    public int rows;
    public int columns;
    private final char[][] board;

    public int finish_x;
    public int finish_y;
    public int from_x;
    public int from_y;
    public int tortuga_x;
    public int tortuga_y;

    Character getPiece(int x, int y) {
        if (isValidCoordinates(x, y)) {
            return board[x][y];
        }
        return null;
    }

    Board(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        board = new char[rows][columns];
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                board[i][j] = Constants.BLANK;
            }
        }

//        createSampleBoard4();
        generateMap();
    }

    boolean isValidCoordinates(int x, int y) {
        return 0 <= x && x < rows && 0 <= y && y < columns;
    }

    /**
     * Tests for ability to kill Kraken
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
     * Tests for Kraken cell
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
        return false;
//        return element == Constants.KRAKEN_CELL || element == Constants.KRAKEN || element == Constants;
    }

    private static int randInRange(int max) {

        if (max < 0) {
            throw new IllegalArgumentException("Max must be greater or equals than 0");
        }

        Random r = new Random();
        return r.nextInt(max);
    }

    void generateMap() {
        setPlayer(randInRange(rows), randInRange(columns));
        int x = randInRange(rows), y = randInRange(columns);

        while (isOccupied(x, y)) {
            x = randInRange(rows);
            y = randInRange(columns);
        }
        setTortuga(x, y);

        while (isOccupied(x, y)) {
            x = randInRange(rows);
            y = randInRange(columns);
        }
        setChest(x, y);

        ArrayList<Tuple<Integer>> shifts = new ArrayList<>();
        shifts.add(new Tuple<>(1, 0));
        shifts.add(new Tuple<>(0, 0));
        shifts.add(new Tuple<>(-1, 0));
        shifts.add(new Tuple<>(0, 1));
        shifts.add(new Tuple<>(0, -1));

        // set kraken
        boolean coordinateFound;
        do {
            coordinateFound = true;
            x = randInRange(rows);
            y = randInRange(columns);

            for (Tuple<Integer> shift : shifts) {
                if (isOccupied(x + shift.getX(), y + shift.getY())) {
                    coordinateFound = false;
                    break;
                }
            }
        }
        while (!coordinateFound);

        setKraken(x, y);

        shifts.add(new Tuple<>(1, 1));
        shifts.add(new Tuple<>(-1, 1));
        shifts.add(new Tuple<>(-1, -1));
        shifts.add(new Tuple<>(1, -1));
        do {
            coordinateFound = true;
            x = randInRange(rows);
            y = randInRange(columns);

            for (Tuple<Integer> shift : shifts) {
                if (isOccupied(x + shift.getX(), y + shift.getY())) {
                    coordinateFound = false;
                    break;
                }
            }
        }
        while (!coordinateFound);
        setDevy(x, y);

        while (isOccupied(x, y) && getPiece(x, y) != Constants.KRAKEN_CELL) { x = randInRange(rows); y = randInRange(columns); }

        setRock(x, y);
    }

    private boolean isOccupied(int x, int y) {
        Character piece = getPiece(x, y);
        if (piece == null) {
            return false;
        }
        if (piece == Constants.KRAKEN || piece == Constants.DAVY || piece == Constants.BLANK)
            return false;
        return true;
    }

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

    public void setRock(int x, int y) {
        ArrayList<Tuple<Integer>> shifts = new ArrayList<>();
        shifts.add(new Tuple<>(0, 0));

        char setChar = Constants.ROCK;
        if (board[x][y] == Constants.KRAKEN_CELL) {
            setChar = Constants.KRAKEN_WITH_ROCK;
        }
        setObj(x, y, shifts, setChar);
    }

    public void setTortuga(int x, int y) {
        ArrayList<Tuple<Integer>> shifts = new ArrayList<>();
        shifts.add(new Tuple<>(0, 0));
        tortuga_x = x;
        tortuga_y = y;
        setObj(x, y, shifts, Constants.TORTUGA);
    }

    public void setChest(int x, int y) {
        finish_x = x;
        finish_y = y;
        ArrayList<Tuple<Integer>> shifts = new ArrayList<>();
        shifts.add(new Tuple<Integer>(0, 0));

        setObj(x, y, shifts, Constants.CHEST);
    }

    public void setDevy(int x, int y) {
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

        setObj(x, y, shifts, Constants.DAVY);
    }

    public void setPlayer(int x, int y) {
        from_x = x;
        from_y = y;
        board[from_x][from_y] = Constants.PLAYER;
    }

    // Mark: supporting functions
    private void setObj(int x, int y, ArrayList<Tuple<Integer>> shifts, char sym) {
        for (Tuple<Integer> shift : shifts) {
            int new_x = x + shift.getX();
            int new_y = y + shift.getY();
            if (isValidCoordinates(new_x, new_y)) {
                board[new_x][new_y] = sym;
            }
        }
    }

    void createSampleBoard() { // given example
        this.setPlayer(0, 0);
        this.setDevy(4, 7);
        this.setChest(8, 7);
        this.setKraken(3, 2);
        this.setRock(6, 4);
        this.setTortuga(0, 6);
    }

    void createSampleBoard1() { // Tortuga (1, 1)
        this.setPlayer(0, 0);
        this.setDevy(4, 7);
        this.setChest(8, 7);
        this.setKraken(2, 2);
        this.setRock(6, 4);
        this.setTortuga(1, 1);
    }

    void createSampleBoard2() { // only through tortuga
        this.setPlayer(0, 0);
        this.setChest(8, 7);
        this.setKraken(7, 5);
        this.setDevy(6, 7);
        this.setRock(6, 4);
        this.setTortuga(0, 6);
    }

    void createSampleBoard3() { // no path
        this.setPlayer(0, 0);

        this.setChest(8, 7);
        this.setKraken(7, 5);
        this.setDevy(6, 7);
        this.setRock(6, 4);
        this.setTortuga(8, 8);
    }

    void createSampleBoard4() { // no path
        this.setPlayer(7, 0);
        this.setTortuga(4, 0);

        this.setChest(0, 7);

        this.setKraken(0, 5);
        this.setDevy(3, 6);
        this.setRock(6, 6);

    }

    public void printBoard() {
        System.out.printf("  ");
        for (int i = 0; i < rows; i++) {
            System.out.printf("%d ", i);
        }
        System.out.printf("\n");
        for (int i = 0; i < rows; i++) {
            System.out.printf("%d ", i);
            for (int j = 0; j < columns; j++) {
                System.out.printf("%c ", board[i][j]);
            }
            System.out.printf("\n");
        }
    }
}

interface SearchAlgorithm {
    Result solve(int from_x, int from_y, int finish_x, int finish_y, ArrayList<Tuple<Integer>> shifts, Board board);

    default ArrayList<Tuple<Integer>> getPath(int from_x, int from_y, int finish_x, int finish_y, int[][] map, ArrayList<Tuple<Integer>> shifts) {
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
    int getPathLength();
}

class Result {
    private final int pathLength;
    private final ArrayList<Tuple<Integer>> path;
    private long amountTimeForExecution;

    Result(int pathLength, ArrayList<Tuple<Integer>> path, long amountTimeForExecution) {
        this.pathLength = pathLength;
        this.path = path;
        this.amountTimeForExecution = amountTimeForExecution;
    }

    int getPathLength() {
        return pathLength;
    }

    ArrayList<Tuple<Integer>> getPath() {
        return path;
    }

    long getAmountTimeForExecution() {
        return amountTimeForExecution;
    }
}

class AStar implements SearchAlgorithm {
    int from_x, from_y, finish_x, finish_y;
    ArrayList<Tuple<Integer>> shifts;
    Board board;
    int[][] map;
    char[][] pathMap;
    long amountTimeForExecution;

    private int pathLength;

    ArrayList<Tuple<Integer>> path;

    static class Node implements Comparable {
        State state;
        int g, h, f;

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

    public int getPathLength() {
        return pathLength;
    }

    int h(int from_x, int from_y, int finish_x, int finish_y) {
        int f_x = from_x, f_y = from_y;
        int diag = min(abs(finish_x - from_x), abs(finish_y - from_y));

        if (f_x < finish_x && f_y < finish_y) {
            f_x += diag;
            f_y += diag;
        }

        if (f_x > finish_x && f_y < finish_y) {
            f_x -= diag;
            f_y += diag;
        }
        if (f_x < finish_x && f_y > finish_y) {
            f_x += diag;
            f_y -= diag;
        }
        if (f_x > finish_x && f_y > finish_y) {
            f_x -= diag;
            f_y -= diag;
        }

        return diag + max(abs(finish_x - f_x), abs(finish_y - f_y));
    }

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

    public void solvePrivate() {
        map[from_x][from_y] = 0;
        PriorityQueue<Node> q = new PriorityQueue<>();

        q.add(new Node(new State(from_x, from_y), 0, h(from_x, from_y, finish_x, finish_y)));
        while (q.size() != 0) {
            Node cur = q.poll();
            State state = cur.state;
            int current_x = state.current_x;
            int current_y = state.current_y;
            if (current_x == board.tortuga_x && current_y == board.tortuga_y) {
                state.cask = true;
            }
            if (current_x == finish_x && current_y == finish_y) {
                return;
            }

            if (!state.kraken_is_dead && state.cask) {
                for (int i = 0; i < shifts.size(); i++) {
                    int new_x = current_x + shifts.get(i).getX();
                    int new_y = current_y + shifts.get(i).getY();
                    if (!board.isValidCoordinates(new_x, new_y)) {
                        continue;
                    }
                    if (board.isKrakenHeart(new_x, new_y) && state.cask) {
                        state.kraken_is_dead = true;
                        break;
                    }
                }
            }

            for (int i = 0; i < shifts.size(); i++) {
                int new_x = current_x + shifts.get(i).getX();
                int new_y = current_y + shifts.get(i).getY();

                if (!board.isValidCoordinates(new_x, new_y)) {
                    continue;
                }

                if (!((board.isKrakenCell(new_x, new_y) && state.kraken_is_dead) || !board.isEnemy(new_x, new_y))) {
                    continue;
                }

                if (map[new_x][new_y] > state.path_length + 1) {
                    map[new_x][new_y] = state.path_length + 1;
                    q.add(new Node(state.move(new_x, new_y), state.path_length + 1, h(new_x, new_y, finish_x, board.finish_y)));
                }
            }
        }
    }
}

class BackTracking implements SearchAlgorithm {
    int from_x, from_y, finish_x, finish_y;
    ArrayList<Tuple<Integer>> shifts;
    Board board;
    int[][] map;
    private int pathLength;
    ArrayList<Tuple<Integer>> path;
    long amountTimeForExecution;

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

    private void backTrackingSearch(State state) {
        int current_x = state.current_x;
        int current_y = state.current_y;

        if (current_x == finish_x && current_y == finish_y) {
            map[current_x][current_y] = min(map[current_x][current_y], state.path_length);
            return;
        }

        if (map[current_x][current_y] > state.path_length) {
            map[current_x][current_y] = state.path_length;
        } else {
            return;
        }

        if (board.getPiece(current_x, current_y) == Constants.TORTUGA) {
            state.cask = true;
        }

        if (!state.kraken_is_dead && state.cask) {
            for (Tuple<Integer> shift : shifts) {
                int new_x = current_x + shift.getX();
                int new_y = current_y + shift.getY();
                if (!board.isValidCoordinates(new_x, new_y)) {
                    continue;
                }
                if (board.isKrakenHeart(new_x, new_y) && state.cask) {
                    state.kraken_is_dead = true;
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

            if ((board.isKrakenCell(new_x, new_y) && state.kraken_is_dead) || !board.isEnemy(new_x, new_y)) {
                if (map[new_x][new_y] > state.path_length + 1) {
                    backTrackingSearch(state.move(new_x, new_y));
                }

            }
        }
    }

    @Override
    public int getPathLength() {
        return pathLength;
    }
}

public class Main {
    void printMap(char[][] map) {
        System.out.printf("-------------------\n");
        System.out.printf("  ");
        for (int i = 0; i < map.length; i++) {
            System.out.printf("%d ", i);
        }
        System.out.printf("\n");
        for (int i = 0; i < map.length; i++) {
            System.out.printf("%d ", i);
            for (int j = 0; j < map[0].length; j++) {
                System.out.printf("%c ", map[i][j]);
            }
            System.out.printf("\n");
        }
        System.out.printf("-------------------\n");
    }

    char[][] getPathMap(ArrayList<Tuple<Integer>> path, int rows, int columns) {
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

    void solve(Board board, SearchAlgorithm algorithm) {
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

        Long ans_time = direct.getAmountTimeForExecution() + toTortuga.getAmountTimeForExecution() + fromTortugaToFinish.getAmountTimeForExecution();

        if (direct.getPathLength() == Constants.INF &&
                (toTortuga.getPathLength() == Constants.INF || fromTortugaToFinish.getPathLength() == Constants.INF)) {
            System.out.printf("Lose\n");
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
        for (int i = 0; i < ans_path.size(); i++) {
            System.out.printf("[%d,%d] ", ans_path.get(i).getX(), ans_path.get(i).getY());
        }
        System.out.printf("\n");
        char[][] pathMap = getPathMap(ans_path, board.rows, board.columns);
        printMap(pathMap);
        System.out.printf("%d nanosec\n", ans_time);
    }

    public static void main(String[] args) {
        PrintStream stdout = System.out;
        var fileAstar = new File("AstarTimes.txt");
        var fileBackTrack = new File("BackTrackTimes.txt");
//        try {
//            System.setOut(new PrintStream());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        Main m = new Main();

        Board board = new Board(9, 9);
//
        board.printBoard();
        m.solve(board, new AStar());

//            try {
//                System.setOut(new PrintStream(fileBackTrack));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        m.solve(board, new BackTracking());
    }
}
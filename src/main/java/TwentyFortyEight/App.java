package TwentyFortyEight;

import processing.core.PApplet;
import processing.core.PFont;
import processing.event.MouseEvent;

import java.util.*;

public class App extends PApplet {

    public static int GRID_SIZE = 4;
    public static final int MAX_GRID_SIZE = 6;
    public static final int CELLSIZE = 100;          // Actual size = CELLSIZE - 2*CELL_BUFFER
    public static final int CELL_BUFFER = 6;
    public static final int UI_SIZE = 120;
    public static int WIDTH = GRID_SIZE * CELLSIZE;
    public static int HEIGHT = GRID_SIZE * CELLSIZE + UI_SIZE;
    public static final int FPS = 60;
    public static final float ANIM_SPEED = 0.5f;

    public static final HashMap<Integer, int[]> color_map = new HashMap<>();

    private Cell[][] board;
    private PFont font;
    private int score = 0;
    private int best = 0;
    private boolean gameOver = false;
    private long startTime;
    private long stopTime = 0;
    private final float brightness_increase = 1.15f;
    private boolean wait_anim = false;
    private boolean moved = false;

    public static Random random = new Random();

    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                int board_size = Integer.parseInt(args[0]);
                if (board_size > 1 && board_size <= MAX_GRID_SIZE) {
                    GRID_SIZE = board_size;
                    WIDTH = GRID_SIZE * CELLSIZE;
                    HEIGHT = GRID_SIZE * CELLSIZE + UI_SIZE;
                } else {
                    System.err.println("Invalid board size: " + board_size + "; Board size set to 4");
                }
            } catch (Exception ignored) {
                System.err.println("Invalid board size; Board size set to 4");
            }
        }
        PApplet.main("TwentyFortyEight.App");
    }

    public void settings() {
        size(WIDTH, HEIGHT);
    }

    // Make a new board
    private Cell[][] createBoard() {
        board = new Cell[GRID_SIZE][GRID_SIZE];
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                board[y][x] = new Cell(x, y);
            }
        }
        randomCell();
        randomCell();
        return board;
    }

    // Cell colors
    static {
        color_map.put(-8, new int[]{117, 100, 82});         // 2,4 text
        color_map.put(-7, new int[]{255, 255, 255});        // 8, 16, ... text
        color_map.put(-6, new int[]{255, 0, 0});            // game over
        color_map.put(-5, new int[]{152, 136, 118});        // score
        color_map.put(-4, new int[]{205, 193, 180});        // Best cell
        color_map.put(-3, new int[]{237, 234, 222});        // Score cell
        color_map.put(-2, new int[]{152, 136, 118});        // Score, Best text
        color_map.put(-1, new int[]{155, 135, 119});        // Table borders
        color_map.put(0, new int[]{189, 172, 152});     // Table empty cells
        color_map.put(2, new int[]{239, 229, 218});
        color_map.put(4, new int[]{235, 216, 182});
        color_map.put(8, new int[]{242, 177, 119});
        color_map.put(16, new int[]{245, 149, 101});
        color_map.put(32, new int[]{248, 128, 99});
        color_map.put(64, new int[]{246, 93, 59});
        color_map.put(128, new int[]{243, 207, 83});
        color_map.put(256, new int[]{237, 204, 99});
        color_map.put(512, new int[]{236, 200, 80});
        color_map.put(1024, new int[]{239, 197, 63});
        color_map.put(2048, new int[]{238, 194, 45});
    }

    // Set up game
    public void setup() {
        frameRate(FPS);                                  // set FPS
        font = createFont("Serif", 36);        // set font
        board = createBoard();                           // make new board
        startTime = millis();                            // start timer
    }

    // Draw UI
    private void draw_UI() {
        // Write score and best
        int[] color = color_map.get(-2);
        fill(color[0], color[1], color[2]);
        textSize(20);
        textAlign(CENTER, CENTER);
        text("SCORE", (float) width / 2 - 60, 30);
        text("BEST", (float) width / 2 + 60, 30);

        // Draw score cell
        color = color_map.get(-3);
        fill(color[0], color[1], color[2]);
        rect((float) width / 2 - 100, 45, 80, 50, 12);
        noFill();
        color = color_map.get(-4);
        stroke(color[0], color[1], color[2]);
        strokeWeight(2);
        rect((float) width / 2 + 20, 45, 80, 50, 12);
        noStroke();

        // Draw score
        color = color_map.get(-5);
        fill(color[0], color[1], color[2]);
        textSize(22);
        text(score, (float) width / 2 - 60, 70);
        text(best, (float) width / 2 + 60, 70);

        // Draw timer
        fill(0);
        textSize(15);
        String display_time;
        if (gameOver) {
            display_time = "Time: " + (stopTime - startTime) / 1000 + "s";
        } else {
            display_time = "Time: " + (millis() - startTime) / 1000 + "s";
        }
        textAlign(RIGHT, TOP);
        text(display_time, width - 10, 10);
        textAlign(LEFT, BASELINE);
    }

    // Draw table
    private void draw_table() {
        int[] color = color_map.get(-1);
        fill(color[0], color[1], color[2]);
        noStroke();
        rect(0, UI_SIZE, WIDTH, WIDTH, 12);
    }

    // Draw empty cells
    private void draw_empty() {
        int pos_x;                         // position x
        int pos_y;                         // position y
        int size;                          // size of cell
        int[] color;                       // color
        int r, g, b;                       // r,g,b values

        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                // Find the position of the cell and avoid the border (CELL_BUFFER)
                pos_x = x * CELLSIZE + CELL_BUFFER;
                pos_y = y * CELLSIZE + UI_SIZE + CELL_BUFFER;
                size = CELLSIZE - 2 * CELL_BUFFER;          // Calculate the cell size

                // Get the color for empty cell
                color = color_map.get(0);
                r = color[0];
                g = color[1];
                b = color[2];

                // Highlight the cell where the mouse is pointing at
                if (mouseX > pos_x && mouseX < (x+1) * CELLSIZE + CELL_BUFFER
                        && mouseY > pos_y && mouseY < (y + 1) * CELLSIZE + UI_SIZE + CELL_BUFFER) {
                    // Brighten color by br_inc (default 10%)
                    r = (int) min(255, color[0]*brightness_increase);
                    g = (int) min(255, color[1]*brightness_increase);
                    b = (int) min(255, color[2]*brightness_increase);
                }
                fill(r, g, b);
                rect(pos_x, pos_y, size, size, 12);
            }
        }
    }

    // Game over text
    private void game_over() {
        textSize(48);
        int[] color = color_map.get(-6);
        fill(color[0], color[1], color[2]);
        textAlign(CENTER);
        text("GAME OVER", (float) width / 2, (float) height / 2);
        textAlign(LEFT);
    }

    // Draw game and texts
    public void draw() {
        try {
            background(250, 248, 239);      // Background color
            textFont(font);                 // Score and cell number color

            // Draw UI, table with empty cells
            draw_UI();
            draw_table();
            draw_empty();

            // Draw cells with value on top of empty cells
            for (int y = 0; y < GRID_SIZE; y++) {
                for (int x = 0; x < GRID_SIZE; x++) {
                    board[y][x].draw(this);
                }
            }

            // Check if the cells are still moving
            if (wait_anim && moved) {
                boolean allDone = true;
                for (int y = 0; y < GRID_SIZE; y++) {
                    for (int x = 0; x < GRID_SIZE; x++) {
                        if (board[y][x].isAnimating()) {
                            allDone = false;
                            break;
                        }
                    }
                }

                // If cells are done moving, spawn a new random cell
                if (allDone) {
                    int num_empty = randomCell();
                    if (score > best) best = score;
                    wait_anim = false;
                    moved = false;
                    // Check if the board is full
                    if (num_empty != 0) {
                        return;
                    }
                    // Only check game over if the board is full
                    if (!canMove()) {
                        gameOver = true;
                        stopTime = millis();
                    }
                }
            }

            // print Game Over Text
            if (gameOver) {
                game_over();
            }

        } catch (Exception e) {
            e.printStackTrace();
            noLoop();
        }
    }

    // Press 'r' or arrow keys
    public void keyPressed() {
        // Reset the game
        if (key == 'r') {
            setup();
            gameOver = false;
            score = 0;
            return;
        }

        // Skip is the board is moving or game is over
        if (gameOver || wait_anim  || moved) return;

        // Move the cells
        if (keyCode == LEFT) moved = moveLeft();
        else if (keyCode == RIGHT) moved = moveRight();
        else if (keyCode == UP) moved = moveUp();
        else if (keyCode == DOWN) moved = moveDown();

        // Animate if cells have moved
        if (moved) {
            wait_anim = true;
        }
    }


    // Spawn cell on mouse click
    public void mouseReleased(MouseEvent e) {
        if (e.getY() <= UI_SIZE) {
            return;
        }
        int y = (e.getY() - UI_SIZE) / CELLSIZE;  // subtract top UI
        int x = e.getX() / CELLSIZE;
        if (y >= 0 && y < GRID_SIZE && x >= 0 && x < GRID_SIZE) {
            board[y][x].place();
        }
    }

    // Add a random cell
    private int randomCell() {
        // Make a list of empty cells
        List<Cell> empty = new ArrayList<>();
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                if (board[y][x].getValue() == 0) empty.add(board[y][x]);
            }
        }
        // Choose one random cell to place
        if (!empty.isEmpty()) {
            Cell c = empty.get(random.nextInt(empty.size()));
            if (random.nextBoolean()) {
                c.setValue(2);
            } else {
                c.setValue(4);
            }
            c.triggerSpawnAnimation();
        }
        // Return number of empty cell
        return empty.size() - 1;
    }

    // Move left, right, up or down
    public boolean moveLeft() {
        // Loop every rows
        for (int y = 0; y < GRID_SIZE; y++) {
            // Find cells with values
            List<Cell> row = new ArrayList<>();
            for (int x = 0; x < GRID_SIZE; x++) {
                if (board[y][x].getValue() != 0) {
                    row.add(board[y][x]);
                }
            }

            int x = 0;
            for (int i = 0; i < row.size(); i++) {
                // Merge cells
                Cell current = row.get(i);
                int value = current.getValue();
                if (i + 1 < row.size() && row.get(i + 1).getValue() == value) {
                    value *= 2;
                    score += value;
                    current.triggerMergeAnimation();
                    i++; // skip the next cell (already merged)
                }

                // Check if the cell move
                if (current.getX() != x || current.getY() != y || current.getValue() != value) {
                    moved = true;
                }

                // Prepare new value and trigger animation
                current.prepareNextValue(value);
                current.moveTo(current.getX(), current.getY(), x, y);
                board[y][x] = current;
                x++;
            }

            // Fill the remaining with empty cells
            while (x < GRID_SIZE) {
                board[y][x] = new Cell(x, y);
                x++;
            }
        }

        return moved;
    }

    public boolean moveRight() {
        // loop rows
        for (int y = 0; y < GRID_SIZE; y++) {
            // find cells with values
            List<Cell> row = new ArrayList<>();
            for (int x = GRID_SIZE - 1; x >= 0; x--) {
                if (board[y][x].getValue() != 0) {
                    row.add(board[y][x]);
                }
            }

            int x = GRID_SIZE - 1;
            for (int i = 0; i < row.size(); i++) {
                // merge cells
                Cell current = row.get(i);
                int value = current.getValue();
                if (i + 1 < row.size() && row.get(i + 1).getValue() == value) {
                    value *= 2;
                    score += value;
                    current.triggerMergeAnimation();
                    i++;
                }
                // check if cell moved
                if (current.getX() != x || current.getY() != y || current.getValue() != value) {
                    moved = true;
                }
                // prepare next val + trigger anim
                current.prepareNextValue(value);
                current.moveTo(current.getX(), current.getY(), x, y);
                board[y][x] = current;
                x--;
            }

            // fill remaining with empty
            while (x >= 0) {
                board[y][x] = new Cell(x, y);
                x--;
            }
        }

        return moved;
    }

    public boolean moveUp() {
        // loop columns
        for (int x = 0; x < GRID_SIZE; x++) {
            List<Cell> column = new ArrayList<>();
            for (int y = 0; y < GRID_SIZE; y++) {
                if (board[y][x].getValue() != 0) {
                    column.add(board[y][x]);
                }
            }

            int y = 0;
            for (int i = 0; i < column.size(); i++) {
                // merge
                Cell current = column.get(i);
                int value = current.getValue();
                if (i + 1 < column.size() && column.get(i + 1).getValue() == value) {
                    value *= 2;
                    score += value;
                    current.triggerMergeAnimation();
                    i++;
                }
                // check cell move
                if (current.getX() != x || current.getY() != y || current.getValue() != value) {
                    moved = true;
                }
                // prepare next val + trigger anim
                current.prepareNextValue(value);
                current.moveTo(current.getX(), current.getY(), x, y);
                board[y][x] = current;
                y++;
            }

            // fill remaining with empty
            while (y < GRID_SIZE) {
                board[y][x] = new Cell(x, y);
                y++;
            }
        }

        return moved;
    }

    public boolean moveDown() {
        // loop columns
        for (int x = 0; x < GRID_SIZE; x++) {
            List<Cell> column = new ArrayList<>();
            for (int y = GRID_SIZE - 1; y >= 0; y--) {
                if (board[y][x].getValue() != 0) {
                    column.add(board[y][x]);
                }
            }

            int y = GRID_SIZE - 1;
            for (int i = 0; i < column.size(); i++) {
                // merge
                Cell current = column.get(i);
                int value = current.getValue();
                if (i + 1 < column.size() && column.get(i + 1).getValue() == value) {
                    value *= 2;
                    score += value;
                    current.triggerMergeAnimation();
                    i++;
                }
                // check cell move
                if (current.getX() != x || current.getY() != y || current.getValue() != value) {
                    moved = true;
                }
                // prep next val + anim
                current.prepareNextValue(value);
                current.moveTo(current.getX(), current.getY(), x, y);
                board[y][x] = current;
                y--;
            }

            // fill remaining with empty
            while (y >= 0) {
                board[y][x] = new Cell(x, y);
                y--;
            }
        }

        return moved;
    }

    // Check if game is over
    private boolean canMove() {
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                int val = board[y][x].getValue();
                if (val == 0) return true;
                if (x < GRID_SIZE - 1 && val == board[y][x + 1].getValue()) return true;
                if (y < GRID_SIZE - 1 && val == board[y + 1][x].getValue()) return true;
            }
        }
        return false;
    }


    public float getBrightness_increase() {
        return brightness_increase;
    }
}
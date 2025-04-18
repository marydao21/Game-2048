package TwentyFortyEight;

import processing.core.PApplet;

public class Cell {
    private int x;
    private int y;
    private int value;
    private float animX;
    private float animY;
    private boolean isAnimating;
    private int nextValue = 0;
    private boolean needUpdate = false;

    private int spawnAnimationTimer = 0;
    private int mergeAnimationTimer = 0;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.value = 0;
        this.animX = x;
        this.animY = y;
        this.isAnimating = false;
    }

    // prepare next val when cell reach destination
    public void prepareNextValue(int val) {
        this.nextValue = val;
        this.needUpdate = true;
    }

    // apply val when cell reach dest
    public void applyNextValue() {
        if (needUpdate) {
            this.value = nextValue;
            nextValue = 0;
            needUpdate = false;
        }
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    // get a random value when click
    public void place() {
        if (this.value == 0) {
            this.value = (App.random.nextInt(2) + 1) * 2;
            triggerSpawnAnimation();
        }
    }

    public void triggerSpawnAnimation() {
        spawnAnimationTimer = 4;
    }

    public void triggerMergeAnimation() {
        mergeAnimationTimer = 6;
    }

    // Get cell color
    private int[] get_color(App app) {
        int[] color = App.color_map.getOrDefault(value, App.color_map.get(2048));
        int r = color[0];
        int g = color[1];
        int b = color[2];
        boolean highlighted = app.mouseX > x * App.CELLSIZE && app.mouseX < (x + 1) * App.CELLSIZE
                && app.mouseY - App.UI_SIZE > y * App.CELLSIZE && app.mouseY - App.UI_SIZE < (y + 1) * App.CELLSIZE;

        // Highlight the cell the mouse is at
        if (highlighted) {
            r = (int) App.min(255, color[0]* app.getBrightness_increase());
            g = (int) App.min(255, color[1]* app.getBrightness_increase());
            b = (int) App.min(255, color[2]* app.getBrightness_increase());
        }
        return new int[]{r, g, b};
    }

    // Draw value
    private void draw_value(float px, float py, float baseSize, App app) {
        // Cell text color
        int[] color;
        if (value <= 4) {
            color = App.color_map.get(-8);
            app.fill(color[0], color[1], color[2]);
        } else {
            color = App.color_map.get(-7);
            app.fill(color[0], color[1], color[2]);
        }

        // Cell text font
        int fontSize = 36;
        if (value >= 1024) fontSize = 22;
        else if (value >= 128) fontSize = 26;
        else if (value >= 16) fontSize = 30;

        app.textAlign(PApplet.CENTER, PApplet.CENTER);
        app.textSize(fontSize);

        // Draw 5 times to simulate bold
        for (int i = 0; i < 5; i++) {
            float offsetX = 0;
            float offsetY = 0;

            // Draw slightly left and right
            if (i % 2 != 0) {
                if (i == 1) {
                    offsetX = 1;
                } else {
                    offsetX = -1;
                }
            }

            // Draw slightly up and down
            if (i >= 2) {
                if (i == 2) {
                    offsetY = 1;
                } else {
                    offsetY = -1;
                }
            }
            app.text(String.valueOf(value), px + (float) baseSize / 2 + offsetX,
                    py + (float) baseSize / 2 + offsetY);
        }
    }

    // Animate moving cell
    private void anim_move() {
        if (isAnimating) {
            animX = PApplet.lerp(animX, x, App.ANIM_SPEED);
            animY = PApplet.lerp(animY, y, App.ANIM_SPEED);

            if (Math.abs(animX - x) < 0.01f && Math.abs(animY - y) < 0.01f) {
                animX = x;
                animY = y;
                isAnimating = false;
                applyNextValue(); // apply deferred value
            }
        }
    }

    public void draw(App app) {
        int baseSize = App.CELLSIZE - 2 * App.CELL_BUFFER;
        float px = animX * App.CELLSIZE + App.CELL_BUFFER;
        float py = animY * App.CELLSIZE + App.CELL_BUFFER + App.UI_SIZE; // calculate the tile’s position on screen

        float scale = 1.0f; // no scale, normal size

        // Increase scale slightly each frame when spawn animation is active
        if (spawnAnimationTimer > 0) {
            scale = 0.9f + 0.01f * spawnAnimationTimer;
            spawnAnimationTimer--;  // count down animation frames

            // Make cell larger when merge animation is active
        } else if (mergeAnimationTimer > 0) {
            scale = 1.0f + 0.05f * mergeAnimationTimer;
            mergeAnimationTimer--;
        }

        // Calculate cell's size after scaling
        int drawSize = (int) (baseSize * scale);
        float offset = (baseSize - drawSize) / 2.0f;

        // Draw cell with value
        if (value > 0) {
            // Get color based on value, if value>2048, default color is 2048
            int[] color = get_color(app);
            app.fill(color[0], color[1], color[2]);

            // Draw cell and value
            app.rect(px + offset, py + offset, drawSize, drawSize, 12);
            draw_value(px, py, baseSize, app);
        }

        // Animate moving
        anim_move();
    }

    public void moveTo(int fromX, int fromY, int toX, int toY) {
        this.x = toX;
        this.y = toY;
        this.animX = fromX;
        this.animY = fromY;
        this.isAnimating = true;
    }


    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

}
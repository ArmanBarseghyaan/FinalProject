package finalproject.pacman.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

/**
 * Призрак преследует Пакмана по кратчайшему пути в связном графе проходов.
 */
public final class Ghost extends MovingEntity {
    private final Color color;
    private boolean frightened;

    public Ghost(int startRow, int startColumn, Color color) {
        super(startRow, startColumn, 2.0);
        this.color = color;
    }

    public void chase(Maze maze, Pacman pacman) {
        Direction next = frightened
                ? maze.directionAway(getRow(maze), getColumn(maze),
                        pacman.getRow(maze), pacman.getColumn(maze), getDirection())
                : maze.directionToward(getRow(maze), getColumn(maze),
                        pacman.getRow(maze), pacman.getColumn(maze), getDirection());
        update(maze, next);
    }

    public void setFrightened(boolean frightened) {
        this.frightened = frightened;
    }

    public boolean isFrightened() {
        return frightened;
    }

    @Override
    public void draw(Graphics2D graphics) {
        int width = Maze.TILE_SIZE - 4;
        int height = Maze.TILE_SIZE - 3;
        int x = (int) Math.round(getX() - width / 2.0);
        int y = (int) Math.round(getY() - height / 2.0);
        Color bodyColor = frightened ? new Color(35, 73, 225) : color;

        Path2D.Double body = new Path2D.Double();
        body.moveTo(x, y + height);
        body.lineTo(x, y + height * 0.45);
        body.curveTo(x, y + height * 0.16, x + width * 0.22, y,
                x + width / 2.0, y);
        body.curveTo(x + width * 0.78, y, x + width, y + height * 0.16,
                x + width, y + height * 0.45);
        body.lineTo(x + width, y + height);
        body.lineTo(x + width * 0.8, y + height * 0.82);
        body.lineTo(x + width * 0.6, y + height);
        body.lineTo(x + width * 0.4, y + height * 0.82);
        body.lineTo(x + width * 0.2, y + height);
        body.closePath();
        graphics.setColor(bodyColor);
        graphics.fill(body);

        int eyeY = y + height / 3;
        graphics.setColor(Color.WHITE);
        graphics.fillOval(x + 5, eyeY, 7, 9);
        graphics.fillOval(x + width - 12, eyeY, 7, 9);

        int pupilX = 0;
        int pupilY = 0;
        switch (getDirection()) {
            case LEFT:
                pupilX = -2;
                break;
            case RIGHT:
                pupilX = 2;
                break;
            case UP:
                pupilY = -2;
                break;
            case DOWN:
                pupilY = 2;
                break;
            default:
                break;
        }
        graphics.setColor(new Color(25, 45, 110));
        graphics.fillOval(x + 8 + pupilX, eyeY + 3 + pupilY, 3, 4);
        graphics.fillOval(x + width - 9 + pupilX, eyeY + 3 + pupilY, 3, 4);

        if (frightened) {
            graphics.setColor(new Color(255, 255, 255));
            graphics.drawLine(x + 7, y + height - 7, x + 11, y + height - 4);
            graphics.drawLine(x + 11, y + height - 4, x + 15, y + height - 7);
            graphics.drawLine(x + 15, y + height - 7, x + 19, y + height - 4);
        }
    }
}

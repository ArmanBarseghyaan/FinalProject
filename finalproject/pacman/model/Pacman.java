package finalproject.pacman.model;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Игрок. Направление, заданное пользователем, применяется при первой
 * возможности, поэтому раннее нажатие не теряется перед перекрёстком.
 */
public final class Pacman extends MovingEntity {
    private Direction bufferedDirection = Direction.RIGHT;
    private double mouthPhase;
    private int mouthAngle = 28;

    public Pacman(int startRow, int startColumn) {
        super(startRow, startColumn, 3.0);
    }

    public void requestDirection(Direction direction) {
        if (direction != null && direction != Direction.STOP) {
            bufferedDirection = direction;
        }
    }

    public void move(Maze maze) {
        update(maze, bufferedDirection);
        mouthPhase += 0.24;
        mouthAngle = 10 + (int) ((1.0 + Math.sin(mouthPhase)) * 19.0);
    }

    public void resetForNewLife() {
        resetPosition();
        bufferedDirection = Direction.RIGHT;
    }

    @Override
    public void draw(Graphics2D graphics) {
        int diameter = Maze.TILE_SIZE - 5;
        int x = (int) Math.round(getX() - diameter / 2.0);
        int y = (int) Math.round(getY() - diameter / 2.0);

        graphics.setColor(new Color(255, 211, 45));
        int facingAngle;
        switch (getDirection()) {
            case LEFT:
                facingAngle = 180;
                break;
            case UP:
                facingAngle = 90;
                break;
            case DOWN:
                facingAngle = 270;
                break;
            default:
                facingAngle = 0;
                break;
        }
        int startAngle = facingAngle + mouthAngle / 2;
        graphics.fillArc(x, y, diameter, diameter, startAngle, 360 - mouthAngle);
    }
}

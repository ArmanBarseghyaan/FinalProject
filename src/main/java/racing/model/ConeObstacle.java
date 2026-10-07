package racing.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ConeObstacle extends Obstacle {

    public ConeObstacle(int x, int y) {
        super(x, y, 32, 32, 4);
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Тень
        gc.setFill(Color.rgb(0, 0, 0, 70 / 255.0));
        gc.fillOval(getX() - 2, getY() + getHeight() - 6, getWidth() + 4, 10);

        // Основание конуса
        gc.setFill(Color.rgb(200, 80, 0));
        gc.fillRect(getX() + 2, getY() + getHeight() - 8, getWidth() - 4, 8);

        // Оранжевое тело конуса
        gc.setFill(Color.rgb(255, 102, 0));
        double[] xPoints = {getX() + 4, getX() + getWidth() / 2.0, getX() + getWidth() - 4};
        double[] yPoints = {getY() + getHeight() - 8, getY(), getY() + getHeight() - 8};
        gc.fillPolygon(xPoints, yPoints, 3);

        // Белые светоотражающие полоски
        gc.setFill(Color.WHITE);
        gc.fillRect(getX() + 10, getY() + 10, 12, 4);
        gc.fillRect(getX() + 7, getY() + 17, 18, 4);
    }
}

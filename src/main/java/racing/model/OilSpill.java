package racing.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class OilSpill extends Obstacle {

    public OilSpill(int x, int y, int speed) {
        super(x, y, 40, 40, speed);
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Черное масляное пятно
        gc.setFill(Color.rgb(20, 20, 20, 220 / 255.0));
        gc.fillOval(getX(), getY(), getWidth(), getHeight());
        gc.fillOval(getX() + 8, getY() - 4, 20, 16);

        // Радужный перелив масла
        gc.setFill(Color.rgb(138, 43, 226, 120 / 255.0));
        gc.fillOval(getX() + 6, getY() + 6, 20, 15);
        gc.setFill(Color.rgb(0, 255, 255, 100 / 255.0));
        gc.fillOval(getX() + 18, getY() + 18, 14, 10);
    }
}

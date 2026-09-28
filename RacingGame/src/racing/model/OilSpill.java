package racing.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class OilSpill extends Obstacle {

    public OilSpill(int x, int y, int speed) {
        super(x, y, 40, 40, speed);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Черное масляное пятно
        g2d.setColor(new Color(20, 20, 20, 220));
        g2d.fillOval(getX(), getY(), getWidth(), getHeight());
        g2d.fillOval(getX() + 8, getY() - 4, 20, 16);

        // Радужный перелив масла
        g2d.setColor(new Color(138, 43, 226, 120));
        g2d.fillOval(getX() + 6, getY() + 6, 20, 15);
        g2d.setColor(new Color(0, 255, 255, 100));
        g2d.fillOval(getX() + 18, getY() + 18, 14, 10);
    }
}
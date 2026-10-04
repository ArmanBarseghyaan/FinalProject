package racing.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class ConeObstacle extends Obstacle {

    public ConeObstacle(int x, int y) {
        super(x, y, 32, 32, 4);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Тень
        g2d.setColor(new Color(0, 0, 0, 70));
        g2d.fillOval(getX() - 2, getY() + getHeight() - 6, getWidth() + 4, 10);

        // Основание конуса
        g2d.setColor(new Color(200, 80, 0));
        g2d.fillRect(getX() + 2, getY() + getHeight() - 8, getWidth() - 4, 8);

        // Оранжевое тело конуса
        g2d.setColor(new Color(255, 102, 0));
        int[] xPoints = {getX() + 4, getX() + getWidth() / 2, getX() + getWidth() - 4};
        int[] yPoints = {getY() + getHeight() - 8, getY(), getY() + getHeight() - 8};
        g2d.fillPolygon(xPoints, yPoints, 3);

        // Белые светоотражающие полоски
        g2d.setColor(Color.WHITE);
        g2d.fillRect(getX() + 10, getY() + 10, 12, 4);
        g2d.fillRect(getX() + 7, getY() + 17, 18, 4);
    }
}
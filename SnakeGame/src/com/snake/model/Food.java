package com.snake.model;

import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * Класс Food представляет еду для змейки в виде реалистичного 3D-яблока с черенком и листиком.
 * Реализует интерфейс Drawable.
 */
public class Food implements Drawable {
    private Point position;
    private final Random random;

    public Food() {
        this.position = new Point(0, 0);
        this.random = new Random();
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public void spawn(int gridWidth, int gridHeight) {
        int x = random.nextInt(gridWidth);
        int y = random.nextInt(gridHeight);
        this.position.setX(x);
        this.position.setY(y);
    }

    @Override
    public void draw(Graphics g, int tileSize) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int px = position.getX() * tileSize;
        int py = position.getY() * tileSize;
        int pad = 2;
        int size = tileSize - pad * 2;

        int cx = px + pad;
        int cy = py + pad;

        // 1. Тень под яблоком
        g2d.setColor(new Color(0, 0, 0, 90));
        g2d.fillOval(cx + 2, cy + size - 4, size - 4, 5);

        // 2. 3D Радиальный градиент тела яблока
        Point2D center = new Point2D.Float(cx + size * 0.38f, cy + size * 0.35f);
        float radius = size * 0.68f;
        float[] dist = {0.0f, 0.75f, 1.0f};
        Color[] colors = {
            new Color(255, 95, 95),
            new Color(225, 25, 25),
            new Color(125, 0, 10)
        };
        RadialGradientPaint appleGrad = new RadialGradientPaint(center, radius, dist, colors);
        g2d.setPaint(appleGrad);

        // Яблоко со слегка объёмной формой
        g2d.fillOval(cx + 1, cy + 3, size - 2, size - 3);

        // Впадинка под черенок на верхушке яблока
        g2d.setColor(new Color(90, 10, 10));
        g2d.fillOval(cx + size / 2 - 2, cy + 2, 4, 3);

        // 3. Изогнутый коричневый черенок (стебелек)
        g2d.setColor(new Color(110, 60, 25));
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawArc(cx + size / 2 - 4, cy - 2, 8, 8, 30, 90);

        // 4. Реалистичный сочный зеленый листик
        g2d.setColor(new Color(46, 204, 113));
        Path2D leaf = new Path2D.Float();
        leaf.moveTo(cx + size / 2, cy + 1);
        leaf.quadTo(cx + size / 2 + 7, cy - 5, cx + size / 2 + 10, cy + 1);
        leaf.quadTo(cx + size / 2 + 5, cy + 5, cx + size / 2, cy + 1);
        leaf.closePath();
        g2d.fill(leaf);

        // Прожилка на листике
        g2d.setColor(new Color(20, 110, 50));
        g2d.setStroke(new BasicStroke(0.9f));
        g2d.drawLine(cx + size / 2, cy + 1, cx + size / 2 + 7, cy - 1);

        // 5. Яркий блик глянца на поверхноти яблока
        g2d.setColor(new Color(255, 255, 255, 190));
        g2d.fillOval(cx + 4, cy + 5, size / 3, size / 5);

        g2d.dispose();
    }
}

package com.snake.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

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
    public void draw(GraphicsContext gc, int tileSize) {
        int px = position.getX() * tileSize;
        int py = position.getY() * tileSize;
        int pad = 2;
        int size = tileSize - pad * 2;

        int cx = px + pad;
        int cy = py + pad;

        // 1. Тень под яблоком
        gc.setFill(Color.rgb(0, 0, 0, 90 / 255.0));
        gc.fillOval(cx + 2, cy + size - 4, size - 4, 5);

        // 2. 3D Радиальный градиент тела яблока
        RadialGradient appleGrad = new RadialGradient(
                0, 0,
                cx + size * 0.38, cy + size * 0.35,
                size * 0.68,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(255, 95, 95)),
                new Stop(0.75, Color.rgb(225, 25, 25)),
                new Stop(1.0, Color.rgb(125, 0, 10))
        );
        gc.setFill(appleGrad);

        // Яблоко со слегка объёмной формой
        gc.fillOval(cx + 1, cy + 3, size - 2, size - 3);

        // Впадинка под черенок на верхушке яблока
        gc.setFill(Color.rgb(90, 10, 10));
        gc.fillOval(cx + size / 2.0 - 2, cy + 2, 4, 3);

        // 3. Изогнутый коричневый черенок (стебелек)
        gc.setStroke(Color.rgb(110, 60, 25));
        gc.setLineWidth(2.0);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        gc.strokeArc(cx + size / 2.0 - 4, cy - 2, 8, 8, 30, 90, ArcType.OPEN);

        // 4. Реалистичный сочный зеленый листик
        gc.setFill(Color.rgb(46, 204, 113));
        gc.beginPath();
        gc.moveTo(cx + size / 2.0, cy + 1);
        gc.quadraticCurveTo(cx + size / 2.0 + 7, cy - 5, cx + size / 2.0 + 10, cy + 1);
        gc.quadraticCurveTo(cx + size / 2.0 + 5, cy + 5, cx + size / 2.0, cy + 1);
        gc.closePath();
        gc.fill();

        // Прожилка на листике
        gc.setStroke(Color.rgb(20, 110, 50));
        gc.setLineWidth(0.9);
        gc.strokeLine(cx + size / 2.0, cy + 1, cx + size / 2.0 + 7, cy - 1);

        // 5. Яркий блик глянца на поверхности яблока
        gc.setFill(Color.rgb(255, 255, 255, 190 / 255.0));
        gc.fillOval(cx + 4, cy + 5, size / 3.0, size / 5.0);
    }
}

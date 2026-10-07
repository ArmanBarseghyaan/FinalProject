package racing.model;

import racing.exception.InvalidSpeedException;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

public class PlayerCar extends GameObject {
    private float exactX;
    private int speed;
    private int health;
    private int bonusScore;
    private boolean nitroActive;
    private int nitroAmount; // 0 - 100
    private static final int MAX_HEALTH = 100;
    private static final float STEER_SPEED = 7.5f;

    public PlayerCar(int x, int y) {
        super(x, y, 44, 82);
        this.exactX = x;
        this.speed = 8;
        this.health = MAX_HEALTH;
        this.bonusScore = 0;
        this.nitroAmount = 100;
        this.nitroActive = false;
    }

    public void reset(int x, int y) {
        setX(x);
        setY(y);
        this.exactX = x;
        this.speed = 8;
        this.health = MAX_HEALTH;
        this.bonusScore = 0;
        this.nitroAmount = 100;
        this.nitroActive = false;
    }

    public void steerLeft() {
        this.exactX = Math.max(84.0f, this.exactX - STEER_SPEED);
        setX((int) this.exactX);
    }

    public void steerRight() {
        this.exactX = Math.min(272.0f, this.exactX + STEER_SPEED);
        setX((int) this.exactX);
    }

    public void moveLeft() {
        steerLeft();
    }

    public void moveRight() {
        steerRight();
    }

    public void takeDamage(int amount) {
        this.health = Math.max(0, this.health - amount);
    }

    public void addScore(int points) {
        this.bonusScore += points;
    }

    public void setNitroActive(boolean active) {
        if (active && nitroAmount > 0) {
            this.nitroActive = true;
        } else {
            this.nitroActive = false;
        }
    }

    public void updateNitro() {
        if (nitroActive && nitroAmount > 0) {
            nitroAmount -= 2;
            if (nitroAmount <= 0) {
                nitroActive = false;
            }
        } else if (!nitroActive && nitroAmount < 100) {
            nitroAmount += 1;
        }
    }

    public void setSpeed(int speed) throws InvalidSpeedException {
        if (speed < 0 || speed > 25) {
            throw new InvalidSpeedException("Скорость вне диапазона (0-25)");
        }
        this.speed = speed;
    }

    @Override
    public void draw(GraphicsContext gc) {
        // 1. Неоновая подсветка днища
        Color neonColor = nitroActive ? Color.rgb(0, 240, 255, 180 / 255.0) : Color.rgb(255, 0, 128, 150 / 255.0);
        gc.setFill(neonColor);
        gc.fillRoundRect(getX() - 10, getY() - 5, getWidth() + 20, getHeight() + 10, 20, 20);

        // 2. Огонь из выхлопных труб при Нитро
        if (nitroActive) {
            gc.setFill(Color.rgb(0, 191, 255));
            gc.fillRect(getX() + 8, getY() + getHeight(), 8, 20);
            gc.fillRect(getX() + getWidth() - 16, getY() + getHeight(), 8, 20);
            gc.setFill(Color.WHITE);
            gc.fillRect(getX() + 10, getY() + getHeight(), 4, 12);
            gc.fillRect(getX() + getWidth() - 14, getY() + getHeight(), 4, 12);
        }

        // 3. Агрессивный корпус
        gc.setFill(Color.rgb(30, 0, 50));
        gc.fillRoundRect(getX(), getY(), getWidth(), getHeight(), 10, 10);

        // Карбоновый капот
        gc.setFill(Color.rgb(20, 20, 20));
        gc.fillRect(getX() + 6, getY() + 4, getWidth() - 12, 30);

        // Яркая винил-графика
        gc.setStroke(Color.rgb(255, 0, 100));
        gc.setLineWidth(1.5);
        double[] xV = {getX() + 2, getX() + 15, getX() + getWidth() - 2};
        double[] yV = {getY() + 40, getY() + 20, getY() + getHeight() - 10};
        gc.strokePolyline(xV, yV, 3);

        // Тонированное лобовое стекло
        gc.setFill(Color.rgb(10, 10, 15, 230 / 255.0));
        gc.fillRoundRect(getX() + 5, getY() + 22, 34, 16, 4, 4);

        // Ксеноновые передние фары
        gc.setFill(Color.rgb(180, 240, 255));
        gc.fillOval(getX() + 2, getY() - 3, 10, 8);
        gc.fillOval(getX() + getWidth() - 12, getY() - 3, 10, 8);

        // Ксеноновый луч на асфальте
        gc.setFill(Color.rgb(0, 200, 255, 40 / 255.0));
        gc.fillArc(getX() - 30, getY() - 90, 104, 90, 60, 60, ArcType.ROUND);

        // Задние диодные фонари
        gc.setFill(Color.rgb(255, 0, 50));
        gc.fillRect(getX() + 2, getY() + getHeight() - 2, 12, 3);
        gc.fillRect(getX() + getWidth() - 14, getY() + getHeight() - 2, 12, 3);

        // Спойлер
        gc.setFill(Color.BLACK);
        gc.fillRect(getX() - 2, getY() + getHeight() - 6, getWidth() + 4, 4);
    }

    public int getSpeed() { return speed; }
    public int getHealth() { return health; }
    public int getBonusScore() { return bonusScore; }
    public boolean isNitroActive() { return nitroActive; }
    public int getNitroAmount() { return nitroAmount; }
}

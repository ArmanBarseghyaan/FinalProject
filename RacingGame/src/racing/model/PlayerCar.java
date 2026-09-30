package racing.model;

import racing.exception.InvalidSpeedException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

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
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // 1. Неоновая подсветка днища
        Color neonColor = nitroActive ? new Color(0, 240, 255, 180) : new Color(255, 0, 128, 150);
        g2d.setColor(neonColor);
        g2d.fillRoundRect(getX() - 10, getY() - 5, getWidth() + 20, getHeight() + 10, 20, 20);

        // 2. Огонь из выхлопных труб при Нитро
        if (nitroActive) {
            g2d.setColor(new Color(0, 191, 255));
            g2d.fillRect(getX() + 8, getY() + getHeight(), 8, 20);
            g2d.fillRect(getX() + getWidth() - 16, getY() + getHeight(), 8, 20);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(getX() + 10, getY() + getHeight(), 4, 12);
            g2d.fillRect(getX() + getWidth() - 14, getY() + getHeight(), 4, 12);
        }

        // 3. Агрессивный корпус
        g2d.setColor(new Color(30, 0, 50));
        g2d.fillRoundRect(getX(), getY(), getWidth(), getHeight(), 10, 10);

        // Карбоновый капот
        g2d.setColor(new Color(20, 20, 20));
        g2d.fillRect(getX() + 6, getY() + 4, getWidth() - 12, 30);

        // Яркая винил-графика
        g2d.setColor(new Color(255, 0, 100));
        int[] xV = {getX() + 2, getX() + 15, getX() + getWidth() - 2};
        int[] yV = {getY() + 40, getY() + 20, getY() + getHeight() - 10};
        g2d.drawPolyline(xV, yV, 3);

        // Тонированное лобовое стекло
        g2d.setColor(new Color(10, 10, 15, 230));
        g2d.fillRoundRect(getX() + 5, getY() + 22, 34, 16, 4, 4);

        // Ксеноновые передние фары
        g2d.setColor(new Color(180, 240, 255));
        g2d.fillOval(getX() + 2, getY() - 3, 10, 8);
        g2d.fillOval(getX() + getWidth() - 12, getY() - 3, 10, 8);

        // Ксеноновый луч на асфальте
        g2d.setColor(new Color(0, 200, 255, 40));
        g2d.fillArc(getX() - 30, getY() - 90, 104, 90, 60, 60);

        // Задние диодные фонари
        g2d.setColor(new Color(255, 0, 50));
        g2d.fillRect(getX() + 2, getY() + getHeight() - 2, 12, 3);
        g2d.fillRect(getX() + getWidth() - 14, getY() + getHeight() - 2, 12, 3);

        // Спойлер
        g2d.setColor(Color.BLACK);
        g2d.fillRect(getX() - 2, getY() + getHeight() - 6, getWidth() + 4, 4);
    }

    public int getSpeed() { return speed; }
    public int getHealth() { return health; }
    public int getBonusScore() { return bonusScore; }
    public boolean isNitroActive() { return nitroActive; }
    public int getNitroAmount() { return nitroAmount; }
}

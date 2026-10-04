package racing.model;

public abstract class Obstacle extends GameObject {
    private int speed;

    public Obstacle(int x, int y, int width, int height, int speed) {
        super(x, y, width, height);
        this.speed = speed;
    }

    public void updatePosition() {
        setY(getY() + speed);
    }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
}
package racing.service;

import racing.exception.CollisionException;
import racing.model.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameEngine {
    private final PlayerCar player;
    private final List<Obstacle> obstacles;
    private final List<CoinItem> coins;
    private final DifficultyLevel difficulty;
    private int score;
    private boolean gameOver;
    private final Random random;
    private int trackOffsetY;

    public GameEngine(DifficultyLevel difficulty) {
        this.player = new PlayerCar(178, 450);
        this.obstacles = new ArrayList<>();
        this.coins = new ArrayList<>();
        this.difficulty = difficulty;
        this.score = 0;
        this.gameOver = false;
        this.random = new Random();
        this.trackOffsetY = 0;
    }

    public void update() throws CollisionException {
        if (gameOver) return;

        player.updateNitro();

        // Если включено Нитро — скорость трассы увеличивается в 2.5 раза
        int currentSpeed = difficulty.getSpeedMultiplier() + (player.isNitroActive() ? 12 : 4);
        trackOffsetY = (trackOffsetY + currentSpeed) % 40;

        int laneX = 120 + random.nextInt(3) * 60;

        // Спавн препятствий
        if (random.nextInt(100) < difficulty.getSpawnRate()) {
            if (random.nextBoolean()) {
                obstacles.add(new ConeObstacle(laneX, -40));
            } else {
                obstacles.add(new OilSpill(laneX, -40, currentSpeed));
            }
        }

        // Спавн бонусов
        if (random.nextInt(100) < 3) {
            coins.add(new CoinItem(laneX + 8, -30));
        }

        // Движение монеток
        Iterator<CoinItem> coinIter = coins.iterator();
        while (coinIter.hasNext()) {
            CoinItem coin = coinIter.next();
            coin.setY(coin.getY() + currentSpeed);

            if (coin.getBounds().intersects(player.getBounds())) {
                coin.applyEffect(player);
                coinIter.remove();
            } else if (coin.getY() > 600) {
                coinIter.remove();
            }
        }

        // Движение препятствий
        Iterator<Obstacle> obsIter = obstacles.iterator();
        while (obsIter.hasNext()) {
            Obstacle obs = obsIter.next();
            obs.setY(obs.getY() + currentSpeed);

            if (obs.getBounds().intersects(player.getBounds())) {
                obsIter.remove();
                if (obs instanceof OilSpill) {
                    player.takeDamage(15);
                } else if (obs instanceof ConeObstacle) {
                    player.takeDamage(35);
                }

                if (player.getHealth() <= 0) {
                    gameOver = true;
                    throw new CollisionException("Критический урон! Игра окончена.");
                }
            } else if (obs.getY() > 600) {
                obsIter.remove();
                score += player.isNitroActive() ? 20 : 10; // Больше очков на нитро
            }
        }
    }

    public PlayerCar getPlayer() { return player; }
    public List<Obstacle> getObstacles() { return obstacles; }
    public List<CoinItem> getCoins() { return coins; }
    public int getTotalScore() { return score + player.getBonusScore(); }
    public boolean isGameOver() { return gameOver; }
    public DifficultyLevel getDifficulty() { return difficulty; }
    public int getTrackOffsetY() { return trackOffsetY; }
}
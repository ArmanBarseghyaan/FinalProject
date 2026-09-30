package racing.ui;

import racing.exception.CollisionException;
import racing.model.CoinItem;
import racing.model.DifficultyLevel;
import racing.model.Obstacle;
import racing.model.OilSpill;
import racing.service.GameEngine;

import java.util.Scanner;

/**
 * Консольная версия (CLI) игры «Гонки».
 * Позволяет управлять автомобилем в текстовом терминале.
 */
public class ConsoleRacingGame {
    private final GameEngine engine;
    private final Scanner scanner;

    public ConsoleRacingGame(DifficultyLevel difficulty) {
        this.engine = new GameEngine(difficulty);
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("\n==========================================");
        System.out.println("   🏎️  JAVA RACING GAME (CONSOLE CLI)");
        System.out.println("==========================================");

        while (true) {
            printTrack();

            if (engine.isGameOver()) {
                System.out.println("\n💥 BUSTED! КРИТИЧЕСКИЙ УРОН! 💥");
                System.out.println("Итоговый счёт: " + engine.getTotalScore());
                System.out.println("\nВыберите действие:");
                System.out.println("  r — Начать заново (РЕСТАРТ)");
                System.out.println("  q — Выйти в главное меню");
                System.out.print("Выбор > ");

                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("r")) {
                    engine.restart();
                    continue;
                } else {
                    System.out.println("Возврат в меню...");
                    return;
                }
            }

            System.out.print("Управление (a-влево, d-вправо, w-нитро, s-вперед, r-рестарт, q-выход) > ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("q")) {
                System.out.println("Выход из гонок...");
                return;
            } else if (input.equals("r")) {
                engine.restart();
                continue;
            } else if (input.equals("a")) {
                engine.getPlayer().moveLeft();
            } else if (input.equals("d")) {
                engine.getPlayer().moveRight();
            }

            boolean nitro = input.contains("w");
            engine.getPlayer().setNitroActive(nitro);

            try {
                engine.update();
            } catch (CollisionException e) {
                System.out.println("\n[СТОЛКНОВЕНИЕ] " + e.getMessage());
            }
        }
    }

    private void printTrack() {
        System.out.println("\n-------------------------------------------------");
        int hp = engine.getPlayer().getHealth();
        int nos = engine.getPlayer().getNitroAmount();
        int score = engine.getTotalScore();
        int displaySpeed = (engine.getDifficulty().getSpeedMultiplier() * 18) + (engine.getPlayer().isNitroActive() ? 85 : 0);

        System.out.printf("HP: %3d/100 | NOS: %3d%% | СЧЁТ: %4d | СКОРОСТЬ: %3d KM/H\n", hp, nos, score, displaySpeed);
        System.out.println("-------------------------------------------------");

        int playerLane = getLaneIndex(engine.getPlayer().getX());

        String[][] grid = new String[8][3];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 3; c++) {
                grid[r][c] = " . ";
            }
        }

        for (Obstacle obs : engine.getObstacles()) {
            int row = obs.getY() / 75;
            int col = getLaneIndex(obs.getX());
            if (row >= 0 && row < 8 && col >= 0 && col < 3) {
                grid[row][col] = (obs instanceof OilSpill) ? "[~]" : "[X]";
            }
        }

        for (CoinItem coin : engine.getCoins()) {
            int row = coin.getY() / 75;
            int col = getLaneIndex(coin.getX());
            if (row >= 0 && row < 8 && col >= 0 && col < 3) {
                grid[row][col] = "($)";
            }
        }

        grid[6][playerLane] = engine.getPlayer().isNitroActive() ? "[🔥]" : "[🏎️]";

        System.out.println("|============== TRACE ==============|");
        for (int r = 0; r < 8; r++) {
            System.out.print("| ");
            for (int c = 0; c < 3; c++) {
                System.out.print(grid[r][c] + "   ");
            }
            System.out.println("|");
        }
        System.out.println("|===================================|");
    }

    private int getLaneIndex(int x) {
        if (x <= 150) return 0;
        if (x <= 210) return 1;
        return 2;
    }
}

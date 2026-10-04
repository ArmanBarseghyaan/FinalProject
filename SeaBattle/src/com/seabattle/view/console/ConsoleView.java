package com.seabattle.view.console;

import com.seabattle.controller.GameController;
import com.seabattle.model.*;
import com.seabattle.view.GameView;

import java.util.Scanner;

/**
 * Консольное представление (CLI) игры «Морской бой».
 * Отрисовывает поле символами (~ — вода, S — корабль, X — ранен/убит, • — промах).
 * Поддерживает команды: place, auto, clear, done, surrender, sound, help.
 */
public class ConsoleView implements GameView {
    private final GameController controller;
    private final Scanner scanner;

    public ConsoleView(GameController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void start() {
        System.out.println("=================================================");
        System.out.println("        ДОБРО ПОЖАЛОВАТЬ В «МОРСКОЙ БОЙ» (CLI)    ");
        System.out.println("=================================================");
        controller.getSoundManager().startBackgroundMusic();

        boolean running = true;
        while (running) {
            GameState state = controller.getGameState();
            GamePhase phase = state.getCurrentPhase();

            switch (phase) {
                case PLACEMENT_PLAYER_1:
                case PLACEMENT_PLAYER_2:
                    handlePlacementPhase();
                    break;
                case SWITCHING_PLAYER:
                    handleSwitchScreen();
                    break;
                case PLAYER_1_TURN:
                case PLAYER_2_TURN:
                    handleTurnPhase();
                    break;
                case GAME_OVER:
                    handleGameOverPhase();
                    running = false;
                    break;
            }
        }
    }

    private void handlePlacementPhase() {
        Player active = controller.getGameState().getActivePlayer();
        System.out.println("\n-------------------------------------------------");
        System.out.println(" ЭКРАН РАССТАНОВКИ: " + active.getName());
        System.out.println("-------------------------------------------------");
        printBoard(active.getBoard(), false);

        System.out.println("\nДоступные команды:");
        System.out.println("  place <размер 1-4> <h|v> <A1-J10> — поставить корабль (напр: place 4 h A1)");
        System.out.println("  auto                              — случайная авто-расстановка (можно повторять)");
        System.out.println("  clear                             — очистить поле");
        System.out.println("  sound                             — вкл/выкл звук");
        System.out.println("  done                              — подтвердить готовность расстановки");
        System.out.print("\n" + active.getName() + " > ");

        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return;

        String[] parts = input.split("\\s+");
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "auto":
                controller.autoPlaceFleet();
                System.out.println("Сгенерирована новая случайная расстановка флота!");
                break;
            case "clear":
                controller.clearFleet();
                System.out.println("Поле очищено.");
                break;
            case "sound":
                controller.getSoundManager().toggleSound();
                System.out.println("Звук " + (controller.getSoundManager().isSoundEnabled() ? "ВКЛЮЧЕН" : "ВЫКЛЮЧЕН"));
                break;
            case "done":
                if (!active.getBoard().isFleetFullyPlaced()) {
                    System.out.println("Ошибка! Вы должны расставить все 10 кораблей перед нажатием 'done'. Выставлено: " 
                            + active.getBoard().getShips().size() + "/10");
                } else {
                    controller.confirmPlacement();
                    System.out.println("Расстановка игрока " + active.getName() + " подтверждена!");
                }
                break;
            case "place":
                if (parts.length < 4) {
                    System.out.println("Неверный формат! Пример: place 4 h A1");
                    break;
                }
                try {
                    int size = Integer.parseInt(parts[1]);
                    ShipType type = getShipTypeBySize(size);
                    if (type == null) {
                        System.out.println("Некорректный размер корабля (допустимо: 1, 2, 3, 4)");
                        break;
                    }
                    Orientation orientation = parts[2].equalsIgnoreCase("v") ? Orientation.VERTICAL : Orientation.HORIZONTAL;
                    Coordinate coord = Coordinate.parse(parts[3]);

                    if (controller.placeShip(type, coord, orientation)) {
                        System.out.println("Корабль успешно установлен!");
                    } else {
                        System.out.println("Ошибка установки! Пересечение с другим кораблем или нарушение буферной зоны 1 клетка.");
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка ввода: " + e.getMessage());
                }
                break;
            default:
                System.out.println("Неизвестная команда. Введите help или используйте место/auto/clear/done.");
        }
    }

    private void handleSwitchScreen() {
        Player nextPlayer = (controller.getGameState().getCurrentPhase() == GamePhase.PLAYER_1_TURN) 
                ? controller.getGameState().getPlayer1() 
                : controller.getGameState().getPlayer2();

        System.out.println("\n=================================================");
        System.out.println("          ПЕРЕДАЧА УПРАВЛЕНИЯ (HOT-SEAT)          ");
        System.out.println("=================================================");
        System.out.println("Передайте компьютер игроку: " + (nextPlayer != null ? nextPlayer.getName() : "следующему игроку"));
        System.out.println("Нажмите ENTER, чтобы продолжить...");
        scanner.nextLine();
        controller.proceedFromSwitchScreen();
    }

    private void handleTurnPhase() {
        Player active = controller.getGameState().getActivePlayer();
        Player opponent = controller.getGameState().getOpponentPlayer();

        System.out.println("\n=================================================");
        System.out.println(" ХОД ИГРОКА: " + active.getName());
        System.out.println("=================================================");

        System.out.println("\n--- СВОЙ ФЛОТ (" + active.getName() + ") ---");
        printBoard(active.getBoard(), false);

        System.out.println("\n--- КАРТА ВЫСТРЕЛОВ ПО СОПЕРНИКУ (" + opponent.getName() + ") ---");
        printBoard(opponent.getBoard(), true); // true = Туман войны

        System.out.println("\nКоманды: <A1-J10> (выстрел), surrender (сдаться), sound (звук)");
        System.out.print(active.getName() + " > ");

        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return;

        if (input.equalsIgnoreCase("surrender")) {
            controller.surrenderCurrentPlayer();
            return;
        } else if (input.equalsIgnoreCase("sound")) {
            controller.getSoundManager().toggleSound();
            System.out.println("Звук " + (controller.getSoundManager().isSoundEnabled() ? "ВКЛЮЧЕН" : "ВЫКЛЮЧЕН"));
            return;
        }

        try {
            Coordinate target = Coordinate.parse(input);
            ShotResult result = controller.makeShot(target);

            switch (result) {
                case MISS:
                    System.out.println(">>> ПРОМАХ! (" + target.toAlgebraic() + ")");
                    handleSwitchScreen();
                    break;
                case HIT:
                    System.out.println(">>> ПОПАДАНИЕ! Игрок " + active.getName() + " делает повторный ход!");
                    break;
                case SUNK:
                    System.out.println(">>> УБИТ! Корабль соперника потоплен! Повторный ход!");
                    break;
                case ALREADY_SHOT:
                    System.out.println("Вы уже стреляли в клетку " + target.toAlgebraic() + "! Выберите другую.");
                    break;
            }
        } catch (Exception e) {
            System.out.println("Некорректный ввод координаты: " + e.getMessage());
        }
    }

    private void handleGameOverPhase() {
        Player winner = controller.getGameState().getWinner();
        System.out.println("\n=================================================");
        System.out.println("              ИГРА ОКОНЧЕНА!                     ");
        System.out.println("=================================================");
        System.out.println(" ПОБЕДИТЕЛЬ: " + (winner != null ? winner.getName() : "Ничья"));
        System.out.println("=================================================");
    }

    private void printBoard(Board board, boolean hideShips) {
        System.out.print("   ");
        for (int c = 0; c < Board.SIZE; c++) {
            System.out.print((char) ('A' + c) + " ");
        }
        System.out.println();

        for (int r = 0; r < Board.SIZE; r++) {
            System.out.printf("%2d ", (r + 1));
            for (int c = 0; c < Board.SIZE; c++) {
                Cell cell = board.getCell(r, c);
                CellState state = cell.getState();

                char symbol = '~'; // Вода по умолчанию
                if (state == CellState.HIT) {
                    symbol = 'X';
                } else if (state == CellState.MISS) {
                    symbol = '•';
                } else if (state == CellState.SHIP) {
                    symbol = hideShips ? '~' : 'S';
                }
                System.out.print(symbol + " ");
            }
            System.out.println();
        }
    }

    private ShipType getShipTypeBySize(int size) {
        switch (size) {
            case 4: return ShipType.BATTLESHIP;
            case 3: return ShipType.CRUISER;
            case 2: return ShipType.DESTROYER;
            case 1: return ShipType.SUBMARINE;
            default: return null;
        }
    }
}

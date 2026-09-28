package com.seabattle.controller;

import com.seabattle.model.*;

/**
 * Контроллер (Controller) по паттерну MVC.
 * Управляет логикой ходов, переключением фаз игры, обработкой выстрелов,
 * сдачей игрока и взаимодействием с аудиосистемой.
 */
public class GameController {
    private final GameState gameState;
    private final SoundManager soundManager;

    public GameController(String player1Name, String player2Name) {
        this.gameState = new GameState(player1Name, player2Name);
        this.soundManager = SoundManager.getInstance();
    }

    public GameState getGameState() {
        return gameState;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    /**
     * Попытка ручной установки корабля активным игроком во время фазы PLACEMENT.
     */
    public boolean placeShip(ShipType type, Coordinate start, Orientation orientation) {
        Player active = gameState.getActivePlayer();
        if (active == null || (gameState.getCurrentPhase() != GamePhase.PLACEMENT_PLAYER_1 && gameState.getCurrentPhase() != GamePhase.PLACEMENT_PLAYER_2)) {
            return false;
        }
        return active.getBoard().placeShip(type, start, orientation);
    }

    /**
     * Случайная авто-расстановка для активного игрока.
     */
    public boolean autoPlaceFleet() {
        Player active = gameState.getActivePlayer();
        if (active == null) return false;
        return active.getBoard().autoPlaceAllShips();
    }

    /**
     * Очистка поля активного игрока.
     */
    public void clearFleet() {
        Player active = gameState.getActivePlayer();
        if (active != null) {
            active.getBoard().clear();
        }
    }

    /**
     * Подтверждение готовности расстановки текущего игрока.
     */
    public boolean confirmPlacement() {
        Player active = gameState.getActivePlayer();
        if (active == null || !active.getBoard().isFleetFullyPlaced()) {
            return false;
        }
        active.setReady(true);

        if (gameState.getCurrentPhase() == GamePhase.PLACEMENT_PLAYER_1) {
            gameState.setCurrentPhase(GamePhase.PLACEMENT_PLAYER_2);
        } else if (gameState.getCurrentPhase() == GamePhase.PLACEMENT_PLAYER_2) {
            // Оба игрока расставили корабли — переходим к передаче хода Игроку 1
            gameState.setCurrentPhase(GamePhase.SWITCHING_PLAYER);
        }
        return true;
    }

    /**
     * Завершение экрана передачи хода и переход к очередному ходу.
     */
    public void proceedFromSwitchScreen() {
        if (gameState.getCurrentPhase() == GamePhase.SWITCHING_PLAYER) {
            if (!gameState.getPlayer1().isReady() || !gameState.getPlayer2().isReady()) {
                return;
            }
            // По умолчанию начинать с Игрока 1 или переключать
            if (gameState.getWinner() != null) {
                gameState.setCurrentPhase(GamePhase.GAME_OVER);
            } else {
                gameState.setCurrentPhase(GamePhase.PLAYER_1_TURN);
            }
        }
    }

    /**
     * Совершение выстрела активным игроком по полю противника.
     */
    public ShotResult makeShot(Coordinate coord) {
        GamePhase current = gameState.getCurrentPhase();
        if (current != GamePhase.PLAYER_1_TURN && current != GamePhase.PLAYER_2_TURN) {
            return ShotResult.ALREADY_SHOT;
        }

        Player active = gameState.getActivePlayer();
        Player opponent = gameState.getOpponentPlayer();
        if (active == null || opponent == null) {
            return ShotResult.ALREADY_SHOT;
        }

        Board opponentBoard = opponent.getBoard();
        ShotResult result = opponentBoard.shoot(coord);

        switch (result) {
            case MISS:
                soundManager.playMissSound();
                // При промахе ход передается другому игроку через экран переключения
                switchTurn();
                break;

            case HIT:
                soundManager.playHitSound();
                // При попадании игрок делает повторный ход (фаза не меняется)
                break;

            case SUNK:
                soundManager.playSunkSound();
                // Проверка условий победы
                if (opponentBoard.allShipsSunk()) {
                    gameState.setWinner(active);
                    gameState.setCurrentPhase(GamePhase.GAME_OVER);
                }
                // При потоплении игрок также делает повторный ход
                break;

            case ALREADY_SHOT:
                // Не меняем состояние
                break;
        }

        return result;
    }

    /**
     * Переключение хода между игроками.
     */
    private void switchTurn() {
        if (gameState.getCurrentPhase() == GamePhase.PLAYER_1_TURN) {
            gameState.setCurrentPhase(GamePhase.PLAYER_2_TURN);
        } else if (gameState.getCurrentPhase() == GamePhase.PLAYER_2_TURN) {
            gameState.setCurrentPhase(GamePhase.PLAYER_1_TURN);
        }
    }

    /**
     * Кнопка/команда «Сдаться».
     */
    public void surrenderCurrentPlayer() {
        Player active = gameState.getActivePlayer();
        Player opponent = gameState.getOpponentPlayer();
        if (active != null && opponent != null && 
           (gameState.getCurrentPhase() == GamePhase.PLAYER_1_TURN || gameState.getCurrentPhase() == GamePhase.PLAYER_2_TURN)) {
            gameState.setWinner(opponent);
            gameState.setCurrentPhase(GamePhase.GAME_OVER);
        }
    }

    /**
     * Начать новую игру заново.
     */
    public void restartGame() {
        gameState.reset();
    }
}

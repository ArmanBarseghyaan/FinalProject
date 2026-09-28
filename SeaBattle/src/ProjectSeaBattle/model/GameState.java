package com.seabattle.model;

/**
 * Хранилище текущего состояния игровой сессии.
 */
public class GameState {
    private final Player player1;
    private final Player player2;
    private GamePhase currentPhase;
    private Player winner;

    public GameState(String player1Name, String player2Name) {
        this.player1 = new Player(player1Name);
        this.player2 = new Player(player2Name);
        this.currentPhase = GamePhase.PLACEMENT_PLAYER_1;
        this.winner = null;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(GamePhase phase) {
        this.currentPhase = phase;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    /**
     * Возвращает игрока, чей ход активен в данный момент.
     */
    public Player getActivePlayer() {
        if (currentPhase == GamePhase.PLACEMENT_PLAYER_1 || currentPhase == GamePhase.PLAYER_1_TURN) {
            return player1;
        } else if (currentPhase == GamePhase.PLACEMENT_PLAYER_2 || currentPhase == GamePhase.PLAYER_2_TURN) {
            return player2;
        }
        return null; // Во время SWITCHING_PLAYER или GAME_OVER
    }

    /**
     * Возвращает противника текущего активного игрока.
     */
    public Player getOpponentPlayer() {
        Player active = getActivePlayer();
        if (active == player1) {
            return player2;
        } else if (active == player2) {
            return player1;
        }
        return null;
    }

    /**
     * Перезапуск игры заново (возврат к расстановке).
     */
    public void reset() {
        player1.reset();
        player2.reset();
        winner = null;
        currentPhase = GamePhase.PLACEMENT_PLAYER_1;
    }
}

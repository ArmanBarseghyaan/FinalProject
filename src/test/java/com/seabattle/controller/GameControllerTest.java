package com.seabattle.controller;

import com.seabattle.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты контроллера логики Морского боя (GameController)")
class GameControllerTest {

    private GameController controller;

    @BeforeEach
    void setUp() {
        controller = new GameController("Игрок 1", "Игрок 2");
    }

    @Test
    @DisplayName("Инициализация контроллера и начальная фаза PLACEMENT_PLAYER_1")
    void testInitialState() {
        assertNotNull(controller.getGameState());
        assertEquals(GamePhase.PLACEMENT_PLAYER_1, controller.getGameState().getCurrentPhase());
        assertEquals("Игрок 1", controller.getGameState().getActivePlayer().getName());
    }

    @Test
    @DisplayName("Авто-расстановка и подтверждение готовности игроков")
    void testConfirmPlacementFlow() {
        // 1. Расстановка первого игрока
        controller.autoPlaceFleet();
        assertTrue(controller.confirmPlacement());
        assertEquals(GamePhase.PLACEMENT_PLAYER_2, controller.getGameState().getCurrentPhase());

        // 2. Расстановка второго игрока
        controller.autoPlaceFleet();
        assertTrue(controller.confirmPlacement());
        assertEquals(GamePhase.SWITCHING_PLAYER, controller.getGameState().getCurrentPhase());

        // 3. Переход к первому ходу
        controller.proceedFromSwitchScreen();
        assertEquals(GamePhase.PLAYER_1_TURN, controller.getGameState().getCurrentPhase());
    }

    @Test
    @DisplayName("Сдача игрока (Surrender)")
    void testSurrenderCurrentPlayer() {
        controller.autoPlaceFleet();
        controller.confirmPlacement();
        controller.autoPlaceFleet();
        controller.confirmPlacement();
        controller.proceedFromSwitchScreen();

        // Сейчас ход Игрока 1
        assertEquals(GamePhase.PLAYER_1_TURN, controller.getGameState().getCurrentPhase());

        controller.surrenderCurrentPlayer();
        assertEquals(GamePhase.GAME_OVER, controller.getGameState().getCurrentPhase());
        assertEquals("Игрок 2", controller.getGameState().getWinner().getName());
    }

    @Test
    @DisplayName("Перезапуск игры заново (Restart)")
    void testRestartGame() {
        controller.restartGame();
        assertEquals(GamePhase.PLACEMENT_PLAYER_1, controller.getGameState().getCurrentPhase());
        assertNull(controller.getGameState().getWinner());
    }
}

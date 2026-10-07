package finalproject.pacman.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты лабиринта Pac-Man")
class MazeTest {

    @Test
    @DisplayName("Лабиринт создается связным, с границами и проходом-туннелем")
    void createsConnectedMazeAndTunnel() {
        Maze maze = new Maze();

        assertEquals(28 * Maze.TILE_SIZE, maze.getWidth());
        assertEquals(31 * Maze.TILE_SIZE, maze.getHeight());
        assertFalse(maze.isWalkable(0, 1));
        assertTrue(maze.isWalkable(1, 1));
        assertTrue(maze.isWalkable(Maze.TUNNEL_ROW, 0));
        assertTrue(maze.isWalkable(Maze.TUNNEL_ROW, Maze.COLUMNS - 1));
        assertTrue(maze.isTunnelWrap(Maze.TUNNEL_ROW, 0, Direction.LEFT));
        assertTrue(maze.isTunnelWrap(Maze.TUNNEL_ROW, Maze.COLUMNS - 1, Direction.RIGHT));
        assertFalse(maze.isTunnelWrap(Maze.TUNNEL_ROW, 0, Direction.RIGHT));
    }

    @Test
    @DisplayName("Точки и энерджайзеры собираются один раз и восстанавливаются")
    void collectsAndResetsPellets() {
        Maze maze = new Maze();
        int initialCount = maze.getRemainingPellets();

        assertTrue(maze.hasPellet(1, 1));
        assertTrue(maze.collectPellet(1, 1));
        assertFalse(maze.collectPellet(1, 1));
        assertEquals(initialCount - 1, maze.getRemainingPellets());

        assertTrue(maze.hasEnergizer(3, 1));
        assertFalse(maze.hasPellet(3, 1));
        assertTrue(maze.collectEnergizer(3, 1));
        assertFalse(maze.collectEnergizer(3, 1));
        assertEquals(initialCount - 2, maze.getRemainingPellets());

        maze.resetCollectibles();
        assertEquals(initialCount, maze.getRemainingPellets());
        assertTrue(maze.hasPellet(1, 1));
        assertTrue(maze.hasEnergizer(3, 1));
    }

    @Test
    @DisplayName("Маршрут к цели выбирает кратчайшее направление")
    void findsShortestDirectionToTarget() {
        Maze maze = new Maze();

        assertEquals(Direction.RIGHT, maze.directionToward(1, 1, 1, 4, Direction.STOP));
        assertEquals(Direction.LEFT, maze.directionToward(1, 4, 1, 1, Direction.STOP));
    }

    @Test
    @DisplayName("Домик призраков и его двери распознаются")
    void identifiesGhostHouseAndDoors() {
        Maze maze = new Maze();

        assertTrue(maze.isInsideGhostHouse(15, 13));
        assertFalse(maze.isInsideGhostHouse(13, 13));
        assertTrue(maze.isGhostDoor(13, 13));
        assertFalse(maze.isGhostDoor(13, 12));
    }
}

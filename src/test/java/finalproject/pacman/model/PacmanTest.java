package finalproject.pacman.model;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты персонажей Pac-Man")
class PacmanTest {

    @Test
    @DisplayName("Пакман движется по запрошенному направлению и сбрасывается для новой жизни")
    void movesAndResetsForNewLife() {
        Maze maze = new Maze();
        Pacman pacman = new Pacman(1, 1);
        double startX = pacman.getX();

        pacman.requestDirection(Direction.RIGHT);
        pacman.move(maze);
        assertEquals(Direction.RIGHT, pacman.getDirection());
        assertEquals(startX + 3.0, pacman.getX(), 0.001);

        pacman.resetForNewLife();
        assertEquals(1, pacman.getRow(maze));
        assertEquals(1, pacman.getColumn(maze));
        assertEquals(Direction.STOP, pacman.getDirection());
        assertEquals(maze.centerX(1), pacman.getX());
    }

    @Test
    @DisplayName("Призрак преследует Пакмана и переключает испуганное состояние")
    void ghostChasesAndCanBeFrightened() {
        Maze maze = new Maze();
        Pacman pacman = new Pacman(1, 5);
        Ghost ghost = new Ghost(1, 1, Color.RED);
        double startX = ghost.getX();

        ghost.chase(maze, pacman);
        assertEquals(Direction.RIGHT, ghost.getDirection());
        assertTrue(ghost.getX() > startX);

        ghost.setFrightened(true);
        assertTrue(ghost.isFrightened());
        ghost.setFrightened(false);
        assertFalse(ghost.isFrightened());
    }
}

package racing.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import racing.exception.CollisionException;
import racing.model.CoinItem;
import racing.model.ConeObstacle;
import racing.model.DifficultyLevel;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты игрового движка гонок")
class GameEngineTest {

    @Test
    @DisplayName("Обновление двигает трассу в соответствии со сложностью")
    void updateAdvancesTrack() throws CollisionException {
        GameEngine engine = new GameEngine(DifficultyLevel.MEDIUM);

        engine.update();

        assertEquals(10, engine.getTrackOffsetY());
        assertFalse(engine.isGameOver());
        assertEquals(DifficultyLevel.MEDIUM, engine.getDifficulty());
    }

    @Test
    @DisplayName("Столкновение с конусом наносит урон и удаляет препятствие")
    void coneCollisionDamagesPlayer() throws CollisionException {
        GameEngine engine = new GameEngine(DifficultyLevel.MEDIUM);
        engine.getObstacles().add(new ConeObstacle(
                engine.getPlayer().getX(), engine.getPlayer().getY() - 10));

        engine.update();

        assertEquals(65, engine.getPlayer().getHealth());
        assertTrue(engine.getObstacles().isEmpty());
        assertFalse(engine.isGameOver());
    }

    @Test
    @DisplayName("Перезапуск восстанавливает начальное состояние гонки")
    void restartRestoresInitialState() {
        GameEngine engine = new GameEngine(DifficultyLevel.HARD);
        engine.getPlayer().takeDamage(40);
        engine.getPlayer().addScore(100);
        engine.getPlayer().setNitroActive(true);
        engine.getObstacles().add(new ConeObstacle(120, 100));
        engine.getCoins().add(new CoinItem(120, 80));

        engine.restart();

        assertEquals(100, engine.getPlayer().getHealth());
        assertEquals(0, engine.getPlayer().getBonusScore());
        assertEquals(100, engine.getPlayer().getNitroAmount());
        assertFalse(engine.getPlayer().isNitroActive());
        assertEquals(178, engine.getPlayer().getX());
        assertEquals(450, engine.getPlayer().getY());
        assertTrue(engine.getObstacles().isEmpty());
        assertTrue(engine.getCoins().isEmpty());
        assertEquals(0, engine.getTotalScore());
        assertEquals(0, engine.getTrackOffsetY());
        assertFalse(engine.isGameOver());
    }
}

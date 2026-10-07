package racing.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import racing.exception.InvalidSpeedException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты машины игрока в гонках")
class PlayerCarTest {

    @Test
    @DisplayName("Повороты ограничены шириной трассы")
    void steeringStaysWithinTrackBounds() {
        PlayerCar car = new PlayerCar(178, 450);

        for (int i = 0; i < 100; i++) {
            car.steerLeft();
        }
        assertEquals(84, car.getX());

        for (int i = 0; i < 100; i++) {
            car.steerRight();
        }
        assertEquals(272, car.getX());
    }

    @Test
    @DisplayName("Урон и очки применяются с правильными ограничениями")
    void damageAndScoreAreApplied() {
        PlayerCar car = new PlayerCar(178, 450);

        car.takeDamage(35);
        car.addScore(50);
        assertEquals(65, car.getHealth());
        assertEquals(50, car.getBonusScore());

        car.takeDamage(100);
        assertEquals(0, car.getHealth());
    }

    @Test
    @DisplayName("Нитро расходуется при ускорении и восстанавливается после него")
    void nitroConsumesAndRecharges() {
        PlayerCar car = new PlayerCar(178, 450);
        car.setNitroActive(true);

        car.updateNitro();
        assertEquals(98, car.getNitroAmount());
        assertTrue(car.isNitroActive());

        for (int i = 0; i < 49; i++) {
            car.updateNitro();
        }
        assertEquals(0, car.getNitroAmount());
        assertFalse(car.isNitroActive());

        car.updateNitro();
        assertEquals(1, car.getNitroAmount());
    }

    @Test
    @DisplayName("Скорость задается только в диапазоне от 0 до 25")
    void validatesSpeedRange() throws InvalidSpeedException {
        PlayerCar car = new PlayerCar(178, 450);

        car.setSpeed(0);
        assertEquals(0, car.getSpeed());
        car.setSpeed(25);
        assertEquals(25, car.getSpeed());
        assertThrows(InvalidSpeedException.class, () -> car.setSpeed(-1));
        assertThrows(InvalidSpeedException.class, () -> car.setSpeed(26));
    }
}

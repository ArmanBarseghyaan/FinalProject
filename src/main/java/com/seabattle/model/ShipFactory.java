package com.seabattle.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Паттерн Фабрика (Factory) для удобного создания и получения стандартизированных
 * типов кораблей и полного состава флота по правилам классического Морского боя.
 */
public class ShipFactory {

    /**
     * Создает базовый список кораблей, которые должны быть размещены на поле каждого игрока:
     * - 1x 4-палубный
     * - 2x 3-палубных
     * - 3x 2-палубных
     * - 4x 1-палубных
     */
    public static List<ShipType> createStandardFleetTypes() {
        List<ShipType> fleet = new ArrayList<>();
        fleet.add(ShipType.BATTLESHIP);
        
        fleet.add(ShipType.CRUISER);
        fleet.add(ShipType.CRUISER);
        
        fleet.add(ShipType.DESTROYER);
        fleet.add(ShipType.DESTROYER);
        fleet.add(ShipType.DESTROYER);
        
        fleet.add(ShipType.SUBMARINE);
        fleet.add(ShipType.SUBMARINE);
        fleet.add(ShipType.SUBMARINE);
        fleet.add(ShipType.SUBMARINE);
        
        return fleet;
    }

    /**
     * Создает экземпляр конкретного корабля с проверкой параметров.
     */
    public static Ship createShip(ShipType type, Coordinate bow, Orientation orientation) {
        return new Ship(type, bow, orientation);
    }
}

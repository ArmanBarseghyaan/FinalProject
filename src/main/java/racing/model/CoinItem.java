package racing.model;

import racing.service.Collectible;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CoinItem extends GameObject implements Collectible {

    public CoinItem(int x, int y) {
        super(x, y, 24, 24);
    }

    @Override
    public void applyEffect(PlayerCar player) {
        player.addScore(50);
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Тень
        gc.setFill(Color.rgb(0, 0, 0, 60 / 255.0));
        gc.fillOval(getX() + 2, getY() + getHeight() - 4, getWidth() - 4, 6);

        // Внешнее кольцо (темно-золотое)
        gc.setFill(Color.rgb(218, 165, 32));
        gc.fillOval(getX(), getY(), getWidth(), getHeight());

        // Внутренний блик (ярко-желтый)
        gc.setFill(Color.rgb(255, 215, 0));
        gc.fillOval(getX() + 2, getY() + 2, getWidth() - 4, getHeight() - 4);

        // Символ доллара
        gc.setFill(Color.rgb(139, 69, 19));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("$", getX() + 8, getY() + 17);
    }
}

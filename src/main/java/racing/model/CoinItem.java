package racing.model;

import racing.service.Collectible;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class CoinItem extends GameObject implements Collectible {

    public CoinItem(int x, int y) {
        super(x, y, 24, 24);
    }

    @Override
    public void applyEffect(PlayerCar player) {
        player.addScore(50);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Тень
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillOval(getX() + 2, getY() + getHeight() - 4, getWidth() - 4, 6);

        // Внешнее кольцо (темно-золотое)
        g2d.setColor(new Color(218, 165, 32));
        g2d.fillOval(getX(), getY(), getWidth(), getHeight());

        // Внутренний блик (ярко-желтый)
        g2d.setColor(new Color(255, 215, 0));
        g2d.fillOval(getX() + 2, getY() + 2, getWidth() - 4, getHeight() - 4);

        // Символ доллара
        g2d.setColor(new Color(139, 69, 19));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("$", getX() + 8, getY() + 17);
    }
}
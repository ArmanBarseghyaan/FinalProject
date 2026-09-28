package racing;

import racing.model.DifficultyLevel;
import racing.ui.GamePanel;
import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Java Racing Game");

        // Можно выставить EASY, MEDIUM или HARD
        GamePanel gamePanel = new GamePanel(DifficultyLevel.MEDIUM);

        frame.add(gamePanel);
        frame.setSize(400, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
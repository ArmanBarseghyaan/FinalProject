import racing.model.DifficultyLevel;

import com.seabattle.controller.GameController;
import com.seabattle.view.console.ConsoleView;
import com.seabattle.view.gui.SwingGuiView;
import com.seabattle.network.NetworkGuiView;
import finalproject.pacman.ui.PacmanGamePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Scanner;

/**
 * Единый главный класс (Main) для игрового меню:
 * 1. Гонки (RacingGame)
 * 2. Морской бой (SeaBattle — Сетевой и Hot-seat)
 * 3. Змейка (Snake Game)
 * 4. Пакман (Pac-Man)
 */
public class Main {

    public static void main(String[] args) {
        boolean forceCli = false;
        if (args.length > 0 && args[0].equalsIgnoreCase("--cli")) {
            forceCli = true;
        }

        if (forceCli || GraphicsEnvironment.isHeadless()) {
            runConsoleMenu();
        } else {
            SwingUtilities.invokeLater(Main::runGuiMenu);
        }
    }

    // ==========================================
    // GUI ИГРОВОЕ МЕНЮ (SWING)
    // ==========================================
    private static void runGuiMenu() {
        JFrame menuFrame = new JFrame("FinalProject - Игровой центр");
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setSize(650, 660);
        menuFrame.setMinimumSize(new Dimension(550, 580));
        menuFrame.setLocationRelativeTo(null);

        Color bgDark = new Color(18, 22, 32);
        Color panelBg = new Color(28, 36, 52);
        Color accentColor = new Color(52, 152, 219);
        Color textColor = new Color(230, 238, 250);
        Color textMuted = new Color(140, 155, 180);

        menuFrame.getContentPane().setBackground(bgDark);
        menuFrame.setLayout(new BorderLayout(0, 15));

        // --- Верхняя панель (Заголовок) ---
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(panelBg);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("СБОРНИК ИГР FINAL PROJECT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(accentColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Выберите игру для начала прохождения");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(textMuted);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(subtitleLabel);

        menuFrame.add(headerPanel, BorderLayout.NORTH);

        // --- Центральная панель с карточками игр ---
        JPanel gamesPanel = new JPanel(new GridLayout(4, 1, 12, 12));
        gamesPanel.setBackground(bgDark);
        gamesPanel.setBorder(new EmptyBorder(0, 25, 10, 25));

        // 1. Карточка "Гонки (Racing Game)"
        gamesPanel.add(createGameCard(
                "ГОНКИ (Racing Game)",
                "Аркадный стилизованный автосимулятор с нитро, монетами и препятствиями",
                new Color(230, 126, 34),
                panelBg, textColor, textMuted,
                e -> launchRacingGameDialog(menuFrame)
        ));

        // 2. Карточка "Морской Бой (Sea Battle)"
        gamesPanel.add(createGameCard(
                "МОРСКОЙ БОЙ (Sea Battle)",
                "Сетевая игра на 2 ПК через Sockets (или Hot-seat) с реалистичной водой и музыкой",
                new Color(41, 128, 185),
                panelBg, textColor, textMuted,
                e -> launchSeaBattleGUI()
        ));

        // 3. Карточка "Змейка (Snake Game)"
        gamesPanel.add(createGameCard(
                "ЗМЕЙКА (Snake Game)",
                "Классическая аркада с растущей змейкой, едой и подсчетом очков",
                new Color(39, 174, 96),
                panelBg, textColor, textMuted,
                e -> launchSnakeGame()
        ));

        // 4. Карточка «Пакман»
        gamesPanel.add(createGameCard(
                "ПАКМАН (Pac-Man)",
                "Собирайте точки в связном лабиринте и избегайте призраков",
                new Color(241, 196, 15),
                panelBg, textColor, textMuted,
                e -> launchPacmanGame()
        ));

        menuFrame.add(gamesPanel, BorderLayout.CENTER);

        // --- Нижняя панель (Кнопка Выхода) ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 15));
        footerPanel.setBackground(bgDark);

        JButton exitBtn = new JButton("ВЫЙТИ ИЗ ПРОГРАММЫ");
        exitBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        exitBtn.setBackground(new Color(192, 57, 43));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setFocusPainted(false);
        exitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exitBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        exitBtn.addActionListener(e -> System.exit(0));

        footerPanel.add(exitBtn);
        menuFrame.add(footerPanel, BorderLayout.SOUTH);

        menuFrame.setVisible(true);
    }

    private static JPanel createGameCard(String title, String description, Color accent,
                                         Color panelBg, Color textColor, Color textMuted,
                                         ActionListener action) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(panelBg);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(accent.darker(), 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(accent);

        JLabel descLbl = new JLabel("<html>" + description + "</html>");
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLbl.setForeground(textMuted);

        infoPanel.add(titleLbl);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(descLbl);

        card.add(infoPanel, BorderLayout.CENTER);

        JButton playBtn = new JButton("ЗАПУСТИТЬ");
        playBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        playBtn.setBackground(accent);
        playBtn.setForeground(Color.WHITE);
        playBtn.setFocusPainted(false);
        playBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        playBtn.setBorder(new EmptyBorder(10, 18, 10, 18));
        playBtn.addActionListener(action);

        JPanel btnPanel = new JPanel(new GridBagLayout());
        btnPanel.setOpaque(false);
        btnPanel.add(playBtn);

        card.add(btnPanel, BorderLayout.EAST);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        playBtn.setToolTipText("Запустить: " + title);

        MouseAdapter launchOnClick = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                action.actionPerformed(new java.awt.event.ActionEvent(
                        event.getSource(), java.awt.event.ActionEvent.ACTION_PERFORMED, "launch"));
            }
        };
        card.addMouseListener(launchOnClick);
        infoPanel.addMouseListener(launchOnClick);
        titleLbl.addMouseListener(launchOnClick);
        descLbl.addMouseListener(launchOnClick);
        card.setToolTipText("Нажмите, чтобы запустить " + title);
        infoPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        titleLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        descLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return card;
    }

    // ==========================================
    // ЗАПУСК ИГР
    // ==========================================

    private static void launchRacingGameDialog(JFrame parentFrame) {
        String[] options = {"Легкий (EASY)", "Средний (MEDIUM)", "Сложный (HARD)"};
        int choice = JOptionPane.showOptionDialog(
                parentFrame,
                "Выберите уровень сложности гонок:",
                "Настройка гонок",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]
        );

        if (choice < 0) return;

        DifficultyLevel level = DifficultyLevel.MEDIUM;
        if (choice == 0) level = DifficultyLevel.EASY;
        else if (choice == 2) level = DifficultyLevel.HARD;

        JFrame gameFrame = new JFrame("Java Racing Game - " + level.name());
        racing.ui.GamePanel gamePanel = new racing.ui.GamePanel(level);
        gameFrame.add(gamePanel);
        gameFrame.setSize(400, 600);
        gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gameFrame.setLocationRelativeTo(parentFrame);
        gameFrame.setResizable(false);
        gameFrame.setVisible(true);
    }

    private static void launchSeaBattleGUI() {
        String[] options = {"Сетевая игра (Client-Server LAN)", "Локальная игра (Hot-seat на 1 ПК)"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Выберите режим игры в «Морской бой»:",
                "Выбор режима «Морской бой»",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            NetworkGuiView view = new NetworkGuiView();
            view.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            view.setVisible(true);
        } else if (choice == 1) {
            GameController controller = new GameController("Игрок 1", "Игрок 2");
            SwingGuiView view = new SwingGuiView(controller);
            view.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            view.start();
        }
    }

    private static void launchSnakeGame() {
        JFrame frame = new JFrame("Змейка (Snake Game)");
        com.snake.ui.GamePanel gamePanel = new com.snake.ui.GamePanel();
        frame.add(gamePanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void launchPacmanGame() {
        JFrame frame = new JFrame("Пакман (Pac-Man)");
        PacmanGamePanel gamePanel = new PacmanGamePanel();
        frame.add(gamePanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent event) {
                gamePanel.stopGame();
            }
        });
        frame.setVisible(true);
        SwingUtilities.invokeLater(gamePanel::requestFocusInWindow);
    }

    // ==========================================
    // КОНСОЛЬНОЕ МЕНЮ (CLI)
    // ==========================================
    private static void runConsoleMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n==========================================");
            System.out.println("   FINAL PROJECT - ИГРОВОЙ ЦЕНТР");
            System.out.println("==========================================");
            System.out.println("1. Гонки (Racing Game)");
            System.out.println("2. Морской бой (Sea Battle - CLI)");
            System.out.println("3. Морской бой (Sea Battle - GUI)");
            System.out.println("4. Змейка (Snake Game)");
            System.out.println("5. Пакман (Pac-Man)");
            System.out.println("6. Выход");
            System.out.print("Выберите пункт (1-6): ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    System.out.println("Запуск гонок...");
                    SwingUtilities.invokeLater(() -> launchRacingGameDialog(null));
                    break;
                case "2":
                    GameController cliController = new GameController("Игрок 1", "Игрок 2");
                    ConsoleView consoleView = new ConsoleView(cliController);
                    consoleView.start();
                    break;
                case "3":
                    SwingUtilities.invokeLater(Main::launchSeaBattleGUI);
                    break;
                case "4":
                    System.out.println("Запуск Змейки...");
                    SwingUtilities.invokeLater(Main::launchSnakeGame);
                    break;
                case "5":
                    if (GraphicsEnvironment.isHeadless()) {
                        System.out.println("Для запуска Пакмана требуется графическая среда.");
                    } else {
                        System.out.println("Запуск Пакмана...");
                        SwingUtilities.invokeLater(Main::launchPacmanGame);
                    }
                    break;
                case "6":
                    System.out.println("До свидания!");
                    return;
                default:
                    System.out.println("Неверный ввод, попробуйте снова.");
            }
        }
    }
}

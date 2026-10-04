package com.seabattle.network;

import com.seabattle.controller.SoundManager;
import com.seabattle.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * Графический интерфейс для сетевой игры «Морской бой» (Client-Server по Java Sockets).
 * Полностью сохраняет дизайн, графику воды, отрисовку кораблей и взрывов из оригинального GUI.
 */
public class NetworkGuiView extends JFrame implements NetworkManager.NetworkListener {

    private static final Color COLOR_BG         = new Color(20, 24, 32);
    private static final Color COLOR_PANEL_BG   = new Color(28, 35, 48);
    private static final Color COLOR_TEXT        = new Color(225, 232, 244);
    private static final Color COLOR_TEXT_MUTED  = new Color(130, 145, 168);
    private static final Color COLOR_ACCENT      = new Color(52, 152, 219);
    private static final Color COLOR_SUCCESS     = new Color(46, 204, 113);
    private static final Color COLOR_DANGER      = new Color(231, 76, 60);
    private static final Color COLOR_WARNING     = new Color(241, 196, 15);

    private final NetworkManager networkManager;
    private final SoundManager soundManager;

    private Board myBoard;
    private Board opponentBoard;

    private boolean isHost = false;
    private boolean myReady = false;
    private boolean opponentReady = false;
    private boolean myTurn = false;
    private boolean gameStarted = false;
    private boolean gameOver = false;
    private boolean waitingForShotResponse = false;
    private String winnerName = "";

    private JPanel mainContentPanel;
    private JLabel statusLabel;
    private JButton soundButton;

    // Элементы экрана подключения
    private JTextField ipField;
    private JTextField portField;
    private JLabel connectionStatusLabel;
    private JButton hostBtn;
    private JButton connectBtn;

    // Параметры расстановки
    private ShipType selectedShipType = ShipType.BATTLESHIP;
    private Orientation selectedOrientation = Orientation.HORIZONTAL;

    public NetworkGuiView() {
        this.networkManager = new NetworkManager(this);
        this.soundManager = SoundManager.getInstance();
        this.myBoard = new Board();
        this.opponentBoard = new Board();

        setTitle("Морской бой — Сетевая игра (LAN)");
        setSize(1080, 740);
        setMinimumSize(new Dimension(980, 680));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (networkManager != null) {
                    networkManager.sendMessage("DISCONNECT");
                    networkManager.closeConnection();
                }
            }
        });

        initTopBar();

        mainContentPanel = new JPanel(new CardLayout());
        mainContentPanel.setBackground(COLOR_BG);
        add(mainContentPanel, BorderLayout.CENTER);

        showConnectionScreen();
    }

    // ───────────────────────────────────────────────────────
    //  ВЕРХНЯЯ ПАНЕЛЬ
    // ───────────────────────────────────────────────────────
    private void initTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(15, 0));
        topBar.setBackground(COLOR_PANEL_BG);
        topBar.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("МОРСКОЙ БОЙ (СЕТЬ)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
        titleLabel.setForeground(COLOR_TEXT);
        titleLabel.setIcon(new SimpleIcon(SimpleIcon.Type.ANCHOR, 22, 22));
        titleLabel.setIconTextGap(10);
        topBar.add(titleLabel, BorderLayout.WEST);

        statusLabel = new JLabel("Подключение по сети...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(COLOR_ACCENT);
        topBar.add(statusLabel, BorderLayout.CENTER);

        soundButton = new JButton("Музыка: Выкл");
        soundButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        soundButton.setBackground(new Color(38, 48, 66));
        soundButton.setForeground(COLOR_TEXT);
        soundButton.setFocusPainted(false);
        soundButton.setIcon(new SimpleIcon(SimpleIcon.Type.SPEAKER_ON, 16, 16));
        soundButton.setIconTextGap(8);
        soundButton.addActionListener(e -> {
            soundManager.toggleSound();
            boolean on = soundManager.isSoundEnabled();
            SoundManager.Soundtrack cur = soundManager.getCurrentSoundtrack();
            soundButton.setText(on ? cur.title : "Музыка: Выкл");
            soundButton.setIcon(new SimpleIcon(on ? SimpleIcon.Type.SPEAKER_ON : SimpleIcon.Type.SPEAKER_OFF, 16, 16));
        });
        topBar.add(soundButton, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);
    }

    private void showSoundtrackDialog() {
        SoundManager.Soundtrack[] options = SoundManager.Soundtrack.values();
        JDialog dialog = new JDialog(this, "Выберите музыку для битвы", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(420, 340);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(COLOR_BG);
        dialog.setLayout(new BorderLayout(0, 0));

        JLabel header = new JLabel("♪  ВЫБЕРИТЕ САУНДТРЕК  ♪", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setForeground(COLOR_ACCENT);
        header.setBorder(new EmptyBorder(20, 10, 10, 10));
        dialog.add(header, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(COLOR_BG);
        listPanel.setBorder(new EmptyBorder(10, 30, 10, 30));

        final SoundManager.Soundtrack[] chosen = {SoundManager.Soundtrack.OFF};

        for (SoundManager.Soundtrack track : options) {
            JButton btn = new JButton(track.title);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(340, 46));
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setBackground(track == SoundManager.Soundtrack.OFF ? new Color(38, 48, 66) : new Color(34, 60, 92));
            btn.setForeground(COLOR_TEXT);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_ACCENT.darker(), 1),
                    new EmptyBorder(8, 12, 8, 12)));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            btn.addActionListener(e -> {
                chosen[0] = track;
                dialog.dispose();
            });

            listPanel.add(btn);
            listPanel.add(Box.createVerticalStrut(8));
        }

        dialog.add(listPanel, BorderLayout.CENTER);
        dialog.setVisible(true);

        soundManager.setSoundtrack(chosen[0]);
        boolean on = chosen[0] != SoundManager.Soundtrack.OFF;
        soundButton.setText(on ? chosen[0].title : "Музыка: Выкл");
        soundButton.setIcon(new SimpleIcon(on ? SimpleIcon.Type.SPEAKER_ON : SimpleIcon.Type.SPEAKER_OFF, 16, 16));
    }

    // ───────────────────────────────────────────────────────
    //  1. ЭКРАН ПОДКЛЮЧЕНИЯ (NETWORK SETUP)
    // ───────────────────────────────────────────────────────
    private void showConnectionScreen() {
        mainContentPanel.removeAll();
        statusLabel.setText("Выбор роли в локальной сети");

        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(COLOR_BG);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(45, 60, 85), 2),
                new EmptyBorder(30, 40, 30, 40)));
        card.setPreferredSize(new Dimension(460, 440));

        JLabel title = new JLabel("СЕТЕВОЙ МОРСКОЙ БОЙ", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(COLOR_ACCENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(15));

        JLabel localIpLbl = new JLabel("Ваш локальный IP: " + NetworkManager.getLocalIpAddress(), SwingConstants.CENTER);
        localIpLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        localIpLbl.setForeground(COLOR_SUCCESS);
        localIpLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(localIpLbl);
        card.add(Box.createVerticalStrut(20));

        // Поля ввода
        JPanel fieldsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        fieldsPanel.setBackground(COLOR_PANEL_BG);
        fieldsPanel.setMaximumSize(new Dimension(380, 80));

        JLabel ipLbl = new JLabel("IP сервера:");
        ipLbl.setForeground(COLOR_TEXT);
        ipLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        ipField = new JTextField("127.0.0.1");
        ipField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ipField.setBackground(new Color(20, 26, 36));
        ipField.setForeground(COLOR_TEXT);
        ipField.setCaretColor(COLOR_TEXT);

        JLabel portLbl = new JLabel("Порт:");
        portLbl.setForeground(COLOR_TEXT);
        portLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        portField = new JTextField("8888");
        portField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        portField.setBackground(new Color(20, 26, 36));
        portField.setForeground(COLOR_TEXT);
        portField.setCaretColor(COLOR_TEXT);

        fieldsPanel.add(ipLbl); fieldsPanel.add(ipField);
        fieldsPanel.add(portLbl); fieldsPanel.add(portField);

        card.add(fieldsPanel);
        card.add(Box.createVerticalStrut(25));

        // Кнопки
        hostBtn = new JButton("Создать сервер (Host)");
        styleButton(hostBtn, COLOR_ACCENT);
        hostBtn.addActionListener(e -> startHost());

        connectBtn = new JButton("Подключиться (Client)");
        styleButton(connectBtn, COLOR_SUCCESS);
        connectBtn.addActionListener(e -> startClient());

        card.add(hostBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(connectBtn);
        card.add(Box.createVerticalStrut(20));

        connectionStatusLabel = new JLabel("Выберите «Создать сервер» или «Подключиться»", SwingConstants.CENTER);
        connectionStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        connectionStatusLabel.setForeground(COLOR_TEXT_MUTED);
        connectionStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(connectionStatusLabel);

        container.add(card);
        mainContentPanel.add(container, "CONNECT");
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private void startHost() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            isHost = true;
            hostBtn.setEnabled(false);
            connectBtn.setEnabled(false);
            ipField.setEnabled(false);
            portField.setEnabled(false);

            String ip = NetworkManager.getLocalIpAddress();
            connectionStatusLabel.setText("Ожидание подключения второго игрока на " + ip + ":" + port + "...");
            connectionStatusLabel.setForeground(COLOR_WARNING);
            statusLabel.setText("Ожидание подключения клиента...");

            networkManager.startServer(port);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Укажите корректный порт (например, 8888)", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startClient() {
        try {
            String host = ipField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            isHost = false;
            hostBtn.setEnabled(false);
            connectBtn.setEnabled(false);
            ipField.setEnabled(false);
            portField.setEnabled(false);

            connectionStatusLabel.setText("Подключение к " + host + ":" + port + "...");
            connectionStatusLabel.setForeground(COLOR_WARNING);
            statusLabel.setText("Попытка подключения...");

            networkManager.connectToServer(host, port);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Укажите корректный номер порта", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ───────────────────────────────────────────────────────
    //  NETWORK LISTENER CALLBACKS
    // ───────────────────────────────────────────────────────
    @Override
    public void onConnected(boolean isHost, String remoteAddress) {
        this.isHost = isHost;
        statusLabel.setText("Соединение установлено! Фаза расстановки");
        showSoundtrackDialog();
        renderCurrentPhase();
    }

    @Override
    public void onConnectionFailed(String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage, "Ошибка сети", JOptionPane.ERROR_MESSAGE);
        hostBtn.setEnabled(true);
        connectBtn.setEnabled(true);
        ipField.setEnabled(true);
        portField.setEnabled(true);
        connectionStatusLabel.setText("Ошибка подключения. Попробуйте снова.");
        connectionStatusLabel.setForeground(COLOR_DANGER);
        statusLabel.setText("Ошибка соединения");
    }

    @Override
    public void onDisconnected() {
        if (!gameOver) {
            JOptionPane.showMessageDialog(this, "Соперник отключился или связь прервана.", "Связь потеряна", JOptionPane.WARNING_MESSAGE);
            statusLabel.setText("Соединение прервано");
        }
    }

    @Override
    public void onMessageReceived(String message) {
        if (message == null || message.trim().isEmpty()) return;

        String[] parts = message.split(":");
        String command = parts[0];

        switch (command) {
            case "READY":
                opponentReady = true;
                if (myReady) {
                    startBattlePhase();
                } else {
                    statusLabel.setText("Соперник готов! Расставьте корабли");
                }
                renderCurrentPhase();
                break;

            case "SHOT":
                int r = Integer.parseInt(parts[1]);
                int c = Integer.parseInt(parts[2]);
                processIncomingShot(r, c);
                break;

            case "SHOT_RESULT":
                int srRow = Integer.parseInt(parts[1]);
                int srCol = Integer.parseInt(parts[2]);
                String resultStr = parts[3];
                String extraCoords = (parts.length > 4) ? parts[4] : "";
                processShotResponse(srRow, srCol, resultStr, extraCoords);
                break;

            case "SURRENDER":
                gameOver = true;
                winnerName = "Вы (Соперник сдался)";
                statusLabel.setText("Победа! Соперник сдался");
                renderCurrentPhase();
                break;

            case "DISCONNECT":
                onDisconnected();
                break;
        }
    }

    private void startBattlePhase() {
        gameStarted = true;
        // Хост ходит первым
        myTurn = isHost;
        statusLabel.setText(myTurn ? "Ваш ход" : "Ход противника");
    }

    // ───────────────────────────────────────────────────────
    //  ОБРАБОТКА ВЫСТРЕЛОВ ПО СЕТИ
    // ───────────────────────────────────────────────────────

    /** Обработка выстрела от соперника по нашему полю (myBoard). */
    private void processIncomingShot(int row, int col) {
        Coordinate coord = new Coordinate(row, col);
        ShotResult res = myBoard.shoot(coord);

        StringBuilder extraMiss = new StringBuilder();
        if (res == ShotResult.SUNK) {
            // Собираем все координаты ячеек, окружающих затонувший корабль, которые стали MISS
            Ship sunkShip = myBoard.getCell(row, col).getShip();
            if (sunkShip != null) {
                for (Coordinate c : sunkShip.getCoordinates()) {
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            int nr = c.getRow() + dr;
                            int nc = c.getCol() + dc;
                            if (myBoard.isValidCoordinate(nr, nc)) {
                                Cell cell = myBoard.getCell(nr, nc);
                                if (cell.getState() == CellState.MISS) {
                                    extraMiss.append(nr).append(",").append(nc).append(";");
                                }
                            }
                        }
                    }
                }
            }
        }

        // Отправляем результат стрелявшему
        networkManager.sendMessage("SHOT_RESULT:" + row + ":" + col + ":" + res.name() + ":" + extraMiss.toString());

        // Озвучка
        if (res == ShotResult.MISS) soundManager.playMissSound();
        else soundManager.playHitSound();

        // Обновление очередности хода
        if (res == ShotResult.MISS) {
            myTurn = true;
            statusLabel.setText("Ваш ход");
        } else {
            myTurn = false;
            statusLabel.setText("Ход противника");
        }

        // Проверка поражения
        if (myBoard.allShipsSunk()) {
            gameOver = true;
            winnerName = "Соперник";
            statusLabel.setText("Поражение!");
        }

        renderCurrentPhase();
    }

    /** Обработка ответа на наш выстрел по вражескому полю. */
    private void processShotResponse(int row, int col, String resultStr, String extraCoords) {
        waitingForShotResponse = false;
        ShotResult res = ShotResult.valueOf(resultStr);

        Cell oppCell = opponentBoard.getCell(row, col);
        if (res == ShotResult.HIT || res == ShotResult.SUNK) {
            oppCell.setState(CellState.HIT);
            soundManager.playHitSound();
            myTurn = true;
            statusLabel.setText("Ваш ход (Попадание!)");

            if (res == ShotResult.SUNK && !extraCoords.isEmpty()) {
                String[] pairs = extraCoords.split(";");
                for (String p : pairs) {
                    if (p.trim().isEmpty()) continue;
                    String[] rc = p.split(",");
                    int nr = Integer.parseInt(rc[0]);
                    int nc = Integer.parseInt(rc[1]);
                    opponentBoard.getCell(nr, nc).setState(CellState.MISS);
                }
            }

            // Проверка победы (если уничтожены 20 палуб)
            int hitDecks = 0;
            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < 10; c++) {
                    if (opponentBoard.getCell(r, c).getState() == CellState.HIT) hitDecks++;
                }
            }
            if (hitDecks >= 20) {
                gameOver = true;
                winnerName = "Вы";
                statusLabel.setText("Победа!");
            }
        } else if (res == ShotResult.MISS) {
            oppCell.setState(CellState.MISS);
            soundManager.playMissSound();
            myTurn = false;
            statusLabel.setText("Ход противника");
        }

        renderCurrentPhase();
    }

    // ───────────────────────────────────────────────────────
    //  ОТРИСОВКА ФАЗ ИГРЫ
    // ───────────────────────────────────────────────────────
    public void renderCurrentPhase() {
        mainContentPanel.removeAll();

        if (gameOver) {
            mainContentPanel.add(createGameOverScreen(), "GAME_OVER");
        } else if (!gameStarted) {
            mainContentPanel.add(createPlacementScreen(), "PLACEMENT");
        } else {
            mainContentPanel.add(createBattleScreen(), "BATTLE");
        }

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    // ───────────────────────────────────────────────────────
    //  1. РАССТАНОВКА КОРАБЛЕЙ
    // ───────────────────────────────────────────────────────
    private JPanel createPlacementScreen() {
        JPanel panel = new JPanel(new BorderLayout(20, 15));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel boardGridPanel = createBoardGridPanel(myBoard, false, false, (row, col) -> {
            if (myReady) return;
            if (myBoard.getRemainingCount(selectedShipType) <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Лимит для корабля \"" + selectedShipType.getName() + "\" исчерпан!",
                        "Лимит флота", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (myBoard.placeShip(selectedShipType, new Coordinate(row, col), selectedOrientation)) {
                if (myBoard.getRemainingCount(selectedShipType) <= 0) {
                    for (ShipType t : ShipType.values()) {
                        if (myBoard.getRemainingCount(t) > 0) { selectedShipType = t; break; }
                    }
                }
                renderCurrentPhase();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Невозможно разместить корабль здесь!\n(Нарушение буферной зоны или выход за поле)",
                        "Ошибка размещения", JOptionPane.WARNING_MESSAGE);
            }
        });
        panel.add(boardGridPanel, BorderLayout.CENTER);

        // Боковая панель управления
        JPanel ctrl = new JPanel();
        ctrl.setLayout(new BoxLayout(ctrl, BoxLayout.Y_AXIS));
        ctrl.setBackground(COLOR_PANEL_BG);
        ctrl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(42, 54, 74), 1),
                new EmptyBorder(12, 14, 12, 14)));
        ctrl.setPreferredSize(new Dimension(320, 0));

        JLabel title = new JLabel("ВАШ ФЛОТ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(COLOR_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        ctrl.add(title);
        ctrl.add(Box.createVerticalStrut(10));

        for (ShipType type : ShipType.values()) {
            int remaining = myBoard.getRemainingCount(type);
            int total = type.getMaxAllowed();
            boolean sel = (type == selectedShipType);

            JPanel card = new JPanel(new BorderLayout(8, 0));
            card.setMaximumSize(new Dimension(290, 44));
            card.setPreferredSize(new Dimension(290, 44));
            card.setBackground(sel ? new Color(38, 56, 82) : new Color(32, 40, 54));
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(sel ? COLOR_ACCENT : (remaining == 0 ? COLOR_SUCCESS : new Color(50, 64, 86)), sel ? 2 : 1),
                    new EmptyBorder(4, 8, 4, 8)));

            card.add(new JLabel(new MiniShipIcon(type, 64, 20)), BorderLayout.WEST);

            JLabel txt = new JLabel("<html><b>" + type.getSize() + "-палубный</b> (" + (total-remaining) + "/" + total + ")</html>");
            txt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            txt.setForeground(remaining == 0 ? COLOR_SUCCESS : COLOR_TEXT);
            card.add(txt, BorderLayout.CENTER);

            JLabel badge = new JLabel(remaining > 0 ? "Осталось: " + remaining : "ГОТОВ");
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setForeground(remaining > 0 ? (sel ? COLOR_ACCENT : COLOR_TEXT_MUTED) : COLOR_SUCCESS);
            card.add(badge, BorderLayout.EAST);

            if (!myReady) {
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                card.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) { selectedShipType = type; renderCurrentPhase(); }
                });
            }
            ctrl.add(card);
            ctrl.add(Box.createVerticalStrut(6));
        }

        ctrl.add(Box.createVerticalStrut(10));

        JLabel orientLabel = new JLabel("Ориентация:");
        orientLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        orientLabel.setForeground(COLOR_TEXT);
        orientLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        ctrl.add(orientLabel);
        ctrl.add(Box.createVerticalStrut(6));

        JRadioButton horiz = new JRadioButton("По горизонтали", selectedOrientation == Orientation.HORIZONTAL);
        JRadioButton vert  = new JRadioButton("По вертикали",   selectedOrientation == Orientation.VERTICAL);
        for (JRadioButton r : new JRadioButton[]{horiz, vert}) {
            r.setForeground(COLOR_TEXT); r.setBackground(COLOR_PANEL_BG); r.setFocusPainted(false);
            r.setEnabled(!myReady);
        }
        ButtonGroup group = new ButtonGroup();
        group.add(horiz); group.add(vert);
        horiz.addActionListener(e -> selectedOrientation = Orientation.HORIZONTAL);
        vert.addActionListener(e -> selectedOrientation = Orientation.VERTICAL);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        radioPanel.setBackground(COLOR_PANEL_BG);
        radioPanel.add(horiz); radioPanel.add(vert);
        ctrl.add(radioPanel);
        ctrl.add(Box.createVerticalStrut(15));

        JButton autoBtn = new JButton("Случайная расстановка");
        autoBtn.setIcon(new SimpleIcon(SimpleIcon.Type.DICE, 16, 16)); autoBtn.setIconTextGap(8);
        styleButton(autoBtn, new Color(42, 58, 80));
        autoBtn.setEnabled(!myReady);
        autoBtn.addActionListener(e -> { myBoard.autoPlaceAllShips(); renderCurrentPhase(); });
        ctrl.add(autoBtn);
        ctrl.add(Box.createVerticalStrut(8));

        JButton clearBtn = new JButton("Очистить поле");
        clearBtn.setIcon(new SimpleIcon(SimpleIcon.Type.TRASH, 16, 16)); clearBtn.setIconTextGap(8);
        styleButton(clearBtn, new Color(42, 58, 80));
        clearBtn.setEnabled(!myReady);
        clearBtn.addActionListener(e -> { myBoard.clear(); renderCurrentPhase(); });
        ctrl.add(clearBtn);
        ctrl.add(Box.createVerticalStrut(15));

        boolean fullyPlaced = myBoard.isFleetFullyPlaced();
        String btnText;
        if (myReady) btnText = "Ожидание готовности соперника...";
        else if (fullyPlaced) btnText = "В БОЙ! Я готов";
        else btnText = "Расставьте все корабли";

        JButton readyBtn = new JButton(btnText);
        readyBtn.setIcon(new SimpleIcon(SimpleIcon.Type.CHECK, 16, 16)); readyBtn.setIconTextGap(8);
        readyBtn.setEnabled(fullyPlaced && !myReady);
        styleButton(readyBtn, fullyPlaced && !myReady ? COLOR_SUCCESS : new Color(45, 52, 64));
        readyBtn.addActionListener(e -> {
            myReady = true;
            networkManager.sendMessage("READY");
            if (opponentReady) {
                startBattlePhase();
            } else {
                statusLabel.setText("Ожидание готовности соперника...");
            }
            renderCurrentPhase();
        });
        ctrl.add(readyBtn);

        panel.add(ctrl, BorderLayout.EAST);
        return panel;
    }

    // ───────────────────────────────────────────────────────
    //  2. ЭКРАН БОЯ (BATTLE SCREEN)
    // ───────────────────────────────────────────────────────
    private JPanel createBattleScreen() {
        JPanel panel = new JPanel(new BorderLayout(15, 10));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JPanel boards = new JPanel(new GridLayout(1, 2, 25, 0));
        boards.setBackground(COLOR_BG);

        // Левое поле: Ваш флот
        JPanel ownWrap = new JPanel(new BorderLayout(0, 8));
        ownWrap.setBackground(COLOR_BG);
        JLabel ownTitle = new JLabel("ВАШ ФЛОТ (Вы)", SwingConstants.CENTER);
        ownTitle.setForeground(COLOR_TEXT); ownTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ownWrap.add(ownTitle, BorderLayout.NORTH);
        ownWrap.add(createBoardGridPanel(myBoard, false, true, null), BorderLayout.CENTER);
        boards.add(ownWrap);

        // Правое поле: Поле соперника (туман войны)
        JPanel oppWrap = new JPanel(new BorderLayout(0, 8));
        oppWrap.setBackground(COLOR_BG);
        JLabel oppTitle = new JLabel(myTurn ? "ПОЛЕ СОПЕРНИКА — Атакуйте!" : "ПОЛЕ СОПЕРНИКА — Ожидание хода...", SwingConstants.CENTER);
        oppTitle.setForeground(myTurn ? COLOR_SUCCESS : COLOR_WARNING); oppTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        oppWrap.add(oppTitle, BorderLayout.NORTH);

        CellClickListener listener = null;
        if (myTurn && !waitingForShotResponse) {
            listener = (r, c) -> {
                Cell cell = opponentBoard.getCell(r, c);
                if (cell.getState() == CellState.EMPTY) {
                    waitingForShotResponse = true;
                    networkManager.sendMessage("SHOT:" + r + ":" + c);
                    renderCurrentPhase();
                }
            };
        }
        oppWrap.add(createBoardGridPanel(opponentBoard, true, true, listener), BorderLayout.CENTER);
        boards.add(oppWrap);

        panel.add(boards, BorderLayout.CENTER);

        // Нижняя панель
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        bottom.setBackground(COLOR_PANEL_BG);
        bottom.setBorder(new LineBorder(new Color(42, 54, 74), 1));

        JButton surrenderBtn = new JButton("Сдаться");
        surrenderBtn.setIcon(new SimpleIcon(SimpleIcon.Type.FLAG, 16, 16)); surrenderBtn.setIconTextGap(8);
        styleButton(surrenderBtn, COLOR_DANGER);
        surrenderBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Вы действительно хотите сдаться?", "Подтверждение",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                networkManager.sendMessage("SURRENDER");
                gameOver = true;
                winnerName = "Соперник (Вы сдались)";
                statusLabel.setText("Поражение");
                renderCurrentPhase();
            }
        });
        bottom.add(surrenderBtn);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    // ───────────────────────────────────────────────────────
    //  3. КОНЕЦ ИГРЫ (GAME OVER)
    // ───────────────────────────────────────────────────────
    private JPanel createGameOverScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BG);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(COLOR_PANEL_BG);
        inner.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_ACCENT, 2), new EmptyBorder(35, 45, 35, 45)));

        JLabel trophy = new JLabel(new SimpleIcon(SimpleIcon.Type.TROPHY, 64, 64));
        trophy.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(trophy);
        inner.add(Box.createVerticalStrut(15));

        boolean iWon = winnerName.startsWith("Вы");
        JLabel wLabel = new JLabel(iWon ? "ПОБЕДА!" : "ПОРАЖЕНИЕ", SwingConstants.CENTER);
        wLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        wLabel.setForeground(iWon ? COLOR_SUCCESS : COLOR_DANGER);
        wLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(wLabel);
        inner.add(Box.createVerticalStrut(8));

        JLabel desc = new JLabel("Победитель: " + winnerName, SwingConstants.CENTER);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        desc.setForeground(COLOR_TEXT);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(desc);
        inner.add(Box.createVerticalStrut(25));

        JButton closeBtn = new JButton("Выйти из сетевой игры");
        styleButton(closeBtn, COLOR_ACCENT);
        closeBtn.addActionListener(e -> {
            if (networkManager != null) networkManager.closeConnection();
            dispose();
        });
        inner.add(closeBtn);

        panel.add(inner);
        return panel;
    }

    // ───────────────────────────────────────────────────────
    //  СЕТКА ИГРОВОГО ПОЛЯ (BOARD GRID RENDERER)
    // ───────────────────────────────────────────────────────
    @FunctionalInterface
    private interface CellClickListener { void onClick(int row, int col); }

    private JPanel createBoardGridPanel(Board board, boolean fogOfWar, boolean showWater, CellClickListener listener) {
        JPanel boardPanel = new JPanel(new GridLayout(11, 11, 1, 1));
        boardPanel.setBackground(new Color(20, 30, 46));
        boardPanel.setBorder(BorderFactory.createLineBorder(new Color(40, 56, 80), 2));

        boardPanel.add(createLabelCell(""));
        for (int c = 0; c < Board.SIZE; c++)
            boardPanel.add(createLabelCell(String.valueOf((char)('А' + (c >= 9 ? c + 1 : c)))));

        for (int r = 0; r < Board.SIZE; r++) {
            boardPanel.add(createLabelCell(String.valueOf(r + 1)));
            for (int c = 0; c < Board.SIZE; c++) {
                int row = r, col = c;
                Cell cell = board.getCell(row, col);
                WarshipCellButton btn = new WarshipCellButton(cell, fogOfWar, showWater, row, col);
                if (listener != null) btn.addActionListener(e -> listener.onClick(row, col));
                else btn.setEnabled(false);
                boardPanel.add(btn);
            }
        }
        return boardPanel;
    }

    private JLabel createLabelCell(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(170, 185, 205));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(22, 30, 42));
        return lbl;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg); btn.setForeground(COLOR_TEXT);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setMaximumSize(new Dimension(280, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // ───────────────────────────────────────────────────────
    //  КНОПКА ЯЧЕЙКИ С РЕАЛИСТИЧНОЙ ВОДОЙ И ВЗРЫВАМИ
    // ───────────────────────────────────────────────────────
    private static class WarshipCellButton extends JButton {
        private final Cell cell;
        private final boolean fogOfWar;
        private final boolean showWater;
        private final int row, col;
        private boolean hovered = false;

        public WarshipCellButton(Cell cell, boolean fogOfWar, boolean showWater, int row, int col) {
            this.cell = cell;
            this.fogOfWar = fogOfWar;
            this.showWater = showWater;
            this.row = row;
            this.col = col;
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { if (isEnabled()) { hovered = true; repaint(); } }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth(), h = getHeight();
            CellState state = cell.getState();
            boolean hasShip = cell.hasShip();
            boolean showShip = hasShip && (!fogOfWar || state == CellState.HIT);

            if (showWater || state == CellState.MISS || state == CellState.HIT) {
                drawPhotorealisticWater(g2, w, h, row, col);
            } else {
                g2.setColor(new Color(24, 34, 52));
                g2.fillRect(0, 0, w, h);
                g2.setColor(new Color(38, 54, 76, 80));
                g2.drawRect(0, 0, w-1, h-1);
            }

            if (showShip) drawRealisticWarship(g2, cell.getShip(), cell.getCoordinate(), w, h, state == CellState.HIT);
            if (state == CellState.HIT) drawExplosion(g2, w, h, hasShip && cell.getShip() != null && cell.getShip().isSunk());
            if (state == CellState.MISS) drawWaterSplash(g2, w, h);

            if (hovered && isEnabled()) {
                g2.setColor(new Color(52, 152, 219, 70));
                g2.fillRect(1, 1, w-2, h-2);
                g2.setColor(new Color(100, 200, 255, 180));
                g2.drawRect(2, 2, w-5, h-5);
                g2.drawLine(w/2, 4, w/2, 9);
                g2.drawLine(w/2, h-9, w/2, h-4);
                g2.drawLine(4, h/2, 9, h/2);
                g2.drawLine(w-9, h/2, w-4, h/2);
            }
            g2.dispose();
        }

        private void drawPhotorealisticWater(Graphics2D g2, int w, int h, int row, int col) {
            GradientPaint deepGrad = new GradientPaint(0, 0, new Color(14, 26, 50), 0, h, new Color(8, 16, 36));
            g2.setPaint(deepGrad);
            g2.fillRect(0, 0, w, h);

            long seed = row * 17L + col * 31L;
            java.util.Random rnd = new java.util.Random(seed);

            for (int k = 0; k < 3; k++) {
                int lx = rnd.nextInt(w);
                int ly = rnd.nextInt(h);
                int lr = 4 + rnd.nextInt(6);
                int alpha = 18 + rnd.nextInt(22);
                RadialGradientPaint caustic = new RadialGradientPaint(
                    new Point2D.Float(lx, ly), lr,
                    new float[]{0f, 1f},
                    new Color[]{new Color(100, 180, 255, alpha), new Color(20, 60, 120, 0)}
                );
                g2.setPaint(caustic);
                g2.fillOval(lx - lr, ly - lr, lr*2, lr*2);
            }

            g2.setStroke(new BasicStroke(0.6f));
            for (int k = 0; k < 3; k++) {
                int wy = (int)(h * (0.25 + k * 0.22 + (seed % 7) * 0.015));
                int waveAlpha = 28 + k * 12;
                g2.setColor(new Color(60, 130, 210, waveAlpha));
                g2.drawArc(-2, wy - 3, w + 4, 6, 0, 180);
            }

            GradientPaint surface = new GradientPaint(0, 0, new Color(80, 160, 240, 30), 0, h/3, new Color(0, 0, 0, 0));
            g2.setPaint(surface);
            g2.fillRect(0, 0, w, h/3);

            g2.setColor(new Color(10, 20, 38, 120));
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawRect(0, 0, w-1, h-1);
        }

        private void drawRealisticWarship(Graphics2D g2, Ship ship, Coordinate coord, int w, int h, boolean damaged) {
            if (ship == null) {
                // Если корабельного объекта на тумане войны нет, рисуем базовый палубный блок
                g2.setColor(damaged ? new Color(50, 54, 60) : new Color(74, 90, 112));
                g2.fillRect(4, 4, w - 8, h - 8);
                return;
            }
            int size = ship.getSize();
            Orientation ori = ship.getOrientation();
            int index = ship.getCoordinates().indexOf(coord);

            Color hullDark  = damaged ? new Color(35, 38, 44)  : new Color(48, 60, 76);
            Color hullDeck  = damaged ? new Color(50, 54, 60)  : new Color(74, 90, 112);
            Color turret    = damaged ? new Color(30, 30, 35)  : new Color(40, 50, 65);
            Color barrel    = damaged ? new Color(80, 80, 80)  : new Color(175, 188, 204);

            int pad = 4;
            if (size == 1) {
                g2.setColor(hullDark);  g2.fillOval(pad+1, pad+1, w-2*pad-2, h-2*pad-2);
                g2.setColor(hullDeck);  g2.fillOval(pad+3, pad+3, w-2*pad-6, h-2*pad-6);
                g2.setColor(turret);    g2.fillRoundRect(w/2-4, h/2-4, 8, 8, 3, 3);
                g2.setColor(barrel);    g2.drawOval(w/2-2, h/2-2, 4, 4);
                return;
            }

            if (ori == Orientation.HORIZONTAL) {
                int y0 = pad+2, h0 = h-2*pad-4;
                if (index == 0) {
                    int[] xp = {pad+2, w, w}; int[] yp = {h/2, y0, y0+h0};
                    g2.setColor(hullDark); g2.fillPolygon(xp, yp, 3);
                } else if (index == size - 1) {
                    int[] xp = {0, w-pad-2, 0}; int[] yp = {y0, h/2, y0+h0};
                    g2.setColor(hullDark); g2.fillPolygon(xp, yp, 3);
                } else {
                    g2.setColor(hullDark); g2.fillRect(0, y0, w, h0);
                }
                g2.setColor(turret); g2.fillOval(w/2-4, h/2-4, 8, 8);
            } else {
                int x0 = pad+2, w0 = w-2*pad-4;
                if (index == 0) {
                    int[] xp = {x0, x0+w0, w/2}; int[] yp = {h, h, pad+2};
                    g2.setColor(hullDark); g2.fillPolygon(xp, yp, 3);
                } else if (index == size - 1) {
                    int[] xp = {x0, x0+w0, w/2}; int[] yp = {0, 0, h-pad-2};
                    g2.setColor(hullDark); g2.fillPolygon(xp, yp, 3);
                } else {
                    g2.setColor(hullDark); g2.fillRect(x0, 0, w0, h);
                }
                g2.setColor(turret); g2.fillOval(w/2-4, h/2-4, 8, 8);
            }
        }

        private void drawExplosion(Graphics2D g2, int w, int h, boolean sunk) {
            int cx = w / 2, cy = h / 2;
            int rMax = Math.min(w, h) / 2 - 2;

            RadialGradientPaint fire = new RadialGradientPaint(
                new Point2D.Float(cx, cy), rMax,
                new float[]{0.0f, 0.4f, 0.75f, 1.0f},
                new Color[]{
                    new Color(255, 255, 220, 240),
                    new Color(255, 140, 0, 220),
                    new Color(200, 30, 0, 190),
                    new Color(40, 10, 0, 0)
                }
            );
            g2.setPaint(fire);
            g2.fillOval(cx - rMax, cy - rMax, rMax * 2, rMax * 2);

            g2.setColor(sunk ? new Color(255, 50, 50, 220) : new Color(255, 200, 50, 220));
            g2.setStroke(new BasicStroke(2.0f));
            int cross = 8;
            g2.drawLine(cx - cross, cy - cross, cx + cross, cy + cross);
            g2.drawLine(cx + cross, cy - cross, cx - cross, cy + cross);
        }

        private void drawWaterSplash(Graphics2D g2, int w, int h) {
            int cx = w / 2, cy = h / 2;
            g2.setColor(new Color(120, 200, 255, 190));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(cx - 5, cy - 5, 10, 10);
            g2.drawOval(cx - 9, cy - 9, 18, 18);
            g2.setColor(new Color(220, 245, 255, 230));
            g2.fillOval(cx - 2, cy - 2, 4, 4);
        }
    }

    // ───────────────────────────────────────────────────────
    //  ИКОНКИ ВЕКТОРНОЙ ОГРИСОВКИ
    // ───────────────────────────────────────────────────────
    private static class SimpleIcon implements Icon {
        public enum Type { ANCHOR, SPEAKER_ON, SPEAKER_OFF, DICE, TRASH, CHECK, TROPHY, FLAG }

        private final Type type;
        private final int width, height;

        public SimpleIcon(Type type, int width, int height) {
            this.type = type;
            this.width = width;
            this.height = height;
        }

        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(c != null ? c.getForeground() : COLOR_TEXT);

            int w = width, h = height;
            switch (type) {
                case ANCHOR:
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.drawOval(w/2-3, 1, 6, 6);
                    g2.drawLine(w/2, 7, w/2, h-3);
                    g2.drawLine(w/2-6, 11, w/2+6, 11);
                    g2.drawArc(3, h/2-2, w-6, h/2, 200, 140);
                    break;
                case SPEAKER_ON:
                    g2.fillRect(2, h/2-4, 4, 8);
                    int[] xp = {6, w/2, w/2, 6}; int[] yp = {h/2-4, 2, h-2, h/2+4};
                    g2.fillPolygon(xp, yp, 4);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawArc(w/2+1, h/2-6, 8, 12, -45, 90);
                    break;
                case SPEAKER_OFF:
                    g2.fillRect(2, h/2-4, 4, 8);
                    int[] xp2 = {6, w/2, w/2, 6}; int[] yp2 = {h/2-4, 2, h-2, h/2+4};
                    g2.fillPolygon(xp2, yp2, 4);
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.drawLine(w/2+3, h/2-4, w-3, h/2+4);
                    g2.drawLine(w-3, h/2-4, w/2+3, h/2+4);
                    break;
                case DICE:
                    g2.drawRoundRect(2, 2, w-4, h-4, 4, 4);
                    g2.fillOval(5, 5, 3, 3); g2.fillOval(w-8, h-8, 3, 3); g2.fillOval(w/2-1, h/2-1, 3, 3);
                    break;
                case TRASH:
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawLine(3, 4, w-3, 4);
                    g2.drawRect(5, 4, w-10, h-6);
                    g2.drawLine(w/2-2, 7, w/2-2, h-4);
                    g2.drawLine(w/2+2, 7, w/2+2, h-4);
                    break;
                case CHECK:
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.drawLine(2, h/2, w/2-1, h-3);
                    g2.drawLine(w/2-1, h-3, w-2, 3);
                    break;
                case TROPHY:
                    g2.setColor(COLOR_WARNING);
                    g2.fillRect(w/3, h-4, w/3, 4);
                    g2.fillRect(w/2-2, h-8, 4, 4);
                    int[] tx = {3, w-3, w/2+5, w/2-5}; int[] ty = {2, 2, h-8, h-8};
                    g2.fillPolygon(tx, ty, 4);
                    break;
                case FLAG:
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.drawLine(3, 2, 3, h-2);
                    int[] fx = {3, w-2, 3}; int[] fy = {2, 7, 12};
                    g2.fillPolygon(fx, fy, 3);
                    break;
            }
            g2.dispose();
        }
    }

    private static class MiniShipIcon implements Icon {
        private final ShipType type;
        private final int width, height;

        public MiniShipIcon(ShipType type, int width, int height) {
            this.type = type;
            this.width = width;
            this.height = height;
        }

        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);

            int size = type.getSize();
            int cellW = (width - 4) / 4;
            int h0 = height - 6;

            for (int i = 0; i < size; i++) {
                int cx = 2 + i * cellW;
                g2.setColor(new Color(52, 152, 219));
                g2.fillRoundRect(cx, 3, cellW - 2, h0, 3, 3);
                g2.setColor(new Color(20, 30, 45));
                g2.fillOval(cx + cellW/2 - 2, 3 + h0/2 - 2, 4, 4);
            }
            g2.dispose();
        }
    }
}

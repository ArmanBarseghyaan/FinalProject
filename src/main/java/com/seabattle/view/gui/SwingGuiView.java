package com.seabattle.view.gui;

import com.seabattle.controller.GameController;
import com.seabattle.controller.SoundManager;
import com.seabattle.model.*;
import com.seabattle.view.GameView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;

/**
 * Графический интерфейс Морского боя.
 * — Реалистичная вода на всех пустых клетках во время боя.
 * — Звук воды при промахе.
 * — Диалог выбора музыки при запуске.
 * — Три саундтрека: Travis Scott FE!N, Eminem Lose Yourself, Eminem Superman.
 */
public class SwingGuiView extends JFrame implements GameView {
    private final GameController controller;

    private static final Color COLOR_BG         = new Color(20, 24, 32);
    private static final Color COLOR_PANEL_BG   = new Color(28, 35, 48);
    private static final Color COLOR_TEXT        = new Color(225, 232, 244);
    private static final Color COLOR_TEXT_MUTED  = new Color(130, 145, 168);
    private static final Color COLOR_WATER_DARK  = new Color(12, 22, 40);
    private static final Color COLOR_WATER_MID   = new Color(18, 36, 62);
    private static final Color COLOR_WATER_LIGHT = new Color(28, 56, 90);
    private static final Color COLOR_ACCENT      = new Color(52, 152, 219);
    private static final Color COLOR_SUCCESS     = new Color(46, 204, 113);
    private static final Color COLOR_DANGER      = new Color(231, 76, 60);

    private JPanel mainContentPanel;
    private JLabel statusLabel;
    private JButton soundButton;

    private ShipType selectedShipType   = ShipType.BATTLESHIP;
    private Orientation selectedOrientation = Orientation.HORIZONTAL;

    public SwingGuiView(GameController controller) {
        this.controller = controller;
        setTitle("Морской бой — Hot-seat");
        setSize(1050, 720);
        setMinimumSize(new Dimension(980, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);
        initTopBar();
        mainContentPanel = new JPanel(new CardLayout());
        mainContentPanel.setBackground(COLOR_BG);
        add(mainContentPanel, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────
    //  ВЕРХНЯЯ ПАНЕЛЬ
    // ─────────────────────────────────────────────────────
    private void initTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(15, 0));
        topBar.setBackground(COLOR_PANEL_BG);
        topBar.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("МОРСКОЙ БОЙ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
        titleLabel.setForeground(COLOR_TEXT);
        titleLabel.setIcon(new SimpleIcon(SimpleIcon.Type.ANCHOR, 22, 22));
        titleLabel.setIconTextGap(10);
        topBar.add(titleLabel, BorderLayout.WEST);

        statusLabel = new JLabel("Фаза расстановки", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
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
            controller.getSoundManager().toggleSound();
            boolean on = controller.getSoundManager().isSoundEnabled();
            SoundManager.Soundtrack cur = controller.getSoundManager().getCurrentSoundtrack();
            soundButton.setText(on ? cur.title : "Музыка: Выкл");
            soundButton.setIcon(new SimpleIcon(on ? SimpleIcon.Type.SPEAKER_ON : SimpleIcon.Type.SPEAKER_OFF, 16, 16));
        });
        topBar.add(soundButton, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);
    }

    // ─────────────────────────────────────────────────────
    //  ДИАЛОГ ВЫБОРА МУЗЫКИ
    // ─────────────────────────────────────────────────────
    private void showSoundtrackDialog() {
        SoundManager.Soundtrack[] options = SoundManager.Soundtrack.values();
        String[] labels = new String[options.length];
        for (int i = 0; i < options.length; i++) labels[i] = options[i].title;

        JDialog dialog = new JDialog(this, "Выберите музыку для битвы", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(420, 340);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(COLOR_BG);
        dialog.setLayout(new BorderLayout(0, 0));

        // Заголовок
        JLabel header = new JLabel("♪  ВЫБЕРИТЕ САУНДТРЕК  ♪", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setForeground(COLOR_ACCENT);
        header.setBorder(new EmptyBorder(20, 10, 10, 10));
        dialog.add(header, BorderLayout.NORTH);

        // Кнопки треков
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
            btn.setBackground(track == SoundManager.Soundtrack.OFF
                    ? new Color(38, 48, 66) : new Color(34, 60, 92));
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

        JLabel hint = new JLabel("Музыка будет играть во время сражения", SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hint.setForeground(COLOR_TEXT_MUTED);
        hint.setBorder(new EmptyBorder(4, 10, 16, 10));
        dialog.add(hint, BorderLayout.SOUTH);

        dialog.setVisible(true); // блокирует до выбора

        // Применяем выбор
        controller.getSoundManager().setSoundtrack(chosen[0]);
        boolean on = chosen[0] != SoundManager.Soundtrack.OFF;
        soundButton.setText(on ? chosen[0].title : "Музыка: Выкл");
        soundButton.setIcon(new SimpleIcon(on ? SimpleIcon.Type.SPEAKER_ON : SimpleIcon.Type.SPEAKER_OFF, 16, 16));
    }

    // ─────────────────────────────────────────────────────
    //  СТАРТ
    // ─────────────────────────────────────────────────────
    @Override
    public void start() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            renderCurrentPhase();
            // Диалог музыки — сразу при запуске
            showSoundtrackDialog();
        });
    }

    // ─────────────────────────────────────────────────────
    //  ОТРИСОВКА ПО ФАЗЕ
    // ─────────────────────────────────────────────────────
    public void renderCurrentPhase() {
        mainContentPanel.removeAll();
        GameState state = controller.getGameState();
        GamePhase phase = state.getCurrentPhase();

        switch (phase) {
            case PLACEMENT_PLAYER_1:
            case PLACEMENT_PLAYER_2:
                statusLabel.setText("Расстановка флота: " + state.getActivePlayer().getName());
                mainContentPanel.add(createPlacementScreen(), "PLACEMENT");
                break;
            case SWITCHING_PLAYER:
                statusLabel.setText("Передача управления");
                mainContentPanel.add(createSwitchScreen(), "SWITCH");
                break;
            case PLAYER_1_TURN:
            case PLAYER_2_TURN:
                statusLabel.setText("Ход: " + state.getActivePlayer().getName());
                mainContentPanel.add(createBattleScreen(), "BATTLE");
                break;
            case GAME_OVER:
                statusLabel.setText("Сражение окончено!");
                mainContentPanel.add(createGameOverScreen(), "GAME_OVER");
                break;
        }
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    // ─────────────────────────────────────────────────────
    //  1. РАССТАНОВКА
    // ─────────────────────────────────────────────────────
    private JPanel createPlacementScreen() {
        Player active = controller.getGameState().getActivePlayer();
        Board board = active.getBoard();

        JPanel panel = new JPanel(new BorderLayout(20, 15));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel boardGridPanel = createBoardGridPanel(board, false, false, (row, col) -> {
            if (board.getRemainingCount(selectedShipType) <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Лимит для корабля \"" + selectedShipType.getName() + "\" исчерпан!\n" +
                        "Максимум: " + selectedShipType.getMaxAllowed() + " шт.",
                        "Лимит флота", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (controller.placeShip(selectedShipType, new Coordinate(row, col), selectedOrientation)) {
                if (board.getRemainingCount(selectedShipType) <= 0) {
                    for (ShipType t : ShipType.values()) {
                        if (board.getRemainingCount(t) > 0) { selectedShipType = t; break; }
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

        // Боковая панель
        JPanel ctrl = new JPanel();
        ctrl.setLayout(new BoxLayout(ctrl, BoxLayout.Y_AXIS));
        ctrl.setBackground(COLOR_PANEL_BG);
        ctrl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(42, 54, 74), 1),
                new EmptyBorder(12, 14, 12, 14)));
        ctrl.setPreferredSize(new Dimension(320, 0));

        JLabel title = new JLabel("КОРАБЛИ ФЛОТА");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(COLOR_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        ctrl.add(title);
        ctrl.add(Box.createVerticalStrut(10));

        for (ShipType type : ShipType.values()) {
            int remaining = board.getRemainingCount(type);
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

            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { selectedShipType = type; renderCurrentPhase(); }
            });
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
        autoBtn.setIcon(new SimpleIcon(SimpleIcon.Type.DICE, 16, 16));
        autoBtn.setIconTextGap(8);
        styleButton(autoBtn, new Color(42, 58, 80));
        autoBtn.addActionListener(e -> { controller.autoPlaceFleet(); renderCurrentPhase(); });
        ctrl.add(autoBtn);
        ctrl.add(Box.createVerticalStrut(8));

        JButton clearBtn = new JButton("Очистить поле");
        clearBtn.setIcon(new SimpleIcon(SimpleIcon.Type.TRASH, 16, 16));
        clearBtn.setIconTextGap(8);
        styleButton(clearBtn, new Color(42, 58, 80));
        clearBtn.addActionListener(e -> { controller.clearFleet(); renderCurrentPhase(); });
        ctrl.add(clearBtn);
        ctrl.add(Box.createVerticalStrut(15));

        boolean ready = board.isFleetFullyPlaced();
        JButton readyBtn = new JButton(ready ? "В БОЙ! Готово" : "Расставьте все корабли");
        readyBtn.setIcon(new SimpleIcon(SimpleIcon.Type.CHECK, 16, 16));
        readyBtn.setIconTextGap(8);
        readyBtn.setEnabled(ready);
        styleButton(readyBtn, ready ? COLOR_SUCCESS : new Color(45, 52, 64));
        readyBtn.addActionListener(e -> { if (controller.confirmPlacement()) renderCurrentPhase(); });
        ctrl.add(readyBtn);

        panel.add(ctrl, BorderLayout.EAST);
        return panel;
    }

    // ─────────────────────────────────────────────────────
    //  2. ПЕРЕДАЧА ХОДА
    // ─────────────────────────────────────────────────────
    private JPanel createSwitchScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BG);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(COLOR_PANEL_BG);
        inner.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(45, 60, 85), 2), new EmptyBorder(35, 45, 35, 45)));

        JLabel icon = new JLabel(new SimpleIcon(SimpleIcon.Type.RADAR, 52, 52));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(icon);
        inner.add(Box.createVerticalStrut(15));

        JLabel t = new JLabel("ПЕРЕДАЧА УПРАВЛЕНИЯ", SwingConstants.CENTER);
        t.setFont(new Font("Segoe UI", Font.BOLD, 22)); t.setForeground(COLOR_TEXT); t.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(t);
        inner.add(Box.createVerticalStrut(10));

        Player active = controller.getGameState().getActivePlayer();
        JLabel desc = new JLabel("Передайте устройство: " + (active != null ? active.getName() : "Следующий"), SwingConstants.CENTER);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 16)); desc.setForeground(COLOR_TEXT_MUTED); desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(desc);
        inner.add(Box.createVerticalStrut(25));

        JButton cont = new JButton("Готов! Начать ход");
        cont.setIcon(new SimpleIcon(SimpleIcon.Type.CHECK, 16, 16)); cont.setIconTextGap(8);
        styleButton(cont, COLOR_ACCENT);
        cont.addActionListener(e -> { controller.proceedFromSwitchScreen(); renderCurrentPhase(); });
        inner.add(cont);

        panel.add(inner);
        return panel;
    }

    // ─────────────────────────────────────────────────────
    //  3. БОЙ
    // ─────────────────────────────────────────────────────
    private JPanel createBattleScreen() {
        Player active   = controller.getGameState().getActivePlayer();
        Player opponent = controller.getGameState().getOpponentPlayer();

        JPanel panel = new JPanel(new BorderLayout(15, 10));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JPanel boards = new JPanel(new GridLayout(1, 2, 25, 0));
        boards.setBackground(COLOR_BG);

        // Своё поле (корабли видны, вода везде)
        JPanel ownWrap = new JPanel(new BorderLayout(0, 8));
        ownWrap.setBackground(COLOR_BG);
        JLabel ownTitle = new JLabel("ВАШ ФЛОТ (" + active.getName() + ")", SwingConstants.CENTER);
        ownTitle.setForeground(COLOR_TEXT); ownTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ownWrap.add(ownTitle, BorderLayout.NORTH);
        // showWater=true — все пустые ячейки отображаются как вода
        ownWrap.add(createBoardGridPanel(active.getBoard(), false, true, null), BorderLayout.CENTER);
        boards.add(ownWrap);

        // Поле соперника (туман войны)
        JPanel oppWrap = new JPanel(new BorderLayout(0, 8));
        oppWrap.setBackground(COLOR_BG);
        JLabel oppTitle = new JLabel("ПОЛЕ СОПЕРНИКА (" + opponent.getName() + ") — Атакуйте!", SwingConstants.CENTER);
        oppTitle.setForeground(COLOR_ACCENT); oppTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        oppWrap.add(oppTitle, BorderLayout.NORTH);
        oppWrap.add(createBoardGridPanel(opponent.getBoard(), true, true, (r, c) -> {
            controller.makeShot(new Coordinate(r, c));
            renderCurrentPhase();
        }), BorderLayout.CENTER);
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
            if (JOptionPane.showConfirmDialog(this, "Сдаться?", "Подтверждение",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                controller.surrenderCurrentPlayer();
                renderCurrentPhase();
            }
        });
        bottom.add(surrenderBtn);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    // ─────────────────────────────────────────────────────
    //  4. КОНЕЦ ИГРЫ
    // ─────────────────────────────────────────────────────
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

        Player winner = controller.getGameState().getWinner();
        JLabel wLabel = new JLabel("ПОБЕДИТЕЛЬ: " + (winner != null ? winner.getName() : "Ничья"), SwingConstants.CENTER);
        wLabel.setFont(new Font("Segoe UI", Font.BOLD, 24)); wLabel.setForeground(COLOR_SUCCESS); wLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(wLabel);
        inner.add(Box.createVerticalStrut(25));

        JButton restart = new JButton("Новая игра");
        restart.setIcon(new SimpleIcon(SimpleIcon.Type.CHECK, 16, 16)); restart.setIconTextGap(8);
        styleButton(restart, COLOR_ACCENT);
        restart.addActionListener(e -> {
            controller.restartGame();
            renderCurrentPhase();
            showSoundtrackDialog();
        });
        inner.add(restart);

        panel.add(inner);
        return panel;
    }

    // ─────────────────────────────────────────────────────
    //  СЕТКА ИГРОВОГО ПОЛЯ
    // ─────────────────────────────────────────────────────
    @FunctionalInterface
    private interface CellClickListener { void onClick(int row, int col); }

    /**
     * @param showWater если true — все пустые ячейки отображаются как реалистичная вода (режим боя)
     */
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

    // ─────────────────────────────────────────────────────
    //  ЯЧЕЙКА ПОЛЯ: ВОДА, КОРАБЛИ, ВЗРЫВЫ
    // ─────────────────────────────────────────────────────
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
            boolean isPending = (state == CellState.EMPTY && fogOfWar);

            // Всегда рисуем воду как основу
            if (showWater || state == CellState.MISS || state == CellState.HIT) {
                drawPhotorealisticWater(g2, w, h, row, col);
            } else {
                // Фаза расстановки — простой тёмный фон
                g2.setColor(new Color(24, 34, 52));
                g2.fillRect(0, 0, w, h);
                g2.setColor(new Color(38, 54, 76, 80));
                g2.drawRect(0, 0, w-1, h-1);
            }

            // Корабль
            if (showShip) drawRealisticWarship(g2, cell.getShip(), cell.getCoordinate(), w, h, state == CellState.HIT);

            // Взрыв
            if (state == CellState.HIT) drawExplosion(g2, w, h, hasShip && cell.getShip().isSunk());

            // Всплеск при промахе
            if (state == CellState.MISS) drawWaterSplash(g2, w, h);

            // Прицел при наведении
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

        /**
         * Фотореалистичная вода: многослойный градиент + каустические блики + рябь волн.
         */
        private void drawPhotorealisticWater(Graphics2D g2, int w, int h, int row, int col) {
            // 1. Трёхслойный водный градиент
            GradientPaint deepGrad = new GradientPaint(0, 0, new Color(14, 26, 50), 0, h, new Color(8, 16, 36));
            g2.setPaint(deepGrad);
            g2.fillRect(0, 0, w, h);

            // 2. Мерцающий каустический слой (уникальный для каждой ячейки)
            long seed = row * 17L + col * 31L;
            java.util.Random rnd = new java.util.Random(seed);

            // Несколько полупрозрачных световых пятен (рассеянный свет сквозь воду)
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

            // 3. Волны (горизонтальные дуги переменной яркости)
            g2.setStroke(new BasicStroke(0.6f));
            int waveCount = 3;
            for (int k = 0; k < waveCount; k++) {
                int wy = (int)(h * (0.25 + k * 0.22 + (seed % 7) * 0.015));
                int waveAlpha = 28 + k * 12;
                g2.setColor(new Color(60, 130, 210, waveAlpha));
                g2.drawArc(-2, wy - 3, w + 4, 6, 0, 180);
            }

            // 4. Тонкая отражающая плёнка на поверхности (горизонтальный блик вверху)
            GradientPaint surface = new GradientPaint(0, 0, new Color(80, 160, 240, 30), 0, h/3, new Color(0, 0, 0, 0));
            g2.setPaint(surface);
            g2.fillRect(0, 0, w, h/3);

            // 5. Границы ячейки (тёмная сетка)
            g2.setColor(new Color(10, 20, 38, 120));
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawRect(0, 0, w-1, h-1);
        }

        /** Отрисовка реалистичного военного корабля по сегментам. */
        private void drawRealisticWarship(Graphics2D g2, Ship ship, Coordinate coord, int w, int h, boolean damaged) {
            int size = ship.getSize();
            Orientation ori = ship.getOrientation();
            int index = ship.getCoordinates().indexOf(coord);

            Color hullDark  = damaged ? new Color(35, 38, 44)  : new Color(48, 60, 76);
            Color hullDeck  = damaged ? new Color(50, 54, 60)  : new Color(74, 90, 112);
            Color hullLight = damaged ? new Color(65, 70, 78)  : new Color(105, 126, 154);
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
                    int[] ix = {pad+6, w, w}; int[] iy = {h/2, y0+3, y0+h0-3};
                    g2.setColor(hullDeck); g2.fillPolygon(ix, iy, 3);
                    g2.setColor(turret); g2.fillOval(w-14, h/2-6, 12, 12);
                    g2.setColor(barrel); g2.fillRect(w-20, h/2-2, 7, 2); g2.fillRect(w-20, h/2+1, 7, 2);
                } else if (index == size-1) {
                    g2.setColor(hullDark); g2.fillRoundRect(0, y0, w-pad-2, h0, 12, 12); g2.fillRect(0, y0, 6, h0);
                    g2.setColor(hullDeck); g2.fillRoundRect(0, y0+3, w-pad-5, h0-6, 8, 8); g2.fillRect(0, y0+3, 6, h0-6);
                    g2.setColor(turret); g2.fillOval(8, h/2-5, 10, 10);
                    g2.setColor(barrel); g2.drawOval(10, h/2-3, 6, 6);
                } else {
                    g2.setColor(hullDark); g2.fillRect(0, y0, w, h0);
                    g2.setColor(hullDeck); g2.fillRect(0, y0+3, w, h0-6);
                    g2.setColor(turret); g2.fillRoundRect(w/4, y0+4, w/2, h0-8, 4, 4);
                    g2.setColor(hullLight); g2.fillRect(w/2-2, y0+6, 4, h0-12);
                    g2.setColor(barrel); g2.drawLine(w/2, y0+2, w/2, y0+h0-2);
                }
            } else {
                int x0 = pad+2, w0 = w-2*pad-4;
                if (index == 0) {
                    int[] xp = {w/2, x0, x0+w0}; int[] yp = {pad+2, h, h};
                    g2.setColor(hullDark); g2.fillPolygon(xp, yp, 3);
                    int[] ix = {w/2, x0+3, x0+w0-3}; int[] iy = {pad+6, h, h};
                    g2.setColor(hullDeck); g2.fillPolygon(ix, iy, 3);
                    g2.setColor(turret); g2.fillOval(w/2-6, h-14, 12, 12);
                    g2.setColor(barrel); g2.fillRect(w/2-2, h-20, 2, 7); g2.fillRect(w/2+1, h-20, 2, 7);
                } else if (index == size-1) {
                    g2.setColor(hullDark); g2.fillRoundRect(x0, 0, w0, h-pad-2, 12, 12); g2.fillRect(x0, 0, w0, 6);
                    g2.setColor(hullDeck); g2.fillRoundRect(x0+3, 0, w0-6, h-pad-5, 8, 8); g2.fillRect(x0+3, 0, w0-6, 6);
                    g2.setColor(turret); g2.fillOval(w/2-5, 8, 10, 10);
                    g2.setColor(barrel); g2.drawOval(w/2-3, 10, 6, 6);
                } else {
                    g2.setColor(hullDark); g2.fillRect(x0, 0, w0, h);
                    g2.setColor(hullDeck); g2.fillRect(x0+3, 0, w0-6, h);
                    g2.setColor(turret); g2.fillRoundRect(x0+4, h/4, w0-8, h/2, 4, 4);
                    g2.setColor(hullLight); g2.fillRect(x0+6, h/2-2, w0-12, 4);
                    g2.setColor(barrel); g2.drawLine(x0+2, h/2, x0+w0-2, h/2);
                }
            }
        }

        /** Огненный взрыв при попадании. */
        private void drawExplosion(Graphics2D g2, int w, int h, boolean isSunk) {
            int cx = w/2, cy = h/2;
            g2.setColor(new Color(25, 20, 20, 180));
            g2.fillOval(cx-15, cy-14, 14, 14); g2.fillOval(cx+2, cy-13, 13, 13);
            g2.fillOval(cx-13, cy+2, 12, 12);  g2.fillOval(cx+1, cy+1, 15, 15);

            int pts = 12;
            int[] xp = new int[pts*2], yp = new int[pts*2];
            double rO = w*0.44, rI = w*0.20;
            for (int i = 0; i < pts*2; i++) {
                double angle = i * Math.PI / pts;
                double r = (i%2==0) ? rO : rI;
                xp[i] = (int)(cx + r*Math.cos(angle));
                yp[i] = (int)(cy + r*Math.sin(angle));
            }
            g2.setColor(new Color(230, 45, 10, 220)); g2.fillPolygon(xp, yp, pts*2);

            Point2D center = new Point2D.Float(cx, cy);
            float rad = (float)(w*0.32);
            RadialGradientPaint fire = new RadialGradientPaint(center, rad,
                new float[]{0f, 0.4f, 0.8f, 1f},
                new Color[]{new Color(255,255,230,255), new Color(255,200,30,240), new Color(235,75,10,210), new Color(160,20,10,0)});
            g2.setPaint(fire); g2.fillOval((int)(cx-rad),(int)(cy-rad),(int)(rad*2),(int)(rad*2));

            g2.setColor(new Color(255,245,100));
            g2.fillRect(cx-10,cy-11,2,2); g2.fillRect(cx+9,cy-8,2,2);
            g2.fillRect(cx-8,cy+9,2,2);   g2.fillRect(cx+8,cy+9,2,2);

            if (isSunk) {
                g2.setColor(new Color(40, 10, 10, 200));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawLine(cx-8, cy-8, cx+8, cy+8);
                g2.drawLine(cx+8, cy-8, cx-8, cy+8);
            }
        }

        /** Всплеск при промахе. */
        private void drawWaterSplash(Graphics2D g2, int w, int h) {
            int cx = w/2, cy = h/2;
            g2.setColor(new Color(110, 185, 245, 170)); g2.setStroke(new BasicStroke(1.8f));
            g2.drawOval(cx-11, cy-11, 22, 22);
            g2.setColor(new Color(180, 225, 255, 200)); g2.setStroke(new BasicStroke(1.2f));
            g2.drawOval(cx-6, cy-6, 12, 12);
            g2.setColor(new Color(240, 250, 255)); g2.fillOval(cx-2, cy-2, 5, 5);
            // Брызги
            g2.setColor(new Color(180, 230, 255, 150));
            g2.fillOval(cx-14, cy-5, 4, 4); g2.fillOval(cx+10, cy-5, 4, 4);
            g2.fillOval(cx-5, cy-14, 4, 4); g2.fillOval(cx-5, cy+10, 4, 4);
        }
    }

    // ─────────────────────────────────────────────────────
    //  ИКОНКА КОРАБЛЯ В МЕНЮ
    // ─────────────────────────────────────────────────────
    private static class MiniShipIcon implements Icon {
        private final ShipType type;
        private final int width, height;
        MiniShipIcon(ShipType type, int w, int h) { this.type=type; width=w; height=h; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int decks = type.getSize(), pad = 2;
            int shipLen = Math.min(width-4, decks*14+6), shipH = height-2*pad-2;
            int sx = x + (width-shipLen)/2, sy = y+pad+1;
            g2.setColor(new Color(55, 70, 92));  g2.fillRoundRect(sx+4, sy, shipLen-4, shipH, 4, 4);
            int[] px={sx, sx+6, sx+6}; int[] py={sy+shipH/2, sy, sy+shipH};
            g2.fillPolygon(px, py, 3);
            g2.setColor(new Color(85, 105, 130)); g2.fillRoundRect(sx+5, sy+2, shipLen-8, shipH-4, 3, 3);
            g2.setColor(new Color(40, 50, 65));
            for (int i = 0; i < decks; i++) {
                int tx = sx+7+i*13;
                if (tx+8 <= sx+shipLen) g2.fillOval(tx, sy+shipH/2-3, 6, 6);
            }
            g2.dispose();
        }
        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }
    }

    // ─────────────────────────────────────────────────────
    //  ИКОНКИ КНОПОК
    // ─────────────────────────────────────────────────────
    private static class SimpleIcon implements Icon {
        public enum Type { ANCHOR, SPEAKER_ON, SPEAKER_OFF, DICE, TRASH, CHECK, RADAR, FLAG, TROPHY }

        private final Type type; private final int width, height;
        SimpleIcon(Type t, int w, int h) { type=t; width=w; height=h; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            switch (type) {
                case ANCHOR:
                    g2.setColor(COLOR_ACCENT); g2.setStroke(new BasicStroke(2.2f));
                    int mx = x+width/2;
                    g2.drawOval(mx-3, y+2, 6, 6); g2.drawLine(mx, y+8, mx, y+height-3);
                    g2.drawLine(mx-6, y+9, mx+6, y+9); g2.drawArc(mx-8, y+height-12, 16, 10, 180, 180);
                    break;
                case SPEAKER_ON:
                    g2.setColor(COLOR_TEXT); g2.fillRect(x+2, y+4, 3, height-8);
                    int[] sx={x+5,x+9,x+9,x+5}; int[] sy={y+4,y+1,y+height-1,y+height-4};
                    g2.fillPolygon(sx, sy, 4); g2.setStroke(new BasicStroke(1.5f));
                    g2.drawArc(x+8, y+3, 6, height-6, -60, 120); g2.drawArc(x+10, y+1, 8, height-2, -60, 120);
                    break;
                case SPEAKER_OFF:
                    g2.setColor(COLOR_TEXT_MUTED); g2.fillRect(x+2, y+4, 3, height-8);
                    int[] ox={x+5,x+9,x+9,x+5}; int[] oy={y+4,y+1,y+height-1,y+height-4};
                    g2.fillPolygon(ox, oy, 4); g2.setColor(COLOR_DANGER); g2.setStroke(new BasicStroke(1.8f));
                    g2.drawLine(x+11, y+4, x+16, y+height-4); g2.drawLine(x+16, y+4, x+11, y+height-4);
                    break;
                case DICE:
                    g2.setColor(COLOR_TEXT); g2.fillRoundRect(x+1, y+1, width-2, height-2, 4, 4);
                    g2.setColor(COLOR_PANEL_BG);
                    g2.fillOval(x+3, y+3, 3, 3); g2.fillOval(x+width-6, y+3, 3, 3);
                    g2.fillOval(x+width/2-1, y+height/2-1, 3, 3);
                    g2.fillOval(x+3, y+height-6, 3, 3); g2.fillOval(x+width-6, y+height-6, 3, 3);
                    break;
                case TRASH:
                    g2.setColor(COLOR_TEXT); g2.setStroke(new BasicStroke(1.5f));
                    g2.drawLine(x+2, y+4, x+width-2, y+4); g2.drawRoundRect(x+3, y+5, width-6, height-7, 2, 2);
                    g2.drawLine(x+6, y+7, x+6, y+height-4); g2.drawLine(x+width-7, y+7, x+width-7, y+height-4);
                    break;
                case CHECK:
                    g2.setColor(COLOR_SUCCESS); g2.setStroke(new BasicStroke(2.5f));
                    g2.drawLine(x+2, y+height/2, x+width/3, y+height-3);
                    g2.drawLine(x+width/3, y+height-3, x+width-2, y+3);
                    break;
                case RADAR:
                    g2.setColor(COLOR_ACCENT); g2.setStroke(new BasicStroke(2.0f));
                    g2.drawOval(x+2, y+2, width-4, height-4); g2.drawOval(x+8, y+8, width-16, height-16);
                    g2.drawLine(x+width/2, y+2, x+width/2, y+height-2);
                    g2.drawLine(x+2, y+height/2, x+width-2, y+height/2);
                    g2.setColor(COLOR_SUCCESS); g2.fillOval(x+width/2+4, y+height/2-8, 4, 4);
                    break;
                case FLAG:
                    g2.setColor(COLOR_TEXT); g2.setStroke(new BasicStroke(2.0f));
                    g2.drawLine(x+3, y+2, x+3, y+height-1);
                    int[] fx={x+3, x+width-2, x+3}; int[] fy={y+2, y+height/3, y+2*height/3};
                    g2.fillPolygon(fx, fy, 3);
                    break;
                case TROPHY:
                    g2.setColor(new Color(241, 196, 15));
                    g2.fillRoundRect(x+6, y+4, width-12, height/2, 6, 6);
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.drawArc(x+2, y+6, 8, 12, 90, 180); g2.drawArc(x+width-10, y+6, 8, 12, -90, 180);
                    g2.fillRect(x+width/2-2, y+height/2+4, 4, height/4);
                    g2.fillRoundRect(x+8, y+3*height/4+4, width-16, 6, 2, 2);
                    break;
            }
            g2.dispose();
        }
        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }
    }
}

package com.seabattle.network;

import com.seabattle.controller.SoundManager;
import com.seabattle.model.Board;
import com.seabattle.model.Cell;
import com.seabattle.model.CellState;
import com.seabattle.model.Coordinate;
import com.seabattle.model.Orientation;
import com.seabattle.model.Ship;
import com.seabattle.model.ShipType;
import com.seabattle.model.ShotResult;
import com.seabattle.view.gui.SeaBattleBoardView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * JavaFX interface for LAN Sea Battle.
 */
public final class NetworkGuiView extends Stage implements NetworkManager.NetworkListener {
    private static final String BG = "#141820";
    private static final String PANEL = "#1c2330";
    private static final String TEXT = "#e1e8f4";
    private static final String ACCENT = "#3498db";

    private final NetworkManager networkManager = new NetworkManager(this);
    private final SoundManager soundManager = SoundManager.getInstance();
    private final BorderPane root = new BorderPane();
    private final StackPane mainContent = new StackPane();
    private final Label statusLabel = new Label();
    private final Button soundButton = new Button();

    private Board myBoard = new Board();
    private Board opponentBoard = new Board();
    private boolean isHost;
    private boolean myReady;
    private boolean opponentReady;
    private boolean myTurn;
    private boolean gameStarted;
    private boolean gameOver;
    private boolean waitingForShotResponse;
    private String winnerName = "";
    private ShipType selectedShipType = ShipType.BATTLESHIP;
    private Orientation selectedOrientation = Orientation.HORIZONTAL;

    private TextField ipField;
    private TextField portField;
    private Label connectionStatusLabel;
    private Button hostButton;
    private Button connectButton;

    public NetworkGuiView() {
        setTitle("Морской бой — Сетевая игра (LAN)");
        setMinWidth(980);
        setMinHeight(680);
        root.setStyle("-fx-background-color: " + BG + ";");
        root.setTop(createTopBar());
        root.setCenter(mainContent);
        setScene(new Scene(root, 1080, 740));
        setOnCloseRequest(event -> {
            networkManager.sendMessage("DISCONNECT");
            networkManager.closeConnection();
            soundManager.stopBackgroundMusic();
        });
        showConnectionScreen();
    }

    private HBox createTopBar() {
        Label title = new Label("⚓  МОРСКОЙ БОЙ (СЕТЬ)");
        title.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: " + TEXT + ";");
        statusLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + ACCENT + ";");
        soundButton.setOnAction(event -> {
            soundManager.toggleSound();
            updateSoundButton();
        });
        updateSoundButton();
        HBox bar = new HBox(20, title, statusLabel, soundButton);
        bar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(14, 20, 14, 20));
        bar.setStyle("-fx-background-color: " + PANEL + ";");
        return bar;
    }

    private void updateSoundButton() {
        SoundManager.Soundtrack soundtrack = soundManager.getCurrentSoundtrack();
        soundButton.setText(soundManager.isSoundEnabled() && soundtrack != SoundManager.Soundtrack.OFF
                ? soundtrack.title : "Музыка: Выкл");
    }

    private void showSoundtrackDialog() {
        Dialog<SoundManager.Soundtrack> dialog = new Dialog<>();
        dialog.initOwner(this);
        dialog.setTitle("Музыка для битвы");
        dialog.setHeaderText("Выберите саундтрек");
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Travis Scott — FE!N"),
                new ButtonType("Eminem — Lose Yourself"),
                new ButtonType("Eminem — Superman"),
                new ButtonType("Без музыки"));
        dialog.setResultConverter(button -> switch (dialog.getDialogPane().getButtonTypes().indexOf(button)) {
            case 0 -> SoundManager.Soundtrack.FEIN;
            case 1 -> SoundManager.Soundtrack.LOSE_YOURSELF;
            case 2 -> SoundManager.Soundtrack.SUPERMAN;
            default -> SoundManager.Soundtrack.OFF;
        });
        Optional<SoundManager.Soundtrack> selection = dialog.showAndWait();
        soundManager.setSoundtrack(selection.orElse(SoundManager.Soundtrack.OFF));
        updateSoundButton();
    }

    private void showConnectionScreen() {
        statusLabel.setText("Выбор роли в локальной сети");
        Label title = heading("СЕТЕВОЙ МОРСКОЙ БОЙ");
        Label localIp = new Label("Ваш локальный IP: " + NetworkManager.getLocalIpAddress());
        localIp.setStyle("-fx-text-fill: #2ecc71;");
        ipField = new TextField("127.0.0.1");
        portField = new TextField("8888");
        ipField.setPromptText("IP сервера");
        portField.setPromptText("Порт");
        hostButton = actionButton("Создать сервер (Host)", ACCENT, this::startHost);
        connectButton = actionButton("Подключиться (Client)", "#2ecc71", this::startClient);
        connectionStatusLabel = new Label("Выберите «Создать сервер» или «Подключиться»");
        connectionStatusLabel.setWrapText(true);
        connectionStatusLabel.setStyle("-fx-text-fill: #8291a8;");
        VBox card = new VBox(16, title, localIp,
                labeledField("IP сервера:", ipField),
                labeledField("Порт:", portField),
                hostButton, connectButton, connectionStatusLabel);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(460);
        card.setPadding(new Insets(30, 40, 30, 40));
        card.setStyle("-fx-background-color: " + PANEL + "; -fx-background-radius: 8;");
        mainContent.getChildren().setAll(card);
    }

    private HBox labeledField(String label, TextField field) {
        Label caption = new Label(label);
        caption.setMinWidth(90);
        caption.setStyle("-fx-text-fill: " + TEXT + ";");
        field.setStyle("-fx-control-inner-background: #141a24; -fx-text-fill: " + TEXT + ";");
        HBox row = new HBox(10, caption, field);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(field, Priority.ALWAYS);
        return row;
    }

    private void startHost() {
        Integer port = parsePort();
        if (port == null) {
            return;
        }
        isHost = true;
        setConnectionControlsDisabled(true);
        connectionStatusLabel.setText("Ожидание подключения второго игрока на "
                + NetworkManager.getLocalIpAddress() + ":" + port + "...");
        connectionStatusLabel.setStyle("-fx-text-fill: #f1c40f;");
        statusLabel.setText("Ожидание подключения клиента...");
        networkManager.startServer(port);
    }

    private void startClient() {
        Integer port = parsePort();
        if (port == null) {
            return;
        }
        String host = ipField.getText().trim();
        if (host.isEmpty()) {
            showError("Укажите IP-адрес сервера.");
            return;
        }
        isHost = false;
        setConnectionControlsDisabled(true);
        connectionStatusLabel.setText("Подключение к " + host + ":" + port + "...");
        connectionStatusLabel.setStyle("-fx-text-fill: #f1c40f;");
        statusLabel.setText("Попытка подключения...");
        networkManager.connectToServer(host, port);
    }

    private Integer parsePort() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            if (port < 1 || port > 65535) {
                throw new NumberFormatException();
            }
            return port;
        } catch (NumberFormatException ex) {
            showError("Укажите корректный порт от 1 до 65535.");
            return null;
        }
    }

    private void setConnectionControlsDisabled(boolean disabled) {
        hostButton.setDisable(disabled);
        connectButton.setDisable(disabled);
        ipField.setDisable(disabled);
        portField.setDisable(disabled);
    }

    @Override
    public void onConnected(boolean host, String remoteAddress) {
        isHost = host;
        statusLabel.setText("Соединение установлено! Фаза расстановки");
        connectionStatusLabel.setText("Подключено: " + remoteAddress);
        showSoundtrackDialog();
        renderCurrentPhase();
    }

    @Override
    public void onConnectionFailed(String errorMessage) {
        showError(errorMessage);
        setConnectionControlsDisabled(false);
        connectionStatusLabel.setText("Ошибка подключения. Попробуйте снова.");
        connectionStatusLabel.setStyle("-fx-text-fill: #e74c3c;");
        statusLabel.setText("Ошибка соединения");
    }

    @Override
    public void onDisconnected() {
        if (!gameOver && isShowing()) {
            showWarning("Соперник отключился или связь прервана.");
            statusLabel.setText("Соединение прервано");
        }
    }

    @Override
    public void onMessageReceived(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        String[] parts = message.split(":", -1);
        switch (parts[0]) {
            case "READY" -> {
                opponentReady = true;
                if (myReady) {
                    startBattlePhase();
                } else {
                    statusLabel.setText("Соперник готов! Расставьте корабли");
                }
                renderCurrentPhase();
            }
            case "SHOT" -> processIncomingShot(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            case "SHOT_RESULT" -> processShotResponse(Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]), parts[3], parts.length > 4 ? parts[4] : "");
            case "SURRENDER" -> {
                gameOver = true;
                winnerName = "Вы (Соперник сдался)";
                statusLabel.setText("Победа! Соперник сдался");
                renderCurrentPhase();
            }
            case "DISCONNECT" -> onDisconnected();
            default -> System.err.println("Неизвестное сетевое сообщение: " + message);
        }
    }

    private void startBattlePhase() {
        gameStarted = true;
        myTurn = isHost;
        statusLabel.setText(myTurn ? "Ваш ход" : "Ход противника");
    }

    private void processIncomingShot(int row, int column) {
        ShotResult result = myBoard.shoot(new Coordinate(row, column));
        String extraMisses = "";
        if (result == ShotResult.SUNK) {
            Ship sunkShip = myBoard.getCell(row, column).getShip();
            if (sunkShip != null) {
                StringBuilder misses = new StringBuilder();
                for (Coordinate coordinate : sunkShip.getCoordinates()) {
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            int r = coordinate.getRow() + dr;
                            int c = coordinate.getCol() + dc;
                            if (myBoard.isValidCoordinate(r, c)
                                    && myBoard.getCell(r, c).getState() == CellState.MISS) {
                                misses.append(r).append(',').append(c).append(';');
                            }
                        }
                    }
                }
                extraMisses = misses.toString();
            }
        }

        networkManager.sendMessage("SHOT_RESULT:" + row + ":" + column + ":" + result.name() + ":" + extraMisses);
        if (result == ShotResult.MISS) {
            soundManager.playMissSound();
            myTurn = true;
            statusLabel.setText("Ваш ход");
        } else {
            soundManager.playHitSound();
            myTurn = false;
            statusLabel.setText("Ход противника");
        }
        if (myBoard.allShipsSunk()) {
            gameOver = true;
            winnerName = "Соперник";
            statusLabel.setText("Поражение!");
        }
        renderCurrentPhase();
    }

    private void processShotResponse(int row, int column, String resultText, String extraCoords) {
        waitingForShotResponse = false;
        ShotResult result = ShotResult.valueOf(resultText);
        Cell cell = opponentBoard.getCell(row, column);
        if (result == ShotResult.HIT || result == ShotResult.SUNK) {
            cell.setState(CellState.HIT);
            soundManager.playHitSound();
            myTurn = true;
            statusLabel.setText("Ваш ход (Попадание!)");
            if (result == ShotResult.SUNK && !extraCoords.isEmpty()) {
                for (String pair : extraCoords.split(";")) {
                    if (!pair.isBlank()) {
                        String[] coordinate = pair.split(",");
                        opponentBoard.getCell(Integer.parseInt(coordinate[0]),
                                Integer.parseInt(coordinate[1])).setState(CellState.MISS);
                    }
                }
            }
            int hitDecks = 0;
            for (int r = 0; r < Board.SIZE; r++) {
                for (int c = 0; c < Board.SIZE; c++) {
                    if (opponentBoard.getCell(r, c).getState() == CellState.HIT) {
                        hitDecks++;
                    }
                }
            }
            if (hitDecks >= 20) {
                gameOver = true;
                winnerName = "Вы";
                statusLabel.setText("Победа!");
            }
        } else if (result == ShotResult.MISS) {
            cell.setState(CellState.MISS);
            soundManager.playMissSound();
            myTurn = false;
            statusLabel.setText("Ход противника");
        }
        renderCurrentPhase();
    }

    public void renderCurrentPhase() {
        if (gameOver) {
            mainContent.getChildren().setAll(createGameOverScreen());
        } else if (!gameStarted) {
            mainContent.getChildren().setAll(createPlacementScreen());
        } else {
            mainContent.getChildren().setAll(createBattleScreen());
        }
    }

    private BorderPane createPlacementScreen() {
        SeaBattleBoardView boardView = new SeaBattleBoardView(myBoard, false, false, (row, column) -> {
            if (myReady) {
                return;
            }
            if (myBoard.getRemainingCount(selectedShipType) <= 0) {
                showWarning("Лимит для корабля \"" + selectedShipType.getName() + "\" исчерпан.");
            } else if (myBoard.placeShip(selectedShipType, new Coordinate(row, column), selectedOrientation)) {
                selectNextAvailableShip();
                renderCurrentPhase();
            } else {
                showWarning("Невозможно разместить корабль здесь: проверьте границы и буферную зону.");
            }
        });

        VBox controls = new VBox(10);
        controls.setPadding(new Insets(14));
        controls.setPrefWidth(330);
        controls.setStyle("-fx-background-color: " + PANEL + "; -fx-background-radius: 6;");
        controls.getChildren().add(heading("ВАШ ФЛОТ"));
        for (ShipType type : ShipType.values()) {
            int remaining = myBoard.getRemainingCount(type);
            Button ship = actionButton(type.getName() + "  —  "
                    + (type.getMaxAllowed() - remaining) + "/" + type.getMaxAllowed()
                    + "   Осталось: " + remaining, type == selectedShipType ? ACCENT : "#202836",
                    () -> { selectedShipType = type; renderCurrentPhase(); });
            ship.setDisable(myReady);
            controls.getChildren().add(ship);
        }
        controls.getChildren().add(heading("Ориентация"));
        ToggleGroup group = new ToggleGroup();
        RadioButton horizontal = new RadioButton("Горизонтально");
        RadioButton vertical = new RadioButton("Вертикально");
        horizontal.setToggleGroup(group);
        vertical.setToggleGroup(group);
        horizontal.setSelected(selectedOrientation == Orientation.HORIZONTAL);
        vertical.setSelected(selectedOrientation == Orientation.VERTICAL);
        styleRadio(horizontal);
        styleRadio(vertical);
        horizontal.setDisable(myReady);
        vertical.setDisable(myReady);
        horizontal.setOnAction(event -> selectedOrientation = Orientation.HORIZONTAL);
        vertical.setOnAction(event -> selectedOrientation = Orientation.VERTICAL);
        controls.getChildren().add(new HBox(10, horizontal, vertical));

        Button auto = actionButton("Случайная расстановка", "#2a3a50", () -> {
            myBoard.autoPlaceAllShips();
            renderCurrentPhase();
        });
        Button clear = actionButton("Очистить поле", "#2a3a50", () -> {
            myBoard.clear();
            renderCurrentPhase();
        });
        auto.setDisable(myReady);
        clear.setDisable(myReady);
        boolean fullyPlaced = myBoard.isFleetFullyPlaced();
        Button ready = actionButton(myReady ? "Ожидание готовности соперника..."
                        : fullyPlaced ? "В БОЙ! Я готов" : "Расставьте все корабли",
                fullyPlaced && !myReady ? "#2ecc71" : "#2d3440", () -> {
                    myReady = true;
                    networkManager.sendMessage("READY");
                    if (opponentReady) {
                        startBattlePhase();
                    } else {
                        statusLabel.setText("Ожидание готовности соперника...");
                    }
                    renderCurrentPhase();
                });
        ready.setDisable(!fullyPlaced || myReady);
        controls.getChildren().addAll(auto, clear, ready);

        BorderPane screen = new BorderPane();
        screen.setPadding(new Insets(16, 22, 16, 22));
        screen.setCenter(centered(boardView));
        screen.setRight(controls);
        BorderPane.setMargin(controls, new Insets(0, 0, 0, 20));
        return screen;
    }

    private void selectNextAvailableShip() {
        if (myBoard.getRemainingCount(selectedShipType) == 0) {
            for (ShipType type : ShipType.values()) {
                if (myBoard.getRemainingCount(type) > 0) {
                    selectedShipType = type;
                    return;
                }
            }
        }
    }

    private BorderPane createBattleScreen() {
        SeaBattleBoardView own = new SeaBattleBoardView(myBoard, false, true, null);
        SeaBattleBoardView opponent = new SeaBattleBoardView(opponentBoard, true, true,
                myTurn && !waitingForShotResponse ? (row, column) -> {
                    if (opponentBoard.getCell(row, column).getState() == CellState.EMPTY) {
                        waitingForShotResponse = true;
                        networkManager.sendMessage("SHOT:" + row + ":" + column);
                        renderCurrentPhase();
                    }
                } : null);
        VBox ownSection = boardSection("ВАШ ФЛОТ", own);
        VBox opponentSection = boardSection(myTurn ? "ПОЛЕ СОПЕРНИКА — АТАКУЙТЕ!"
                : "ПОЛЕ СОПЕРНИКА — ОЖИДАНИЕ ХОДА", opponent);
        HBox boards = new HBox(24, ownSection, opponentSection);
        boards.setAlignment(Pos.CENTER);

        Button surrender = actionButton("Сдаться", "#e74c3c", () -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Вы действительно хотите сдаться?");
            confirm.initOwner(this);
            confirm.showAndWait().filter(ButtonType.OK::equals).ifPresent(button -> {
                networkManager.sendMessage("SURRENDER");
                gameOver = true;
                winnerName = "Соперник (Вы сдались)";
                statusLabel.setText("Поражение");
                renderCurrentPhase();
            });
        });
        HBox footer = new HBox(surrender);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(10));
        BorderPane screen = new BorderPane(boards);
        screen.setBottom(footer);
        return screen;
    }

    private StackPane createGameOverScreen() {
        boolean won = winnerName.startsWith("Вы");
        Label result = heading(won ? "ПОБЕДА!" : "ПОРАЖЕНИЕ");
        result.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: "
                + (won ? "#2ecc71" : "#e74c3c") + ";");
        Label winner = new Label("Победитель: " + winnerName);
        winner.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 15px;");
        Button close = actionButton("Выйти из сетевой игры", ACCENT, () -> {
            networkManager.closeConnection();
            close();
        });
        VBox card = new VBox(18, result, winner, close);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setStyle("-fx-background-color: " + PANEL + "; -fx-background-radius: 8;");
        return centered(card);
    }

    private VBox boardSection(String title, SeaBattleBoardView board) {
        VBox section = new VBox(8, heading(title), board);
        section.setAlignment(Pos.CENTER);
        return section;
    }

    private StackPane centered(javafx.scene.Node node) {
        StackPane pane = new StackPane(node);
        pane.setAlignment(Pos.CENTER);
        return pane;
    }

    private Label heading(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TEXT + ";");
        return label;
    }

    private Button actionButton(String text, String color, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: " + TEXT
                + "; -fx-font-weight: bold; -fx-padding: 9px 13px;");
        button.setOnAction(event -> action.run());
        return button;
    }

    private void styleRadio(RadioButton radio) {
        radio.setStyle("-fx-text-fill: " + TEXT + ";");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.initOwner(this);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.initOwner(this);
        alert.showAndWait();
    }
}

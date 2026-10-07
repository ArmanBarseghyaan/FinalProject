package com.seabattle.view.gui;

import com.seabattle.controller.GameController;
import com.seabattle.controller.SoundManager;
import com.seabattle.model.Board;
import com.seabattle.model.Coordinate;
import com.seabattle.model.GamePhase;
import com.seabattle.model.GameState;
import com.seabattle.model.Orientation;
import com.seabattle.model.Player;
import com.seabattle.model.ShipType;
import com.seabattle.view.GameView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * JavaFX interface for local, hot-seat Sea Battle.
 */
public final class JavaFxGuiView implements GameView {
    private static final String BG = "#141820";
    private static final String PANEL = "#1c2330";
    private static final String TEXT = "#e1e8f4";
    private static final String ACCENT = "#3498db";

    private final GameController controller;
    private final Stage stage = new Stage();
    private final BorderPane root = new BorderPane();
    private final Label statusLabel = new Label();
    private final Button soundButton = new Button();

    private ShipType selectedShipType = ShipType.BATTLESHIP;
    private Orientation selectedOrientation = Orientation.HORIZONTAL;

    public JavaFxGuiView(GameController controller) {
        this.controller = controller;
        stage.setTitle("Морской бой — Hot-seat");
        stage.setMinWidth(980);
        stage.setMinHeight(680);
        root.setStyle("-fx-background-color: " + BG + ";");
        root.setTop(createTopBar());
        stage.setScene(new Scene(root, 1050, 720));
        stage.setOnCloseRequest(event -> controller.getSoundManager().stopBackgroundMusic());
    }

    @Override
    public void start() {
        stage.show();
        renderCurrentPhase();
        showSoundtrackDialog();
    }

    private HBox createTopBar() {
        Label title = new Label("⚓  МОРСКОЙ БОЙ");
        title.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: " + TEXT + ";");
        statusLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + ACCENT + ";");
        soundButton.setOnAction(event -> {
            controller.getSoundManager().toggleSound();
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
        SoundManager manager = controller.getSoundManager();
        SoundManager.Soundtrack track = manager.getCurrentSoundtrack();
        soundButton.setText(manager.isSoundEnabled() && track != SoundManager.Soundtrack.OFF
                ? track.title : "Музыка: Выкл");
    }

    private void showSoundtrackDialog() {
        Dialog<SoundManager.Soundtrack> dialog = new Dialog<>();
        dialog.initOwner(stage);
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
        controller.getSoundManager().setSoundtrack(selection.orElse(SoundManager.Soundtrack.OFF));
        updateSoundButton();
    }

    public void renderCurrentPhase() {
        GameState state = controller.getGameState();
        GamePhase phase = state.getCurrentPhase();
        switch (phase) {
            case PLACEMENT_PLAYER_1, PLACEMENT_PLAYER_2 -> {
                statusLabel.setText("Расстановка флота: " + state.getActivePlayer().getName());
                root.setCenter(createPlacementScreen());
            }
            case SWITCHING_PLAYER -> {
                statusLabel.setText("Передача управления");
                root.setCenter(createSwitchScreen());
            }
            case PLAYER_1_TURN, PLAYER_2_TURN -> {
                statusLabel.setText("Ход: " + state.getActivePlayer().getName());
                root.setCenter(createBattleScreen());
            }
            case GAME_OVER -> {
                statusLabel.setText("Сражение окончено!");
                root.setCenter(createGameOverScreen());
            }
        }
    }

    private BorderPane createPlacementScreen() {
        Player active = controller.getGameState().getActivePlayer();
        Board board = active.getBoard();
        SeaBattleBoardView boardView = new SeaBattleBoardView(board, false, false, (row, column) -> {
            if (board.getRemainingCount(selectedShipType) <= 0) {
                showWarning("Лимит для корабля \"" + selectedShipType.getName() + "\" исчерпан.");
            } else if (controller.placeShip(selectedShipType, new Coordinate(row, column), selectedOrientation)) {
                selectNextAvailableShip(board);
                renderCurrentPhase();
            } else {
                showWarning("Невозможно разместить корабль здесь: проверьте границы и буферную зону.");
            }
        });

        VBox controls = new VBox(10);
        controls.setPadding(new Insets(14));
        controls.setPrefWidth(330);
        controls.setStyle("-fx-background-color: " + PANEL + "; -fx-background-radius: 6;");
        Label heading = heading("КОРАБЛИ ФЛОТА");
        controls.getChildren().add(heading);
        for (ShipType type : ShipType.values()) {
            int remaining = board.getRemainingCount(type);
            Button ship = new Button(type.getName() + "  —  "
                    + (type.getMaxAllowed() - remaining) + "/" + type.getMaxAllowed()
                    + "   Осталось: " + remaining);
            ship.setMaxWidth(Double.MAX_VALUE);
            ship.setStyle(buttonStyle(type == selectedShipType ? ACCENT : "#202836"));
            ship.setOnAction(event -> {
                selectedShipType = type;
                renderCurrentPhase();
            });
            controls.getChildren().add(ship);
        }

        Label orientationLabel = new Label("Ориентация");
        orientationLabel.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-weight: bold;");
        ToggleGroup orientationGroup = new ToggleGroup();
        RadioButton horizontal = new RadioButton("Горизонтально");
        RadioButton vertical = new RadioButton("Вертикально");
        horizontal.setToggleGroup(orientationGroup);
        vertical.setToggleGroup(orientationGroup);
        horizontal.setSelected(selectedOrientation == Orientation.HORIZONTAL);
        vertical.setSelected(selectedOrientation == Orientation.VERTICAL);
        styleRadio(horizontal);
        styleRadio(vertical);
        horizontal.setOnAction(event -> selectedOrientation = Orientation.HORIZONTAL);
        vertical.setOnAction(event -> selectedOrientation = Orientation.VERTICAL);
        controls.getChildren().addAll(orientationLabel, new HBox(10, horizontal, vertical));

        Button auto = actionButton("Случайная расстановка", "#2a3a50",
                () -> { controller.autoPlaceFleet(); renderCurrentPhase(); });
        Button clear = actionButton("Очистить поле", "#2a3a50",
                () -> { controller.clearFleet(); renderCurrentPhase(); });
        boolean ready = board.isFleetFullyPlaced();
        Button confirm = actionButton(ready ? "В БОЙ! Готово" : "Расставьте все корабли",
                ready ? "#2ecc71" : "#2d3440", () -> {
                    if (controller.confirmPlacement()) {
                        renderCurrentPhase();
                    }
                });
        confirm.setDisable(!ready);
        controls.getChildren().addAll(auto, clear, confirm);

        BorderPane screen = new BorderPane();
        screen.setPadding(new Insets(16, 22, 16, 22));
        screen.setCenter(centered(boardView));
        screen.setRight(controls);
        BorderPane.setMargin(controls, new Insets(0, 0, 0, 20));
        return screen;
    }

    private void selectNextAvailableShip(Board board) {
        if (board.getRemainingCount(selectedShipType) == 0) {
            for (ShipType type : ShipType.values()) {
                if (board.getRemainingCount(type) > 0) {
                    selectedShipType = type;
                    return;
                }
            }
        }
    }

    private StackPane createSwitchScreen() {
        Player active = controller.getGameState().getActivePlayer();
        Label title = heading("ПЕРЕДАЧА УПРАВЛЕНИЯ");
        Label prompt = new Label("Передайте устройство: "
                + (active == null ? "следующему игроку" : active.getName()));
        prompt.setStyle("-fx-font-size: 16px; -fx-text-fill: #8291a8;");
        Button proceed = actionButton("Готов! Начать ход", ACCENT, () -> {
            controller.proceedFromSwitchScreen();
            renderCurrentPhase();
        });
        VBox content = new VBox(20, title, prompt, proceed);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        content.setStyle("-fx-background-color: " + PANEL + "; -fx-background-radius: 8;");
        return centered(content);
    }

    private BorderPane createBattleScreen() {
        Player active = controller.getGameState().getActivePlayer();
        Player opponent = controller.getGameState().getOpponentPlayer();
        SeaBattleBoardView own = new SeaBattleBoardView(active.getBoard(), false, true, null);
        SeaBattleBoardView enemy = new SeaBattleBoardView(opponent.getBoard(), true, true, (row, column) -> {
            controller.makeShot(new Coordinate(row, column));
            renderCurrentPhase();
        });
        VBox ownBox = boardSection("ВАШ ФЛОТ (" + active.getName() + ")", own);
        VBox enemyBox = boardSection("ПОЛЕ СОПЕРНИКА — АТАКУЙТЕ", enemy);
        HBox boards = new HBox(24, ownBox, enemyBox);
        boards.setAlignment(Pos.CENTER);
        Button surrender = actionButton("Сдаться", "#e74c3c", () -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Вы действительно хотите сдаться?");
            confirm.initOwner(stage);
            confirm.showAndWait().filter(ButtonType.OK::equals).ifPresent(button -> {
                controller.surrenderCurrentPlayer();
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
        Player winner = controller.getGameState().getWinner();
        Label result = heading("ПОБЕДИТЕЛЬ: " + (winner == null ? "Ничья" : winner.getName()));
        result.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2ecc71;");
        Button restart = actionButton("Новая игра", ACCENT, () -> {
            controller.restartGame();
            selectedShipType = ShipType.BATTLESHIP;
            renderCurrentPhase();
            showSoundtrackDialog();
        });
        VBox content = new VBox(22, result, restart);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        content.setStyle("-fx-background-color: " + PANEL + "; -fx-background-radius: 8;");
        return centered(content);
    }

    private VBox boardSection(String title, SeaBattleBoardView board) {
        Label label = heading(title);
        VBox box = new VBox(8, label, board);
        box.setAlignment(Pos.CENTER);
        return box;
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
        button.setStyle(buttonStyle(color));
        button.setOnAction(event -> action.run());
        return button;
    }

    private String buttonStyle(String color) {
        return "-fx-background-color: " + color + "; -fx-text-fill: " + TEXT
                + "; -fx-font-weight: bold; -fx-padding: 9px 13px;";
    }

    private void styleRadio(RadioButton radio) {
        radio.setStyle("-fx-text-fill: " + TEXT + ";");
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.initOwner(stage);
        alert.showAndWait();
    }
}

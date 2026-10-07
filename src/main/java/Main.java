import com.seabattle.controller.GameController;
import com.seabattle.network.NetworkGuiView;
import com.seabattle.view.console.ConsoleView;
import com.seabattle.view.gui.JavaFxGuiView;
import finalproject.pacman.ui.PacmanGamePanel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import racing.model.DifficultyLevel;
import racing.ui.GamePanel;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * JavaFX launcher and menu for the game collection.
 */
public class Main extends Application {
    private static final String BG = "#121620";
    private static final String PANEL = "#1c2434";

    private Stage primaryStage;
    private static boolean cliToolkitStarted;

    public static void main(String[] args) {
        if (args.length > 0 && "--cli".equalsIgnoreCase(args[0])) {
            runConsoleMenu();
            return;
        }
        Application.launch(Main.class, args);
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("FinalProject — Игровой центр");
        stage.setMinWidth(560);
        stage.setMinHeight(600);
        stage.setScene(new Scene(createMenu(), 680, 700));
        stage.show();
    }

    private VBox createMenu() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: " + BG + ";");

        Label title = new Label("СБОРНИК ИГР FINAL PROJECT");
        title.setStyle("-fx-font-size: 23px; -fx-font-weight: bold; -fx-text-fill: #3498db;");
        Label subtitle = new Label("Выберите игру для начала");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #8c9bb4;");
        VBox header = new VBox(7, title, subtitle);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(4, 0, 12, 0));
        root.getChildren().add(header);

        root.getChildren().addAll(
                gameCard("ГОНКИ", "Аркадные гонки с нитро, монетами и препятствиями",
                        "#e67e22", this::launchRacing),
                gameCard("МОРСКОЙ БОЙ", "Сетевая игра по LAN или локальный режим для двух игроков",
                        "#2980b9", this::chooseSeaBattleMode),
                gameCard("ЗМЕЙКА", "Классическая аркада со змейкой и едой",
                        "#27ae60", () -> showGame(new com.snake.ui.GamePanel(), "Змейка", 500, 525)),
                gameCard("ПАКМАН", "Собирайте точки в лабиринте и избегайте призраков",
                        "#f1c40f", this::launchPacman));

        Button exit = new Button("ВЫЙТИ ИЗ ПРОГРАММЫ");
        exit.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold;");
        exit.setOnAction(event -> Platform.exit());
        HBox footer = new HBox(exit);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(7, 0, 0, 0));
        root.getChildren().add(footer);
        return root;
    }

    private HBox gameCard(String title, String description, String accent, Runnable action) {
        Label name = new Label(title);
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + accent + ";");
        Label details = new Label(description);
        details.setWrapText(true);
        details.setStyle("-fx-text-fill: #8c9bb4; -fx-font-size: 12px;");
        VBox info = new VBox(5, name, details);
        info.setAlignment(Pos.CENTER_LEFT);
        Button play = new Button("ЗАПУСТИТЬ");
        play.setStyle("-fx-background-color: " + accent + "; -fx-text-fill: white; -fx-font-weight: bold;");
        play.setOnAction(event -> action.run());
        HBox card = new HBox(18, info, play);
        card.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);
        card.setPadding(new Insets(15, 17, 15, 17));
        card.setStyle("-fx-background-color: " + PANEL + "; -fx-border-color: " + accent
                + "; -fx-border-radius: 7px; -fx-background-radius: 7px;");
        return card;
    }

    private void launchRacing() {
        List<String> options = List.of("Легкий (EASY)", "Средний (MEDIUM)", "Сложный (HARD)");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(options.get(1), options);
        if (primaryStage != null) {
            dialog.initOwner(primaryStage);
        }
        dialog.setTitle("Настройка гонок");
        dialog.setHeaderText("Выберите уровень сложности:");
        Optional<String> choice = dialog.showAndWait();
        if (choice.isEmpty()) {
            return;
        }
        DifficultyLevel difficulty = switch (options.indexOf(choice.get())) {
            case 0 -> DifficultyLevel.EASY;
            case 2 -> DifficultyLevel.HARD;
            default -> DifficultyLevel.MEDIUM;
        };
        showGame(new GamePanel(difficulty), "Java Racing Game - " + difficulty.name(), 400, 600);
    }

    private void chooseSeaBattleMode() {
        List<String> options = List.of("Сетевая игра (Client-Server LAN)",
                "Локальная игра (Hot-seat на 1 ПК)");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(options.get(0), options);
        if (primaryStage != null) {
            dialog.initOwner(primaryStage);
        }
        dialog.setTitle("Морской бой");
        dialog.setHeaderText("Выберите режим игры:");
        dialog.showAndWait().ifPresent(choice -> {
            if (options.get(0).equals(choice)) {
                NetworkGuiView view = new NetworkGuiView();
                view.show();
            } else {
                new JavaFxGuiView(new GameController("Игрок 1", "Игрок 2")).start();
            }
        });
    }

    private void launchPacman() {
        PacmanGamePanel panel = new PacmanGamePanel();
        showGame(panel, "Пакман (Pac-Man)", panel.getCanvasWidth(), panel.getCanvasHeight());
    }

    private void showGame(javafx.scene.Parent content, String title, double width, double height) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(content, width, height));
        stage.setResizable(false);
        if (primaryStage != null) {
            stage.initOwner(primaryStage);
        }
        stage.setOnCloseRequest(event -> {
            if (content instanceof GamePanel racing) {
                racing.stopGame();
            } else if (content instanceof com.snake.ui.GamePanel snake) {
                snake.stopGame();
            } else if (content instanceof PacmanGamePanel pacman) {
                pacman.stopGame();
            }
        });
        stage.show();
        content.requestFocus();
    }

    private static void runConsoleMenu() {
        Scanner scanner = new Scanner(System.in);
        Main launcher = new Main();
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
                case "1" -> runCliGuiAction(launcher::launchRacing);
                case "2" -> new ConsoleView(new GameController("Игрок 1", "Игрок 2")).start();
                case "3" -> runCliGuiAction(launcher::chooseSeaBattleMode);
                case "4" -> runCliGuiAction(() -> launcher.showGame(
                        new com.snake.ui.GamePanel(), "Змейка", 500, 525));
                case "5" -> runCliGuiAction(launcher::launchPacman);
                case "6" -> {
                    if (cliToolkitStarted) {
                        Platform.runLater(Platform::exit);
                    }
                    System.out.println("До свидания!");
                    return;
                }
                default -> System.out.println("Неверный ввод, попробуйте снова.");
            }
        }
    }

    private static synchronized void runCliGuiAction(Runnable action) {
        if (cliToolkitStarted) {
            Platform.runLater(action);
            return;
        }
        cliToolkitStarted = true;
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                action.run();
            });
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(action);
        }
    }
}

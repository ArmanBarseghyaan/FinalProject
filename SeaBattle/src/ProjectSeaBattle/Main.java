package seabattle;

import com.seabattle.controller.GameController;
import com.seabattle.view.GameView;
import com.seabattle.view.console.ConsoleView;
import com.seabattle.view.gui.SwingGuiView;

import java.awt.GraphicsEnvironment;
import javax.swing.*;

/**
 * Главный класс приложения (Точка входа).
 * Позволяет запускать игру в графическом (Swing GUI) или консольном (CLI) режиме.
 */
public class Main {
    public static void main(String[] args) {
        boolean useCli = false;

        if (args.length > 0 && args[0].equalsIgnoreCase("--cli")) {
            useCli = true;
        } else if (args.length == 0 && GraphicsEnvironment.isHeadless()) {
            useCli = true;
        }

        GameController controller = new GameController("Игрок 1", "Игрок 2");
        GameView view;

        if (useCli) {
            view = new ConsoleView(controller);
        } else {
            // По умолчанию запускаем Java Swing GUI
            view = new SwingGuiView(controller);
        }

        view.start();
    }
}

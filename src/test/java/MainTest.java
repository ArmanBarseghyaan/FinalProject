import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Тесты главного запуска приложения")
class MainTest {

    @Test
    @DisplayName("Main запускает консольное меню, обрабатывает ввод и корректно выходит")
    void mainRunsAndExitsConsoleMenu() {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String input = "invalid\n6\n";

        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

            Main.main(new String[]{"--cli"});
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }

        String console = output.toString(StandardCharsets.UTF_8);
        assertTrue(console.contains("FINAL PROJECT - ИГРОВОЙ ЦЕНТР"));
        assertTrue(console.contains("Неверный ввод"));
        assertTrue(console.contains("До свидания!"));
    }
}

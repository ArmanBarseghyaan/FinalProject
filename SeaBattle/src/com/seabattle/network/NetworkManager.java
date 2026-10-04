package com.seabattle.network;

import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Сетевой менеджер для управления Java Sockets (ServerSocket / Socket).
 * Выполняет все сетевые операции чтение/записи в фоновых потоках,
 * а вызовы уведомлений интерфейса передает через SwingUtilities.invokeLater().
 */
public class NetworkManager {

    public interface NetworkListener {
        void onConnected(boolean isHost, String remoteAddress);
        void onConnectionFailed(String errorMessage);
        void onMessageReceived(String message);
        void onDisconnected();
    }

    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    private Thread acceptThread;
    private Thread listenThread;
    private volatile boolean isRunning = false;
    private boolean isHost = false;

    private final NetworkListener listener;

    public NetworkManager(NetworkListener listener) {
        this.listener = listener;
    }

    public boolean isHost() {
        return isHost;
    }

    public static String getLocalIpAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    /**
     * Запуск сервера на указанном порту в фоновом потоке.
     */
    public void startServer(int port) {
        this.isHost = true;
        this.isRunning = true;

        acceptThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Сервер запущен на порту " + port + ". Ожидание подключения...");
                socket = serverSocket.accept();
                System.out.println("Игрок подключился: " + socket.getRemoteSocketAddress());
                initStreamsAndStartListening();
            } catch (IOException e) {
                if (isRunning) {
                    notifyConnectionFailed("Ошибка запуска сервера: " + e.getMessage());
                }
            }
        }, "Battleship-Server-Accept-Thread");
        acceptThread.start();
    }

    /**
     * Подключение клиента к указанному хосту в фоновом потоке.
     */
    public void connectToServer(String host, int port) {
        this.isHost = false;
        this.isRunning = true;

        new Thread(() -> {
            try {
                System.out.println("Подключение к серверу " + host + ":" + port + "...");
                socket = new Socket(host, port);
                System.out.println("Успешно подключено к серверу!");
                initStreamsAndStartListening();
            } catch (IOException e) {
                if (isRunning) {
                    notifyConnectionFailed("Не удалось подключиться к " + host + ":" + port + "\n" + e.getMessage());
                }
            }
        }, "Battleship-Client-Connect-Thread").start();
    }

    private void initStreamsAndStartListening() throws IOException {
        writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        String remoteAddress = socket.getRemoteSocketAddress().toString();
        SwingUtilities.invokeLater(() -> {
            if (listener != null) {
                listener.onConnected(isHost, remoteAddress);
            }
        });

        listenThread = new Thread(() -> {
            try {
                String line;
                while (isRunning && (line = reader.readLine()) != null) {
                    final String msg = line;
                    SwingUtilities.invokeLater(() -> {
                        if (listener != null) {
                            listener.onMessageReceived(msg);
                        }
                    });
                }
            } catch (IOException e) {
                if (isRunning) {
                    System.out.println("Сетевой поток завершен: " + e.getMessage());
                }
            } finally {
                closeConnection();
                SwingUtilities.invokeLater(() -> {
                    if (listener != null) {
                        listener.onDisconnected();
                    }
                });
            }
        }, "Battleship-Network-Listen-Thread");
        listenThread.start();
    }

    /**
     * Отправка сообщения по сети в фоновом потоке.
     */
    public void sendMessage(String message) {
        if (writer != null && isRunning) {
            new Thread(() -> {
                synchronized (writer) {
                    writer.println(message);
                }
            }, "Battleship-Network-Send-Thread").start();
        }
    }

    private void notifyConnectionFailed(String errorMsg) {
        closeConnection();
        SwingUtilities.invokeLater(() -> {
            if (listener != null) {
                listener.onConnectionFailed(errorMsg);
            }
        });
    }

    public synchronized void closeConnection() {
        isRunning = false;
        try {
            if (writer != null) {
                writer.close();
                writer = null;
            }
            if (reader != null) {
                reader.close();
                reader = null;
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии сокетов: " + e.getMessage());
        }
    }
}

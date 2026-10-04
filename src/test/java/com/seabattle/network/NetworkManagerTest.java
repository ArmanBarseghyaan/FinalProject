package com.seabattle.network;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты сетевого менеджера Java Sockets (NetworkManager)")
class NetworkManagerTest {

    @Test
    @DisplayName("Получение локального IP адреса машины")
    void testGetLocalIpAddress() {
        String ip = NetworkManager.getLocalIpAddress();
        assertNotNull(ip);
        assertFalse(ip.trim().isEmpty());
    }

    @Test
    @DisplayName("Инициализация сетевого менеджера и закрытие неактивного соединения")
    void testNetworkManagerLifecycle() {
        NetworkManager manager = new NetworkManager(new NetworkManager.NetworkListener() {
            @Override public void onConnected(boolean isHost, String remoteAddress) {}
            @Override public void onConnectionFailed(String errorMessage) {}
            @Override public void onMessageReceived(String message) {}
            @Override public void onDisconnected() {}
        });

        assertFalse(manager.isHost());
        assertDoesNotThrow(manager::closeConnection);
    }
}

package com.example.lab_09;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketThread extends Thread {

    public interface MessageListener {
        void onMessage(String msg);
        void onStatus(String status);
    }

    private static final String TAG = "ServerSocketThread";
    private final int port;
    private volatile boolean running = true;
    private ServerSocket serverSocket;
    private final MessageListener listener;

    public ServerSocketThread(int port, MessageListener listener) {
        this.port = port;
        this.listener = listener;
    }

    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
        interrupt();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            Log.i(TAG, "Start server, port " + port);
            if (listener != null) listener.onStatus("Server pornit pe port " + port);

            while (running) {
                Log.i(TAG, "Se așteaptă conectarea clientului..");
                if (listener != null) listener.onStatus("Se așteaptă conectarea clientului..");

                Socket socket = serverSocket.accept();
                Log.i(TAG, "Conexiune client: " + socket);
                if (listener != null) listener.onStatus("Client conectat: " + socket);

                startReader(socket);
            }
        } catch (IOException e) {
            Log.e(TAG, "Eroare server: " + e.getMessage(), e);
            if (listener != null) listener.onStatus("Eroare server: " + e.getMessage());
        }
    }

    private void startReader(final Socket socket) {
        new Thread(() -> {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "utf-8"))) {

                String line;
                while ((line = in.readLine()) != null && running) {
                    Log.i(TAG, "Mesaj receptionat: " + line);
                    if (listener != null) listener.onMessage(line);
                }
            } catch (IOException e) {
                Log.e(TAG, "Eroare reader: " + e.getMessage(), e);
                if (listener != null) listener.onStatus("Reader oprit: " + e.getMessage());
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
            }
        }).start();
    }
}

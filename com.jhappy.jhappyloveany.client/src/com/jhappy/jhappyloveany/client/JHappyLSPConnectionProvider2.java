package com.jhappy.jhappyloveany.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.eclipse.lsp4e.server.StreamConnectionProvider;

public class JHappyLSPConnectionProvider2 implements StreamConnectionProvider {

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    @Override
    public void start() throws IOException {
        // 別で起動しているLSPサーバーのポート（例: 5007）に接続しにいく
        try {
            socket = new Socket("localhost", 5007);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            System.err.println("LSP: Successfully connected to Server on port 5007!");
        } catch (IOException e) {
            System.err.println("LSP: Failed to connect to server. Is it running?");
            throw e;
        }
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public InputStream getErrorStream() {
        // Socket通信では標準エラー出力の分離はしないため null でOK
        return null;
    }

    @Override
    public void stop() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                System.err.println("LSP: Connection closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
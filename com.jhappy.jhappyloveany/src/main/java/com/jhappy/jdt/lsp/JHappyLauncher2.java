package com.jhappy.jdt.lsp;

import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

public class JHappyLauncher2 {
    
    public static void main(String[] args) {
        int port = 5007; // プラグイン側と合わせる

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.err.println("LSP Server (Debug Mode) started on port " + port);
            System.err.println("Waiting for Eclipse plugin to connect...");

            while (true) {
                // プラグインからの接続を待機
                Socket socket = serverSocket.accept();
                System.err.println("Client connected! Starting LSP session...");

                try {
                    JHappyLanguageServer server = new JHappyLanguageServer();

                    // ★ System.in/out ではなく、Socketのストリームを使う！
                    Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(
                            server, 
                            socket.getInputStream(), 
                            socket.getOutputStream()
                    );

                    server.connect(launcher.getRemoteProxy());

                    // 通信待機（クライアントが切断するまでブロックされる）
                    launcher.startListening().get();

                } catch (Exception e) {
                    System.err.println("Session ended or crashed: " + e.getMessage());
                } finally {
                    socket.close();
                    System.err.println("Client disconnected. Waiting for next connection...");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
package com.jhappy.jdt.lsp;

import java.util.concurrent.Future;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * 
 */
public class JHappyLauncher {public static void main(String[] args) {
    try {
        // サーバーインスタンスの生成
        JHappyLanguageServer server = new JHappyLanguageServer();

        // デバッグログが必要な場合は System.err に出す（System.out は通信用なので厳禁）
        java.io.PrintWriter traceWriter = new java.io.PrintWriter(System.err);

        // 標準入出力を使用してエディタと接続
        Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(
                server, System.in, System.out, false, traceWriter);

        // エディタ側のプロキシをサーバーに渡す
        server.connect(launcher.getRemoteProxy());

        Future<Void> listening = launcher.startListening();

        try {
            // クライアントが切断するまでここでブロック（待機）
            listening.get(); 
        } finally {
            // ★ 重要：テスト終了後にプロセスが残らないよう、明示的に終了を指示
            System.err.println("LSP Server is shutting down...");
            System.exit(0);
        }
        

    } catch (Exception e) {
        e.printStackTrace(System.err);
        System.exit(1);
    }
}

	//    public static void main(String[] args) throws IOException {
	//        // サーバーインスタンスの生成
	//        JHappyLanguageServer server = new JHappyLanguageServer();
	//
	//        int port = 5007; 
	//        try (ServerSocket serverSocket = new ServerSocket(port)) {
	//            System.err.println("LSP Server started on port " + port);
	//            System.err.println("Eclipseプラグイン側から接続されるのを待っています...");
	//
	//            while (true) {
	//                // 接続を待機
	//                Socket socket = serverSocket.accept();
	//                System.err.println("Eclipseと接続しました！");
	//
	//                // ソケットの入出力ストリームを使ってランチャーを生成
	//                Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(
	//                    server, 
	//                    socket.getInputStream(), 
	//                    socket.getOutputStream()
	//                );
	//
	//                // 【重要】クライアントプロキシをサーバーに渡し、リスニングを開始
	//                server.connect(launcher.getRemoteProxy());
	//                
	//                // startListening() は別スレッドで実行される（戻り値はFuture）ため、
	//                // これによりサーバーが実際に通信を開始します
	//                launcher.startListening();
	//            }
	//        } catch (Exception e) {
	//            e.printStackTrace();
	//        }
	//    }

	//    public static void main(String[] args) throws IOException {
	//    	int port = 5007;
	//        
	//        // ServerSocketの設定を微調整
	//        ServerSocket serverSocket = new ServerSocket();
	//        serverSocket.setReuseAddress(true); // ポートの再利用を許可
	//        serverSocket.bind(new InetSocketAddress(port));
	//
	//        System.err.println("LSP Server debug mode started on port " + port);
	//
	//        // シャットダウン時の処理を登録（Ctrl+Cなどで終了したときに呼ばれる）
	//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
	//            System.err.println("Shutting down LSP server...");
	//            try {
	//                if (!serverSocket.isClosed()) serverSocket.close();
	//            } catch (IOException e) { e.printStackTrace(); }
	//        }));
	//        
	//        try (serverSocket) {
	//            System.err.println("LSP Server debug mode started on port " + port);
	//
	//            while (true) {
	//                // エディタ（プラグイン）からの接続を待機
	//                try (Socket socket = serverSocket.accept()) {
	//                    System.err.println("Client connected. Starting session...");
	//
	//                    // 接続ごとにサーバーインスタンスを作成すれば、エディタを閉じても
	//                    // 次の接続で新しく初期化される
	//                    JHappyLanguageServer server = new JHappyLanguageServer();
	//
	//                    Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(
	//                        server, 
	//                        socket.getInputStream(), 
	//                        socket.getOutputStream()
	//                    );
	//
	//                    server.connect(launcher.getRemoteProxy());
	//                    
	//                    // 完了するまで待機（これがないとすぐ次の accept に行ってしまう）
	//                    launcher.startListening().get(); 
	//                    System.err.println("Client disconnected. Waiting for next connection...");
	//                } catch (Exception e) {
	//                    System.err.println("Session error: " + e.getMessage());
	//                }
	//            }
	//        }
	//    }
}
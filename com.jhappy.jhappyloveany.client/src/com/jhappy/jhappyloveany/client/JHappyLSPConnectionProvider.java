package com.jhappy.jhappyloveany.client;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class JHappyLSPConnectionProvider extends ProcessStreamConnectionProvider {

    public JHappyLSPConnectionProvider() {
    	List<String> commands = new ArrayList<>();

        // Eclipse自体を実行しているJavaのパスを動的に取得する
        // これにより、OSへのインストール状況に関わらず、Eclipseが動いているJavaで起動できます
        String javaHome = System.getProperty("java.home");
        String javaExecutable = javaHome + File.separator + "bin" + File.separator + "java";

        commands.add(javaExecutable);
        
        // ★ システムプロパティ "jhappy.debug" が "true" の場合のみデバッグ有効
        if (Boolean.getBoolean("jhappy.debug")) {
            // ポート8000で待機。suspend=yにすると接続されるまで停止、nならそのまま起動
            commands.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000");
            System.out.println("★ LSP Server Debug Mode ENABLED on port 8000 ★");
        }
        
        commands.add("-jar");
        // JARのパス
        String jarPath = getBundleFilePath("lib/com.jhappy.jhappyloveany-version-jar-with-dependencies.jar");
        commands.add(jarPath);

        setCommands(commands);
    }
    
    

    private String getBundleFilePath(String relativePath) {
        Bundle bundle = FrameworkUtil.getBundle(this.getClass());
        try {
            
        	//
            java.net.URL bundleUrl = bundle.getEntry(relativePath);
            if (bundleUrl == null) {
                System.err.println("Resource not found: " + relativePath);
                return null;
            }
            // bundleentry:// 形式から file:// 形式に変換
            java.net.URL fileUrl = FileLocator.toFileURL(bundleUrl);
            return new File(fileUrl.toURI()).getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } catch (URISyntaxException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			 return "";
		}
    }
	
}
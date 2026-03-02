package com.jhappy.jhappyloveany.client;

import java.io.File;
import java.io.IOException;
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
        System.out.println(javaExecutable);

        commands.add(javaExecutable);
        commands.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000");
        commands.add("-jar");
        // JARのパス
        String jarPath = getBundleFilePath("lib/com.jhappy.jhappyloveany-0.0.1-SNAPSHOT-jar-with-dependencies.jar");
        commands.add(jarPath);

        setCommands(commands);
    }
    
    

    private String getBundleFilePath(String relativePath) {
        Bundle bundle = FrameworkUtil.getBundle(this.getClass());
        try {
            // relativePath に対する URL を取得
            java.net.URL bundleUrl = bundle.getEntry(relativePath);
            if (bundleUrl == null) {
                System.err.println("Resource not found: " + relativePath);
                return null;
            }
            // bundleentry:// 形式から file:// 形式に変換
            java.net.URL fileUrl = FileLocator.toFileURL(bundleUrl);
            return new File(fileUrl.getPath()).getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
	
}
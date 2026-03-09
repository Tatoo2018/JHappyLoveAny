package  com.jhappy.jhappyloveany.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * テスト用プラグイン内のリソースをワークスペースのプロジェクトにコピーするためのユーティリティ
 */
public class TestResourceUtility {

    /**
     * 指定したプラグイン内のパスから、ターゲットとなるEclipseリソース（Project等）へ再帰的にコピーします
     * @param bundleId テストリソースが含まれるプラグインID
     * @param sourcePath プラグイン内のパス（例: "test-resources/my-app"）
     * @param targetContainer コピー先のプロジェクトやフォルダ
     */
    public static void copyBundleResources(String bundleId, String sourcePath, IContainer targetContainer) 
            throws IOException, CoreException {
        
        Bundle bundle = Platform.getBundle(bundleId);
        Enumeration<String> entries = bundle.getEntryPaths(sourcePath);

        if (entries == null) return;

        while (entries.hasMoreElements()) {
            String entryPath = entries.nextElement();
            String name = new Path(entryPath).lastSegment();

            if (entryPath.endsWith("/")) {
                // フォルダの場合
                IFolder folder = targetContainer.getFolder(new Path(name));
                if (!folder.exists()) {
                    folder.create(true, true, null);
                }
                copyBundleResources(bundleId, entryPath, folder);
            } else {
                // ファイルの場合
                IFile file = targetContainer.getFile(new Path(name));
                URL url = bundle.getEntry(entryPath);
                try (InputStream is = url.openStream()) {
                    if (file.exists()) {
                        file.setContents(is, true, true, null);
                    } else {
                        file.create(is, true, null);
                    }
                }
            }
        }
    }
}
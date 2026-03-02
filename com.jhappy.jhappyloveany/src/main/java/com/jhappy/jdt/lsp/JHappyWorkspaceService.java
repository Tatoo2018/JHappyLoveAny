package com.jhappy.jdt.lsp;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.services.WorkspaceService;

import com.jhappy.jdt.lsp.properties.PropertiesScanner;
import com.jhappy.jdt.lsp.setting.QueryConfig;
import com.jhappy.jdt.lsp.setting.QuerySetting;
import com.jhappy.jdt.lsp.xml.XmlScanner;
import com.jhappy.jdt.util.EclipseUtil;

/**
 * 
 */
public class JHappyWorkspaceService implements WorkspaceService {

	/**
	 * 
	 */
	private final JHappyLanguageServer server;

	public JHappyWorkspaceService(JHappyLanguageServer server) {
		this.server = server;
	}

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		// 設定変更時の処理（必要に応じて実装）
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {

		for (FileEvent event : params.getChanges()) {

			String uriStr = event.getUri();
			String lowerUri = uriStr.toLowerCase();
			URI fileUri = URI.create(uriStr);
			Path filePath = Paths.get(fileUri);
			Path projectUri = server.findProjectUriFor(filePath);

			//設定ファイルの更新の場合は、フルスキャンを実行
			if (lowerUri.endsWith("/+" + QuerySetting.JHAPPYQUERIES_XML)) {

				//設定ファイルを再読み込み
				server.loadQueriesConfig(projectUri.toAbsolutePath().toString());

				if (projectUri != null) {
					CompletableFuture.runAsync(() -> {
						server.scanAllFiles();
					});
				}
				
			} else {
				if (isExcluded(filePath)) {
					continue;
				}

				String absolutePath = filePath.toAbsolutePath().toString();

				if (event.getType() == FileChangeType.Deleted) {

					server.getFilePropertyCache().remove(absolutePath);
					
				} else {
					
					server.getFilePropertyCache().remove(absolutePath);

					QuerySetting querySetting = server.getProjectQueryConfigs()
							.get(projectUri.toAbsolutePath().toString());
					
					if (querySetting != null) {
						
						List<QueryConfig> configs = querySetting.configs;

						//
						if (lowerUri.endsWith(".xml")) {
							
							List<DataEntry> entries = XmlScanner.loadXmlFile(filePath, projectUri, configs);
							if (entries != null && !entries.isEmpty()) {
								server.getFilePropertyCache().put(absolutePath, entries);
							}
						
						} else if (lowerUri.endsWith(".properties")) {
					
							List<DataEntry> entries = PropertiesScanner.loadPropertyFile(filePath, projectUri,
									configs);
							if (entries != null && !entries.isEmpty()) {
								server.getFilePropertyCache().put(absolutePath, entries);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * ファイルがプロジェクトの出力フォルダ（bin, target等）内にあるか判定します。
	 */
	private boolean isExcluded(Path filePath) {

		// サーバーが保持している全てのワークスペースフォルダに対してチェック
		for (WorkspaceFolder folder : server.getWorkspaceFolderList()) {

			Path projectRoot = null;
			try {
				projectRoot = Paths.get(new URI(folder.getUri()));

			} catch (URISyntaxException e) {
				server.logError("failed WorkspaceFolder : " + folder.getUri(), e);
				return false;
			}

			if (filePath.startsWith(projectRoot)) {
				// そのプロジェクトの除外リストを取得（キャッシュ化しておくとより高速）
				Set<String> excludes = EclipseUtil.getOutputFolders(folder.getUri());

				Path relativePath = projectRoot.relativize(filePath);
				String relStr = relativePath.toString();

				for (String ex : excludes) {
					if (relStr.equals(ex) || relStr.startsWith(ex + "/")) {
						return true;
					}
				}
			}

		}
		return false;
	}
}
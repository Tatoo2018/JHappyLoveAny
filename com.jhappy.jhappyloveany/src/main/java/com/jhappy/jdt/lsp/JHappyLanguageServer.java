package com.jhappy.jdt.lsp;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.DidChangeWatchedFilesRegistrationOptions;
import org.eclipse.lsp4j.DocumentFilter;
import org.eclipse.lsp4j.FileSystemWatcher;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.SaveOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentRegistrationOptions;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextDocumentSyncOptions;
import org.eclipse.lsp4j.WatchKind;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceServerCapabilities;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import com.jhappy.jdt.lsp.properties.PropertiesScanner;
import com.jhappy.jdt.lsp.setting.QueryConfig;
import com.jhappy.jdt.lsp.setting.QuerySetting;
import com.jhappy.jdt.lsp.xml.XmlScanner;
import com.jhappy.jdt.util.EclipseUtil;

/**
 * 
 */
public class JHappyLanguageServer implements LanguageServer, LanguageClientAware {

	private LanguageClient client;

	private final JHappyTextDocumentService textDocumentService = new JHappyTextDocumentService(this);

	private final WorkspaceService workspaceService = new JHappyWorkspaceService(this);

	private final Map<String, QuerySetting> projectQueryConfigs = new java.util.concurrent.ConcurrentHashMap<>();

	private List<WorkspaceFolder> workspaceFolderList = new ArrayList<WorkspaceFolder>();

	private final Map<String, List<DataEntry>> filePropertyCache = new ConcurrentHashMap<>();

	public Map<String, List<DataEntry>> getFilePropertyCache() {
		return filePropertyCache;
	}

	public Map<String, QuerySetting> getProjectQueryConfigs() {
		return projectQueryConfigs;
	}

	public List<WorkspaceFolder> getWorkspaceFolderList() {
		return workspaceFolderList;
	}

	public void setWorkspaceFolderList(List<WorkspaceFolder> workspaceFolderList) {
		this.workspaceFolderList = workspaceFolderList;
	}

	/**
	 *
	 */
	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		
		log("initialize start");

		//
		ServerCapabilities capabilities = new ServerCapabilities();

		// 
		WorkspaceServerCapabilities workspaceCaps = new WorkspaceServerCapabilities();
		capabilities.setWorkspace(workspaceCaps);

		workspaceFolderList = params.getWorkspaceFolders();

		//IDE側でファイルが変更されたときLSP側に通知するように設定
		// Set up text document synchronization
		TextDocumentSyncOptions syncOptions = new TextDocumentSyncOptions();

		//receive all content of file when it is changed
		syncOptions.setChange(TextDocumentSyncKind.Full);
		syncOptions.setOpenClose(true);

		//receive event when it is saved
		SaveOptions saveOptions = new SaveOptions(true);
		syncOptions.setSave(saveOptions);

		//
		capabilities.setTextDocumentSync(Either.forRight(syncOptions));
		capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);

		// Enable core LSP features
		capabilities.setDefinitionProvider(true);// Jump to definition
		capabilities.setHoverProvider(true);// Show hover information

		//Enable code completion
		CompletionOptions completionOptions = new CompletionOptions();
		completionOptions.setTriggerCharacters(java.util.List.of("\"", "."));
		capabilities.setCompletionProvider(completionOptions);

		//
		InitializeResult result = new InitializeResult(capabilities);

		return CompletableFuture.completedFuture(result);
	}

	/**
	 *
	 */
	@Override
	public void initialized(InitializedParams params) {
		
		log("initialized start");

		projectQueryConfigs.clear();

		// Scan all projects (
		if (workspaceFolderList != null && !workspaceFolderList.isEmpty()) {
			CompletableFuture.runAsync(() -> {
				for (WorkspaceFolder folder : workspaceFolderList) {
					projectQueryConfigs.putAll(QuerySetting.loadQueriesConfig(folder.getUri()));
				}
				scanAllFiles();
			});
		}

		try {

			// Issue #547 対策: LSP4E側の準備が整うまで2秒待機
			Thread.sleep(2000);

			if (client != null) {

				// Register file watchers to monitor changes in resource files
				List<FileSystemWatcher> watchers = new ArrayList<>();

				//type of events that you want to observe
				// The value '7' is a bitmask for WatchKind: Create (1) | Change (2) | Delete (4) = 7
				int typeofevents = WatchKind.Create | WatchKind.Change | WatchKind.Delete;
				watchers.add(new FileSystemWatcher(Either.forLeft("**/*.properties"), typeofevents));
				watchers.add(new FileSystemWatcher(Either.forLeft("**/*.xml"), typeofevents));

				DidChangeWatchedFilesRegistrationOptions watchOptions = new DidChangeWatchedFilesRegistrationOptions(
						watchers);

				List<Registration> registrations = new ArrayList<>();
				registrations.add(new Registration(
						"jhappyloveany-resource-watcher",
						"workspace/didChangeWatchedFiles",
						watchOptions));

				//Setting for hover
				TextDocumentRegistrationOptions hoverOptions = new TextDocumentRegistrationOptions();
				hoverOptions.setDocumentSelector(List.of(
						new DocumentFilter("java", null, null),
						new DocumentFilter("properties", null, null)));
				registrations.add(new Registration(
						"jhappyloveany-hover",
						"textDocument/hover",
						hoverOptions));

				RegistrationParams registrationParams = new RegistrationParams(registrations);

				client.registerCapability(registrationParams).thenAccept(v -> {

				}).exceptionally(ex -> {
					System.err.println("LSP: Failed to register dynamic capabilities.");
					ex.printStackTrace(System.err);
					return null;
				});
			}

		} catch (InterruptedException e) {
			e.printStackTrace(System.err);
			Thread.currentThread().interrupt();
		}

	}

	/**
	 * プロジェクト内の全プロパティファイルを走査しキャッシュ
	 */
	public void scanAllFiles() {

		try {

			for (WorkspaceFolder workspaceFolder : workspaceFolderList) {

				Path rootPath = Paths.get(new URI(workspaceFolder.getUri()));

				String rootPathStr = rootPath.toAbsolutePath().toString();
				filePropertyCache.keySet().removeIf(key -> key.startsWith(rootPathStr));

				Set<String> excludes = EclipseUtil.getOutputFolders(rootPath.toAbsolutePath().toString());

				String uriString = workspaceFolder.getUri();

				// URIを一度 Path に変換して絶対パス文字列を取得し、それをキーにする
				String absolutePathKey = Paths.get(new URI(uriString)).toAbsolutePath().toString();

				QuerySetting querySetting = projectQueryConfigs.get(absolutePathKey);

				if (querySetting == null) {
					continue;
				}

				for (QueryConfig config : querySetting.configs) {

					String searchRootPath = config.data.searchRootPath;

					if (!StringUtils.isBlank(searchRootPath)) {

						Path searchRootPathPath = Paths.get(searchRootPath);

						QuerySetting oneQuerySetting = new QuerySetting();
						oneQuerySetting.completionMatch = querySetting.completionMatch;
						oneQuerySetting.configs = new ArrayList<>(Arrays.asList(config));

						scan(searchRootPathPath, excludes, oneQuerySetting);
					}

				}

				if (Files.exists(rootPath)) {
					scan(rootPath, excludes, projectQueryConfigs.get(rootPath.toAbsolutePath().toString()));
				
				}

			}
		} catch (Exception e) {
	
			logError("error scanAllFiles " , e);
			
		}
	}

	/**
	 * @param rootPath
	 * @param excludes
	 * @param querySetting
	 * @throws IOException
	 */
	private void scan(Path rootPath, Set<String> excludes, QuerySetting querySetting) throws IOException {

		if (Files.exists(rootPath)) {

			if (querySetting != null) {

				List<QueryConfig> configs = querySetting.configs;

				try (var stream = Files.walk(rootPath)) {
					stream
							.filter(path -> {

								Path relativePath = rootPath.relativize(path);
								String relStr = relativePath.toString();
								for (String excludePath : excludes) {
									if (relStr.equals(excludePath) || relStr.startsWith(excludePath + "/")) {
										return false;
									}
								}
								String name = path.toString().toLowerCase();
								return name.endsWith(".properties") || name.endsWith(".xml");

							})
							.forEach(path -> {

								String absolutePath = path.toAbsolutePath().toString();
								
								if (path.toString().toLowerCase().endsWith(".xml")) {
									List<DataEntry> entries = XmlScanner.loadXmlFile(path, rootPath, configs);
									if (entries != null && !entries.isEmpty()) {
										filePropertyCache.put(absolutePath, entries);
									}
								} else {
									List<DataEntry> entries = PropertiesScanner.loadPropertyFile(path, rootPath,
											configs);
									if (entries != null && !entries.isEmpty()) {
										filePropertyCache.put(absolutePath, entries);
									}
								}
							});
				}
			}
		}

	}

	/**
	 * 指定されたファイルの絶対パスから、所属するプロジェクトのルートURIを特定します。
	 * * @param filePath 判定対象ファイルのパス
	 * @return 所属するプロジェクトのURI（見つからない場合は null）
	 */
	public Path findProjectUriFor(Path filePath) {

		//
		String targetPath = filePath.toAbsolutePath().toString();

		for (WorkspaceFolder folder : workspaceFolderList) {
			try {
				
				Path projectPath = Paths.get(new URI(folder.getUri())).toAbsolutePath();
				String projectPathStr = projectPath.toString();

				if (targetPath.startsWith(projectPathStr)) {
					return projectPath;
				}
			} catch (Exception e) {
			
				continue;
			}
		}

		return null;
	}

	/**
	 * @return
	 */
	public List<DataEntry> getAllProperties() {
		return filePropertyCache.values().stream().flatMap(List::stream).collect(java.util.stream.Collectors.toList());
	}

	/**
	 * @param targetUri
	 */
	public void loadQueriesConfig(String targetUri) {
		projectQueryConfigs.putAll(QuerySetting.loadQueriesConfig(targetUri));

	}

	public void logError(String message, Throwable t) {
		if (t != null) t.printStackTrace(System.err);
		if (client != null) {
			client.logMessage(new MessageParams(MessageType.Error, message + (t != null ? ": " + t.getMessage() : "")));
		}
	}

	public void log(String message) {
		if (client != null)
			client.logMessage(new MessageParams(MessageType.Log, message));
	}

	public void logInfo(String message) {
		if (client != null)
			client.logMessage(new MessageParams(MessageType.Info, message));
	}

	public void logWarning(String message) {
		if (client != null)
			client.logMessage(new MessageParams(MessageType.Warning, message));
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		return CompletableFuture.completedFuture(null);
	}

	/**
	 *
	 */
	@Override
	public void exit() {
		System.exit(0);
	}

	/**
	 *
	 */
	@Override
	public TextDocumentService getTextDocumentService() {
		return textDocumentService;
	}

	/**
	 *
	 */
	@Override
	public WorkspaceService getWorkspaceService() {
		return workspaceService;
	}

	/**
	 *
	 */
	@Override
	public void connect(LanguageClient client) {
		this.client = client;
	}
}
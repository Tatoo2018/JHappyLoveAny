package com.jhappy.jdt.lsp;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionItemLabelDetails;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.jhappy.jdt.lsp.setting.QuerySetting;
import com.jhappy.jdt.util.JDTUtil;
import com.jhappy.jdt.util.StringUtils;

/**
 * 
 */
public class JHappyTextDocumentService implements TextDocumentService {

	public JHappyTextDocumentService(JHappyLanguageServer server) {
		this.server = server;
	}

	private final JHappyLanguageServer server;

	private final Map<String, String> docs = new ConcurrentHashMap<>();


	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {

		String uri = params.getTextDocument().getUri();

		Path filePath;

		try {
			filePath = java.nio.file.Paths.get(new java.net.URI(uri));
		} catch (URISyntaxException e) {
			server.logError("failed load uri : " + uri , e);
			return emptyResult();
		}
		
		Path projectRootUri = server.findProjectUriFor(filePath);

		String content = docs.get(uri);

		if (content == null) {
			return emptyResult();
		}
		
		//
		CompilationUnit cu = createCompilationUnit(content);

		//
		int offset = cu.getPosition(params.getPosition().getLine() + 1, params.getPosition().getCharacter());

		ASTNode node = NodeFinder.perform(cu, offset, 0);

		StringLiteral literalStringNode = JDTUtil.findStringLiteral(node);
		
		if (literalStringNode != null) {

			Range rangeToReplace = createRange(cu, literalStringNode.getStartPosition() + 1,
					literalStringNode.getStartPosition() + literalStringNode.getLength() - 1);

			//補完対象の入力中文字列
			
			String prefix = getEditingString(content, offset, literalStringNode);
			String prefixOfLower = prefix.toLowerCase();

			List<CompletionItem> items = new ArrayList<>();
			
			int count = 0; 
	        int limit = 200;
	    	QuerySetting setting = server.getProjectQueryConfigs().get(projectRootUri.toAbsolutePath().toString());

			for (DataEntry entry : server.getAllProperties() ) {
				
				// ★ 上限に達したらループを抜ける
	            if (count >= limit) {
	                break;
	            }

				String completionKey = entry.key;
				String completionValue = entry.value;
				String completionFilePath = entry.filePath;
				String completionFileName = entry.fileName;
				String completionType = entry.type;
				int completionLineNumber = entry.lineNumber;
				CompletionItemKind completionItemType = getCompletionItemKind(completionType);
				

				boolean keyMatch = false;
				boolean valueMatch = false;
				if (setting != null && "contains".equals(setting.completionMatch)) {
					keyMatch = completionKey.toLowerCase().contains(prefixOfLower);
					valueMatch = completionValue.toLowerCase().contains(prefixOfLower);
				} else {
					keyMatch = completionKey.toLowerCase().startsWith(prefixOfLower);
					valueMatch = completionValue.toLowerCase().startsWith(prefixOfLower);
				}

				String completionLabel = "";
				if (keyMatch) {
					completionLabel = completionKey.replace("\n", " ").replace("\r", " ");
				} else if (valueMatch) {
					completionLabel = completionValue.replace("\n", " ").replace("\r", " ");
				}

				if (keyMatch || valueMatch) {

					String escapedText = StringUtils.escapeMinimal(completionKey);

					CompletionItem item = new CompletionItem();
					item.setLabel(completionLabel);
					item.setTextEdit(Either.forLeft(new TextEdit(rangeToReplace, escapedText)));

					item.setKind(completionItemType);
					item.setSortText((keyMatch ? "001_" : "002_") + completionKey);

					CompletionItemLabelDetails labelDetails = new CompletionItemLabelDetails();
					labelDetails.setDetail(" - " + completionFileName + " (" + completionType + ")");
					item.setLabelDetails(labelDetails);

					String displayValue = converToMarkdown(completionValue);

					MarkupContent markup = new MarkupContent();
					markup.setKind(MarkupKind.MARKDOWN);
					markup.setValue(createMarkDown(completionKey, completionFilePath, completionLineNumber,
							displayValue));

					item.setDocumentation(markup);

					items.add(item);
				}
			}
			return CompletableFuture.completedFuture(Either.forLeft(items));
		}
		return emptyResult();
	}

	/**
	 * @param type
	 * @return
	 */
	private CompletionItemKind getCompletionItemKind(String type) {
		CompletionItemKind completionType = null;
		if ("xml".equals(type)) {
			completionType = CompletionItemKind.File;
		} else if ("property".equals(type)) {
			completionType = CompletionItemKind.Property;
		}
		return completionType;
	}

	

	@Override
	public CompletableFuture<Hover> hover(HoverParams params) {

		return CompletableFuture.supplyAsync(() -> {

			String uri = params.getTextDocument().getUri();

			String content = docs.get(uri);
			if (content == null) {
				return null;
			}
			

			CompilationUnit cu = createCompilationUnit(content);
			Position pos = params.getPosition();
			
			int offset = cu.getPosition(pos.getLine() + 1, pos.getCharacter());

			String key = JDTUtil.getStringLiteralValueAt(cu, offset);
			
			if (key == null)
				return null;

			// キャッシュから検索
			List<DataEntry> matches = server.getAllProperties().stream()
					.filter(e -> e.key.equals(key))
					.collect(Collectors.toList());

			if (!matches.isEmpty()) {
				
				StringBuilder markdown = new StringBuilder();

				for (DataEntry entry : matches) {
					// 改行コードを <br> に変換
					// Markdownのコードクオート(`)内ではタグが効かないため、
					// 値を強調したい場合は太字(**)などを使います。
					String displayValue = entry.value
							.replace("&", "&amp;")
							.replace("<", "&lt;")
							.replace(">", "&gt;")
							.replace("\\", "\\\\");

					displayValue = displayValue.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;").replace("\n", "<br>") // ★ brに置換
							.replace("\r", "");

					markdown.append("**Key**:  \n").append(entry.key).append("  \n");
					markdown.append("**Value**:  \n").append(displayValue).append("  \n"); // クオートを外してHTMLを有効化
					markdown.append("**File**:  \n").append(entry.filePath).append(" (Line: ")
							.append(entry.lineNumber + 1).append(")");
				}

				MarkupContent markup = new MarkupContent();
				markup.setKind(MarkupKind.MARKDOWN);
				markup.setValue(markdown.toString());

				// ★ 1文字分のRangeを指定することでキャッシュを回避
				Range minimalRange = new Range(pos, new Position(pos.getLine(), pos.getCharacter() + 1));

				return new Hover(markup, minimalRange);
			}
			return null;
		});
	}

	@Override
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
			DefinitionParams params) {

		return CompletableFuture.supplyAsync(() -> {

			String uri = params.getTextDocument().getUri();
			String content = docs.get(uri);
			if (content == null)
				return null;

			//
			CompilationUnit cu = createCompilationUnit(content);
			
			int offset = cu.getPosition(params.getPosition().getLine() + 1, params.getPosition().getCharacter());
			
			String literalString = JDTUtil.getStringLiteralValueAt(cu, offset);

			if (literalString != null) {

				List<Location> locations = server.getAllProperties().stream()
						
						.filter(entry -> entry.key.equals(literalString) || entry.value.equals(literalString))
						
						.map(entry -> {
						
							Range range = new Range(new Position(entry.lineNumber, 0),
									new Position(entry.lineNumber, literalString.length()));

							Path path = Paths.get(entry.filePath);

							String safeUri = path.toUri().toString();
							Location location = new Location(safeUri, range);

							return location;
						})
						.collect(Collectors.toList());

				return Either.forLeft(locations);
			}
			return null;
		});
	}

	/**
	 * @param content
	 * @return
	 */
	private CompilationUnit createCompilationUnit(String content) {
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setSource(content.toCharArray());
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		return cu;
	}

	/**
	 * @param rawText
	 * @return
	 */
	private static String converToMarkdown(String rawText) {

		String displayValue = rawText
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\\", "\\\\");

		displayValue = displayValue.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;").replace("\n", "<br>") // ★ brに置換
				.replace("\r", " ");

		return displayValue;
	}
	
	/**
	 * @param completionKey
	 * @param completionFilePath
	 * @param completionLineNumber
	 * @param displayValue
	 * @return
	 */
	private String createMarkDown(String completionKey, String completionFilePath, int completionLineNumber,
			String displayValue) {
		StringBuilder markdown = new StringBuilder();
		markdown.append("**Key**:  \n").append(completionKey).append("  \n");
		markdown.append("**Value**:  \n").append(displayValue).append("  \n");
		markdown.append("**File**:  \n").append(completionFilePath).append(" (Line: ")
				.append(completionLineNumber + 1).append(")");
		return markdown.toString();
	}

	/**
	 * 文字リテラルのうち、カーソル途中までの生の文字列を返す
	 * @param content
	 * @param offset
	 * @param literalNode
	 * @return
	 */
	private String getEditingString(String content, int offset, StringLiteral literalNode) {
		
		// リテラルの開始位置（ダブルクォートの位置）
		int literalStart = literalNode.getStartPosition();

		// カーソルがダブルクォートの直後、またはリテラル内にある場合
		if (offset > literalStart) {
			
			// literalNode.getLiteralValue() は「リテラル全体の確定値」を返します。
			// 入力途中の値を正確に取るため、開始からカーソル位置までの「生文字列」を一度切り出します。
			String rawSubString = content.substring(literalStart + 1, offset);

			// 【重要】切り出した「入力途中の生文字列」をJavaのルールでデコードします。
			// これにより、ユーザーが「\"」と打っていたら「"」としてマッチングに利用できます。
			return StringEscapeUtils.unescapeJava(rawSubString);
		}
		return "";
	}

	/**
	 * オフセットからLSP用のRangeを作成する
	 */
	private Range createRange(CompilationUnit cu, int start, int end) {
		return new Range(
				new Position(cu.getLineNumber(start) - 1, cu.getColumnNumber(start)),
				new Position(cu.getLineNumber(end) - 1, cu.getColumnNumber(end)));
	}

	

	/**
	 * @return
	 */
	private CompletableFuture<Either<List<CompletionItem>, CompletionList>> emptyResult() {
		return CompletableFuture.completedFuture(Either.forLeft(new ArrayList<>()));
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		docs.put(params.getTextDocument().getUri(), params.getTextDocument().getText());
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		docs.put(params.getTextDocument().getUri(), params.getContentChanges().get(0).getText());
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		docs.remove(params.getTextDocument().getUri());
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {

	}

}
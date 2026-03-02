package com.jhappy.jdt.lsp.properties;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.jhappy.jdt.lsp.DataEntry;
import com.jhappy.jdt.lsp.setting.QueryConfig;

/**
 * 
 */
public class PropertiesScanner {

	/**
	 * @param path
	 * @param rootPath2
	 * @param allPropertyCache
	 * @param configs
	 */
	public static List<DataEntry> loadPropertyFile(Path path, Path rootPath, List<QueryConfig> configs) {

		Path relativePath = rootPath.relativize(path);

		String relStr = relativePath.toString();

		if (configs == null)
			return null;

		if (!isTargetFile(configs, relStr))
			return null;

		List<DataEntry> newEntries = new ArrayList<>();

		try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

			Properties props = new Properties();
			props.load(br);

			List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

			Map<String, Integer> lineMap = buildKeyLineMap(lines);

			for (String key : props.stringPropertyNames()) {

				String value = props.getProperty(key);

				// Mapから O(1) で行番号を取得（万が一見つからない場合は先頭の0行目とする）
				int lineNumber = lineMap.getOrDefault(key, 0);

				newEntries.add(new DataEntry(key, value, path, lineNumber, "property"));

			}
			return newEntries;

		} catch (IllegalArgumentException e) {

			e.printStackTrace(); 
		    
		    // ユーザーに分かりやすいメッセージを作成
		    String errorMessage = String.format(
		        "LSP: [Invalid Unicode Escape] Found a malformed '\\u' sequence in your properties file.\n" +
		        "File: %s\n" +
		        "Tip: Ensure all Unicode escapes are followed by exactly 4 hex digits (e.g., \\u0020).",
		        path.toAbsolutePath()
		    );

		    // 1. サーバーのコンソール（System.err）へ出力
		    System.err.println(errorMessage);
			

			newEntries.add(new DataEntry("error occured", e.getMessage(), path, 0, "property"));

		} catch (IOException e) {
			System.err.println("LSP: ERROR " + relativePath + ":" + e.getMessage());
			e.printStackTrace();
		}
		return newEntries;
	}

	/**
	 * @param configs
	 * @param relStr
	 * @return
	 */
	private static boolean isTargetFile(List<QueryConfig> configs, String relStr) {

		boolean isTargetFile = false;
		for (QueryConfig config : configs) {
			if ("properties".equals(config.data.type) && config.data.pathPattern.matcher(relStr).find()) {
				isTargetFile = true;
				break;
			}
		}
		return isTargetFile;
	}
	

	/**
	 * Scanning for property keys and mapping them to their physical line occurrences.
	 * プロパティのキーをスキャンし、それらを物理的な出現行へとマッピングします。
	 *
	 */
	private static Map<String, Integer> buildKeyLineMap(List<String> lines) {

		Map<String, Integer> lineMap = new HashMap<>();

		for (int i = 0; i < lines.size(); i++) {

			String line = lines.get(i).trim();

			// コメント行や空行はスキップm
			if (line.isEmpty() || line.startsWith("#") || line.startsWith("!")) {
				continue;
			}

			int separatorIdx = -1;
			boolean escape = false;
			for (int j = 0; j < line.length(); j++) {
				char c = line.charAt(j);
				if (escape) {
					escape = false;
				} else if (c == '\\') {
					escape = true;
				} else if (c == '=' || c == ':' || Character.isWhitespace(c)) {
					separatorIdx = j;
					break;
				}
			}

			String rawKey = (separatorIdx == -1) ? line : line.substring(0, separatorIdx);

			//
			String logicalKey = unescapePropertiesKey(rawKey);

			// If the key is duplicated, only the last occurrence is stored.
			lineMap.put(logicalKey, i);

		}
		return lineMap;
	}

	/**
	 * 抽出した生のキー文字列を、Javaの Properties 仕様に合わせてデコードします。
	 */
	private static String unescapePropertiesKey(String rawKey) {

		StringBuilder sb = new StringBuilder(rawKey.length());

		boolean escape = false;

		for (int i = 0; i < rawKey.length(); i++) {
			char c = rawKey.charAt(i);
			if (escape) {
				escape = false;
				if (c == 'u' && i + 4 < rawKey.length()) {
					try {
						// ユニコードエスケープを本来の文字に変換
						int code = Integer.parseInt(rawKey.substring(i + 1, i + 5), 16);
						sb.append((char) code);
						i += 4;
						continue;
					} catch (NumberFormatException e) {
						// 無効な形式の場合はスキップせずそのまま扱う
					}
				} else if (c == 't') {
					sb.append('\t');
				} else if (c == 'n') {
					sb.append('\n');
				} else if (c == 'r') {
					sb.append('\r');
				} else {
					sb.append(c);
				} // \= や \: は =, : に戻る
			} else if (c == '\\') {
				escape = true;
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
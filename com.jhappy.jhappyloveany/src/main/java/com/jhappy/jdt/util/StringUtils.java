package com.jhappy.jdt.util;

import java.util.Map;

import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.LookupTranslator;

/**
 * 
 */
public class StringUtils {

	// Javaリテラルに必要な最小限のエスケープだけを行うトランスレータ
	private static final CharSequenceTranslator CUSTOM_JAVA_ESCAPER = new AggregateTranslator(
			new LookupTranslator(Map.of(
					"\"", "\\\"", // ダブルクォート -> \"
					"\\", "\\\\", // バックスラッシュ -> \\
					"\b", "\\b", // バックスペース
					"\n", "\\n", // 改行
					"\r", "\\r", // 復帰
					"\f", "\\f", // フォームフィード
					"\t", "\\t" // タブ
			)));

	/**
	 * @param input
	 * @return
	 */
	public static String escapeMinimal(String input) {
		if (input == null)
			return null;
		return CUSTOM_JAVA_ESCAPER.translate(input);
	}
}

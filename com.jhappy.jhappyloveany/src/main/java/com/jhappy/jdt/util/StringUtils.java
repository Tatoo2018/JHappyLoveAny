package com.jhappy.jdt.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.LookupTranslator;

/**
 * 
 */
public class StringUtils {

	// Java 8互換の静的初期化ブロックを使用してトランスレータを作成
    private static final CharSequenceTranslator CUSTOM_JAVA_ESCAPER;

    static {
        Map<CharSequence, CharSequence> lookupMap = new HashMap<>();
        lookupMap.put("\"", "\\\""); // ダブルクォート -> \"
        lookupMap.put("\\", "\\\\"); // バックスラッシュ -> \\
        lookupMap.put("\b", "\\b");   // バックスペース
        lookupMap.put("\n", "\\n");   // 改行
        lookupMap.put("\r", "\\r");   // 復帰
        lookupMap.put("\f", "\\f");   // フォームフィード
        lookupMap.put("\t", "\\t");   // タブ

        CUSTOM_JAVA_ESCAPER = new AggregateTranslator(
                new LookupTranslator(lookupMap)
        );
    }
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

package com.jhappy.jdt.lsp;

import java.nio.file.Path;

/**
 * プロパティキャッシュ用のクラス
 */
public  class DataEntry {
	
	//プロパティキー
	public String key;

	//プロパティ値
	public String value;

	//ファイル名
	public String fileName;

	//ファイルパス
	public String filePath;

	//プロパティファイル内の行番号
	public int lineNumber;

	public String type;

	/**
	 * @param key
	 * @param value
	 * @param path
	 * @param lineNumber
	 */
	public DataEntry(String key, String value, Path path, int lineNumber, String type) {
		this.key = key;
		this.value = value;
		this.fileName = path.getFileName().toString();
		this.filePath = path.toAbsolutePath().toString();
		this.lineNumber = lineNumber;
		this.type = type;
	}

	/**
	 * クラス内の全フィールドの状態を文字列として出力します。
	 * デバッグ時やエラーログで、キャッシュの内容を詳細に確認するために使用します。
	 */
	@Override
	public String toString() {
	    return String.format(
	        "DataEntry { " +
	        "type='%s', " +
	        "key='%s', " +
	        "value='%s', " +
	        "fileName='%s', " +
	        "lineNumber=%d, " +
	        "filePath='%s' " +
	        "}",
	        type,           // xml または property 
	        key,            // プロパティキー 
	        value,          // プロパティ値 
	        fileName,       // ファイル名のみ 
	        lineNumber + 1, // 1ベースの行番号（エディタの表示と同期） 
	        filePath        // システム上の絶対パス 
	    );
	}

	/**
	 *
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DataEntry that = (DataEntry) o;
		// filePath, key, lineNumber が一致すれば同一とみなす
		// (valueは変更される可能性があるため、一意性の判定には含めないのが一般的です)
		return lineNumber == that.lineNumber &&
				java.util.Objects.equals(key, that.key) &&
				java.util.Objects.equals(filePath, that.filePath);
	}

	/**
	 *
	 */
	@Override
	public int hashCode() {
		// equals で使用したフィールドと同じものを使ってハッシュ値を生成
		return java.util.Objects.hash(key, filePath, lineNumber);
	}
}
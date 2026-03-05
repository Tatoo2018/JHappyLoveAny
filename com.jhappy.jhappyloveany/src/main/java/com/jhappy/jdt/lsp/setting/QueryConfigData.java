package com.jhappy.jdt.lsp.setting;

import java.util.regex.Pattern;

/**
 * 検索のための設定ファイルの中身
 */
public class QueryConfigData {
	
	public Pattern pathPattern;
	public String xpath;
	public boolean isTrim;
	public String type;
	public String searchRootPath;

	public QueryConfigData() {

	}
}
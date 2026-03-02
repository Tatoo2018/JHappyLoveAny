package com.jhappy.jdt.lsp.setting;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 検索のための設定ファイルの中身
 */
public class QueryConfigData {
	
	public Pattern pathPattern;
	public List<String> xpaths;
	public boolean trim;
	public String type;
	public String searchRootPath;

	public QueryConfigData() {

	}
}
package com.jhappy.jdt.lsp.setting;

import java.util.stream.Collectors;

/**
 * 
 */
public class QueryConfig {

	public QueryConfigData data = new QueryConfigData();

	/**
	 * @param regex
	 * @param xpathContent
	 * @param trim
	 * @param type
	 * @param searchRootPath
	 */
	public QueryConfig(String regex, String xpathContent, boolean trim, String type, String searchRootPath) {
		this.data.pathPattern = java.util.regex.Pattern.compile(regex);
		// CDATA内の改行を考慮してリスト化
		this.data.xpaths = java.util.Arrays.stream(xpathContent.split("\n"))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toList());
		this.data.trim = trim;
		this.data.type = type;
		this.data.searchRootPath = searchRootPath;
	}
}
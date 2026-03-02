package com.jhappy.jdt.lsp.xml;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.jhappy.jdt.lsp.DataEntry;
import com.jhappy.jdt.lsp.setting.QueryConfig;

/**
 * 
 */
public class XmlScanner {

	/**
	 * @param path
	 * @param projectPath
	 * @param configs
	 * @return
	 */
	public static List<DataEntry> loadXmlFile(Path path, Path projectPath, List<QueryConfig> configs) {

		try {

			String relativePath = projectPath.relativize(path).toString();

			List<String> matchedXpathList = filterXPathList(configs, relativePath);

			// scan xml file
			List<DataEntry> newEntries = JHappyXmlScanner.scan(path, matchedXpathList);

			return newEntries;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param configs
	 * @param filepath
	 * @return
	 */
	private static List<String> filterXPathList(List<QueryConfig> configs, String filepath) {
		
		List<String> matchedXpathList = new ArrayList<>();
		for (QueryConfig config : configs) {
			if ("xml".equals(config.data.type) && config.data.pathPattern.matcher(filepath).find()) {
				matchedXpathList.addAll(config.data.xpaths);
			}
		}
		return matchedXpathList;
	}
}

package com.jhappy.jdt.lsp.xml;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jhappy.jdt.lsp.DataEntry;
import com.jhappy.jdt.lsp.setting.QueryConfig;

/**
 * 
 */
public class JHappyXmlScanner {

	private static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();

	/**
	 * @param path
	 * @param projectPath
	 * @param configs
	 * @return
	 * @throws XPathExpressionException 
	 */
	public static List<DataEntry> loadXmlFile(Path path, Path projectPath, List<QueryConfig> configs) {

		Path relativePath = projectPath.relativize(path);

		List<QueryConfig> matchedConfigs = filterXPathList(configs, relativePath);

		// scan xml file
		if (0 < matchedConfigs.size()) {
			List<DataEntry> newEntries = JHappyXmlScanner.scan(path, matchedConfigs);
			return newEntries;
		}

		return new ArrayList<>();

	}

	/**
	 * @param configs
	 * @param filepath
	 * @return
	 */
	private static List<QueryConfig> filterXPathList(List<QueryConfig> configs, Path filepath) {

		List<QueryConfig> matchedConfigs = new ArrayList<>();
		for (QueryConfig config : configs) {
			if ("xml".equals(config.data.type) && config.data.pathPattern.matcher(filepath.toString()).find()) {
				matchedConfigs.add(config);
			}
		}
		return matchedConfigs;
	}

	/**
	 * @param path
	 * @param xpaths
	 * @return
	 * @throws XPathExpressionException 
	 * @throws Exception
	 */
	public static List<DataEntry> scan(Path path, List<QueryConfig> configs){

		List<DataEntry> entries = new ArrayList<>();

		Document doc;
		try {
			doc = JHappyXmlSourceParser.parse(path.toFile());
		} catch (Exception e) {
			e.printStackTrace();
			return entries;
		}
		XPath xpathProcessor = XPATH_FACTORY.newXPath();

		for (QueryConfig config : configs) {

			String expression = config.data.xpath;

			NodeList nodes = null;
			
			try {
				nodes = (NodeList) xpathProcessor.evaluate(expression, doc, XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				e.printStackTrace();
				return new ArrayList<>();
			}
		

			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				
				String value = node.getTextContent();
				if (config.data.isTrim) {
					value = value == null ? null : value.trim();
				}

				// 行番号の取得 (属性の場合は親要素から取得)
				Node lineNode = (node.getNodeType() == Node.ATTRIBUTE_NODE)
						? ((Attr) node).getOwnerElement()
						: node;
				Integer line = (Integer) lineNode.getUserData(JHappyXmlSourceParser.LINE_NUMBER_KEY);

				if (line != null && !value.isEmpty()) {
					entries.add(new DataEntry(value, value, path, line - 1, "xml"));
				}
			}
		}

		return entries;
	}
}
package com.jhappy.jdt.lsp.xml;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
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
	 */
	public static List<DataEntry> loadXmlFile(Path path, Path projectPath, List<QueryConfig> configs) {

		try {

			Path relativePath = projectPath.relativize(path);

			List<String> matchedXpathList = filterXPathList(configs, relativePath);

			// scan xml file
			if(0 < matchedXpathList.size()) {
				List<DataEntry> newEntries = JHappyXmlScanner.scan(path, matchedXpathList);
				return newEntries;
			}
			

			return null;

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
	private static List<String> filterXPathList(List<QueryConfig> configs, Path filepath) {
		
		List<String> matchedXpathList = new ArrayList<>();
		for (QueryConfig config : configs) {
			if ("xml".equals(config.data.type) && config.data.pathPattern.matcher(filepath.toString()).find()) {
				matchedXpathList.addAll(config.data.xpaths);
			}
		}
		return matchedXpathList;
	}

	/**
	 * @param path
	 * @param xpaths
	 * @return
	 * @throws Exception
	 */
	public static List<DataEntry> scan(Path path, List<String> xpaths) throws Exception {
		
		

		List<DataEntry> entries = new ArrayList<>();

		Document doc = JHappyXmlSourceParser.parse(path.toFile());
		XPath xpathProcessor = XPATH_FACTORY.newXPath();

		for (String expression : xpaths) {
			String trimmedExpr = expression.trim();
			System.err.println("DEBUG: Testing XPath -> [" + trimmedExpr + "]");

			NodeList nodes = null;
			nodes = (NodeList) xpathProcessor.evaluate(expression, doc, XPathConstants.NODESET);

			System.err.println("DEBUG: Found nodes count: " + nodes.getLength());
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				String value = node.getTextContent().trim();

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
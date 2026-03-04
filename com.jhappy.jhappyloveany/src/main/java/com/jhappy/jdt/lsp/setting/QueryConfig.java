package com.jhappy.jdt.lsp.setting;

import javax.xml.xpath.XPathExpressionException;

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
	 * @throws XPathExpressionException 
	 */
	public QueryConfig(String regex, String xpath, boolean isTrim, String type, String searchRootPath)
			throws XPathExpressionException {

		validXPath(xpath);

		this.data.pathPattern = java.util.regex.Pattern.compile(regex);
		this.data.xpath = xpath;
		this.data.isTrim = isTrim;
		this.data.type = type;
		this.data.searchRootPath = searchRootPath;

	}

	private boolean validXPath(String xpathExpression) throws XPathExpressionException {

		if (xpathExpression == null || xpathExpression.trim().isEmpty()) {
			return false;
		}
		try {
			javax.xml.xpath.XPathFactory factory = javax.xml.xpath.XPathFactory.newInstance();
			javax.xml.xpath.XPath xpath = factory.newXPath();
			// コンパイルを試みることで構文チェックを行う
			xpath.compile(xpathExpression);
			return true;
		} catch (javax.xml.xpath.XPathExpressionException e) {
			throw e;
		}
	}
}
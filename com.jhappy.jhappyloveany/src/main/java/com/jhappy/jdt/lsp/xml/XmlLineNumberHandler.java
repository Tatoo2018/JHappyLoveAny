package com.jhappy.jdt.lsp.xml;

import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.DefaultHandler;

import com.jhappy.jdt.lsp.DataEntry;

/**
 * 
 */
public class XmlLineNumberHandler extends DefaultHandler {
	private Locator locator;
	private Stack<String> elementStack = new Stack<>();
	private StringBuilder characters = new StringBuilder();
	private final List<String> targetPaths;
	private final List<DataEntry> entries;
	private final java.nio.file.Path filePath;

	/**
	 * @param filePath
	 * @param targetPaths
	 * @param entries
	 */
	public XmlLineNumberHandler(java.nio.file.Path filePath, List<String> targetPaths,
			List<DataEntry> entries) {
		this.filePath = filePath;
		this.targetPaths = targetPaths;
		this.entries = entries;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	/**
	 *
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		elementStack.push(qName);
		characters.setLength(0); // 中身をリセット

		// パスを構築 (例: /project/artifactId)
		String currentPath = "/" + String.join("/", elementStack);

		// 属性値もキャッシュしたい場合の処理
		for (int i = 0; i < attributes.getLength(); i++) {
			String attrPath = currentPath + "/@" + attributes.getQName(i);

			System.err.println("startElement for " + qName + " (" + attrPath + " attr)" + targetPaths);
			if (targetPaths.contains(attrPath)) {
				int line = locator.getLineNumber() - 1; // 0-based
				entries.add(new DataEntry(attributes.getValue(i), attributes.getValue(i),
						filePath, line, "xml"));
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		characters.append(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		String currentPath = "/" + String.join("/", elementStack);
		String value = characters.toString().trim();

		// ターゲットパスに一致し、かつ値がある場合にキャッシュ
		if (targetPaths.contains(currentPath) && !value.isEmpty()) {
			int line = locator.getLineNumber() - 1;
			entries.add(new DataEntry(value, value, filePath, line, "xml"));
		}
		elementStack.pop();
	}
}
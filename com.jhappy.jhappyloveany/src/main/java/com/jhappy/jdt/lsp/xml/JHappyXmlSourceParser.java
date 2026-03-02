package com.jhappy.jdt.lsp.xml;

import java.io.File;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 */
public class JHappyXmlSourceParser {
	
	public static final String LINE_NUMBER_KEY = "line";

	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static Document parse(File file) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
		dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		Document doc = dbf.newDocumentBuilder().newDocument();

		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		SAXParser saxParser = spf.newSAXParser();

		DefaultHandler handler = new DefaultHandler() {
			private Locator locator;
			private Stack<Element> elementStack = new Stack<>();

			@Override
			public void setDocumentLocator(Locator locator) {
				this.locator = locator;
			}

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) {
				Element el = doc.createElementNS(uri, qName);
				for (int i = 0; i < attributes.getLength(); i++) {
					el.setAttributeNS(attributes.getURI(i), attributes.getQName(i), attributes.getValue(i));
				}
				// 行番号を保存 (1-based)
				el.setUserData(LINE_NUMBER_KEY, locator.getLineNumber(), null);

				if (elementStack.isEmpty())
					doc.appendChild(el);
				else
					elementStack.peek().appendChild(el);
				elementStack.push(el);
			}

			@Override
			public void endElement(String uri, String localName, String qName) {
				elementStack.pop();
			}

			@Override
			public void characters(char[] ch, int start, int length) {
				String text = new String(ch, start, length);
				if (!elementStack.isEmpty() && !text.trim().isEmpty()) {
					elementStack.peek().appendChild(doc.createTextNode(text));
				}
			}
		};

		saxParser.parse(file, handler);
		return doc;
	}
}
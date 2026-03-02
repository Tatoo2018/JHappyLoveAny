package com.jhappy.jdt.lsp.setting;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 */
public class QuerySetting {

	public static final String JHAPPYQUERIES_XML = "jhappyqueries.xml";

	public String completionMatch;

	public List<QueryConfig> configs = new ArrayList<>();

	/**
	 * @param targetUri
	 * @return
	 */
	public static Map<String, QuerySetting> loadQueriesConfig(String targetUri) {
		
		Map<String, QuerySetting> projectQueryConfigs = new java.util.concurrent.ConcurrentHashMap<>();

		try {
			
			Path rootPath;
			if (targetUri.startsWith("file:")) {
				rootPath = Paths.get(new URI(targetUri));
			} else {
				// 普通のパスとして解釈
				rootPath = Paths.get(targetUri);
			}
	
			Path configPath = rootPath.resolve(JHAPPYQUERIES_XML); // 指定のXML

			QuerySetting querySetting = new QuerySetting();

			if (Files.exists(configPath)) {
				querySetting.configs = new ArrayList<>();
				
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

				dbf.setNamespaceAware(true);
			
				// 1. DOCTYPE宣言自体を拒否する設定を false にする（これで読み込めるようになります）
				dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);

				// 2. 外部エンティティの解決（XXE攻撃対策）だけを個別に無効化する
				dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
				dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

				// 3. 外部DTDのロードを禁止する
				dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

				// 4. エンティティ参照を展開しない設定（念のため）
				dbf.setExpandEntityReferences(false);
				
				
				Document doc = dbf.newDocumentBuilder().parse(configPath.toFile());
				
				NodeList nodes = doc.getElementsByTagName("query");
				NodeList queriesNode = doc.getElementsByTagName("queries");
				if (queriesNode.getLength() == 1) {
					Element el = (Element) queriesNode.item(0);
					querySetting.completionMatch = el.getAttribute("completionMatch");
				}

				for (int i = 0; i < nodes.getLength(); i++) {
					Element el = (Element) nodes.item(i);
					querySetting.configs.add(new QueryConfig(
							el.getAttribute("filepath"),
							el.getTextContent(),
							"yes".equals(el.getAttribute("trim")),
							el.getAttribute("type"),
							el.getAttribute("searchrootpath")));
				}
				projectQueryConfigs.put(rootPath.toAbsolutePath().toString(), querySetting);
			}else {
				System.err.println("JHappy: "+configPath.toAbsolutePath().toString()+" doesn't exist");
			}
			
		} catch (Exception e) {
			System.err.println("LSP: Failed to load queries.xml: " + targetUri);
			e.printStackTrace();
		
		}

		return projectQueryConfigs;
	}

}

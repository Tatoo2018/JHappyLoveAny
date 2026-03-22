package com.jhappy.jdt.util;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 */
public class EclipseUtil {

	/**
	 * プロジェクトのアウトプットフォルダを取得
	 * Eclipseの場合、ここにコンパイル後のclassファイルや、その他リソースファイル（プロパティファイルやXMLなど）
	 * が出力されるので、元のファイルと二重に存在することがあるので、このアウトプットフォルダを走査対象から除外すべき。
	 * 逆にclassファイルを走査する場合は、このフォルダが走査対象になる
	 * 通常はデフォルトで[bin]というフォルダのはず

	 * 指定されたプロジェクトフォルダのアウトプットディレクトリ（bin, target等）を判定します。
	 * * @param targetUri 解析対象のプロジェクトルートURI
	 * @return 除外すべきフォルダパスのセット（相対パス形式）
	 */
	public static Set<String> getOutputFolders(String targetUri) {

		Set<String> outputFolders = new HashSet<>();
		Path projectPath;

		try {
			projectPath = Paths.get(new URI(targetUri));
		} catch (Exception e) {
			// URIが不正な場合のフォールバック
			String plainPath = targetUri.replaceFirst("^file:/+", "/");
			projectPath = Paths.get(plainPath);
		}

		// Eclipseの .classpath ファイルを優先的に解析
		Path classpathFilePath = projectPath.resolve(".classpath");

		if (Files.exists(classpathFilePath)) {
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				
				// 外部エンティティ参照による攻撃を防ぐための安全な設定
				factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.parse(classpathFilePath.toFile());

				NodeList nList = doc.getElementsByTagName("classpathentry");
				for (int i = 0; i < nList.getLength(); i++) {
					Element element = (Element) nList.item(i);
					// <classpathentry kind="output" path="bin"/> を抽出
					if ("output".equals(element.getAttribute("kind"))) {
						String path = element.getAttribute("path");
						if (path != null && !path.isEmpty()) {
							outputFolders.add(path);
						}
					}
				}
			} catch (Exception e) {
				System.err.println("LSP: Error parsing .classpath in " + targetUri + ": " + e.getMessage());
			}
		}

		// VScodeなど、出力先や無視すべきフォルダを.gitignoreなどから読み取りたいが
		// それは未実装なのでデフォルト設定を固定で行う
		if (outputFolders.isEmpty()) {
			// Mavenなら target、標準Eclipseなら bin
			outputFolders.add("bin");
			outputFolders.add("target");
			outputFolders.add("build"); // Gradle/VSCode用に追加
	        outputFolders.add("node_modules");
		}

		return outputFolders;
	}
}

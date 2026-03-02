package com.jhappy.jhappyloveany.test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class PropertyAndXmlGenerator {
    private static final int TOTAL_FILES = 500; // 各形式500個、合計1000ファイル
    private static final int DEPTH = 4;
    private static final int ENTRIES_PER_FILE = 50;
    private static final String ROOT_DIR = "src/";

    public static void main(String[] args) throws IOException {
        for (int i = 0; i < TOTAL_FILES; i++) {
            int currentDepth = i % DEPTH;
            StringBuilder pathBuilder = new StringBuilder(ROOT_DIR);
            for (int d = 0; d <= currentDepth; d++) {
                pathBuilder.append("/dir").append(d);
            }

            Path dirPath = Paths.get(pathBuilder.toString());
            Files.createDirectories(dirPath);

            // プロパティファイルの生成
            createPropertyFile(dirPath.resolve("test_" + i + ".properties"), i);

            // XMLファイルの生成
            createXmlFile(dirPath.resolve("config_" + i + ".xml"), i);

            if (i % 50 == 0) {
                System.out.println(i + " pairs (Prop/XML) created...");
            }
        }
        System.out.println("Finished! Total 1000 files created with unique keys.");
    }

    private static void createPropertyFile(Path path, int fileIndex) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("# Generated Property File " + fileIndex + "\n");
            for (int j = 0; j < ENTRIES_PER_FILE; j++) {
                // キーを全ファイルでユニークにする (e.g., prop_f10_e49)
                String key = "prop_f" + fileIndex + "_e" + j;
                writer.write(key + "=" + generateValue(j) + "\n");
            }
        }
    }

    private static void createXmlFile(Path path, int fileIndex) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<root>\n");
            writer.write("  \n");
            for (int j = 0; j < ENTRIES_PER_FILE; j++) {
                // XMLのkeyも全ファイルでユニークにする (e.g., xml_f10_e49)
                String key = "xml_f" + fileIndex + "_e" + j;
                String value = generateValue(j);

                // XML用のエスケープ処理
                String safeValue = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                writer.write("  <entry key=\"" + key + "\">" + safeValue + "</entry>\n");
            }
            writer.write("</root>\n");
        }
    }

    private static String generateValue(int index) {
        switch (index % 6) {
            case 0: return "【重要】設定：" + UUID.randomUUID().toString().substring(0, 5);
            case 1: return "欢迎使用系统 (Welcome)";
            case 2: return "Special characters: <>&\"'";
            case 3: return "Long text: " + UUID.randomUUID() + " - " + UUID.randomUUID();
            case 4: return "Escaped: \\n\\t\\\\u3042";
            default: return "Value_" + index;
        }
    }
}
package com.jhappy.jdt.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.icu.ICUNormalizer2Filter;
import org.apache.lucene.analysis.icu.ICUTransformFilter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import com.ibm.icu.text.Normalizer2;
import com.ibm.icu.text.Transliterator;
import com.jhappy.jdt.lsp.DataEntry;

public class JHappyIndexManager {

	private final Directory directory = new ByteBuffersDirectory();

	// JHappyIndexManager.java
	private final Analyzer analyzer = new Analyzer() {
	    @Override
	    protected TokenStreamComponents createComponents(String fieldName) {
	    	
	        //
	        Tokenizer source = new KeywordTokenizer();
	        
	        TokenStream filter = new ICUTransformFilter(source, Transliterator.getInstance("Hiragana-Katakana"));
	        
	        
	        filter = new ICUNormalizer2Filter(filter, 
		            Normalizer2.getInstance(null, "nfkc_cf", Normalizer2.Mode.COMPOSE));
	        
	      
	        filter = new LowerCaseFilter(filter);
	        
	        return new TokenStreamComponents(source, filter);
	    }
	};
	
	/**
	 * 指定されたファイルパスに関連付けられたすべてのインデックスデータを削除する
	 */
	public void removeFileIndex(String filePath) throws IOException {
	    IndexWriterConfig config = new IndexWriterConfig(analyzer);
	    try (IndexWriter writer = new IndexWriter(directory, config)) {
	        // filePathフィールドが一致するドキュメントをすべて削除
	        writer.deleteDocuments(new Term("filePath", filePath));
	        writer.commit(); // 変更を即座に確定
	    }
	}

	/**
	 * DataEntryのリストをインデックスに登録/更新する
	 */
	public void updateIndex(String filePath, List<DataEntry> entries) throws IOException {
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		try (IndexWriter writer = new IndexWriter(directory, config)) {
			// 指定ファイルの既存データを削除（パスをIDとして使用）
			writer.deleteDocuments(new Term("filePath", filePath));

			for (DataEntry entry : entries) {
				Document doc = new Document();
				// 検索対象フィールド (キーと値)
				doc.add(new TextField("key", entry.key, Field.Store.YES));
				doc.add(new TextField("value", entry.value, Field.Store.YES));
				
				// 2. 完全一致用（アナライザーを通さず、そのまま格納する）
	            doc.add(new StringField("key_raw", entry.key, Field.Store.YES));
	            doc.add(new StringField("value_raw", entry.value, Field.Store.YES));

				// フィルタリング・表示用フィールド
				doc.add(new StringField("filePath", entry.filePath, Field.Store.YES));
				doc.add(new StringField("fileName", entry.fileName, Field.Store.YES));
				doc.add(new StringField("type", entry.type, Field.Store.YES));
				doc.add(new IntPoint("lineNumber", entry.lineNumber));
				doc.add(new StoredField("lineNumber", entry.lineNumber));

				writer.addDocument(doc);
			}
			writer.commit();
		}
	}
	
	/**
	 * インデックス内のすべてのデータを削除（クリア）する
	 */
	public void clearAll() throws IOException {
	    IndexWriterConfig config = new IndexWriterConfig(analyzer);
	    try (IndexWriter writer = new IndexWriter(directory, config)) {
	        writer.deleteAll();
	        writer.commit(); // 変更を確定させる
	    }
	}
	
	/**
	 * HoverやJump用の完全一致検索
	 * 大文字小文字を厳密に区別して判定します。
	 */
	public List<DataEntry> searchExact(String searchText) throws IOException {
	    List<DataEntry> results = new ArrayList<>();
	    if (searchText == null || searchText.isEmpty()) return results;

	    try (IndexReader reader = DirectoryReader.open(directory)) {
	        IndexSearcher searcher = new IndexSearcher(reader);
	        
	        // ★ ここでは toLowerCase() をせず、生の searchText を使う
	        BooleanQuery.Builder builder = new BooleanQuery.Builder();
	        builder.add(new TermQuery(new Term("key_raw", searchText)), BooleanClause.Occur.SHOULD);
	        builder.add(new TermQuery(new Term("value_raw", searchText)), BooleanClause.Occur.SHOULD);

	        TopDocs topDocs = searcher.search(builder.build(), 100);

	        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
	            Document doc = searcher.doc(scoreDoc.doc);
	            results.add(mapDocToEntry(doc));
	        }
	    }
	    return results;
	}



	public List<DataEntry> search(String searchText, MatchType type, int limit) throws IOException {
	    List<DataEntry> finalResults = new ArrayList<>();
	    // 重複排除用（同じ行がKeyとValue両方でヒットする場合があるため）
	    java.util.Set<String> seenIdentifiers = new java.util.HashSet<>();

	    try (IndexReader reader = DirectoryReader.open(directory)) {
	        IndexSearcher searcher = new IndexSearcher(reader);
	        String termText = normalizeText(searchText.toLowerCase());
	        
	        searchInField(searcher, "key", termText, type, limit, "key", finalResults, seenIdentifiers);

	        searchInField(searcher, "value", termText, type, limit, "value", finalResults, seenIdentifiers);
	 
	    }
	    return finalResults;
	}
	
	public String normalizeText(String text) throws IOException {
		    if (text == null) return "";
		    StringBuilder sb = new StringBuilder();
		    // この ts を通すことで、ユーザーの入力「あいう」が「アイウ」として抽出されます
		    try (TokenStream ts = analyzer.tokenStream(null, new java.io.StringReader(text))) {
		        ts.reset();
		        org.apache.lucene.analysis.tokenattributes.CharTermAttribute termAttr = 
		            ts.addAttribute(org.apache.lucene.analysis.tokenattributes.CharTermAttribute.class);
		        while (ts.incrementToken()) {
		            sb.append(termAttr.toString());
		        }
		        ts.end();
		    }
		    return sb.toString();
		}

	/**
	 * 特定のフィールドに対して検索を行い、結果リストに追加する補助メソッド
	 */
	private void searchInField(IndexSearcher searcher, String fieldName, String text, MatchType type, 
	                          int limit, String tag, List<DataEntry> results, java.util.Set<String> seen) throws IOException {
	    
	    Query query = buildSingleFieldQuery(fieldName, text, type);
	    TopDocs topDocs = searcher.search(query, limit);

	    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
	        Document doc = searcher.doc(scoreDoc.doc);
	        
	        // 重複チェック用のID（パス + 行番号）
	        String identifier = doc.get("filePath") + ":" + doc.get("lineNumber");
	        
	        // 初めて見つけた場合、またはタグを優先したい場合に処理
	        if (!seen.contains(identifier)) {
	            DataEntry entry = mapDocToEntry(doc);
	            entry.setMatchedField(tag); // ここで「key」か「value」かを記録
	            results.add(entry);
	            seen.add(identifier);
	        }
	    }
	}

	private Query buildSingleFieldQuery(String field, String text, MatchType type) {
	    switch (type) {
	        case PREFIX:
	            return new PrefixQuery(new Term(field, text));
	        case CONTAINS:
	            return new WildcardQuery(new Term(field, "*" + text + "*"));
	        case FUZZY:
	        	//fuzzy logic is not completed yet
	  
	          // return new FuzzyQuery(new Term(field, text), 2);
	        	BooleanQuery.Builder builder = new BooleanQuery.Builder();
	            
	            // 1. 部分一致 (Wildcard): "Microsoft..." -> "*microsoft*"
	            // これにより、先頭を数文字削ってもヒットするようになります
	            builder.add(new WildcardQuery(new Term(field, "*" + text + "*")), BooleanClause.Occur.SHOULD);
	            
	            // 2. 本来の曖昧検索 (Fuzzy): 近い綴り用
	            builder.add(new FuzzyQuery(new Term(field, text), 2, 0), BooleanClause.Occur.SHOULD);
	            
	            return builder.build();
	        default:
	            return new PrefixQuery(new Term(field, text));
	    }
	}

	

	private DataEntry mapDocToEntry(Document doc) {
	
		return new DataEntry(
				doc.get("key"),
				doc.get("value"),
				java.nio.file.Paths.get(doc.get("filePath")),
				doc.getField("lineNumber").numericValue().intValue(),
				doc.get("type"));
	}
	
	public enum MatchType {
		PREFIX, // 前方一致
		CONTAINS, // 部分一致（中間一致）
		FUZZY // あいまい検索（綴りミス許容）<<開発中
	}
}
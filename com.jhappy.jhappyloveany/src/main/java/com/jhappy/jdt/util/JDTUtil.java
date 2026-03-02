package com.jhappy.jdt.util;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * 
 */
public class JDTUtil {

	public static boolean isInsideStringLiteral(String source, int offset) {
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		// AST（抽象構文木）を生成
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		// 指定したオフセット（カーソル位置）にあるノードを探す
		ASTNode node = NodeFinder.perform(cu, offset, 0);

		// そのノード、またはその親が「StringLiteral」かどうかを判定
		while (node != null) {
			if (node instanceof StringLiteral) {
				return true;
			}
			node = node.getParent();
		}
		return false;
	}

	/**
	 * パース済みの CompilationUnit と計算済みの offset を使って、
	 * その位置にある文字列リテラルの「中身（デコード済み）」を返す
	 */
	public static String getStringLiteralValueAt(CompilationUnit cu, int offset) {
		
		if (cu == null)
			return null;

		
		ASTNode node = NodeFinder.perform(cu, offset, 0);

		// ノードを親方向に辿り、StringLiteral (ダブルクォートで囲まれたリテラル) を探す
		ASTNode current = node;
		while (current != null) {
			if (current instanceof StringLiteral) {
				// すべて解決された後の「生の文字列」を返してくれる
				return ((StringLiteral) current).getLiteralValue();
			}
			current = current.getParent();
		}
		return null;
	}

	/**
	 * ノードまたはその親を遡って StringLiteral を探す
	 */
	public static StringLiteral findStringLiteral(ASTNode node) {
		ASTNode current = node;
		while (current != null) {
			if (current instanceof StringLiteral)
				return (StringLiteral) current;
			current = current.getParent();
		}
		return null;
	}

}

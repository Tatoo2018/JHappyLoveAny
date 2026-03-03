package com.jhappy.jhappyloveany.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.lsp4e.operations.completion.LSContentAssistProcessor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;

@DisplayName("UI Test: UIperformTest")
class UIperformTest extends UiTest {

	@Test
	public void testMenuCommandExecution() throws Exception {

		Display.getDefault().syncExec(() -> {
			try {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();

				IViewPart pview = page.showView("org.eclipse.ui.navigator.ProjectExplorer");
				page.activate(pview);

				StructuredSelection pselection = new StructuredSelection(file1);
				pview.getSite().getSelectionProvider().setSelection(pselection);

				//ツリー展開
				if (pview instanceof ISetSelectionTarget) {
					((ISetSelectionTarget) pview).selectReveal(pselection);
				} else {
					pview.getSite().getSelectionProvider().setSelection(pselection);
				}

				IViewPart jview = page.showView("org.eclipse.jdt.ui.PackageExplorer");
				page.activate(jview);

				StructuredSelection selection = new StructuredSelection(file1);
				jview.getSite().getSelectionProvider().setSelection(selection);

				//ツリー展開
				if (jview instanceof ISetSelectionTarget) {
					((ISetSelectionTarget) jview).selectReveal(selection);
				} else {
					jview.getSite().getSelectionProvider().setSelection(selection);
				}

				// ツリーが展開されるアニメーションを処理させるためにUIを回す
				while (Display.getDefault().readAndDispatch())
					;

				Thread.sleep(1000);

				StructuredSelection selection2 = new StructuredSelection(project);
				// 単なる setSelection ではなく、プロジェクトに対しても Reveal を使って確実にフォーカスを戻す
				if (jview instanceof ISetSelectionTarget) {
					((ISetSelectionTarget) jview).selectReveal(selection2);
				} else {
					jview.getSite().getSelectionProvider().setSelection(selection2);
				}

				// ★ 超重要：プロジェクトが選択されたという「情報」がEclipse全体に行き渡るまで待つ！
				while (Display.getDefault().readAndDispatch())
					;

				// 6. Eclipseにファイルが出来たことを認識させる（リフレッシュ）
				project.refreshLocal(IResource.DEPTH_INFINITE, null);

				// 8. Eclipse標準のテキストエディタでファイルを開く！
				IDE.openEditor(page, file1);

				System.out.println("エディタを開きました！6秒後にテストを終了して片付けます...");

				try {
					// test-resources の中身をプロジェクトの src フォルダへインポート
					importResources("test-resources/com/test", "src/resources");
				} catch (Exception e) {
					e.printStackTrace();
					fail("リソースのインポートに失敗しました: " + e.getMessage());
				}

				String searchWord = "KEY";
				String srcprefix = "public class TestCompletion { \n";
				String srcVarDeclaration1 = "String str1 = \"" + searchWord + "\";\n";
				String srcVarDeclaration2 = "String str2 = \"" + "こんにちわ" + "\";\n";
				String srcVarDeclaration3 = "String str3 = \"" + searchWord + "\";\n";
				String srcVarDeclaration4 = "String str4 = \"" + searchWord + "\";\n";
				String srcsuffix = " }";
				// searchWordの末尾にカーソルがある状態をシミュレート
				int offsetForSearch = srcprefix.length() + srcVarDeclaration1.length() - 3;

				String code = srcprefix + srcVarDeclaration1 + srcVarDeclaration2 + srcVarDeclaration3
						+ srcVarDeclaration4 + srcsuffix;

				IFile javaFile = createFile("src/TestCompletion.java", code);

				// LSPサーバーの準備を待つ (スキャン完了待ち)
				Thread.sleep(3000);

				StructuredSelection pselection2 = new StructuredSelection(javaFile);
				pview.getSite().getSelectionProvider().setSelection(pselection2);

				// ★ 単に選択するだけでなく、ツリーを展開して該当ファイルにスクロールさせる魔法の命令
				if (pview instanceof ISetSelectionTarget) {
					((ISetSelectionTarget) pview).selectReveal(pselection2);
				} else {
					pview.getSite().getSelectionProvider().setSelection(pselection2);
				}

				// 1. まずエディタを開く（これでLSPサーバーが自動起動する）
				IEditorPart editor = IDE.openEditor(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
						javaFile);

				Object viewer = null;
				Class<?> clazz = editor.getClass();

				while (clazz != null && viewer == null) {
					try {
						// getMethod ではなく getDeclaredMethod を使用（protectedも対象）
						Method method = clazz.getDeclaredMethod("getSourceViewer");
						method.setAccessible(true); // アクセス制限を解除
						viewer = method.invoke(editor);
					} catch (NoSuchMethodException e) {
						// 現在のクラスになければ親クラス（AbstractTextEditorなど）を探索
						clazz = clazz.getSuperclass();
					}
				}

				if (viewer instanceof org.eclipse.jface.text.ITextViewer) {
					org.eclipse.jface.text.ITextViewer textViewer = (org.eclipse.jface.text.ITextViewer) viewer;

					// 補完の実行
					LSContentAssistProcessor processor = new LSContentAssistProcessor();

					ICompletionProposal[] proposals = processor.computeCompletionProposals(textViewer, offsetForSearch);

					for (ICompletionProposal proposal : proposals) {
						System.out.println("PROPOSAL RESULT :" + proposal.getDisplayString() + " "
								+ proposal.getAdditionalProposalInfo());
					}

					assertNotNull(proposals, "補完候補リストがnullです");
					assertTrue(proposals.length > 0, "補完候補が1件も見つかりません");
				} else {
					fail("SourceViewerを取得できませんでした。取得されたオブジェクト: " + viewer);
				}

				//to check by human
				long endTime = System.currentTimeMillis() + 1000;
				Display display = Display.getDefault();
				while (System.currentTimeMillis() < endTime) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}

				// 2. LSPサーバーの準備を待つ (スキャン完了待ち)
				Thread.sleep(3000);

			} catch (Exception e) {
				e.printStackTrace();
				fail("UI Test failed: " + e.getMessage());
			} finally {

			}
		});

		// ※ syncExec の外にあった Thread.sleep は削除しました
	}

	/**
	 * テストプラグイン内のリソースをプロジェクトへコピーする
	 * @param sourcePathBundle テストプロジェクト内のパス (例: "test-resources/com/test")
	 * @param targetPathProject コピー先のプロジェクト内パス (例: "src/resources")
	 */
	void importResources(String sourcePathBundle, String targetPathProject) throws Exception {
		// 1. テストプロジェクト(Fragment)のBundleを取得 [cite: 66]
		Bundle bundle = Platform.getBundle("com.jhappy.jhappyloveany.test");
		URL directoryUrl = FileLocator.find(bundle, new Path(sourcePathBundle), null);

		if (directoryUrl == null) {
			throw new RuntimeException("Resource not found in bundle: " + sourcePathBundle);
		}

		// ファイルシステム上のパスに変換
		File fileSystemDir = new File(FileLocator.toFileURL(directoryUrl).toURI());

		if (fileSystemDir.isDirectory()) {
			copyRecursive(fileSystemDir, targetPathProject);
		}
	}

	private void copyRecursive(File sourceNode, String targetPath) throws Exception {
		if (sourceNode.isDirectory()) {
			createFolderRecursively(targetPath); // 既存のフォルダ作成メソッドを利用 [cite: 103]
			for (File child : sourceNode.listFiles()) {
				copyRecursive(child, targetPath + "/" + child.getName());
			}
		} else {
			// ファイルの内容をバイト配列として読み込み、createFileで作成
			try (InputStream is = new java.io.FileInputStream(sourceNode)) {
				byte[] bytes = is.readAllBytes();
				createFile(targetPath, new String(bytes, StandardCharsets.UTF_8)); // 既存のメソッドを利用 
			}
		}
	}

}
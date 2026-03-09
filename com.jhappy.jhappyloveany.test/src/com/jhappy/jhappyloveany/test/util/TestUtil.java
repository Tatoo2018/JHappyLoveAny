package  com.jhappy.jhappyloveany.test.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.lsp4e.operations.completion.LSCompletionProposal;
import org.eclipse.lsp4e.operations.completion.LSContentAssistProcessor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ISetSelectionTarget;

/**
 * テスト用プラグイン内のリソースをワークスペースのプロジェクトにコピーするためのユーティリティ
 */
public class TestUtil {

    
    /**
     * JUnitプラグインテストプロジェクトに存在するtestsrcfolderの内容を
     * プロジェクトにコピーして、入力補完のテストを実行
     * 
     * @param bundleName
     * @param project
     * @param testsrcfolder　コピー元のフォルダ
     * @param samplefile1　コピーできたか確認するため、コピー対象のファイルパスを指定してください。
     * @param wordlist 入力補完をテストしたいキーワード、このキーワードをもとに、コピーしたリソースを解析した補完候補に対して、入力補完をトリガーします。
     * @throws CoreException
     * @throws IOException
     */
    public static void doTest(String bundleName, IProject project, String testsrcfolder, String samplefile1, List<String> wordlist)
			throws CoreException, IOException {

		TestResourceUtility.copyBundleResources(
				bundleName,
				testsrcfolder,
				project);

		IFile targetJavaFile = project.getFile(samplefile1);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		assertTrue(targetJavaFile.exists(), "コピーされたファイルが存在すること");

		Display.getDefault().syncExec(() -> {

			try {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();
				IViewPart jview = page.showView("org.eclipse.jdt.ui.PackageExplorer");

				//複数の入力テキストが起動するか複数チェック
				for (String searchWord : wordlist) {

					System.out.println("Start test proposal : word = " + searchWord);

					String srcprefix = "package com.test;\n public class TestCompletion { \n";
					String srcVarDeclaration1 = "public static String str1 = \"" + searchWord + "\";\n";
					String srcVarDeclaration2 = "public static String str2 = \"" + "こんにちわ" + "\";\n";
					String srcVarDeclaration3 = "public static String str3 = \"" + "Hello" + "\";\n";
					String srcVarDeclaration4 = "public static String str4 = \"" + "你好" + "\";\n";
					String srcsuffix = " public static void main(String[] args){\nSystem.out.println(str2);\n}\n}";

					//入力補完をトリガーする位置を計算
					int offsetForSearch = srcprefix.length() + srcVarDeclaration1.length() - 3;

					//テストに使用するJavaファイルの内容
					String code = srcprefix + srcVarDeclaration1 + srcVarDeclaration2 + srcVarDeclaration3
							+ srcVarDeclaration4 + srcsuffix;

					//ファイルを作成
					IFile javaFile = createFile(project, "src/com/test/TestCompletion.java", code);
					System.out.println("Made java file : " + code);

					IEditorPart editor = IDE.openEditor(page, javaFile);

					Object viewer = null;
					Class<?> clazz = editor.getClass();

					while (clazz != null && viewer == null) {
						try {
							Method method = clazz.getDeclaredMethod("getSourceViewer");
							method.setAccessible(true);
							viewer = method.invoke(editor);
						} catch (NoSuchMethodException e) {
							clazz = clazz.getSuperclass();
						}
					}

					// ファイルを作成しただけだと、2回目以降上書きしてもエディター側に反映されないので
					// エディタ側にも手動で反映して保存
					if (viewer instanceof ITextViewer) {
						ITextViewer textViewer = (ITextViewer) viewer;
						// エディタの中身を新しいコードで上書き
						textViewer.getDocument().set(code);
					}
					// エディタを保存してLSPに通知
					if (editor.isDirty()) {
						editor.doSave(new NullProgressMonitor());
						while (Display.getDefault().readAndDispatch())
							;
					}

					Thread.sleep(1000);

					//
					StructuredSelection pselection2 = new StructuredSelection(javaFile);
					jview.getSite().getSelectionProvider().setSelection(pselection2);

					//
					if (jview instanceof ISetSelectionTarget) {
						((ISetSelectionTarget) jview).selectReveal(pselection2);
					} else {
						jview.getSite().getSelectionProvider().setSelection(pselection2);
					}

					if (viewer instanceof ITextViewer) {

						ITextViewer textViewer = (ITextViewer) viewer;

						String fullText = textViewer.getDocument().get();
						textViewer.setSelectedRange(offsetForSearch, 0);

						while (Display.getDefault().readAndDispatch())
							;

						// setSelectedRangeの直後に入れる
						int offset = textViewer.getSelectedRange().x;
						String text = textViewer.getDocument().get();
						System.out.println("CURSOR AT: [" + text.charAt(offset - 1) + "|" + text.charAt(offset) + "]");

						LSContentAssistProcessor processor = new LSContentAssistProcessor();

						System.out.println("trigger processor.computeCompletionProposals CURSOR AT: ["
								+ text.charAt(offset - 1) + "|" + text.charAt(offset) + "] : \n " + fullText);
						ICompletionProposal[] proposals = processor.computeCompletionProposals(textViewer,
								offsetForSearch);

						for (ICompletionProposal proposal : proposals) {

							if (proposal instanceof LSCompletionProposal) {
								LSCompletionProposal lspproposal = (LSCompletionProposal) proposal;
								String contextDisplayString = lspproposal.getContextDisplayString();
								System.out.println("PROPOSAL RESULT :\n"
										+ contextDisplayString
										+ "  : " + proposal.getDisplayString()
										+ " "
										+ proposal.getAdditionalProposalInfo());
							} else {
								System.out.println("PROPOSAL RESULT :\\n"
										+ proposal.getDisplayString()
										+ " "
										+ proposal.getAdditionalProposalInfo());
							}

						}
						assertNotNull(proposals, "補完候補リストがnullです");
						assertTrue(proposals.length > 0, "補完候補が1件も見つかりません");

						while (Display.getDefault().readAndDispatch())
							;
		

					} else {
						fail("SourceViewerを取得できませんでした。取得されたオブジェクト: " + viewer);
					}

				}
				
				//to check by human
				long endTime = System.currentTimeMillis() + 2000;
				Display display = Display.getDefault();
				while (System.currentTimeMillis() < endTime) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				fail("UI Test failed: " + e.getMessage());
			} finally {

			}
		});
	}

	/**
	 * @param project
	 * @param path
	 * @param content
	 * @return
	 * @throws CoreException
	 */
	public static IFile createFile(IProject project, String path, String content) throws CoreException {
		IFile file = project.getFile(path);
		InputStream source = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

		if (file.exists()) {
			// すでに存在する場合は中身をセット（上書き）
			file.setContents(source, IResource.FORCE, new NullProgressMonitor());
		} else {
			// 存在しない場合は新規作成
			file.create(source, IResource.NONE, new NullProgressMonitor());
		}
		return file;
	}

	/**
	 * @param project
	 * @param sourceNode
	 * @param targetPath
	 * @throws Exception
	 */
	public static void copyRecursive(IProject project, File sourceNode, String targetPath) throws Exception {
		if (sourceNode.isDirectory()) {
			createFolderRecursively(project, targetPath); // 既存のフォルダ作成メソッドを利用 [cite: 103]
			for (File child : sourceNode.listFiles()) {
				copyRecursive(project, child, targetPath + "/" + child.getName());
			}
		} else {
			// ファイルの内容をバイト配列として読み込み、createFileで作成
			try (InputStream is = new java.io.FileInputStream(sourceNode)) {
				byte[] bytes = is.readAllBytes();
				createFile(project, targetPath, new String(bytes, StandardCharsets.UTF_8)); // 既存のメソッドを利用 
			}
		}
	}

	
	/**
	 * @param project
	 * @param folderPath
	 * @return
	 * @throws CoreException
	 */
	public static IFolder createFolderRecursively(IProject project, String folderPath) throws CoreException {
		String[] segments = folderPath.split("/");
		IFolder currentFolder = null;
		for (String segment : segments) {
			if (currentFolder == null) {
				currentFolder = project.getFolder(segment);
			} else {
				currentFolder = currentFolder.getFolder(segment);
			}
			if (!currentFolder.exists()) {
				currentFolder.create(true, true, new NullProgressMonitor());
			}
		}
		return currentFolder;
	}
}
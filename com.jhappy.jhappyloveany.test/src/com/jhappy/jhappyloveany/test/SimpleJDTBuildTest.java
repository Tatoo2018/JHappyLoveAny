package com.jhappy.jhappyloveany.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * テストを行うため、Javaプロジェクトを生成し、サンプルファイルをコンパイルした環境を準備します。
 * 実際のテストはexecTest()内に実装してください。
 */
@DisplayName("Integration Test: Setup Java Project")
public abstract class SimpleJDTBuildTest {

	// 作成されたプロジェクト
	IProject project = null;
	
	//　環境構築時サンプル用に作成したJavaファイル　com/Hello.javaとして配置されます。
    IFile samplejavafile = null;
   
    public static String BUNDLE_NAME = "com.jhappy.jhappyloveany.test";

	@AfterEach
	void tearDown() throws CoreException {
		//
		if (project.exists()) {
			project.delete(true, true, null);
		}
	}

	@Test
	abstract void execTest() throws Exception;

	@BeforeEach
	void setup() throws Exception {

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		
		try {
			//プロジェクトを新規作成
			String projectName = "SampleJavaProject";
			project = root.getProject(projectName);
			project.create(null);
			project.open(null);

			//java用のnatureを追加
			IProjectDescription description = project.getDescription();
			description.setNatureIds(new String[] { JavaCore.NATURE_ID });
			project.setDescription(description, null);

			//javaプロジェクトとして取得
			IJavaProject javaProject = JavaCore.create(project);

			//
			String javaHome = System.getProperty("java.home");
			File jreHome = new File(javaHome);

			IVMInstallType vmType = JavaRuntime
					.getVMInstallType("org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType");
//			if (vmType == null) {
//				IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
//				if (types.length > 0)
//					vmType = types[0];
//			}
//
//			IVMInstall vmInstall = vmType.findVMInstall("eclipse-builtin-jre");
//			if (vmInstall != null) {
				vmType.disposeVMInstall("eclipse-builtin-jre");
//			}

			IVMInstall  vmInstall = vmType.createVMInstall("eclipse-builtin-jre");
			vmInstall.setInstallLocation(jreHome);
			JavaRuntime.saveVMConfiguration();
			
			//srcフォルダ作成
			IFolder srcFolder = project.getFolder("src");
			srcFolder.create(true, true, null);

			//srcフォルダとjreにをクラスパスエントリーにまとめる
			List<IClasspathEntry> entries = new ArrayList<>();
			entries.add(JavaCore.newSourceEntry(srcFolder.getFullPath()));
			entries.add(JavaCore.newContainerEntry(JavaRuntime.newDefaultJREContainerPath()));
			//srcフォルダ、jre,outputフォルダ(bin)を設定
			javaProject.setRawClasspath(
					entries.toArray(new IClasspathEntry[0]),
					project.getFolder("bin").getFullPath(),
					null);

			//コンパイルオプションを設定
			Hashtable<String, String> options = JavaCore.getOptions();
			JavaCore.setComplianceOptions(JavaCore.VERSION_17, options);
			options.put(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
			javaProject.setOptions(options);

			//「com/Hello.java」ファイルを作成
			IFolder comFolder = srcFolder.getFolder("com");
			comFolder.create(true, true, null);
			samplejavafile = comFolder.getFile("Hello.java");
			samplejavafile.create(
					new ByteArrayInputStream(
							"package com; public class Hello { String s = \"Build Success\"; }".getBytes()),
					true, null);

			//プロジェクトをリフレッシュしてからビルド開始
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
			Job.getJobManager().join(JavaCore.PLUGIN_ID, null);
		//	project.build(IncrementalProjectBuilder.FULL_BUILD, null);

			//UIスレッドを回して、目視で確認できるように待機
			while (Display.getDefault().readAndDispatch());

			project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());

			//コンパイルエラーがでてるか確認
			IMarker[] markers = project.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true,
					IResource.DEPTH_INFINITE);
			for (IMarker m : markers) {
				if (m.getAttribute(IMarker.SEVERITY, 0) == IMarker.SEVERITY_ERROR) {
					System.err.println("❌ Error: " + m.getAttribute(IMarker.MESSAGE, ""));
				}
			}

			// デバグように現在のクラスパスを表示
			for (IClasspathEntry entry : javaProject.getResolvedClasspath(true)) {
				System.out.println("Resolved Classpath: " + entry.getPath());
			}

			assertEquals(0, getErrorCount(project), "ビルドエラーが解消されていません。Resolved Classpathを確認してください。");
			System.out.println("✅ すべてのビルドエラーが解消されました！");

		} finally {
			// 確認のためプロジェクトは保持
		}
	}

	private int getErrorCount(IResource resource) throws CoreException {
		return resource.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE).length;
	}
}
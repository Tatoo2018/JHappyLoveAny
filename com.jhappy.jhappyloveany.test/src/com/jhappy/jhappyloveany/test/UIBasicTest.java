package com.jhappy.jhappyloveany.test;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jhappy.jhappyloveany.test.util.TestUtil;

@DisplayName("UI Test: UIperformTest")

class UIBasicTest extends SimpleJDTBuildTest {

	@Test
	public void execTest() throws Exception {
		
		//テスト用のサンプルファイルが保存してあるフォルダ
		final String TEST_RESOURCES_FOLDER = "test-resources/samplefiles";

		TestUtil.doTest(BUNDLE_NAME,project, TEST_RESOURCES_FOLDER, "src/com/test/complicatedtest_UTF8.properties",
				List.of("unicode.test", "long.description", "テスト"));

		// ※ syncExec の外にあった Thread.sleep は削除しました
	}

	

}
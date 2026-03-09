package com.jhappy.jhappyloveany.test;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jhappy.jhappyloveany.test.util.TestUtil;

/**
 * プロパティファイルへの入力補完をテスト
 * テストのためのリソースはtest-resources/samplefilesを使用
 */
@DisplayName("UI Test: UIPropertyTest")
class UIPropertyTest extends SimpleJDTBuildTest {

	@Test
	public void execTest() throws Exception {
		
		//テスト用のサンプルファイルが保存してあるフォルダ
		final String TEST_RESOURCES_FOLDER = "test-resources/samplefiles";

		TestUtil.doTest(BUNDLE_NAME,project, TEST_RESOURCES_FOLDER, "src/com/test/complicatedtest_UTF8.properties",
				List.of("unicode.test", "long.description", "テスト"));

	}

	

}
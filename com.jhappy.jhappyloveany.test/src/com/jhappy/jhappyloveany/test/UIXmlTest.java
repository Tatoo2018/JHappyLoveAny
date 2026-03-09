package com.jhappy.jhappyloveany.test;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jhappy.jhappyloveany.test.util.TestUtil;

/**
 * XMLファイルへの入力補完をテスト
 * テストのためのリソースはtest-resources/samplefiles2を使用
 */
@DisplayName("UI XML Test: UIperformTest")
class UIXmlTest extends SimpleJDTBuildTest {

	@Test
	public void execTest() throws Exception {
		
		//テスト用のサンプルファイルが保存してあるフォルダ
		final String TEST_RESOURCES_FOLDER = "test-resources/samplefiles2";

		TestUtil.doTest(BUNDLE_NAME,project, TEST_RESOURCES_FOLDER, "src/com/test/dir0/dir1/config_1.xml",
				List.of("xml_f10", "xml_f101_e0", "xml_f200_e0"));

	}

	

}
package com.jhappy.jhappyloveany.client.ui;

import java.util.regex.Pattern;

import javax.xml.xpath.XPathFactory;

import org.eclipse.osgi.util.NLS; // 変数埋め込み用

public class QueryValidators {
    private static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();

    // 必須入力チェック
    public static final IValidator TYPE_REQUIRED = m -> 
        (m.getType().isEmpty()) ? new ValidationResult(false, Messages.Validate_Error_TypeRequired) : ValidationResult.OK;

    public static final IValidator FILEPATH_REQUIRED = m -> 
        (m.getFilepath().isEmpty()) ? new ValidationResult(false, Messages.Validate_Error_FilepathRequired) : ValidationResult.OK;
        
        // 正規表現チェック
        public static final IValidator TYPE_KIND = m -> {
            
        	java.util.List<String> validTypes = java.util.List.of("xml", "properties");
            
            if (validTypes.contains(m.getType())) {
                return ValidationResult.OK;
            }
            
            return new ValidationResult(false, Messages.Validate_Error_TypeKind);
        };

    // 正規表現チェック
    public static final IValidator FILEPATH_REGEX = m -> {
        try {
            if (!m.getFilepath().isEmpty()) Pattern.compile(m.getFilepath());
            return ValidationResult.OK;
        } catch (Exception e) {
            // 例外メッセージを含む場合は NLS.bind を使用
            return new ValidationResult(false, NLS.bind(Messages.Validate_Error_InvalidRegex, e.getMessage()));
        }
    };

    // XPathチェック (XMLタイプ時のみ実行)
    public static final IValidator XPATH_SPECIFIC = m -> {
        if (!"xml".equals(m.getType())) return ValidationResult.OK;
        if (m.getXpath().isEmpty()) return new ValidationResult(false, Messages.Validate_Error_XpathRequired);
        try {
            XPATH_FACTORY.newXPath().compile(m.getXpath());
            return ValidationResult.OK;
        } catch (Exception e) {
            return new ValidationResult(false, NLS.bind(Messages.Validate_Error_InvalidXpath, e.getMessage()));
        }
    };

    // 共通メッセージ（ツールチップのデフォルトなど）
    public static final String MSG_VALID_CONFIG = Messages.Validate_Msg_ValidConfig;

    public static IValidator compose(IValidator... validators) {
        return m -> {
            for (IValidator v : validators) {
                ValidationResult r = v.validate(m);
                if (!r.isValid) return r;
            }
            return ValidationResult.OK;
        };
    }
}
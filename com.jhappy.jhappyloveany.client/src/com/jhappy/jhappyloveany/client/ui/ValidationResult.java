package com.jhappy.jhappyloveany.client.ui;

// バリデーション結果のコンテナ
class ValidationResult {
    public final boolean isValid;
    public final String message;
    public static final ValidationResult OK = new ValidationResult(true, "");

    public ValidationResult(boolean isValid, String message) {
        this.isValid = isValid;
        this.message = message;
    }
}

// バリデーション実行用のインターフェース
@FunctionalInterface
interface IValidator {
    ValidationResult validate(QueryModel model);
}
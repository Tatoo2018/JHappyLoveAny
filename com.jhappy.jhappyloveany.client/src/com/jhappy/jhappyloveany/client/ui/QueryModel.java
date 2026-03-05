package com.jhappy.jhappyloveany.client.ui;

import java.util.HashMap;
import java.util.Map;

public class QueryModel {
    private String filepath;
    private String type;
    private String trim;
    private String xpath;
    private String description;

    // バリデーション結果をカラム(Key)ごとに保持
    private final Map<String, ValidationResult> validationResults = new HashMap<>();

    public QueryModel(String filepath, String type, String trim, String xpath, String description) {
    	this.filepath = (filepath != null) ? filepath : "";
        this.type = (type != null) ? type : "";
        this.trim = (trim != null) ? trim : "yes";
        this.xpath = (xpath != null) ? xpath : "";
        this.description = (description != null) ? description : "";
    }

    // 外部（PropertyPage等）からバリデーションを実行させる
    public void validate(String key, IValidator validator) {
        validationResults.put(key, validator.validate(this));
    }

    public ValidationResult getResult(String key) {
        return validationResults.getOrDefault(key, ValidationResult.OK);
    }
    private java.util.Map<String, ValidationResult> results = new java.util.HashMap<>();

    public void runValidation(String key, IValidator validator) {
        results.put(key, validator.validate(this));
    }

    public ValidationResult getValidation(String key) {
        return results.getOrDefault(key, ValidationResult.OK);
    }

    public boolean hasError() {
        return results.values().stream().anyMatch(r -> !r.isValid);
    }
    
    // Getter (Null安全)
    public String getFilepath() { return filepath; }
    public String getType() { return type;}
    public String getTrim() { return trim; }
    public String getXpath() { return xpath; }
    public String getDescription() { return description; }
}
package com.jhappy.jhappyloveany.client.ui;

public class QueryModel {
    private String filepath;
    private String type;
    private String trim;
    private String xpath; // XMLのボディ部分 (CDATA)
    private String description;

    public QueryModel(String filepath, String type, String trim, String xpath, String description) {
        this.filepath = filepath;
        this.type = type;
        this.trim = trim;
        this.xpath = xpath;
        this.description = description;
    }

    // Getter
    public String getFilepath() { return filepath != null ? filepath : ""; }
    public String getType() { return type != null ? type : "xml"; }
    public String getTrim() { return trim != null ? trim : "yes"; }
    public String getXpath() { return xpath != null ? xpath : ""; }
    public String getDescription() { return description != null ? description : ""; }
}